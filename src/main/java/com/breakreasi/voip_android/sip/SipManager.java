package com.breakreasi.voip_android.sip;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;

import org.pjsip.PjCameraInfo2;
import org.pjsip.pjsua2.AccountInfo;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.VideoWindow;

import java.util.ArrayList;
import java.util.List;

public class SipManager {
    private Context mContext;
    private CameraManager cm;
    private AudioManager am;
    private SipConfig config;
    private SipVideo videoStream;
    private Endpoint endpoint;
    private EpConfig epConfig;
    private AccountSip account;
    private SettingSip setting;
    private String displayName;
    private String username;
    private String password;
    private CallSip call;
    private String authSessionFor;
    private String callTo;
    private boolean callIsVideo;
    private List<SipManagerCallback> callbacks;

    public SipManager(Context context, CameraManager cm, AudioManager am) {
        this.mContext = context;
        this.cm = cm;
        this.am = am;
        callbacks = new ArrayList<>();
        initConfig();
        initEngine();
    }

    public void initConfig() {
        config = new SipConfig();
        videoStream = new SipVideo(this);
    }

    public SipConfig getConfig() {
        return config;
    }

    public SipVideo getVideo() {
        return videoStream;
    }

    private void initEngine() {
        try {
            PjCameraInfo2.SetCameraManager(cm);

            endpoint = new Endpoint();
            endpoint.libCreate();

            epConfig = new EpConfig();
            epConfig.getUaConfig().setUserAgent(String.format(
                    "Jasvicall Billing SIP Client/%s (%s %s; Android %s)",
                    1.1,
                    Build.MANUFACTURER,
                    Build.MODEL,
                    Build.VERSION.RELEASE
            ));
            endpoint.libInit(epConfig);

            TransportConfig transportConfig = new TransportConfig();
            transportConfig.setPort(config.getSIP_PORT());
            endpoint.transportCreate(config.getSIP_TRANSPORT_UDP(), transportConfig);
            endpoint.transportCreate(config.getSIP_TRANSPORT_TCP(), transportConfig);
            endpoint.libStart();

        } catch (Exception ignored) {
        }

        init();
    }

    private void init() {
        setting = new SettingSip(this);
        account = new AccountSip(mContext, this);
    }

    public SettingSip getSettingSip() {
        return setting;
    }

    public AccountSip getAccountSip() {
        return account;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public boolean getLogin() {
        return account.getLogin();
    }

    public AudioManager getAudioManager() {
        return am;
    }

    public void register(String displayName, String username, String password, boolean registration) {
        authSessionFor = "";
        this.displayName = displayName;
        this.username = username;
        this.password = password;
        account.register(displayName, username, password, registration);
    }

    public CallSip initCall() {
        call = account.initCall();
        return call;
    }

    public void makeCall(String callTo, boolean callIsVideo) {
        authSessionFor = "makeCall";
        this.callTo = callTo;
        this.callIsVideo = callIsVideo;
        account.register(displayName, username, password, true);
    }

    public CallSip getCall() {
        return call;
    }

    public AccountSip getAccount() {
        return account;
    }

    public void registerListener(SipManagerCallback callback) {
        callbacks.add(callback);
    }

    public void unregisterListener(SipManagerCallback callback) {
        callbacks.remove(callback);
    }

    public void onAccountSipStatus(AccountInfo accountInfo, String status) {
        if (status.equals("success")) {
            if (authSessionFor.equals("makeCall")) {
                initCall().createCall(callTo, callIsVideo);
                authSessionFor = "";
            }
        }
        for (SipManagerCallback callback : callbacks) {
            callback.onSipAccountInfo(accountInfo, status);
        }
    }

    public void onCall(CallSip call, String status) {
        if (status.contains("incoming")) {
            this.call = call;
        }
        if (status.contains("connected")) {
            setting.setAudioCommunication();
            if (status.contains("video")) {
                setting.turnOnSpeakerphone();
            } else {
                setting.turnOffSpeakerphone();
            }
        }
        if (status.contains("disconnected")) {
            setting.setAudioNormal();
        }
        for (SipManagerCallback callback : callbacks) {
            callback.onSipCall(call, status);
        }
    }

    public void onSipVideo(VideoWindow videoWindow, String status) {
        if (status.equals("video_local")) {
            if (getVideo().getLocalVideoHandler() != null) {
                Log.d("xcodex AAAA", "local");
                SurfaceUtil.surfaceToTop(getVideo().getLocalVideo());
                getVideo().getLocalVideoHandler().resetVideoWindow();
                getVideo().getLocalVideoHandler().setVideoWindow(videoWindow);
            }
        } else if (status.equals("video_remote")) {
            if (getVideo().getRemoteVideoHandler() != null) {
                Log.d("xcodex AAAA", "remote");
                SurfaceUtil.surfaceToBottom(getVideo().getRemoteVideo());
                SurfaceUtil.resizeSurface(getVideo().getRemoteVideo(), videoWindow, true);
                getVideo().getRemoteVideoHandler().resetVideoWindow();
                getVideo().getRemoteVideoHandler().setVideoWindow(videoWindow);
            }
        }
        for (SipManagerCallback callback : callbacks) {
            callback.onSipVideo(videoWindow, status);
        }
    }

    public void destroy() {
        epConfig.delete();
        endpoint.delete();
        try {
            endpoint.libDestroy();
        } catch (Exception ignored) {
        }
    }
}
