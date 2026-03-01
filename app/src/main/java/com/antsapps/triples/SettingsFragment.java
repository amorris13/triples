package com.antsapps.triples;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.ListPreference;

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

    setupUniquenessEnforcement(
        new String[] {
          getString(R.string.pref_color_0),
          getString(R.string.pref_color_1),
          getString(R.string.pref_color_2)
        });

    setupUniquenessEnforcement(
        new String[] {
          getString(R.string.pref_shape_0),
          getString(R.string.pref_shape_1),
          getString(R.string.pref_shape_2)
        });
  }

  private void setupUniquenessEnforcement(final String[] keys) {
    for (String key : keys) {
      Preference pref = findPreference(key);
      if (pref != null) {
        pref.setOnPreferenceChangeListener(
            new Preference.OnPreferenceChangeListener() {
              @Override
              public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newStrValue = (String) newValue;
                for (String otherKey : keys) {
                  if (otherKey.equals(preference.getKey())) {
                    continue;
                  }
                  ListPreference otherPref = findPreference(otherKey);
                  if (otherPref != null && newStrValue.equals(otherPref.getValue())) {
                    // Swap values
                    String oldValue = ((ListPreference) preference).getValue();
                    otherPref.setValue(oldValue);
                    break;
                  }
                }
                return true;
              }
            });
      }
    }
  }
}
