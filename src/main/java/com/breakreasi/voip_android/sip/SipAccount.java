package com.breakreasi.voip_android.sip;

import android.content.Context;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AccountInfo;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnRegStartedParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.pj_constants_;
import org.pjsip.pjsua2.pjmedia_srtp_use;
import org.pjsip.pjsua2.pjsip_status_code;
import java.util.Objects;

public class SipAccount extends Account {
    private Context context;
    private SipManager manager;
    private AccountConfig accCfg;
    private SipCall call;
    public String displayName;
    public String username;
    public String password;
    public boolean isCreated;
    public boolean isRegistered;

    public SipAccount(Context mContext, SipManager mManager) {
        context = mContext;
        manager = mManager;
        isRegistered = false;
        call = null;
        displayName = "";
        username = "";
        password = "";
    }

    public void setAuth(String displayName, String username, String password) {
        this.displayName = displayName;
        this.username = username;
        this.password = password;
    }

    public void register(String displayName, String username, String password) {
        if (isCreated) {
            try {
                setRegistration(true);
            } catch (Exception ignored) {
                manager.onAccountSipStatus(null, "Registration failed");
            }
            return;
        }

        isCreated = false;
        isRegistered = false;

        setAuth(displayName, username, password);

        AuthCredInfoVector credArray = new AuthCredInfoVector();
        AuthCredInfo cred = new AuthCredInfo("digest", "*", username, 0, password);
        credArray.add(cred);

        accCfg = new AccountConfig();
        accCfg.setIdUri("\"" + displayName + "\" <sip:" + username + "@" + manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT() + ">");
        accCfg.getSipConfig().setAuthCreds(credArray);
        accCfg.getSipConfig().getProxies().clear();
//        accCfg.getSipConfig().getProxies().add("sip:"+manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT() + ";transport=UDP;port=" + manager.getConfig().getSIP_PORT());
        accCfg.getRegConfig().setRegistrarUri("sip:" + manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT());
        accCfg.getRegConfig().setRegisterOnAdd(true);
        accCfg.getRegConfig().setDropCallsOnFail(true);
        accCfg.getVideoConfig().setDefaultCaptureDevice(1);
        accCfg.getVideoConfig().setDefaultRenderDevice(0);
        accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
        accCfg.getVideoConfig().setAutoShowIncoming(true);
//        accCfg.getNatConfig().setSdpNatRewriteUse(pj_constants_.PJ_TRUE);
//        accCfg.getNatConfig().setViaRewriteUse(pj_constants_.PJ_TRUE);
        accCfg.getNatConfig().setIceEnabled(false);
        accCfg.getNatConfig().setTurnEnabled(false);
        accCfg.getNatConfig().setSipStunUse(pj_constants_.PJ_FALSE);
        accCfg.getNatConfig().setMediaStunUse(pj_constants_.PJ_FALSE);
        accCfg.getMediaConfig().setSrtpUse(pjmedia_srtp_use.PJMEDIA_SRTP_OPTIONAL);
        accCfg.getMediaConfig().setSrtpSecureSignaling(0);
        accCfg.getMediaConfig().setRtcpMuxEnabled(true);
        accCfg.getMediaConfig().setStreamKaEnabled(true);
        accCfg.getCallConfig().setTimerSessExpiresSec(300);
        accCfg.getCallConfig().setTimerMinSESec(90);

        manager.getSettingSip().configureCamera();
        int capDev = accCfg.getVideoConfig().getDefaultCaptureDevice();
        manager.getSettingSip().configureVidDev(capDev);

        try {
            create(accCfg);
            isCreated = true;
        } catch (Exception e) {
            isCreated = false;
            manager.onAccountSipStatus(null, Objects.requireNonNull(e.getMessage()));
        }
    }

    public boolean isRegistered() {
        return this.isRegistered;
    }

    public void logout() {
        try {
            setRegistration(false);
            delete();
            isRegistered = false;
        } catch (Exception ignored) {
        }
    }

    public void checkAccountInfo() {
        try {
            AccountInfo info = getInfo();
            int code = info.getRegStatus();
            String text = info.getRegStatusText();
            switch (code) {
                case pjsip_status_code.PJSIP_SC_OK:
                    isRegistered = true;
                    manager.onAccountSipStatus(info, "Registered successfully");
                    break;
                case pjsip_status_code.PJSIP_SC_UNAUTHORIZED:
                    isRegistered = false;
                    manager.onAccountSipStatus(null, "Unauthorized (401) - Check username/password");
                    break;
                default:
                    isRegistered = false;
                    manager.onAccountSipStatus(null, "Registration failed: " + code + " " + text);
                    break;
            }
        } catch (Exception ignored) {
            isRegistered = false;
            manager.onAccountSipStatus(null, "Registration failed");
        }
    }

    @Override
    public void onRegStarted(OnRegStartedParam prm) {
        super.onRegStarted(prm);
        checkAccountInfo();
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        super.onRegState(prm);
        checkAccountInfo();
    }

    public SipCall initCall() {
        if (call != null) call.delete();
        call = new SipCall(context, manager);
        manager.onCall(call, "initialize");
        return call;
    }

    public SipCall getCall() {
        return call;
    }

    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        super.onIncomingCall(prm);
        if (call != null) call.delete();
        call = new SipCall(context, manager, prm.getCallId());
        call.sendRinging();
        try {
            String stat_video = (call.getInfo().getRemOfferer() && call.getInfo().getRemVideoCount() > 0) ? "video" : "audio";
            manager.onCall(call, "call_incoming_" + stat_video);
        } catch (Exception ignored) {
            manager.onCall(call, "call_incoming_voice");
        }
    }
}
