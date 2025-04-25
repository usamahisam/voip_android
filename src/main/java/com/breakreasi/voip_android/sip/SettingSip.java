package com.breakreasi.voip_android.sip;

import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.CodecInfo;
import org.pjsip.pjsua2.CodecInfoVector2;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.VidCodecParam;
import org.pjsip.pjsua2.pjmedia_aud_dev_route;

public class SettingSip {
    private SipManager sipManager;
    private Endpoint endpoint;

    public SettingSip(SipManager sipManager) {
        this.sipManager = sipManager;
        endpoint = sipManager.getEndpoint();
        init();
    }

    private void init() {
        configureDev();
        configureAudio();
        configureCodecs();
        configureVideoCodecs();
    }

    private void configureDev() {
//        int capDev = sipManager.getAccountSip().getAccountConfig().getVideoConfig().getDefaultCaptureDevice();
//        VidDevManager vidDevMgr = endpoint.vidDevManager();
//        try {
//            vidDevMgr.setCaptureOrient(capDev, 90);
//        } catch (Exception ignored) {
//        }
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
        // Set bitrate
        long AVG_BITRATE = sipManager.getConfig().getSIP_VIDEO_BITRATE() / 2;
        param.getEncFmt().setAvgBps(AVG_BITRATE * 1000);
        param.getEncFmt().setMaxBps(sipManager.getConfig().getSIP_VIDEO_BITRATE() * 1000);
        // Set frame rate
        param.getEncFmt().setFpsDenum(sipManager.getConfig().getSIP_VIDEO_FPS() - 5);
        param.getEncFmt().setFpsNum(sipManager.getConfig().getSIP_VIDEO_FPS());
        try {
            endpoint.setVideoCodecParam(codecId, param);
        } catch (Exception ignored) {
        }
    }
}
