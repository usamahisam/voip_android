package com.breakreasi.voip_android.sip;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import org.pjsip.PjCameraInfo2;
import org.pjsip.pjsua2.AccountInfo;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.pj_qos_type;

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
    private SipAccount account;
    private SipSetting setting;
    private String displayName;
    private String username;
    private String password;
    private SipCall call;
    private String authSessionFor;
    private String callTo;
    private boolean callIsVideo;
    private List<SipManagerCallback> callbacks;

    public SipManager(Context context, CameraManager cm, AudioManager am) {
        this.mContext = context;
        this.cm = cm;
        this.am = am;
        callbacks = new ArrayList<>();
        initStart();
        initEngine();
    }

    public void initStart() {
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
            epConfig.getUaConfig().getStunServer().add("stun:stun.l.google.com:19302");
            epConfig.getMedConfig().setHasIoqueue(true);
            epConfig.getMedConfig().setClockRate(16000);
            epConfig.getMedConfig().setQuality(10);
            epConfig.getMedConfig().setEcOptions(1);
            epConfig.getMedConfig().setEcTailLen(200);
            epConfig.getMedConfig().setThreadCnt(2);
            endpoint.libInit(epConfig);

            TransportConfig udpTransport = new TransportConfig();
            udpTransport.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
            udpTransport.setPort(config.getSIP_PORT());

            endpoint.transportCreate(config.getSIP_TRANSPORT_UDP(), udpTransport);

            endpoint.libStart();

        } catch (Exception ignored) {
        }

        init();
    }

    private void init() {
        setting = new SipSetting(this);
        account = new SipAccount(mContext, this);
    }

    public SipSetting getSettingSip() {
        return setting;
    }

    public SipAccount getAccountSip() {
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
        if (getLogin()) {
            account.accountInfo();
        } else {
            account.register(displayName, username, password, registration);
        }
    }

    public SipCall initCall() {
        call = account.initCall();
        return call;
    }

    public void makeCall(String callTo, boolean callIsVideo) {
        this.callTo = callTo;
        this.callIsVideo = callIsVideo;
        if (account.getLogin()) {
            this.call = initCall();
            this.call.createCall(callTo, callIsVideo);
            authSessionFor = "";
        } else {
            authSessionFor = "makeCall";
            account.register(displayName, username, password, true);
        }
    }

    public void accept() {
        if (getCall() == null) {
            onCall(null, "call_disconnected");
            return;
        }
        getCall().accept();
    }

    public void decline() {
        if (getCall() == null) {
            onCall(null, "call_disconnected");
            return;
        }
        getCall().decline();
    }

    public SipCall getCall() {
        return call;
    }

    public SipAccount getAccount() {
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
                this.call = initCall();
                this.call.createCall(callTo, callIsVideo);
                authSessionFor = "";
            }
        }
        for (SipManagerCallback callback : callbacks) {
            callback.onSipAccountInfo(accountInfo, status);
        }
    }

    public void onCall(SipCall call, String status) {
        if (status.contains("incoming")) {
            this.call = call;
        }
        if (status.contains("connected")) {
            getVideo().startRemoteVideo();
            getVideo().startLocalVideo();
        }
        if (status.contains("media_video")) {
            setting.setAudioCommunication();
            if (status.contains("_on")) {
                setting.turnOnSpeakerphone();
            } else {
                setting.turnOffSpeakerphone();
            }
        }
        if (status.contains("disconnected")) {
            setting.setAudioNormal();
            this.call = null;
        }
        for (SipManagerCallback callback : callbacks) {
            callback.onSipCall(call, status);
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
