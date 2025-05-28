package com.breakreasi.voip_android.sip;

import android.content.Context;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;

import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.Call;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallMediaInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.CallVidSetStreamParam;
import org.pjsip.pjsua2.Media;
import org.pjsip.pjsua2.MediaFmtChangedEvent;
import org.pjsip.pjsua2.OnCallMediaEventParam;
import org.pjsip.pjsua2.OnCallMediaStateParam;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.VideoPreview;
import org.pjsip.pjsua2.VideoPreviewOpParam;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.VideoWindowHandle;
import org.pjsip.pjsua2.pjmedia_dir;
import org.pjsip.pjsua2.pjmedia_event_type;
import org.pjsip.pjsua2.pjmedia_rtcp_fb_type;
import org.pjsip.pjsua2.pjmedia_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsua2;
import org.pjsip.pjsua2.pjsua_call_flag;
import org.pjsip.pjsua2.pjsua_call_media_status;
import org.pjsip.pjsua2.pjsua_call_vid_strm_op;
import org.pjsip.pjsua2.pjsua_vid_req_keyframe_method;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SipCall extends Call {
    private Context context;
    private SipManager manager;
    private int CALL_STATUS;
    private VideoWindow remoteVideo;
    private VideoPreview localVideo;
    private SipIncomingVideo incomingVideoCallback;

    public SipCall(Context cContext, SipManager mManager) {
        super(mManager.getAccountSip());
        context = cContext;
        manager = mManager;
    }

    public SipCall(Context cContext, SipManager mManager, int callId) {
        super(mManager.getAccountSip(), callId);
        context = cContext;
        manager = mManager;
    }

    @Override
    public void onCallState(OnCallStateParam prm) {
        listeningCallInfo();
    }

    @Override
    public void onCallMediaEvent(OnCallMediaEventParam prm) {
        int evType = prm.getEv().getType();
        switch (evType) {
            case pjmedia_event_type.PJMEDIA_EVENT_FMT_CHANGED:
                try {
                    CallInfo callInfo = getInfo();
                    CallMediaInfo mediaInfo = callInfo.getMedia().get((int)prm.getMedIdx());
                    if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                            mediaInfo.getDir() == pjmedia_dir.PJMEDIA_DIR_DECODING) {
                        MediaFmtChangedEvent fmtEvent = prm.getEv().getData().getFmtChanged();
                        int w = (int) fmtEvent.getNewWidth();
                        int h = (int) fmtEvent.getNewHeight();
                        if (incomingVideoCallback != null) {
                            incomingVideoCallback.getSize(w, h);
                        }
                    }
                } catch (Exception ignored) {
                }
                break;
            case pjmedia_event_type.PJMEDIA_EVENT_RX_RTCP_FB:
                if (prm.getEv().getData() != null &&
                        prm.getEv().getData().getRtcpFb().getFbType() == pjmedia_rtcp_fb_type.PJMEDIA_RTCP_FB_NACK &&
                        prm.getEv().getData().getRtcpFb().getIsParamLengthZero()
                ) {
                    sendKeyFrame();
                }
                break;
        }
        listeningCallInfo();
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        listeningCallInfo();
        try {
            CallInfo info = getInfo();
            String statVideo = "off";
            for (int i = 0; i < info.getMedia().size(); i++) {
                Media media = getMedia(i);
                CallMediaInfo mediaInfo = info.getMedia().get(i);
                if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO
                        && media != null
                        && mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                    mediaAudio(media);
                } else if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_VIDEO
                        && mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE
                        && mediaInfo.getVideoIncomingWindowId() != pjsua2.INVALID_ID) {
                    mediaVideo(mediaInfo);
                    statVideo = "on";
                }
            }
            onCall(this, "call_media_video_" + statVideo);
        } catch (Exception ignored) {
        }
    }

    private void listeningCallInfo() {
        try {
            CallInfo callInfo = getCallInfo();
            listeningCallStatus(callInfo);
        } catch (Exception ignored) {
            onCall(this, "call_error");
        }
    }

    private void listeningCallStatus(CallInfo callInfo) {
        if (CALL_STATUS == callInfo.getState()) {
            return;
        }
        CALL_STATUS = callInfo.getState();
        if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_CALLING) {
            onCall(this, "call_calling");
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
            onCall(this, "call_ringing");
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_CONNECTING) {
            onCall(this, "call_connecting");
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
            onCall(this, "call_connected");
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            onCall(this, "call_disconnected " + callInfo.getLastReason());
            stopVideoFeeds();
            delete();
        } else if (CALL_STATUS == pjsip_inv_state.PJSIP_INV_STATE_NULL) {
            onCall(this, "call_null");
        }
    }

    @Nullable
    public SipCallData getCallData() {
        try {
            String displayName = "";
            String phone = "";
            String remoteUri = getCallInfo().getRemoteUri();
            Pattern pattern = Pattern.compile("\"?(.*?)\"?\\s*<sip:([^@>]+)@");
            Matcher matcher = pattern.matcher(remoteUri);
            if (matcher.find()) {
                displayName = matcher.group(1);
                phone = matcher.group(2);
            }
            return new SipCallData(displayName, phone);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void mediaVideo(CallMediaInfo cmi) {
        if (remoteVideo != null) {
            remoteVideo.delete();
        }
        if (localVideo != null) {
            localVideo.delete();
        }
        localVideo = new VideoPreview(manager.getSettingSip().getIdCamera(SipCamera.FRONT));
        remoteVideo = new VideoWindow(cmi.getVideoIncomingWindowId());
    }

    private void stopVideoFeeds() {
        stopIncomingVideoFeed();
        stopPreviewVideoFeed();
    }

    public void setIncomingVideoCallback(SipIncomingVideo incomingVideoCallback) {
        this.incomingVideoCallback = incomingVideoCallback;
    }

    public void setIncomingVideoFeed(Surface surface) {
        if (remoteVideo != null) {
            VideoWindowHandle videoWindowHandle = new VideoWindowHandle();
            videoWindowHandle.getHandle().setWindow(surface);
            try {
                remoteVideo.setWindow(videoWindowHandle);
                int w = (int) remoteVideo.getInfo().getSize().getW();
                int h = (int) remoteVideo.getInfo().getSize().getH();
                if (incomingVideoCallback != null) {
                    incomingVideoCallback.getSize(w, h);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void startPreviewVideoFeed(Surface surface) {
        if (localVideo != null) {
            VideoWindowHandle videoWindowHandle = new VideoWindowHandle();
            videoWindowHandle.getHandle().setWindow(surface);
            VideoPreviewOpParam videoPreviewOpParam = new VideoPreviewOpParam();
            videoPreviewOpParam.setWindow(videoWindowHandle);
            try {
                localVideo.start(videoPreviewOpParam);
            } catch (Exception e) {
            }
        }
    }

    public void stopIncomingVideoFeed() {
        if (remoteVideo != null) {
            try {
                remoteVideo.delete();
            } catch (Exception ignored) {
            }
        }
    }

    public void stopPreviewVideoFeed() {
        if (localVideo != null) {
            try {
                localVideo.stop();
            } catch (Exception ignored) {
            }
        }
    }

    private void mediaAudio(Media media) {
        AudioMedia audioMedia = AudioMedia.typecastFromMedia(media);
        try {
            manager.getSettingSip().configureAudio(audioMedia);
            onCall(this, "call_media_audio");
        } catch (Exception ignored) {
        }
    }

    public void createCall(String destinationUser, boolean isVideo) {
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
            onCall(SipCall.this, "call_makecall");
        } catch (Exception e) {
            onCall(SipCall.this, "call_makecall_error");
        }
    }

    public void sendRinging() {
        try {
            CallOpParam callOpParam = new CallOpParam();
            callOpParam.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
            answer(callOpParam);
        } catch (Exception ignored) {
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

    private void onCall(SipCall call, String status) {
        if (manager == null) return;
        manager.onCall(call, status);
    }

    public CallInfo getCallInfo() {
        try {
            return getInfo();
        } catch (Exception e) {
            return null;
        }
    }

    private void sendKeyFrame() {
        try {
            vidSetStream(pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_SEND_KEYFRAME, new CallVidSetStreamParam());
        } catch (Exception ignored) {
        }
    }
}
