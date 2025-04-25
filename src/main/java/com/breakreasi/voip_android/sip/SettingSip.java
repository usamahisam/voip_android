package com.breakreasi.voip_android.sip;

import android.util.Log;

import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.CodecInfo;
import org.pjsip.pjsua2.CodecInfoVector2;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.MediaFormatVideo;
import org.pjsip.pjsua2.VidCodecParam;
import org.pjsip.pjsua2.VidDevManager;
import org.pjsip.pjsua2.VideoDevInfo;
import org.pjsip.pjsua2.VideoDevInfoVector;
import org.pjsip.pjsua2.VideoDevInfoVector2;
import org.pjsip.pjsua2.pjmedia_aud_dev_route;
import org.pjsip.pjsua2.pjmedia_orient;

public class SettingSip {
    private SipManager sipManager;
    private Endpoint endpoint;

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

    public void configureVidDev(int capDev) {
        VidDevManager vidMgr = endpoint.vidDevManager();
        try {
            vidMgr.setCaptureOrient(capDev, pjmedia_orient.PJMEDIA_ORIENT_ROTATE_270DEG);
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
                if (codecId.startsWith("H264")
                        || codecId.startsWith("VP8")
                        || codecId.startsWith("VP9")) {
                    endpoint.videoCodecSetPriority(codecId, (short) 255);
                    setVideoCodecParam(codecId, endpoint.getVideoCodecParam(codecId));
                } else {
                    endpoint.videoCodecSetPriority(codecId, (short) 0);
                    setVideoCodecParam(codecId, endpoint.getVideoCodecParam(codecId));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void setVideoCodecParam(String codecId, VidCodecParam param) {
        // Set resolusi
        param.getEncFmt().setWidth(sipManager.getConfig().getSIP_VIDEO_WIDTH());
        param.getEncFmt().setHeight(sipManager.getConfig().getSIP_VIDEO_HEIGHT());
        param.getDecFmt().setWidth(sipManager.getConfig().getSIP_VIDEO_WIDTH());
        param.getDecFmt().setHeight(sipManager.getConfig().getSIP_VIDEO_HEIGHT());
//        // Set bitrate
//        param.getEncFmt().setAvgBps(sipManager.getConfig().getSIP_VIDEO_BITRATE() * 1000);
//        param.getEncFmt().setMaxBps(sipManager.getConfig().getSIP_VIDEO_BITRATE() * 1000);
//        // Set frame rate
//        param.getEncFmt().setFpsDenum(sipManager.getConfig().getSIP_VIDEO_FPS());
//        param.getEncFmt().setFpsNum(sipManager.getConfig().getSIP_VIDEO_FPS());
        try {
            endpoint.setVideoCodecParam(codecId, param);
        } catch (Exception ignored) {
        }
    }
}
