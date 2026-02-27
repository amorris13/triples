package com.antsapps.triples;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

public class SettingsFragment extends PreferenceFragmentCompat {

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.preferences);

    SeekBarPreference animationDurationPref = findPreference(getString(R.string.pref_animation_speed));
    if (animationDurationPref != null) {
      animationDurationPref.setSummaryProvider(
          (Preference.SummaryProvider<SeekBarPreference>)
              preference -> getString(R.string.pref_animation_duration_summary, preference.getValue()));
    }
  }
}
