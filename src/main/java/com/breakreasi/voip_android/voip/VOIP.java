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

import com.breakreasi.voip_android.agora.AgoraIEventListener2;
import com.breakreasi.voip_android.agora.AgoraManager;
import com.breakreasi.voip_android.notification.NotificationCall;
import com.breakreasi.voip_android.sip.SipCall;
import com.breakreasi.voip_android.sip.SipCallData;
import com.breakreasi.voip_android.sip.SipCamera;
import com.breakreasi.voip_android.sip.SipManager;
import com.breakreasi.voip_android.sip.SipManagerCallback;

import org.pjsip.pjsua2.AccountInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.rtc2.Constants;

public class VOIP implements SipManagerCallback, AgoraIEventListener2 {
    private Context context;
    private VOIPType type;
    private List<VOIPCallback> callbacks;
    private CameraManager cm;
    private AudioManager am;
    private NotificationManager nm;
    private SipManager sip;
    private AgoraManager agora;
    private boolean isCallActive;
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
        this.isCallActive = false;
        setType(type);
        if (cm == null) {
            cm = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
        if (am == null) {
            am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        if (nm == null) {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (notificationCall == null) {
            notificationCall = new NotificationCall(context, this);
        }
//        if (this.type == VOIPType.SIP && sip != null) {
//            sip.destroy();
//        }
//        if (this.type == VOIPType.AGORA && agora != null) {
//            agora.destroyEngine();
//        }
        if (this.type == VOIPType.SIP) {
            sip = new SipManager(context, cm, am);
            sip.registerListener(this);
        }
        if (this.type == VOIPType.AGORA) {
            agora = new AgoraManager(context);
            agora.registerEventListener(this);
        }
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

    public void auth(String username, String password, String token, boolean isVideo) {
        if (this.type == VOIPType.SIP) {
            sip.register(username, username, password);
        } else if (this.type == VOIPType.AGORA) {
            agora.auth(username, token, isVideo);
        }
    }

    public boolean isCallActive() {
        return isCallActive;
    }

    public void makeCall(String to, boolean isVideo) {
        if (this.type == VOIPType.SIP) {
            sip.makeCall(to, isVideo);
        }
    }

    public void accept() {
        if (this.type == VOIPType.SIP) {
            sip.accept();
        } else if (this.type == VOIPType.AGORA) {
            agora.accept();
        }
    }

    public void hangup() {
        if (this.type == VOIPType.SIP) {
            sip.decline();
        } else if (this.type == VOIPType.AGORA) {
            agora.hangup();
        }
    }

    public void setMute(boolean isMute) {
        if (this.type == VOIPType.SIP) {
            if (isMute) {
                sip.getSettingSip().muteMic();
            } else {
                sip.getSettingSip().unmuteMic();
            }
        } else if (this.type == VOIPType.AGORA) {
            if (isMute) {
                agora.rtcEngine().muteLocalAudioStream(true);
            } else {
                agora.rtcEngine().muteLocalAudioStream(false);
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
        } else if (this.type == VOIPType.AGORA) {
            if (isLoudspeaker) {
                agora.rtcEngine().enableLocalAudio(true);
            } else {
                agora.rtcEngine().enableLocalAudio(false);
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
        } else if (this.type == VOIPType.AGORA) {
            agora.rtcEngine().switchCamera();
        }
    }

    public void setLocalVideo(SurfaceView localVideo) {
        if (this.type == VOIPType.SIP) {
            sip.getVideo().setLocalVideo(localVideo);
        } else if (this.type == VOIPType.AGORA) {
            agora.setLocalVideo(localVideo);
        }
    }

    public void unsetLocalVideo() {
        if (this.type == VOIPType.SIP) {
            sip.getVideo().unsetLocalVideo();
        } else if (this.type == VOIPType.AGORA) {
            agora.unsetLocalVideo();
        }
    }

    public void setRemoteVideo(SurfaceView remoteVideo) {
        if (this.type == VOIPType.SIP) {
            sip.getVideo().setRemoteVideo(remoteVideo);
        } else if (this.type == VOIPType.AGORA) {
            agora.setRemoteVideo(remoteVideo);
        }
    }

    public void changeSurfaceRemoteVideo() {
        if (this.type == VOIPType.SIP) {
            sip.getVideo().toggleSurfaceRemoteFit();
        } else if (this.type == VOIPType.AGORA) {
        }
    }

    public void unsetRemoteVideo() {
        if (this.type == VOIPType.SIP) {
            sip.getVideo().unsetRemoteVideo();
        } else if (this.type == VOIPType.AGORA) {
            agora.unsetRemoteVideo();
        }
    }

    @Nullable
    public VOIPCallData getCallData() {
        if (this.type == VOIPType.SIP) {
            if (sip == null) return null;
            if (sip.getCall() == null) return null;
            SipCallData data = sip.getCall().getCallData();
            if (data != null) {
                return new VOIPCallData(data.getDisplayName(), data.getPhone());
            }
        } else if (this.type == VOIPType.AGORA) {
            return new VOIPCallData(agora.getDisplayName(), agora.getDisplayName());
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
        isCallActive = status.contains("connected");
        uiHandler.post(() -> {
            if (status.contains("incoming")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (br != null) {
                        getNotification().notifyCall(status.contains("agora") ? VOIPType.AGORA : VOIPType.SIP, br);
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

    @Override
    public void onAgoraStatus(String status) {
        if (status.contains("success")) {
            notifyCallbacks("call_incoming_agora");
        }
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
    }

    @Override
    public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        isCallActive = true;
        if (agora.getIsVideo()) {
            agora.startRemoteVideo(uid);
            agora.startLocalVideo();
        }
        notifyCallbacks("call_connected");
        notifyCallbacks("call_media_video_" + (agora.getIsVideo() ? "on" : "off"));
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        isCallActive = false;
        notifyCallbacks("call_disconnected");
    }

    @Override
    public void onConnectionStateChanged(int status, int reason) {
        if (status == Constants.CONNECTION_STATE_DISCONNECTED) {
            isCallActive = false;
            notifyCallbacks("call_disconnected");
        }
    }

    @Override
    public void onPeersOnlineStatusChanged(Map<String, Integer> map) {

    }

    @Override
    public void onError(int err) {
        notifyCallbacks("call_disconnected");
    }

    public void destroy() {
        if (sip != null) {
            sip.unregisterListener(this);
            sip.destroy();
            sip = null;
        }
        if (agora != null) {
            agora.removeEventListener(this);
            agora.destroyEngine();
            agora = null;
        }
    }
}