package com.antsapps.triples;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.antsapps.triples.stats.BaseStatisticsFragment;

/**
 * Created by anthony on 1/12/13.
 */
public abstract class BaseGameListActivity extends Activity
    implements GameHelper.GameHelperListener {
  public static final String SIGNIN_PREFS = "signin_prefs";
  public static final String SIGNED_IN_PREVIOUSLY = "signed_in_previously";
  public static final String UPLOADED_EXISTING_TOP_SCORE = "uploaded_existing_top_score_2";
  private static final String TAB_NUMBER = "tab";

  private DrawerLayout mDrawerLayout;
  private ActionBarDrawerToggle mDrawerToggle;
  private ViewPager mViewPager;
  private TabsAdapter mTabsAdapter;
  protected GameHelper mHelper;
  private GameHelper.GameHelperListener mGameHelperListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.game_overview);

    String[] gameModes = new String[] {"Classic", "Arcade"};
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    ListView drawerList = (ListView) findViewById(R.id.left_drawer);

    // Set the adapter for the list view
    drawerList.setAdapter(new ArrayAdapter<String>(this,
        R.layout.drawer_list_item, gameModes));
    // Set the list's click listener
    drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        switch (position) {
          case 0:
            startActivity(new Intent(getBaseContext(), ClassicGameListActivity.class));
            break;
          case 1:
            startActivity(new Intent(getBaseContext(), ArcadeGameListActivity.class));
            break;
        }
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
      }
    });

    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerToggle = new ActionBarDrawerToggle(
        this,                  /* host Activity */
        mDrawerLayout,         /* DrawerLayout object */
        R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
        R.string.drawer_open,  /* "open drawer" description */
        R.string.drawer_close  /* "close drawer" description */
    ) {

      /** Called when a drawer has settled in a completely closed state. */
      public void onDrawerClosed(View view) {
//        getActionBar().setTitle(mTitle);
      }

      /** Called when a drawer has settled in a completely open state. */
      public void onDrawerOpened(View drawerView) {
//        getActionBar().setTitle(mDrawerTitle);
      }
    };

    // Set the drawer toggle as the DrawerListener
    mDrawerLayout.setDrawerListener(mDrawerToggle);

    final ActionBar bar = getActionBar();
    bar.setDisplayHomeAsUpEnabled(true);
    bar.setHomeButtonEnabled(true);

    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    mViewPager = (ViewPager) findViewById(R.id.pager);

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
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
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
    MenuInflater inflater = getMenuInflater();
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
    // Pass the event to ActionBarDrawerToggle, if it returns
    // true, then it has handled the app icon touch event
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }

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
    outState.putInt(TAB_NUMBER, getActionBar()
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
