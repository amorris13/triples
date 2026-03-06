package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.antsapps.triples.stats.BaseStatisticsFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.antsapps.triples.stats.ArcadeStatisticsFragment;
import com.antsapps.triples.stats.ClassicStatisticsFragment;

public class StatisticsActivity extends BaseTriplesActivity {

  public static final String GAME_TYPE = "game_type";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.statistics_activity);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    String gameType = getIntent().getStringExtra(GAME_TYPE);
    int accentColor;
    String title;
    if ("Arcade".equals(gameType)) {
      accentColor = ContextCompat.getColor(this, R.color.arcade_accent);
      title = getString(R.string.arcade_label);
    } else if ("Daily".equals(gameType)) {
      accentColor = ContextCompat.getColor(this, R.color.daily_accent);
      title = getString(R.string.daily_label);
    } else {
      accentColor = ContextCompat.getColor(this, R.color.classic_accent);
      title = getString(R.string.classic_label);
    }

    setTitle(title);

    if (savedInstanceState == null) {
      Fragment fragment;
      if ("Arcade".equals(gameType)) {
        fragment = new ArcadeStatisticsFragment();
      } else if ("Daily".equals(gameType)) {
        fragment = new DailyStatisticsFragment();
      } else {
        fragment = new ClassicStatisticsFragment();
      }

      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.add(R.id.statistics_container, fragment);
      ft.commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.statistics, menu);
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
    int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      finish();
      return true;
    } else if (itemId == R.id.help) {
      Intent helpIntent = new Intent(getBaseContext(), HelpActivity.class);
      startActivity(helpIntent);
      return true;
    } else if (itemId == R.id.settings) {
      Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
      startActivity(settingsIntent);
      return true;
    } else if (itemId == R.id.signout) {
      signOut();
      return true;
    } else if (itemId == R.id.export_to_csv) {
      Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.statistics_container);
      if (fragment instanceof BaseStatisticsFragment) {
        ((BaseStatisticsFragment) fragment).exportToCsv();
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
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
