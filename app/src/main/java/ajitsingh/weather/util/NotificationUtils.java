package ajitsingh.weather.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import ajitsingh.weather.R;
import ajitsingh.weather.Weather;

public class NotificationUtils {
    public static void sendNotification(Context activity, String title, String content) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(activity)
                        .setSmallIcon(R.drawable.ic_weather)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSound(alarmSound)
                        .setStyle(new NotificationCompat.InboxStyle());
        Intent resultIntent = new Intent(activity, Weather.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(activity);
        stackBuilder.addParentStack(Weather.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    public static void vibrateFor(Context context, int milliseconds) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(milliseconds);
    }
}
