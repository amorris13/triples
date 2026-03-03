package com.antsapps.triples;

import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
    } else {
      accentColor = ContextCompat.getColor(this, R.color.classic_accent);
      title = getString(R.string.classic_label);
    }

    SpannableString spannableTitle = new SpannableString(title);
    spannableTitle.setSpan(new ForegroundColorSpan(accentColor), 0, spannableTitle.length(), 0);
    setTitle(spannableTitle);

    if (getSupportActionBar() != null) {
      final Drawable upArrow = ContextCompat.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_ab_back_material);
      if (upArrow != null) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
          upArrow.setColorFilter(new BlendModeColorFilter(accentColor, BlendMode.SRC_ATOP));
        } else {
          upArrow.setColorFilter(accentColor, PorterDuff.Mode.SRC_ATOP);
        }
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
      }
    }

    if (savedInstanceState == null) {
      Fragment fragment;
      if ("Arcade".equals(gameType)) {
        fragment = new ArcadeStatisticsFragment();
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
    tintMenuIcons(menu);
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
