package com.breakreasi.voip_android.sip;

import org.pjsip.pjsua2.AccountInfo;
import org.pjsip.pjsua2.VideoWindow;

public interface SipManagerCallback {
    void onSipAccountInfo(AccountInfo accountInfo, String status);
    void onSipCall(SipCall call, String status);
    void onSipVideo(VideoWindow videoWindow, String status);
}
