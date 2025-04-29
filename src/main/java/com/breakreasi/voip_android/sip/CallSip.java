package com.breakreasi.voip_android.sip;

import android.content.Context;

import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.MediaFormatVideo;
import org.pjsip.pjsua2.OnCallMediaEventParam;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoPreviewOpParam;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.pjmedia_dir;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsua2;
import org.pjsip.pjsua2.pjsua_call_media_status;

import java.util.ArrayList;
import java.util.List;

public class CallSip extends Call {
    private Context context;
    private SipManager manager;
    private int CALL_STATUS;
    private VideoWindow remoteVideo;
    private VideoPreview localVideo;

    public CallSip(Context cContext, SipManager mManager) {
        super(mManager.getAccount());
        context = cContext;
        manager = mManager;
    }

    public CallSip(Context cContext, SipManager mManager, int callId) {
        super(mManager.getAccount(), callId);
        context = cContext;
        manager = mManager;
    }

    @Override
    public void onCallState(OnCallStateParam prm) {
        listeningCallInfo();
    }

    @Override
    public void onCallMediaEvent(OnCallMediaEventParam prm) {
        listeningCallInfo();
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        listeningCallInfo();
    }

    private void listeningCallInfo() {
        try {
            CallInfo callInfo = getInfo();
            listeningCallStatus(callInfo);
            listeningCallMedia(callInfo);
        } catch (Exception ignored) {
            onCall(this, "call_error");
        }
    }

    private void listeningCallStatus(CallInfo callInfo) {
        if (CALL_STATUS == callInfo.getState()) {
            return;
        }
        String ci_set_call;
        if (callInfo.getSetting().getVideoCount() == 1L) {
            ci_set_call = "video";
        } else {
            ci_set_call = "voice";
        }
        CALL_STATUS = callInfo.getState();
        if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_CALLING) {
            onCall(this, "call_calling");
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
            onCall(this, "call_ringing");
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_CONNECTING) {
            onCall(this, "call_connecting");
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
            onCall(this, "call_connected_" + ci_set_call);
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            onCall(this, "call_disconnected " + callInfo.getLastReason());
            delete();
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_NULL) {
            onCall(this, "call_null");
        }
    }

    private void listeningCallMedia(CallInfo callInfo) {
        try {
            CallMediaInfoVector cmiv = callInfo.getMedia();
            for (int i = 0; i < cmiv.size(); i++) {
                CallMediaInfo cmi = cmiv.get(i);
                if (cmi.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                    if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_VIDEO) {
                        mediaVideo(cmi);
                    }
                    if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO) {
                        mediaAudio(cmi);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void mediaVideo(CallMediaInfo cmi) {
        if (remoteVideo == null && cmi.getVideoIncomingWindowId() != pjsua2.INVALID_ID) {
            remoteVideo = new VideoWindow(cmi.getVideoIncomingWindowId());
            onVideo(remoteVideo, "video_remote");
        }
        if (localVideo == null && (cmi.getDir() & pjmedia_dir.PJMEDIA_DIR_ENCODING) != 0) {
            localVideo = new VideoPreview(cmi.getVideoCapDev());
            try {
                localVideo.start(new VideoPreviewOpParam());
                onVideo(remoteVideo, "video_local");
            } catch (Exception ignored) {
            }
        }
    }

    private void mediaAudio(CallMediaInfo cmi) {

    }

    public void call(String destinationUser, boolean isVideo) {
        String sipUri = "sip:" + destinationUser + "@" + manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT();
        CallOpParam callOpParam = new CallOpParam();
        callOpParam.getOpt().setAudioCount(1);
        callOpParam.getOpt().setVideoCount(isVideo ? 1 : 0);
        try {
            makeCall(sipUri, callOpParam);
            onCall(CallSip.this, "call_makecall");
        } catch (Exception e) {
            onCall(CallSip.this, "call_makecall_error");
        }
    }

    public void accept() {
        CallOpParam callOpParam = new CallOpParam();
        callOpParam.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
        try {
            answer(callOpParam);
            onCall(this, "call_answer");
        } catch (Exception ignored) {
            onCall(this, "call_answer_error");
        }
    }

    public void decline() {
        CallOpParam callOpParam = new CallOpParam();
        callOpParam.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
        try {
            hangup(callOpParam);
            onCall(this, "call_decline");
        } catch (Exception ignored) {
            onCall(this, "call_decline_error");
        }
    }

    public void declineInCall() {
        CallOpParam callOpParam = new CallOpParam();
        callOpParam.setStatusCode(pjsip_status_code.PJSIP_SC_REQUEST_TERMINATED);
        try {
            hangup(callOpParam);
            onCall(this, "call_hangup");
        } catch (Exception ignored) {
            onCall(this, "call_hangup_error");
        }
    }

    private void onCall(CallSip call, String status) {
        if (manager == null) return;
        manager.onCall(call, status);
    }

    private void onVideo(VideoWindow localVideoWindow, String status) {
        if (manager == null) return;
        manager.onSipVideo(localVideoWindow, status);
    }

    public CallInfo getCallInfo() {
        try {
            return getInfo();
        } catch (Exception e) {
            return null;
        }
    }
}
