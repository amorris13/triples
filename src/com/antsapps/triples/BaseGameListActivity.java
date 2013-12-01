package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.antsapps.triples.stats.BaseStatisticsFragment;

/**
 * Created by anthony on 1/12/13.
 */
public abstract class BaseGameListActivity extends SherlockFragmentActivity
    implements GameHelper.GameHelperListener {
  public static final String SIGNIN_PREFS = "signin_prefs";
  public static final String SIGNED_IN_PREVIOUSLY = "signed_in_previously";
  public static final String UPLOADED_EXISTING_TOP_SCORE = "uploaded_existing_top_score_2";
  private static final String TAB_NUMBER = "tab";
  private ViewPager mViewPager;
  private TabsAdapter mTabsAdapter;
  protected GameHelper mHelper;
  private GameHelper.GameHelperListener mGameHelperListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final ActionBar bar = getSupportActionBar();
    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    setContentView(R.layout.game_overview);
    mViewPager = (ViewPager) findViewById(R.id.pager);
    setContentView(mViewPager);

    mTabsAdapter = new TabsAdapter(this, mViewPager);

    mTabsAdapter.addTab(
        bar.newTab().setText(R.string.current),
        getCurrentGamesFragment(),
        null);
    mTabsAdapter.addTab(
        bar.newTab().setText(R.string.statistics),
        getStatisticsFragmentClass(),
        null);

    if (savedInstanceState != null) {
      bar.setSelectedNavigationItem(savedInstanceState.getInt(TAB_NUMBER, 0));
    }

    mHelper = new GameHelper(this);
    mHelper.setup(this, GameHelper.CLIENT_PLUS | GameHelper.CLIENT_GAMES);
  }

  protected abstract Class<? extends BaseStatisticsFragment> getStatisticsFragmentClass();

  protected abstract Class<? extends BaseCurrentGameListFragment> getCurrentGamesFragment();

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
        Intent newGameIntent = createNewGame();
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

  protected abstract Intent createNewGame();

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

  protected abstract void uploadExistingTopScoresIfNecessary();

  public void setGameHelperListener(GameHelper.GameHelperListener listener) {
    mGameHelperListener = listener;
  }
}
