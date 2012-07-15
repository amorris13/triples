package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

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
  private Application mApplication;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mApplication = Application.getInstance(getApplication());

    ActionBar bar = getSupportActionBar();
    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    bar.setDisplayHomeAsUpEnabled(false);
    bar.addTab(bar
        .newTab()
        .setText(R.string.current)
        .setTabListener(
            new TabListener<CurrentGameListFragment>(this,
                CurrentGameListFragment.TAG, CurrentGameListFragment.class,
                null)));

    bar.addTab(bar
        .newTab()
        .setText(R.string.completed)
        .setTabListener(
            new TabListener<CompletedGameListFragment>(this,
                CompletedGameListFragment.TAG, CompletedGameListFragment.class,
                null)));


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
        Intent intent = new Intent(getBaseContext(), GameActivity.class);
        intent.putExtra(Game.ID_TAG, game.getId());
        startActivity(intent);
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

  public class TabListener<T extends Fragment> implements ActionBar.TabListener {
    private final FragmentActivity mActivity;
    private final String mTag;
    private final Class<T> mClass;
    private final Bundle mArgs;
    private Fragment mFragment;

    public TabListener(FragmentActivity activity,
        String tag,
        Class<T> clz,
        Bundle args) {
      mActivity = activity;
      mTag = tag;
      mClass = clz;
      mArgs = args;
      FragmentTransaction ft = mActivity
          .getSupportFragmentManager().beginTransaction();

      // Check to see if we already have a fragment for this tab, probably
      // from a previously saved state. If so, deactivate it, because our
      // initial state is that a tab isn't shown.
      mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
      if (mFragment != null && !mFragment.isDetached()) {
        ft.detach(mFragment);
      }
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
      ft = mActivity.getSupportFragmentManager().beginTransaction();

      if (mFragment == null) {
        mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
        ft.add(android.R.id.content, mFragment, mTag);
        ft.commit();
      } else {
        ft.attach(mFragment);
        ft.commit();
      }

    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
      ft = mActivity.getSupportFragmentManager().beginTransaction();

      if (mFragment != null) {
        ft.detach(mFragment);
        ft.commitAllowingStateLoss();
      }

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
  }
}