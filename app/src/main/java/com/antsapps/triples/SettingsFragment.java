package com.antsapps.triples;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;
import com.antsapps.triples.backend.Application;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.Calendar;

public class SettingsFragment extends PreferenceFragmentCompat {

  public static final int DEFAULT_ANIMATION_DURATION = 8;

  private ActivityResultLauncher<String> mRequestPermissionLauncher =
      registerForActivityResult(
          new ActivityResultContracts.RequestPermission(),
          isGranted -> {
            SwitchPreferenceCompat enabledPref =
                findPreference(NotificationUtils.PREF_DAILY_NOTIFICATION_ENABLED);
            if (isGranted) {
              NotificationUtils.scheduleDailyNotification(getContext(), true);
            } else {
              if (enabledPref != null) {
                enabledPref.setChecked(false);
              }
            }
          });

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

    updateDailyNotificationPreferences();
    updateAccountPreferences();
    updateAboutPreferences();
  }

  private void updateDailyNotificationPreferences() {
    SwitchPreferenceCompat enabledPref =
        findPreference(NotificationUtils.PREF_DAILY_NOTIFICATION_ENABLED);
    Preference timePref = findPreference("pref_daily_notification_time");

    enabledPref.setOnPreferenceChangeListener(
        (preference, newValue) -> {
          boolean enabled = (boolean) newValue;
          if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              if (ContextCompat.checkSelfPermission(
                      getContext(), Manifest.permission.POST_NOTIFICATIONS)
                  == PackageManager.PERMISSION_GRANTED) {
                NotificationUtils.scheduleDailyNotification(getContext(), true);
              } else {
                mRequestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
              }
            } else {
              NotificationUtils.scheduleDailyNotification(getContext(), true);
            }
          } else {
            NotificationUtils.cancelDailyNotification(getContext());
          }
          return true;
        });

    updateTimePrefSummary(timePref);
    timePref.setOnPreferenceClickListener(
        preference -> {
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
          int hour = prefs.getInt(NotificationUtils.PREF_DAILY_NOTIFICATION_HOUR, 20);
          int minute = prefs.getInt(NotificationUtils.PREF_DAILY_NOTIFICATION_MINUTE, 0);

          TimePickerDialog timePickerDialog =
              new TimePickerDialog(
                  getContext(),
                  (view, hourOfDay, minuteOfHour) -> {
                    prefs
                        .edit()
                        .putInt(NotificationUtils.PREF_DAILY_NOTIFICATION_HOUR, hourOfDay)
                        .putInt(NotificationUtils.PREF_DAILY_NOTIFICATION_MINUTE, minuteOfHour)
                        .apply();
                    updateTimePrefSummary(timePref);
                    NotificationUtils.scheduleDailyNotification(getContext());
                  },
                  hour,
                  minute,
                  android.text.format.DateFormat.is24HourFormat(getContext()));
          timePickerDialog.show();
          return true;
        });
  }

  private void updateTimePrefSummary(Preference timePref) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    int hour = prefs.getInt(NotificationUtils.PREF_DAILY_NOTIFICATION_HOUR, 20);
    int minute = prefs.getInt(NotificationUtils.PREF_DAILY_NOTIFICATION_MINUTE, 0);

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, minute);

    java.text.DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(getContext());
    timePref.setSummary(
        getString(
            R.string.pref_daily_notification_time_summary, dateFormat.format(calendar.getTime())));
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
              startActivity(Intent.createChooser(intent, getString(R.string.pref_feedback_title)));
            } else {
              Toast.makeText(getContext(), R.string.no_email_clients, Toast.LENGTH_SHORT).show();
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
