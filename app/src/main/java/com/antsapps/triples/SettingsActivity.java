package com.antsapps.triples;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;

public class SettingsActivity extends BaseTriplesActivity {
  private SharedPreferences mSharedPref;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    setSignInListener(
        signedInAndConnected -> {
          SettingsFragment fragment =
              (SettingsFragment) getSupportFragmentManager().findFragmentByTag(".SettingsFragment");
          if (fragment != null) {
            fragment.updateAccountPreferences();
          }
        });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case android.R.id.home:
        // app icon in action bar clicked; go back
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
