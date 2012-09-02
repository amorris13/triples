package com.antsapps.triples;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity implements
    OnSharedPreferenceChangeListener {
  private SharedPreferences mSharedPref;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    mSharedPref.registerOnSharedPreferenceChangeListener(this);

    ActionBar actionBar = getSupportActionBar();
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
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
      String key) {
    if (key.equals(getString(R.string.pref_orientation))) {
      updateOrientationSummary();
    }
  }

  private void updateOrientationSummary() {
    ListPreference orientationPref = (ListPreference) findPreference(getString(R.string.pref_orientation));

    orientationPref.setSummary(orientationPref.getEntry());
  }
}
