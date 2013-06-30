package com.antsapps.triples;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Game;
import com.antsapps.triples.backend.Period;
import com.antsapps.triples.backend.Statistics;
import com.antsapps.triples.stats.StatisticsFragment;

public class GameListActivity extends SherlockFragmentActivity implements GameHelper.GameHelperListener {
  private static final String TAB_NUMBER = "tab";
  public static final String SIGNIN_PREFS = "signin_prefs";
  public static final String SIGNED_IN_PREVIOUSLY = "signed_in_previously";
  public static final String UPLOADED_EXISTING_TOP_SCORE = "uploaded_existing_top_score";
  private ViewPager mViewPager;
  private TabsAdapter mTabsAdapter;
  private Application mApplication;

  private GameHelper mHelper;

  private GameHelper.GameHelperListener mGameHelperListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mApplication = Application.getInstance(getApplication());

    setContentView(R.layout.game_overview);
    final ActionBar bar = getSupportActionBar();
    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    mViewPager = (ViewPager) findViewById(R.id.pager);
    setContentView(mViewPager);

    mTabsAdapter = new TabsAdapter(this, mViewPager);

    mTabsAdapter.addTab(
        bar.newTab().setText(R.string.current),
        CurrentGameListFragment.class,
        null);
    mTabsAdapter.addTab(
        bar.newTab().setText(R.string.statistics),
        StatisticsFragment.class,
        null);

    if (savedInstanceState != null) {
      bar.setSelectedNavigationItem(savedInstanceState.getInt(TAB_NUMBER, 0));
    }

    mHelper = new GameHelper(this);
    mHelper.setup(this, GameHelper.CLIENT_PLUS | GameHelper.CLIENT_GAMES);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mHelper.onStart(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mHelper.onStop();
  }

  @Override
  protected void onActivityResult(int request, int response, Intent data) {
    super.onActivityResult(request, response, data);
    mHelper.onActivityResult(request, response, data);
  }

  public GameHelper getGameHelper() {
    return mHelper;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getSupportMenuInflater();
    inflater.inflate(R.menu.game_list, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.signout).setVisible(mHelper.isSignedIn());
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.new_game:
        Game game = Game.createFromSeed(System.currentTimeMillis());
        mApplication.addGame(game);
        Intent newGameIntent = new Intent(getBaseContext(), GameActivity.class);
        newGameIntent.putExtra(Game.ID_TAG, game.getId());
        startActivity(newGameIntent);
        return true;
      case R.id.help:
        Intent helpIntent = new Intent(getBaseContext(), HelpActivity.class);
        startActivity(helpIntent);
        return true;
      case R.id.settings:
        Intent settingsIntent = new Intent(getBaseContext(),
            SettingsActivity.class);
        startActivity(settingsIntent);
        return true;
      case R.id.signout:
        mHelper.signOut();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(TAB_NUMBER, getSupportActionBar()
        .getSelectedNavigationIndex());
  }

  @Override
  public void onSignInFailed() {
    if (mGameHelperListener != null) {
      mGameHelperListener.onSignInFailed();
    }
  }

  @Override
  public void onSignInSucceeded() {
    if (mGameHelperListener != null) {
      mGameHelperListener.onSignInSucceeded();
    }
    invalidateOptionsMenu();
    uploadExistingTopScoresIfNecessary();
  }

  @Override
  public void onSignOut() {
    if (mGameHelperListener != null) {
      mGameHelperListener.onSignOut();
    }
    invalidateOptionsMenu();
  }

  private void uploadExistingTopScoresIfNecessary() {
    SharedPreferences settings = getSharedPreferences(SIGNIN_PREFS, 0);
    if (settings.getBoolean(UPLOADED_EXISTING_TOP_SCORE, false)) {
      return;
    }

    Statistics stats = mApplication.getStatistics(Period.ALL_TIME);
    mHelper.getGamesClient().submitScore(GamesServices.Leaderboard.CLASSIC, stats.getFastestTime());

    // We need an Editor object to make preference changes.
    // All objects are from android.context.Context
    SharedPreferences.Editor editor = settings.edit();
    editor.putBoolean(UPLOADED_EXISTING_TOP_SCORE, true);
    // Commit the edits!
    editor.commit();
  }

  public void setGameHelperListener(GameHelper.GameHelperListener listener) {
    mGameHelperListener = listener;
  }

  /**
   * This is a helper class that implements the management of tabs and all
   * details of connecting a ViewPager with associated TabHost. It relies on a
   * trick. Normally a tab host has a simple API for supplying a View or Intent
   * that each tab will show. This is not sufficient for switching between
   * pages. So instead we make the content part of the tab host 0dp high (it is
   * not shown) and the TabsAdapter supplies its own dummy view to show as the
   * tab content. It listens to changes in tabs, and takes care of switch to the
   * correct paged in the ViewPager whenever the selected tab changes.
   */
  public static class TabsAdapter extends FragmentPagerAdapter implements
      ActionBar.TabListener, ViewPager.OnPageChangeListener {
    private final Context mContext;
    private final ActionBar mActionBar;
    private final ViewPager mViewPager;
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    static final class TabInfo {
      private final Class<?> clss;
      private final Bundle args;

      TabInfo(Class<?> _class, Bundle _args) {
        clss = _class;
        args = _args;
      }
    }

    public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
      super(activity.getSupportFragmentManager());
      mContext = activity;
      mActionBar = activity.getSupportActionBar();
      mViewPager = pager;
      mViewPager.setAdapter(this);
      mViewPager.setOnPageChangeListener(this);
    }

    public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
      TabInfo info = new TabInfo(clss, args);
      tab.setTag(info);
      tab.setTabListener(this);
      mTabs.add(info);
      mActionBar.addTab(tab);
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return mTabs.size();
    }

    @Override
    public Fragment getItem(int position) {
      TabInfo info = mTabs.get(position);
      return Fragment.instantiate(mContext, info.clss.getName(), info.args);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
        int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
      mActionBar.setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
      Object tag = tab.getTag();
      for (int i = 0; i < mTabs.size(); i++) {
        if (mTabs.get(i) == tag) {
          mViewPager.setCurrentItem(i);
        }
      }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
  }
}