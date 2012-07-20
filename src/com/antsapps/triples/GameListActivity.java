package com.antsapps.triples;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
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

public class GameListActivity extends SherlockFragmentActivity {
  private static final String TAB_NUMBER = "tab";
  private ViewPager mViewPager;
  private TabsAdapter mTabsAdapter;
  private Application mApplication;

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
        bar.newTab().setText(R.string.completed),
        CompletedGameListFragment.class,
        null);

    if (savedInstanceState != null) {
      bar.setSelectedNavigationItem(savedInstanceState.getInt(TAB_NUMBER, 0));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getSupportMenuInflater();
    inflater.inflate(R.menu.game_list, menu);
    return super.onCreateOptionsMenu(menu);
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