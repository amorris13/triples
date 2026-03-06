package com.antsapps.triples;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import com.antsapps.triples.backend.Application;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

    updateAccountPreferences();
  }

  public void updateAccountPreferences() {
    BaseTriplesActivity activity = (BaseTriplesActivity) getActivity();
    if (activity == null) return;

    Preference userPref = findPreference("account_user");
    Preference signInOutPref = findPreference("account_signin_out");
    Preference deleteDataPref = findPreference("account_delete_data");

    if (activity.isSignedIn()) {
      String email = activity.getSignedInUserEmail();
      userPref.setSummary(getString(R.string.account_signed_in_as, email != null ? email : ""));
      signInOutPref.setTitle(R.string.account_sign_out);
      signInOutPref.setOnPreferenceClickListener(preference -> {
        activity.signOut();
        return true;
      });
    } else {
      userPref.setSummary(R.string.account_not_signed_in);
      signInOutPref.setTitle(R.string.account_sign_in);
      signInOutPref.setOnPreferenceClickListener(preference -> {
        activity.signIn();
        return true;
      });
    }

    deleteDataPref.setOnPreferenceClickListener(preference -> {
      showDeleteDataConfirmation();
      return true;
    });
  }

  private void showDeleteDataConfirmation() {
    new MaterialAlertDialogBuilder(getContext())
        .setTitle(R.string.account_delete_data_dialog_title)
        .setMessage(R.string.account_delete_data_dialog_message)
        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            deleteData();
          }
        })
        .setNegativeButton(R.string.no, null)
        .show();
  }

  private void deleteData() {
    BaseTriplesActivity activity = (BaseTriplesActivity) getActivity();
    if (activity == null) return;

    Application application = Application.getInstance(getContext());
    application.clearAllData();
    CloudSaveManager.deleteFromCloud(activity);
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
    if (preference.getKey().equals(getString(R.string.pref_theme))) {
      String theme = (String) newValue;
      switch (theme) {
        case "light":
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
          break;
        case "dark":
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
          break;
        case "system":
          AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
          break;
      }
    }
    Bundle bundle = new Bundle();
    bundle.putString(AnalyticsConstants.Param.SETTING_NAME, preference.getKey());
    bundle.putString(AnalyticsConstants.Param.SETTING_VALUE, String.valueOf(newValue));
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.CHANGE_SETTING, bundle);
    return true;
  }
}
