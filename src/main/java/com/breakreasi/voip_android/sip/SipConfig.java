package com.breakreasi.voip_android.sip;

import org.pjsip.pjsua2.pjsip_transport_type_e;

public class SipConfig {
    private String SIP_SERVER = "voip.jasvicall.my.id";
    private  int SIP_PORT = 5160;
    private int SIP_TRANSPORT_UDP = pjsip_transport_type_e.PJSIP_TRANSPORT_UDP;
    private int SIP_TRANSPORT_TCP = pjsip_transport_type_e.PJSIP_TRANSPORT_TCP;
    private String SIP_ACCOUNT_USERNAME = "100";
    private String SIP_ACCOUNT_PASSWORD = "100";
    /**
     * Resolution (QCIF 176x144, CIF 320x240, VGA 640x480, XGA 1024x768, HD 1280x720, FHD 1920x1080)
     */
    private long SIP_VIDEO_WIDTH = 480;
    private long SIP_VIDEO_HEIGHT = 640;
    /**
     * Bitrate (128 kbps, 256 kbps, 512 kbps, 1024 kbps, 2048 kbps)
     */
    private long SIP_VIDEO_BITRATE_AVG = 256;
    private long SIP_VIDEO_BITRATE_MAX = 1024;
    /**
     * Frame rate (1, 5, 10, 15, 20, 25, 30)
     */
    private int SIP_VIDEO_FPS_DENUM = 10;
    private int SIP_VIDEO_FPS_NUM = 30;

    public String getSIP_SERVER() {
        return SIP_SERVER;
    }

    public void setSIP_SERVER(String SIP_SERVER) {
        this.SIP_SERVER = SIP_SERVER;
    }

    public int getSIP_PORT() {
        return SIP_PORT;
    }

    public void setSIP_PORT(int SIP_PORT) {
        this.SIP_PORT = SIP_PORT;
    }

    public int getSIP_TRANSPORT_UDP() {
        return SIP_TRANSPORT_UDP;
    }

    public void setSIP_TRANSPORT_UDP(int SIP_TRANSPORT_UDP) {
        this.SIP_TRANSPORT_UDP = SIP_TRANSPORT_UDP;
    }

    public int getSIP_TRANSPORT_TCP() {
        return SIP_TRANSPORT_TCP;
    }

    public void setSIP_TRANSPORT_TCP(int SIP_TRANSPORT_TCP) {
        this.SIP_TRANSPORT_TCP = SIP_TRANSPORT_TCP;
    }

    public String getSIP_ACCOUNT_USERNAME() {
        return SIP_ACCOUNT_USERNAME;
    }

    public void setSIP_ACCOUNT_USERNAME(String SIP_ACCOUNT_USERNAME) {
        this.SIP_ACCOUNT_USERNAME = SIP_ACCOUNT_USERNAME;
    }

    public String getSIP_ACCOUNT_PASSWORD() {
        return SIP_ACCOUNT_PASSWORD;
    }

    public void setSIP_ACCOUNT_PASSWORD(String SIP_ACCOUNT_PASSWORD) {
        this.SIP_ACCOUNT_PASSWORD = SIP_ACCOUNT_PASSWORD;
    }

    public long getSIP_VIDEO_WIDTH() {
        return SIP_VIDEO_WIDTH;
    }

    public void setSIP_VIDEO_WIDTH(long SIP_VIDEO_WIDTH) {
        this.SIP_VIDEO_WIDTH = SIP_VIDEO_WIDTH;
    }

    public long getSIP_VIDEO_HEIGHT() {
        return SIP_VIDEO_HEIGHT;
    }

    public void setSIP_VIDEO_HEIGHT(long SIP_VIDEO_HEIGHT) {
        this.SIP_VIDEO_HEIGHT = SIP_VIDEO_HEIGHT;
    }

    public long getSIP_VIDEO_BITRATE_AVG() {
        return SIP_VIDEO_BITRATE_AVG;
    }

    public void setSIP_VIDEO_BITRATE_AVG(long SIP_VIDEO_BITRATE_AVG) {
        this.SIP_VIDEO_BITRATE_AVG = SIP_VIDEO_BITRATE_AVG;
    }

    public long getSIP_VIDEO_BITRATE_MAX() {
        return SIP_VIDEO_BITRATE_MAX;
    }

    public void setSIP_VIDEO_BITRATE_MAX(long SIP_VIDEO_BITRATE_MAX) {
        this.SIP_VIDEO_BITRATE_MAX = SIP_VIDEO_BITRATE_MAX;
    }

    public int getSIP_VIDEO_FPS_DENUM() {
        return SIP_VIDEO_FPS_DENUM;
    }

    public void setSIP_VIDEO_FPS_DENUM(int SIP_VIDEO_FPS_DENUM) {
        this.SIP_VIDEO_FPS_DENUM = SIP_VIDEO_FPS_DENUM;
    }

    public int getSIP_VIDEO_FPS_NUM() {
        return SIP_VIDEO_FPS_NUM;
    }

    public void setSIP_VIDEO_FPS_NUM(int SIP_VIDEO_FPS_NUM) {
        this.SIP_VIDEO_FPS_NUM = SIP_VIDEO_FPS_NUM;
    }
}
