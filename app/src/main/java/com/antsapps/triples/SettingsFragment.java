package com.antsapps.triples;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.SeekBarPreference;
import com.antsapps.triples.backend.Application;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;

public class SettingsFragment extends PreferenceFragmentCompat {

  public static final int DEFAULT_ANIMATION_DURATION = 8;
  public static final int ANIMATION_DURATION_MULTIPLIER = 100;

  private FirebaseAnalytics mFirebaseAnalytics;

  @Override
  public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.preferences);

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

    setupAnalyticsLogging(getPreferenceScreen());

    SeekBarPreference animationDurationPreference =
        findPreference(getString(R.string.pref_animation_speed));
    updateAnimationDurationSummary(
        animationDurationPreference, animationDurationPreference.getValue());
    animationDurationPreference.setOnPreferenceChangeListener(
        (preference, newValue) -> {
          updateAnimationDurationSummary((SeekBarPreference) preference, (int) newValue);
          logAnalyticsEventOnPreferenceChange(preference, newValue);
          return true;
        });

    findPreference(getString(R.string.pref_theme))
        .setOnPreferenceChangeListener(
            (preference, newValue) -> {
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
              logAnalyticsEventOnPreferenceChange(preference, newValue);
              return true;
            });

    updateAccountPreferences();
    updateAboutPreferences();
  }

  private void updateAnimationDurationSummary(
      SeekBarPreference animationDurationPreference, int value) {
    animationDurationPreference.setSummary(
        getString(R.string.pref_animation_duration_summary, value * ANIMATION_DURATION_MULTIPLIER));
  }

  private void updateAboutPreferences() {
    Preference versionPref = findPreference("pref_version");
    if (versionPref != null) {
      try {
        PackageInfo pInfo =
            getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
        String version = pInfo.versionName;
        int versionCode =
            (int) androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(pInfo);
        versionPref.setSummary(getString(R.string.version_format, version, versionCode));
      } catch (PackageManager.NameNotFoundException e) {
        versionPref.setSummary("Unknown");
      }
    }

    Preference feedbackPref = findPreference("pref_feedback");
    if (feedbackPref != null) {
      feedbackPref.setOnPreferenceClickListener(
          preference -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"anthonymorris13+triples@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Triples Feedback");
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
              startActivity(intent);
            }
            return true;
          });
    }
  }

  public void updateAccountPreferences() {
    BaseTriplesActivity activity = (BaseTriplesActivity) getActivity();
    if (activity == null) return;

    Preference userPref = findPreference("account_user");
    Preference signInOutPref = findPreference("account_signin_out");
    Preference deleteDataPref = findPreference("account_delete_data");

    if (activity.isSignedIn()) {
      String info = activity.getSignedInUserInfo();
      userPref.setSummary(getString(R.string.account_signed_in_as, info != null ? info : ""));
      signInOutPref.setTitle(R.string.account_sign_out);
      signInOutPref.setOnPreferenceClickListener(
          preference -> {
            activity.signOut();
            return true;
          });
    } else {
      userPref.setSummary(R.string.account_not_signed_in);
      signInOutPref.setTitle(R.string.account_sign_in);
      signInOutPref.setOnPreferenceClickListener(
          preference -> {
            activity.signIn();
            return true;
          });
    }

    deleteDataPref.setOnPreferenceClickListener(
        preference -> {
          showDeleteDataConfirmation();
          return true;
        });
  }

  private void showDeleteDataConfirmation() {
    AlertDialog dialog =
        new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.account_delete_data_dialog_title)
            .setMessage(R.string.account_delete_data_dialog_message)
            .setPositiveButton(
                R.string.account_delete_button, (dialogInterface, which) -> deleteData())
            .setNegativeButton(R.string.no, null)
            .create();
    dialog.show();
    Button deleteButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
    if (deleteButton != null) {
      deleteButton.setTextColor(ContextCompat.getColor(getContext(), R.color.colorError));
    }
  }

  private void deleteData() {
    BaseTriplesActivity activity = (BaseTriplesActivity) getActivity();
    if (activity == null) return;

    Application application = Application.getInstance(getContext());
    application.clearAllData();
    CloudSaveManager.deleteFromCloud(activity);
  }

  private void setupAnalyticsLogging(PreferenceGroup group) {
    for (int i = 0; i < group.getPreferenceCount(); i++) {
      Preference p = group.getPreference(i);
      if (p instanceof PreferenceGroup) {
        setupAnalyticsLogging((PreferenceGroup) p);
      } else {
        p.setOnPreferenceChangeListener(
            (preference, newValue) -> {
              logAnalyticsEventOnPreferenceChange(preference, newValue);
              return true;
            });
      }
    }
  }

  private void logAnalyticsEventOnPreferenceChange(Preference preference, Object newValue) {
    Bundle bundle = new Bundle();
    bundle.putString(AnalyticsConstants.Param.SETTING_NAME, preference.getKey());
    bundle.putString(AnalyticsConstants.Param.SETTING_VALUE, String.valueOf(newValue));
    mFirebaseAnalytics.logEvent(AnalyticsConstants.Event.CHANGE_SETTING, bundle);
  }

  public static int getAnimationDuration(Context context) {
    int prefValue =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(context.getString(R.string.pref_animation_speed), DEFAULT_ANIMATION_DURATION);
    return prefValue <= 10 ? prefValue * ANIMATION_DURATION_MULTIPLIER : prefValue;
  }
}
