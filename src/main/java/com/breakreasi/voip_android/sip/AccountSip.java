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
import org.pjsip.pjsua2.pjsip_status_code;

public class AccountSip extends Account {
    private Context context;
    private SipManager manager;
    private AccountConfig accCfg;
    private CallSip call;
    public String username;
    public String password;
    public boolean registration;
    public boolean isLogin;

    public AccountSip(Context mContext, SipManager mManager) {
        context = mContext;
        manager = mManager;
        isLogin = false;
        call = null;
        username = "";
        password = "";
        registration = false;
    }

    public void register(String username, String password, boolean registration) {
        this.username = username;
        this.password = password;
        this.registration = registration;

        AuthCredInfoVector credArray = new AuthCredInfoVector();
        AuthCredInfo cred = new AuthCredInfo("digest", "*", username, 0, password);
        credArray.add(cred);

        accCfg = new AccountConfig();
        accCfg.setIdUri("sip:" + username + "@" + manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT());
        accCfg.getSipConfig().setAuthCreds(credArray);
        accCfg.getSipConfig().getProxies().clear();
        accCfg.getSipConfig().getProxies().add("sip:"+manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT() + ";transport=UDP;port=" + manager.getConfig().getSIP_PORT());
        accCfg.getSipConfig().getProxies().add("sip:"+manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT() + ";transport=TCP;port=" + manager.getConfig().getSIP_PORT());
        accCfg.getRegConfig().setRegistrarUri("sip:" + manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT());
        accCfg.getRegConfig().setRegisterOnAdd(true);
        accCfg.getRegConfig().setDropCallsOnFail(true);
        accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
        accCfg.getVideoConfig().setAutoShowIncoming(true);

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

    private void accountInfo() {
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
            manager.onAccountSipStatus(null, e.getMessage());
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

    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        super.onIncomingCall(prm);
        if (call != null) call.delete();
        call = new CallSip(context, manager, prm.getCallId());
        manager.onCall(call, "call_incoming");
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
