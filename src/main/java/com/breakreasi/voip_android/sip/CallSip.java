package com.breakreasi.voip_android.sip;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallMediaInfoVector;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.MediaSize;
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
import org.pjsip.pjsua2.pjsua_call_flag;
import org.pjsip.pjsua2.pjsua_call_media_status;
import org.pjsip.pjsua2.pjsua_vid_req_keyframe_method;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        resetMedia();
    }

    public CallSip(Context cContext, SipManager mManager, int callId) {
        super(mManager.getAccount(), callId);
        context = cContext;
        manager = mManager;
        resetMedia();
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
            CallInfo callInfo = getCallInfo();
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
        String stat_video = (callInfo.getRemOfferer() && callInfo.getRemVideoCount() > 0) ? "video" : "audio";
        CALL_STATUS = callInfo.getState();
        if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_CALLING) {
            onCall(this, "call_calling");
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
            onCall(this, "call_ringing");
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_CONNECTING) {
            onCall(this, "call_connecting");
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
            onCall(this, "call_connected_" + stat_video);
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            onCall(this, "call_disconnected " + callInfo.getLastReason());
            resetMedia();
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
                        mediaAudio(i);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public SipCallData getCallData() {
        try {
            String displayName = "";
            String phone = "";
            String stat_video = (getCallInfo().getRemOfferer() && getCallInfo().getRemVideoCount() > 0) ? "video" : "audio";
            String remoteUri = getCallInfo().getRemoteUri();
            Pattern pattern = Pattern.compile("\"?(.*?)\"?\\s*<sip:([^@>]+)@");
            Matcher matcher = pattern.matcher(remoteUri);
            if (matcher.find()) {
                displayName = matcher.group(1);
                phone = matcher.group(2);
            }
            return new SipCallData(displayName, phone, stat_video.equals("video"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private void mediaVideo(CallMediaInfo cmi) {
        if (remoteVideo == null && cmi.getVideoIncomingWindowId() != pjsua2.INVALID_ID) {
            remoteVideo = new VideoWindow(cmi.getVideoIncomingWindowId());
            onVideo(remoteVideo, "video_remote");
        }
        if (localVideo == null && (cmi.getDir() & pjmedia_dir.PJMEDIA_DIR_ENCODING) != 0) {
            localVideo = new VideoPreview(manager.getSettingSip().getIdCamera(SipCamera.FRONT));
            try {
                localVideo.start(new VideoPreviewOpParam());
                onVideo(localVideo.getVideoWindow(), "video_local");
            } catch (Exception ignored) {
            }
        }
    }

    public void resetMediaVideo() {
        if (localVideo != null) {
            try {
                localVideo.stop();
                localVideo.delete();
            } catch (Exception ignored) {
            }
        }
        localVideo = null;
        remoteVideo = null;
    }

    private void mediaAudio(int medIdx) {
        try {
            AudioMedia am = getAudioMedia(medIdx);
            manager.getSettingSip().configureAudio(am);
        } catch (Exception ignored) {
        }
    }

    private void resetMediaAudio() {
        manager.getSettingSip().stopAudioTransmit();
    }

    private void resetMedia() {
        resetMediaAudio();
        resetMediaVideo();
    }

    public void createCall(String destinationUser, boolean isVideo) {
        resetMedia();
        String sipUri = "sip:" + destinationUser + "@" + manager.getConfig().getSIP_SERVER() + ":" + manager.getConfig().getSIP_PORT();
        CallOpParam callOpParam = new CallOpParam();
        callOpParam.getOpt().setAudioCount(1);
        callOpParam.getOpt().setVideoCount(isVideo ? 1 : 0);
        if (!isVideo) {
            callOpParam.getOpt().setFlag(pjsua_call_flag.PJSUA_CALL_INCLUDE_DISABLED_MEDIA);
        }
        callOpParam.getOpt().setReqKeyframeMethod(pjsua_vid_req_keyframe_method.PJSUA_VID_REQ_KEYFRAME_RTCP_PLI);
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
        callOpParam.getOpt().setAudioCount(1);
        try {
            boolean isVideo = (getCallInfo().getRemOfferer() && getCallInfo().getRemVideoCount() > 0);
            callOpParam.getOpt().setVideoCount(isVideo ? 1 : 0);
            if (!isVideo) {
                callOpParam.getOpt().setFlag(pjsua_call_flag.PJSUA_CALL_INCLUDE_DISABLED_MEDIA);
            }
        } catch (Exception ignored) {
        }
        callOpParam.getOpt().setReqKeyframeMethod(pjsua_vid_req_keyframe_method.PJSUA_VID_REQ_KEYFRAME_RTCP_PLI);
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

    private void onVideo(VideoWindow videoWindow, String status) {
        if (manager == null) return;
        manager.onSipVideo(videoWindow, status);
    }

    public CallInfo getCallInfo() {
        try {
            return getInfo();
        } catch (Exception e) {
            return null;
        }
    }
}
