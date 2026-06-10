package com.antsapps.triples;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import java.util.Calendar;

public class NotificationUtils {

  public static final String PREF_DAILY_NOTIFICATION_ENABLED = "pref_daily_notification_enabled";
  public static final String PREF_DAILY_NOTIFICATION_HOUR = "pref_daily_notification_hour";
  public static final String PREF_DAILY_NOTIFICATION_MINUTE = "pref_daily_notification_minute";

  public static void scheduleDailyNotification(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    boolean isEnabled = prefs.getBoolean(PREF_DAILY_NOTIFICATION_ENABLED, false);
    scheduleDailyNotification(context, isEnabled);
  }

  public static void scheduleDailyNotification(Context context, boolean isEnabled) {
    if (!isEnabled) {
      cancelDailyNotification(context);
      return;
    }

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    int hour = prefs.getInt(PREF_DAILY_NOTIFICATION_HOUR, 20); // Default 8 PM
    int minute = prefs.getInt(PREF_DAILY_NOTIFICATION_MINUTE, 0);

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, DailyNotificationReceiver.class);
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, hour);
    calendar.set(Calendar.MINUTE, minute);
    calendar.set(Calendar.SECOND, 0);

    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
      calendar.add(Calendar.DAY_OF_YEAR, 1);
    }

    alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.getTimeInMillis(),
        AlarmManager.INTERVAL_DAY,
        pendingIntent);
  }

  public static void cancelDailyNotification(Context context) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, DailyNotificationReceiver.class);
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    alarmManager.cancel(pendingIntent);
  }
}
