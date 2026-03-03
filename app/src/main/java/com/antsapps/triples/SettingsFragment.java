package com.antsapps.triples;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import com.google.firebase.analytics.FirebaseAnalytics;

public class SettingsFragment extends PreferenceFragmentCompat
    implements Preference.OnPreferenceChangeListener {

  private FirebaseAnalytics mFirebaseAnalytics;

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.preferences);

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

    setupListeners(getPreferenceScreen());

    SeekBarPreference animationDurationPref = findPreference(getString(R.string.pref_animation_speed));
    if (animationDurationPref != null) {
      animationDurationPref.setSummary(
          getString(R.string.pref_animation_duration_summary, animationDurationPref.getValue()));
    }
  }

  private void setupListeners(androidx.preference.PreferenceGroup group) {
    for (int i = 0; i < group.getPreferenceCount(); i++) {
      Preference p = group.getPreference(i);
      if (p instanceof androidx.preference.PreferenceGroup) {
        setupListeners((androidx.preference.PreferenceGroup) p);
      } else {
        p.setOnPreferenceChangeListener(this);
      }
    }
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    if (preference.getKey().equals(getString(R.string.pref_animation_speed))) {
      preference.setSummary(getString(R.string.pref_animation_duration_summary, (Integer) newValue));
    }
    Bundle bundle = new Bundle();
    bundle.putString(AnalyticsConstants.Param.SETTING_NAME, preference.getKey());
    bundle.putString(AnalyticsConstants.Param.SETTING_VALUE, String.valueOf(newValue));
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.CHANGE_SETTING, bundle);
    return true;
  }
}
