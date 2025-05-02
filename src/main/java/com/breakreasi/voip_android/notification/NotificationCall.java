package com.breakreasi.voip_android.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.breakreasi.voip_android.R;
import com.breakreasi.voip_android.voip.VOIP;
import com.breakreasi.voip_android.voip.VOIPCallData;

import java.util.Calendar;

public class NotificationCall {
    private static String CHANNEL_ID = "VOIP_ANDROID_123456";
    private static int NM_ID = 1001;

    private Context context;
    private VOIP voip;
    private Ringtone ringtone;

    public NotificationCall(Context context, VOIP voip) {
        this.context = context;
        this.voip = voip;
    }

    public void notificationChannel() {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, "Call Notifications", NotificationManager.IMPORTANCE_HIGH);
            if (voip.getNotificationManager() != null) {
                voip.getNotificationManager().createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotifyCall(Class<? extends BroadcastReceiver> broadcastClass, String title, String message, boolean isVideoCall) {
        notificationChannel();
        NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationCompat.setContentTitle(title);
        notificationCompat.setContentText(message);
        notificationCompat.setSmallIcon(R.drawable.btn_startcall_normal);
        notificationCompat.setOngoing(true);
        notificationCompat.setAutoCancel(false);
        notificationCompat.setShowWhen(false);
        notificationCompat.setTimeoutAfter(9000000);
        notificationCompat.setTicker("CALL_STATUS");
        notificationCompat.setDefaults(Notification.DEFAULT_ALL);
        notificationCompat.setLights(0xff0000ff, 3000000, 100000);
        notificationCompat.setWhen(Calendar.getInstance().getTimeInMillis());
        notificationCompat.setCategory(NotificationCompat.CATEGORY_CALL);
        notificationCompat.setPriority(NotificationCompat.PRIORITY_MAX);

        Intent jawabIntent = new Intent(context, broadcastClass);
        jawabIntent.setAction("VOIP_ACTION_ANSWER_CALL");
        PendingIntent jawabPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                jawabIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Intent tolakIntent = new Intent(context, broadcastClass);
        tolakIntent.setAction("VOIP_ACTION_DECLINE_CALL");
        PendingIntent tolakPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                tolakIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        notificationCompat.addAction(new NotificationCompat.Action.Builder(IconCompat.createWithResource(context, R.drawable.btn_startcall), "JAWAB", jawabPendingIntent).build());
        notificationCompat.addAction(new NotificationCompat.Action.Builder(IconCompat.createWithResource(context, R.drawable.btn_endcall), "TOLAK", tolakPendingIntent).build());
        if (voip.getAudioManager().getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            notificationCompat.setVibrate(new long[]{0, 10 * DateUtils.SECOND_IN_MILLIS});
        } else if (voip.getAudioManager().getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            notificationCompat.setVibrate(new long[]{0, 10 * DateUtils.SECOND_IN_MILLIS});
            toneDial();
        }
        Notification notification  = notificationCompat.build();
        notification.flags = notification.flags | Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR;
        return notification;
    }

    public void notifyCall(Class<? extends BroadcastReceiver> broadcastClass) {
        VOIPCallData data = voip.getCallData();
        if (data == null) {
            return;
        }
        String title = "Panggilan " + (data.isVideo() ? "video" : "audio") + " masuk";
        String message = data.getDisplayName() + " - " + data.getPhone();
        boolean isVideoCall = data.isVideo();
        if (voip.getNotificationManager() == null) {
            return;
        }
        voip.getNotificationManager().notify(NM_ID, buildNotifyCall(broadcastClass, title, message, isVideoCall));
    }

    public void cancelNotify() {
        stopMediaPlayer();
        if (voip.getNotificationManager() == null) {
            return;
        }
        voip.getNotificationManager().cancel(NM_ID);
    }

    private void toneDial() {
        stopMediaPlayer();
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ context.getPackageName() + "/" + R.raw.basic_ring);
        ringtone = RingtoneManager.getRingtone(context, soundUri);
        try {
            ringtone.play();
        } catch (Exception ignored) {
            ringtone = null;
        }
    }

    public synchronized void stopMediaPlayer() {
        if (ringtone != null) {
            try {
                ringtone.stop();
            } catch (Exception ignored) {
            }
            ringtone = null;
        }
    }
}
