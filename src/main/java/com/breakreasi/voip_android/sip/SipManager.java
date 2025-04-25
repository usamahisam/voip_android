package com.breakreasi.voip_android.sip;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.os.Build;

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
    private SipConfig config;
    private Endpoint endpoint;
    private EpConfig epConfig;
    private AccountSip account;
    private SettingSip setting;
    private List<SipManagerCallback> callbacks;

    public SipManager(Context context, CameraManager cm) {
        this.mContext = context;
        this.cm = cm;
        callbacks = new ArrayList<>();
        initConfig();
        initEngine();
    }

    public void initConfig() {
        config = new SipConfig();
    }

    public SipConfig getConfig() {
        return config;
    }

    private void initEngine() {
        try {
            PjCameraInfo2.SetCameraManager(cm);

            endpoint = new Endpoint();
            endpoint.libCreate();

            EpConfig epConfig = new EpConfig();
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
        account = new AccountSip(mContext, this);
        setting = new SettingSip(this);
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

    public void register(String username, String password, boolean registration) {
        account.register(username, password, registration);
    }

    public CallSip initCall() {
        return account.initCall();
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
        for (SipManagerCallback callback : callbacks) {
            callback.onSipAccountInfo(accountInfo, status);
        }
    }

    public void onCall(CallSip call, String status) {
        for (SipManagerCallback callback : callbacks) {
            callback.onSipCall(call, status);
        }
    }

    public void onSipVideo(VideoWindow localVideoWindow, VideoWindow rocalVideoWindow) {
        for (SipManagerCallback callback : callbacks) {
            callback.onSipVideo(localVideoWindow, rocalVideoWindow);
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
