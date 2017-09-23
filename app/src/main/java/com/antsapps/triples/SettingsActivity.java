package com.antsapps.triples;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity
    implements OnSharedPreferenceChangeListener {
  private SharedPreferences mSharedPref;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    mSharedPref.registerOnSharedPreferenceChangeListener(this);

    ActionBar actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    updateOrientationSummary();
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

  @Override
  protected void onDestroy() {
    mSharedPref.unregisterOnSharedPreferenceChangeListener(this);

    super.onDestroy();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(getString(R.string.pref_orientation))) {
      updateOrientationSummary();
    }
  }

  private void updateOrientationSummary() {
    ListPreference orientationPref =
        (ListPreference) findPreference(getString(R.string.pref_orientation));

    orientationPref.setSummary(orientationPref.getEntry());
  }
}
