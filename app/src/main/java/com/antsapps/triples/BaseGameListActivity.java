package com.antsapps.triples;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.antsapps.triples.stats.BaseStatisticsFragment;
import com.google.firebase.analytics.FirebaseAnalytics;

/** Created by anthony on 1/12/13. */
public abstract class BaseGameListActivity extends BaseTriplesActivity {
  public static final String SIGNIN_PREFS = "signin_prefs";
  public static final String SIGNED_IN_PREVIOUSLY = "signed_in_previously";
  public static final String UPLOADED_EXISTING_TOP_SCORE = "uploaded_existing_top_score_2";

  public static final String NAV_DRAWER_PREFS = "nav_drawer_prefs";
  public static final String OPENED_NAV_DRAWER_PREVIOUSLY = "opened_navdrawer_prev";

  private static final String TAB_NUMBER = "tab";

  protected FirebaseAnalytics mFirebaseAnalytics;

  private DrawerLayout mDrawerLayout;
  private ActionBarDrawerToggle mDrawerToggle;
  private ViewPager mViewPager;
  private TabsAdapter mTabsAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.game_overview);

    String[] gameModes = new String[] {"Classic", "Arcade"};
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    ListView drawerList = (ListView) findViewById(R.id.mode_list);

    // Set the adapter for the list view
    drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, gameModes));
    // Set the list's click listener
    drawerList.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
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
    mDrawerToggle =
        new ActionBarDrawerToggle(
            this, /* host Activity */
            mDrawerLayout, /* DrawerLayout object */
            R.string.drawer_open, /* "open drawer" description */
            R.string.drawer_close /* "close drawer" description */);

    // Set the drawer toggle as the DrawerListener
    mDrawerLayout.setDrawerListener(mDrawerToggle);

    maybeOpenNavDrawer();

    final ActionBar bar = getSupportActionBar();
    bar.setDisplayHomeAsUpEnabled(true);
    bar.setHomeButtonEnabled(true);

    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    mViewPager = (ViewPager) findViewById(R.id.pager);

    mTabsAdapter = new TabsAdapter(this, mViewPager);

    mTabsAdapter.addTab(bar.newTab().setText(R.string.current), getCurrentGamesFragment(), null);
    mTabsAdapter.addTab(
        bar.newTab().setText(R.string.statistics), getStatisticsFragmentClass(), null);

    if (savedInstanceState != null) {
      bar.setSelectedNavigationItem(savedInstanceState.getInt(TAB_NUMBER, 0));
    }

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
  }

  private void maybeOpenNavDrawer() {
    SharedPreferences settings = getSharedPreferences(NAV_DRAWER_PREFS, 0);
    if (settings.getBoolean(OPENED_NAV_DRAWER_PREVIOUSLY, false)) {
      return;
    }

    mDrawerLayout.openDrawer(Gravity.LEFT);

    // We need an Editor object to make preference changes.
    // All objects are from android.context.Context
    SharedPreferences.Editor editor = settings.edit();
    editor.putBoolean(OPENED_NAV_DRAWER_PREVIOUSLY, true);
    // Commit the edits!
    editor.commit();
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
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.game_list, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(R.id.signout).setVisible(isSignedIn());
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
        logNewGame();
        startActivity(newGameIntent);
        return true;
      case R.id.help:
        Intent helpIntent = new Intent(getBaseContext(), HelpActivity.class);
        startActivity(helpIntent);
        return true;
      case R.id.settings:
        Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
        startActivity(settingsIntent);
        return true;
      case R.id.signout:
        signOut();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  protected abstract Intent createNewGame();

  private void logNewGame() {
    Bundle bundle = new Bundle();
    bundle.putString(AnalyticsConstants.Param.GAME_TYPE, getAnalyticsGameType());
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.NEW_GAME, bundle);
  }

  protected abstract String getAnalyticsGameType();

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(TAB_NUMBER, getSupportActionBar().getSelectedNavigationIndex());
  }

  @Override
  public void onSignInSucceeded() {
    super.onSignInSucceeded();
    invalidateOptionsMenu();
  }

  @Override
  public void onSignOut() {
    super.onSignOut();
    invalidateOptionsMenu();
  }
}
