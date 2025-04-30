package com.breakreasi.voip_android.sip;

import android.content.Context;
import android.util.Log;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AccountInfo;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnRegStartedParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.pj_constants_;
import org.pjsip.pjsua2.pj_qos_type;
import org.pjsip.pjsua2.pjmedia_srtp_use;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.Objects;

public class AccountSip extends Account {
    private Context context;
    private SipManager manager;
    private AccountConfig accCfg;
    private CallSip call;
    public String displayName;
    public String username;
    public String password;
    public boolean registration;
    public boolean isLogin;

    public AccountSip(Context mContext, SipManager mManager) {
        context = mContext;
        manager = mManager;
        isLogin = false;
        call = null;
        displayName = "";
        username = "";
        password = "";
        registration = false;
    }

    public void setAuth(String displayName, String username, String password) {
        this.displayName = displayName;
        this.username = username;
        this.password = password;
    }

    public void register(String displayName, String username, String password, boolean registration) {
        setAuth(displayName, username, password);
        this.registration = registration;

        AuthCredInfoVector credArray = new AuthCredInfoVector();
        AuthCredInfo cred = new AuthCredInfo("digest", "*", username, 0, password);
        credArray.add(cred);

        accCfg = new AccountConfig();
        accCfg.setIdUri("\"" + displayName + "\" <sip:" + username + "@" + manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT() + ">");
        accCfg.getSipConfig().setAuthCreds(credArray);
//        accCfg.getSipConfig().getProxies().clear();
//        accCfg.getSipConfig().getProxies().add("sip:"+manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT() + ";transport=UDP;port=" + manager.getConfig().getSIP_PORT());
//        accCfg.getSipConfig().getProxies().add("sip:"+manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT() + ";transport=TCP;port=" + manager.getConfig().getSIP_PORT());
        accCfg.getRegConfig().setRegistrarUri("sip:" + manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT());
        accCfg.getRegConfig().setRegisterOnAdd(true);
        accCfg.getRegConfig().setDropCallsOnFail(true);
        accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
        accCfg.getVideoConfig().setAutoShowIncoming(true);
        accCfg.getMediaConfig().getTransportConfig().setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
        accCfg.getMediaConfig().setSrtpUse(pjmedia_srtp_use.PJMEDIA_SRTP_OPTIONAL);
        accCfg.getMediaConfig().setSrtpSecureSignaling(0);
        accCfg.getCallConfig().setTimerSessExpiresSec(300);

        manager.getSettingSip().configureCamera();
        int capDev = accCfg.getVideoConfig().getDefaultCaptureDevice();
        manager.getSettingSip().configureVidDev(capDev);

        try {
            create(accCfg);
        } catch (Exception e) {
            manager.onAccountSipStatus(null, e.getMessage());
        }
    }

    public AccountConfig getAccountConfig() {
        return accCfg;
    }

    public void accountInfo() {
        try {
            AccountInfo accInfo = getInfo();
            if (accInfo.getRegStatus() == pjsip_status_code.PJSIP_SC_TRYING) {
                // trying wkwk
            } else if (accInfo.getRegStatus() == pjsip_status_code.PJSIP_SC_OK) {
                isLogin = true;
                accInfo.setOnlineStatus(true);
                manager.onAccountSipStatus(accInfo, "success");
            } else if (accInfo.getRegStatus() == pjsip_status_code.PJSIP_SC_UNAUTHORIZED) {
                if (this.registration) {
                    this.registration = false;
                    setRegistration(true);
                } else {
                    isLogin = false;
                    manager.onAccountSipStatus(null, accInfo.getRegStatus() + " " + accInfo.getRegStatusText());
                }
            }
        } catch (Exception e) {
            isLogin = false;
            manager.onAccountSipStatus(null, Objects.requireNonNull(e.getMessage()));
        }
    }

    public boolean getLogin() {
        return isLogin;
    }

    public CallSip initCall() {
        if (call != null) call.delete();
        call = new CallSip(context, manager);
        manager.onCall(call, "initialize");
        return call;
    }

    public CallSip getCall() {
        return call;
    }

    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        super.onIncomingCall(prm);
        if (call != null) call.delete();
        call = new CallSip(context, manager, prm.getCallId());
        try {
            String stat_video = (call.getInfo().getRemOfferer() && call.getInfo().getRemVideoCount() > 0) ? "video" : "audio";
            manager.onCall(call, "call_incoming_" + stat_video);
        } catch (Exception ignored) {
            manager.onCall(call, "call_incoming_voice");
        }
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        super.onRegState(prm);
        accountInfo();
    }

    @Override
    public void onRegStarted(OnRegStartedParam prm) {
        super.onRegStarted(prm);
        accountInfo();
    }
}
