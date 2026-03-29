package com.antsapps.triples;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.DailyGame;
import com.antsapps.triples.backend.Game;

public class DailyNotificationReceiver extends BroadcastReceiver {

  public static final String CHANNEL_ID = "daily_puzzle_reminder";
  public static final int NOTIFICATION_ID = 101;

  @Override
  public void onReceive(Context context, Intent intent) {
    Application application = Application.getInstance(context);
    DailyGame todayGame = application.getDailyGameByGameDay(DailyGame.Day.forToday());

    if (todayGame == null || todayGame.getGameState() != Game.GameState.COMPLETED) {
      showNotification(context);
    }
  }

  private void showNotification(Context context) {
    createNotificationChannel(context);

    Intent intent = new Intent(context, DailyGameActivity.class);
    DailyGame todayGame =
        Application.getInstance(context).getDailyGameForDate(DailyGame.Day.forToday());
    intent.putExtra(Game.ID_TAG, todayGame.getId());

    PendingIntent pendingIntent =
        PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.daily_notification_title))
            .setContentText(context.getString(R.string.daily_notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
    try {
      notificationManager.notify(NOTIFICATION_ID, builder.build());
    } catch (SecurityException e) {
      // Handle missing permission
    }
  }

  private void createNotificationChannel(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = context.getString(R.string.daily_notification_channel_name);
      String description = context.getString(R.string.daily_notification_channel_description);
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
      channel.setDescription(description);
      NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }
}
