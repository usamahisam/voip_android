package com.breakreasi.voip_android.voip;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import com.breakreasi.voip_android.notification.NotificationCall;
import com.breakreasi.voip_android.sip.SipCall;
import com.breakreasi.voip_android.sip.SipCallData;
import com.breakreasi.voip_android.sip.SipCamera;
import com.breakreasi.voip_android.sip.SipManager;
import com.breakreasi.voip_android.sip.SipManagerCallback;

import org.pjsip.pjsua2.AccountInfo;
import org.pjsip.pjsua2.VideoWindow;

import java.util.ArrayList;
import java.util.List;

public class VOIP implements SipManagerCallback {
    private Context context;
    private VOIPType type;
    private List<VOIPCallback> callbacks;
    private CameraManager cm;
    private AudioManager am;
    private NotificationManager nm;
    private SipManager sip;
    private NotificationCall notificationCall;
    private Class<? extends BroadcastReceiver> br;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public VOIP(Context context) {
        this.context = context;
        this.callbacks = new ArrayList<>();
    }

    public void setType(VOIPType type) {
        this.type = type;
    }

    public void init(VOIPType type, Context context) {
        setType(type);
        cm = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationCall == null) {
            notificationCall = new NotificationCall(context, this);
        }
        if (this.type == VOIPType.SIP
        && sip == null) {
            sip = new SipManager(context, cm, am);
        }
        sip.registerListener(this);
    }

    public AudioManager getAudioManager() {
        return am;
    }

    public NotificationManager getNotificationManager() {
        return nm;
    }

    public NotificationCall getNotification() {
        return notificationCall;
    }

    public void auth(String username, String password) {
        if (this.type == VOIPType.SIP) {
            sip.register(username, username, password, false);
        }
    }

    public void makeCall(String to, boolean isVideo) {
        if (this.type == VOIPType.SIP) {
            sip.makeCall(to, isVideo);
        }
    }

    public void accept() {
        if (this.type == VOIPType.SIP) {
            sip.accept();
        }
    }

    public void hangup() {
        if (this.type == VOIPType.SIP) {
            sip.decline();
        }
    }

    public void setMute(boolean isMute) {
        if (this.type == VOIPType.SIP) {
            if (isMute) {
                sip.getSettingSip().muteMic();
            } else {
                sip.getSettingSip().unmuteMic();
            }
        }
    }

    public void setLoudspeaker(boolean isLoudspeaker) {
        if (this.type == VOIPType.SIP) {
            if (isLoudspeaker) {
                sip.getSettingSip().turnOnSpeakerphone();
            } else {
                sip.getSettingSip().turnOffSpeakerphone();
            }
        }
    }

    public void switchCamera(VOIPCamera voipCamera) {
        if (this.type == VOIPType.SIP) {
            if (voipCamera == VOIPCamera.AUTO) {
                if (sip.getSettingSip().getSipCamera() == SipCamera.FRONT) {
                    sip.getSettingSip().switchCamera(SipCamera.BACK);
                } else {
                    sip.getSettingSip().switchCamera(SipCamera.FRONT);
                }
            } else if (voipCamera == VOIPCamera.FRONT) {
                sip.getSettingSip().switchCamera(SipCamera.FRONT);
            } else if (voipCamera == VOIPCamera.BACK) {
                sip.getSettingSip().switchCamera(SipCamera.BACK);
            }
        }
    }

    public void setLocalVideo(SurfaceView localVideo) {
        if (this.type == VOIPType.SIP) {
            sip.getVideo().setLocalVideo(localVideo);
        }
    }

    public void unsetLocalVideo() {
        if (this.type == VOIPType.SIP) {
            sip.getVideo().unsetLocalVideo();
        }
    }

    public void setRemoteVideo(SurfaceView remoteVideo) {
        if (this.type == VOIPType.SIP) {
            sip.getVideo().setRemoteVideo(remoteVideo);
        }
    }

    public void changeSurfaceRemoteVideo() {
        if (this.type == VOIPType.SIP) {
            sip.getVideo().toggleSurfaceRemoteFit();
        }
    }

    public void unsetRemoteVideo() {
        if (this.type == VOIPType.SIP) {
            sip.getVideo().unsetRemoteVideo();
        }
    }

    @Nullable
    public VOIPCallData getCallData() {
        if (this.type == VOIPType.SIP) {
            SipCallData data = sip.getCall().getCallData();
            if (data != null) {
                return new VOIPCallData(data.getDisplayName(), data.getPhone());
            }
        }
        return null;
    }

    public void setCallback(VOIPCallback callback) {
        if (callback != null && !callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public void unsetCallback(VOIPCallback callback) {
        callbacks.remove(callback);
    }

    public void notifyCallbacks(String status) {
        uiHandler.post(() -> {
            if (status.contains("incoming")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (br != null) {
                        getNotification().notifyCall(br);
                    }
                }
            }
            if (status.contains("disconnected")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (br != null) {
                        getNotification().cancelNotify();
                    }
                }
            }
        });
        for (VOIPCallback callback : new ArrayList<>(callbacks)) {
            callback.onStatus(status);
        }
    }

    public void setBroadcastNotification(Class<? extends BroadcastReceiver> br) {
        this.br = br;
    }

    @Override
    public void onSipAccountInfo(AccountInfo accountInfo, String status) {
        notifyCallbacks("account_" + status);
    }

    @Override
    public void onSipCall(SipCall call, String status) {
        notifyCallbacks(status);
    }

    public void destroy() {
        if (this.type == VOIPType.SIP) {
            sip.destroy();
        }
    }
}