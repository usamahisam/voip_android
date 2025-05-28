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
import com.breakreasi.voip_android.voip.VOIPType;

import java.util.Calendar;

public class NotificationCall {
    private static final String CHANNEL_ID = "VOIP_ANDROID_123456";
    private static final int NM_ID = 1001;

    private final Context context;
    private final VOIP voip;
    private Ringtone ringtone;

    public NotificationCall(Context context, VOIP voip) {
        this.context = context;
        this.voip = voip;
    }

    public void notificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for incoming VOIP calls");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            if (voip.getNotificationManager() != null) {
                voip.getNotificationManager().createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotifyCall(Class<? extends BroadcastReceiver> broadcastClass, VOIPType voipType, String title, String message) {
        notificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.btn_startcall_normal);
        builder.setOngoing(true);
        builder.setAutoCancel(false);
//        builder.setShowWhen(true);
//        builder.setTimeoutAfter(120 * DateUtils.SECOND_IN_MILLIS);
        builder.setTicker("CALL_STATUS");
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setWhen(Calendar.getInstance().getTimeInMillis());
        builder.setCategory(NotificationCompat.CATEGORY_CALL);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // Full-screen intent for incoming call UI
//        Intent fullScreenIntent = new Intent(context, );
//        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
//                context,
//                3,
//                fullScreenIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
        builder.setFullScreenIntent(null, true);
        builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());

        // Actions: Answer & Decline
        Intent jawabIntent = new Intent(context, broadcastClass);
        jawabIntent.setAction("VOIP_ACTION_ANSWER_CALL");
        jawabIntent.putExtra("action_to", voipType.name());
        PendingIntent jawabPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                jawabIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent tolakIntent = new Intent(context, broadcastClass);
        tolakIntent.setAction("VOIP_ACTION_DECLINE_CALL");
        jawabIntent.putExtra("action_to", voipType.name());
        PendingIntent tolakPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                tolakIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        builder.addAction(new NotificationCompat.Action.Builder(
                IconCompat.createWithResource(context, R.drawable.btn_startcall),
                "JAWAB",
                jawabPendingIntent
        ).build());

        builder.addAction(new NotificationCompat.Action.Builder(
                IconCompat.createWithResource(context, R.drawable.btn_endcall),
                "TOLAK",
                tolakPendingIntent
        ).build());

        // Vibration + Ringtone
        if (voip.getAudioManager().getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            builder.setVibrate(new long[]{0, 1000, 1000});
        } else if (voip.getAudioManager().getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            builder.setVibrate(new long[]{0, 1000, 1000});
            playRingtone();
        }

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        return notification;
    }

    public void notifyCall(VOIPType voipType, Class<? extends BroadcastReceiver> broadcastClass) {
        VOIPCallData data = voip.getCallData();
        if (data == null || voip.getNotificationManager() == null) return;

        String title = "Panggilan masuk";
        String message = data.getDisplayName() + " - " + data.getPhone();

        voip.getNotificationManager().notify(NM_ID, buildNotifyCall(broadcastClass, voipType, title, message));

        new android.os.Handler().postDelayed(() -> {
            if (voip.getCallData() != null && !voip.isCallActive()) {
                showMissedCallNotification();
                cancelNotify();
            }
        }, 30 * 1000);
    }

    private void showMissedCallNotification() {
        VOIPCallData data = voip.getCallData();
        if (data == null || voip.getNotificationManager() == null) return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.btn_endcall)
                .setContentTitle("Panggilan tak terjawab")
                .setContentText(data.getDisplayName() + " - " + data.getPhone())
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        voip.getNotificationManager().notify(NM_ID + 1, builder.build());
    }

    public void cancelNotify() {
        stopRingtone();
        if (voip.getNotificationManager() != null) {
            voip.getNotificationManager().cancel(NM_ID);
        }
    }

    private void playRingtone() {
        stopRingtone();
        try {
            Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    context.getPackageName() + "/" + R.raw.basic_ring);
            ringtone = RingtoneManager.getRingtone(context, soundUri);
            if (ringtone != null && !ringtone.isPlaying()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.setLooping(true);
                }
                ringtone.play();
            }
        } catch (Exception ignored) {
        }
    }

    public synchronized void stopRingtone() {
        if (ringtone != null) {
            try {
                if (ringtone.isPlaying()) ringtone.stop();
            } catch (Exception ignored) {
            }
            ringtone = null;
        }
    }
}
