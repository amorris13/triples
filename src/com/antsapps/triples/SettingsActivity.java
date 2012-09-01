package com.antsapps.triples;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements
    OnSharedPreferenceChangeListener {
  private SharedPreferences mSharedPref;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    mSharedPref.registerOnSharedPreferenceChangeListener(this);

    updateOrientationSummary();
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
