package com.breakreasi.voip_android.sip;

import android.media.AudioManager;

import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.CallVidSetStreamParam;
import org.pjsip.pjsua2.CodecFmtpVector;
import org.pjsip.pjsua2.CodecInfo;
import org.pjsip.pjsua2.CodecInfoVector2;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.MediaFormatVideo;
import org.pjsip.pjsua2.VidCodecParam;
import org.pjsip.pjsua2.VidDevManager;
import org.pjsip.pjsua2.VideoDevInfo;
import org.pjsip.pjsua2.VideoDevInfoVector2;
import org.pjsip.pjsua2.pjmedia_aud_dev_route;
import org.pjsip.pjsua2.pjmedia_orient;
import org.pjsip.pjsua2.pjsua_call_vid_strm_op;

public class SipSetting {
    private SipManager sipManager;
    private Endpoint endpoint;
    private AudioMedia audioMedia;
    private int frontCamera = 1;
    private int backCamera = 1;
    private SipCamera sipCamera;

    public SipSetting(SipManager sipManager) {
        this.sipManager = sipManager;
        endpoint = sipManager.getEndpoint();
        init();
    }

    private void init() {
        configureAudio();
        configureCodecs();
        configureVideoCodecs();
    }

    public void setAudioCommunication() {
        sipManager.getAudioManager().setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    public void setAudioNormal() {
        sipManager.getAudioManager().setMode(AudioManager.MODE_NORMAL);
    }

    public void turnOnSpeakerphone() {
        sipManager.getAudioManager().setSpeakerphoneOn(true);
    }

    public void turnOffSpeakerphone() {
        sipManager.getAudioManager().setSpeakerphoneOn(false);
    }

    public void configureAudio(AudioMedia am) {
        audioMedia = am;
        try {
            endpoint.audDevManager().getCaptureDevMedia().startTransmit(audioMedia);
            endpoint.audDevManager().getCaptureDevMedia().adjustTxLevel(2.0f);
            endpoint.audDevManager().getCaptureDevMedia().adjustRxLevel(2.0f);
            audioMedia.startTransmit(endpoint.audDevManager().getPlaybackDevMedia());
            audioMedia.adjustRxLevel(2.0f);
            audioMedia.adjustTxLevel(2.0f);
        } catch (Exception ignored) {
        }
    }

    public void stopAudioTransmit() {
        if (audioMedia == null) {
            return;
        }
        try {
            endpoint.audDevManager().getCaptureDevMedia().stopTransmit(audioMedia);
            audioMedia.stopTransmit(endpoint.audDevManager().getPlaybackDevMedia());
            audioMedia = null;
        } catch (Exception ignored) {
        }
    }

    public void muteMic() {
        try {
            if (audioMedia != null) {
                audioMedia.adjustTxLevel(0.0f);
            }
            endpoint.audDevManager().getCaptureDevMedia().adjustTxLevel(0.0f);
            endpoint.audDevManager().getCaptureDevMedia().stopTransmit(audioMedia);
        } catch (Exception ignored) {
        }
    }

    public void unmuteMic() {
        try {
            if (audioMedia != null) {
                audioMedia.adjustTxLevel(2.0f);
            }
            endpoint.audDevManager().getCaptureDevMedia().adjustTxLevel(2.0f);
            endpoint.audDevManager().getCaptureDevMedia().startTransmit(audioMedia);
        } catch (Exception ignored) {
        }
    }

    public void muteMicOther() {
        try {
            if (audioMedia != null) {
                audioMedia.adjustRxLevel(0.0f);
            }
            endpoint.audDevManager().getCaptureDevMedia().adjustRxLevel(0.0f);
        } catch (Exception ignored) {
        }
    }

    public void unmuteMicOther() {
        try {
            if (audioMedia != null) {
                audioMedia.adjustRxLevel(2.0f);
            }
            endpoint.audDevManager().getCaptureDevMedia().adjustRxLevel(2.0f);
        } catch (Exception ignored) {
        }
    }

    public void configureVidDev(int capDev) {
        try {
            endpoint.vidDevManager().setCaptureOrient(capDev, pjmedia_orient.PJMEDIA_ORIENT_ROTATE_270DEG, true);
        } catch (Exception ignored) {
        }
    }

    public void configureCamera() {
        VidDevManager vidMgr = endpoint.vidDevManager();
        try {
            VideoDevInfoVector2 devices = vidMgr.enumDev2();
            for (VideoDevInfo dev : devices) {
                if (dev.getName().contains("camera")) {
                    if (dev.getName().toLowerCase().contains("front")) {
                        frontCamera = dev.getId();
                    } else if (dev.getName().toLowerCase().contains("back")) {
                        backCamera = dev.getId();
                    }
                }
            }
            switchCamera(SipCamera.FRONT);
        } catch (Exception ignored) {
        }
    }

    public SipCamera getSipCamera() {
        return sipCamera;
    }

    public int getIdCamera(SipCamera sipCamera) {
        if (sipCamera == SipCamera.BACK) {
            return backCamera;
        }
        return frontCamera;
    }

    public void switchCamera(SipCamera sipCamera) {
        this.sipCamera = sipCamera;
        CallVidSetStreamParam param = new CallVidSetStreamParam();
        if (sipCamera == SipCamera.FRONT) {
            param.setCapDev(frontCamera);
        } else if (sipCamera == SipCamera.BACK) {
            param.setCapDev(backCamera);
        }
        try {
            sipManager.getCall().vidSetStream(pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_CHANGE_CAP_DEV, param);
        } catch (Exception ignored) {
        }
    }

    private void configureAudio() {
        AudDevManager audDevManager = endpoint.audDevManager();
        try {
            audDevManager.setOutputRoute(pjmedia_aud_dev_route.PJMEDIA_AUD_DEV_ROUTE_LOUDSPEAKER);
        } catch (Exception ignored) {
        }
    }

    private void configureCodecs() {
        try {
            CodecInfoVector2 codecs = endpoint.codecEnum2();
            for (CodecInfo codec : codecs) {
                String codecId = codec.getCodecId();
                if (codecId.startsWith("G729")) {
                    endpoint.codecSetPriority(codecId, (short) 254);
                } else if (codecId.startsWith("opus")) {
                    endpoint.codecSetPriority(codecId, (short) 240);
                } else if (codecId.startsWith("PCMA")) {
                    endpoint.codecSetPriority(codecId, (short) 235);
                } else if (codecId.startsWith("PCMU")) {
                    endpoint.codecSetPriority(codecId, (short) 230);
                } else {
                    endpoint.codecSetPriority(codecId, (short) 0);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void configureVideoCodecs() {
        try {
            CodecInfoVector2 codecs = endpoint.videoCodecEnum2();
            for (CodecInfo codec : codecs) {
                String codecId = codec.getCodecId();
                if (codecId.contains("H264/97")) {
                    endpoint.videoCodecSetPriority(codecId, (short) 128);
                    setVideoCodecParam(codecId, endpoint.getVideoCodecParam(codecId));
                } else if (codecId.contains("H264/99")) {
                    endpoint.videoCodecSetPriority(codecId, (short) 127);
                    setVideoCodecParam(codecId, endpoint.getVideoCodecParam(codecId));
//                } else if (codecId.contains("VP8")) {
//                    endpoint.videoCodecSetPriority(codecId, (short) 126);
//                    setVideoCodecParam(codecId, endpoint.getVideoCodecParam(codecId));
//                } else if (codecId.contains("VP9")) {
//                    endpoint.videoCodecSetPriority(codecId, (short) 125);
//                    setVideoCodecParam(codecId, endpoint.getVideoCodecParam(codecId));
                } else {
                    endpoint.videoCodecSetPriority(codecId, (short) 0);
                    setVideoCodecParam(codecId, endpoint.getVideoCodecParam(codecId));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void setVideoCodecParam(String codecId, VidCodecParam param) {
        /**
         getEncFmt() artinya format video untuk ENCODE (video yang kita kirim ke lawan bicara).
         getDecFmt() artinya format video untuk DECODE (video yang kita terima dari lawan bicara).
         */
        MediaFormatVideo mediaFormatVideo = param.getEncFmt();
        mediaFormatVideo.setWidth(sipManager.getConfig().getSIP_VIDEO_WIDTH());
        mediaFormatVideo.setHeight(sipManager.getConfig().getSIP_VIDEO_HEIGHT());
        mediaFormatVideo.setAvgBps(sipManager.getConfig().getSIP_VIDEO_BITRATE_AVG() * 1000);
        mediaFormatVideo.setMaxBps(sipManager.getConfig().getSIP_VIDEO_BITRATE_MAX() * 1000);
//        mediaFormatVideo.setFpsDenum(sipManager.getConfig().getSIP_VIDEO_FPS_DENUM());
//        mediaFormatVideo.setFpsNum(sipManager.getConfig().getSIP_VIDEO_FPS_NUM());
        param.setEncFmt(mediaFormatVideo);

        CodecFmtpVector codecFmtpVector = param.getDecFmtp();
        for (int i = 0; i < codecFmtpVector.size(); i++) {
            if ("profile-level-id".equals(codecFmtpVector.get(i).getName())) {
                // janus = 42e01f
                // local = 42e01e
                codecFmtpVector.get(i).setVal("42e01f");
                break;
            }
        }
        param.setDecFmtp(codecFmtpVector);

        try {
            endpoint.setVideoCodecParam(codecId, param);
        } catch (Exception ignored) {
        }
    }
}
