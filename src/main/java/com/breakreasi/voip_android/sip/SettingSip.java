package com.breakreasi.voip_android.sip;

import android.media.AudioManager;
import android.util.Log;

import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.AudioMedia;
import org.pjsip.pjsua2.CallVidSetStreamParam;
import org.pjsip.pjsua2.CodecFmtp;
import org.pjsip.pjsua2.CodecInfo;
import org.pjsip.pjsua2.CodecInfoVector2;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.VidCodecParam;
import org.pjsip.pjsua2.VidDevManager;
import org.pjsip.pjsua2.VideoDevInfo;
import org.pjsip.pjsua2.VideoDevInfoVector2;
import org.pjsip.pjsua2.pjmedia_aud_dev_route;
import org.pjsip.pjsua2.pjmedia_orient;
import org.pjsip.pjsua2.pjsua_call_vid_strm_op;

import java.util.Map;

public class SettingSip {
    private SipManager sipManager;
    private Endpoint endpoint;
    private AudioMedia audioMedia;
    private int frontCamera = 1;
    private int backCamera = 1;
    private SipCamera sipCamera;

    public SettingSip(SipManager sipManager) {
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
            audioMedia.startTransmit(endpoint.audDevManager().getPlaybackDevMedia());
            audioMedia.adjustRxLevel(2.0f);
            audioMedia.adjustTxLevel(2.0f);
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
                if (codecId.startsWith("G729")
                        || codecId.startsWith("PCMA")
                        || codecId.startsWith("PCMU")
                        || codecId.startsWith("opus")) {
                    endpoint.codecSetPriority(codecId, (short) 255);
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
                setVideoCodecParam(codecId, endpoint.getVideoCodecParam(codecId));
                if (codecId.contains("H264")) {
                    endpoint.videoCodecSetPriority(codecId, (short) 255);
                } else if (codecId.contains("VP8")) {
                    endpoint.videoCodecSetPriority(codecId, (short) 240);
                } else if (codecId.contains("VP9")) {
                    endpoint.videoCodecSetPriority(codecId, (short) 230);
                } else {
                    endpoint.videoCodecSetPriority(codecId, (short) 0);
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
//        CodecFmtp fmtp = new CodecFmtp();
//        fmtp.setName("packetization-mode");
//        fmtp.setVal("1");
//        param.getEncFmtp().set(0, fmtp);
//        CodecFmtp fmtp2 = new CodecFmtp();
//        fmtp2.setName("profile-level-id");
//        fmtp2.setVal("42e01f");
//        param.getEncFmtp().set(1, fmtp2);
        // Set resolusi
        param.getEncFmt().setWidth(sipManager.getConfig().getSIP_VIDEO_WIDTH());
////        param.getDecFmt().setWidth(sipManager.getConfig().getSIP_VIDEO_WIDTH());
        param.getEncFmt().setHeight(sipManager.getConfig().getSIP_VIDEO_HEIGHT());
////        param.getDecFmt().setHeight(sipManager.getConfig().getSIP_VIDEO_HEIGHT());
//        // Set bitrate
        param.getEncFmt().setAvgBps(sipManager.getConfig().getSIP_VIDEO_BITRATE_AVG() * 1024);
//        param.getDecFmt().setAvgBps(sipManager.getConfig().getSIP_VIDEO_BITRATE_AVG() * 1024);
        param.getEncFmt().setMaxBps(sipManager.getConfig().getSIP_VIDEO_BITRATE_MAX() * 1024);
//        param.getDecFmt().setMaxBps(sipManager.getConfig().getSIP_VIDEO_BITRATE_MAX() * 1024);
//        // Set frame rate
        param.getEncFmt().setFpsDenum(sipManager.getConfig().getSIP_VIDEO_FPS_DENUM());
        param.getEncFmt().setFpsNum(sipManager.getConfig().getSIP_VIDEO_FPS_NUM());
        try {
            endpoint.setVideoCodecParam(codecId, param);
        } catch (Exception ignored) {
        }
    }
}
