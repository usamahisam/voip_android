package com.breakreasi.voip_android.sip;

import android.view.SurfaceView;

public class SipVideo implements SipIncomingVideo {
    private SipManager manager;
    private SurfaceView localVideo, remoteVideo;
    private SipVideoSurfaceHandler localVideoHandler, remoteVideoHandler;
    private int w, h;
    private boolean toggleRemoteSurfaceFix = false;

    public SipVideo(SipManager manager) {
        this.manager = manager;
    }

    public void setLocalVideo(SurfaceView localVideo) {
        this.localVideo = localVideo;
        this.localVideoHandler = new SipVideoSurfaceHandler(manager);
    }

    public void startLocalVideo() {
        if (localVideo == null) return;
        if (localVideoHandler == null) return;
        SipSurfaceUtil.surfaceToTop(localVideo);
        this.localVideo.getHolder().addCallback(this.localVideoHandler);
    }

    public void unsetLocalVideo() {
        this.localVideo = null;
        this.localVideoHandler = null;
    }

    public void setRemoteVideo(SurfaceView remoteVideo) {
        this.remoteVideo = remoteVideo;
        this.remoteVideoHandler = new SipVideoSurfaceHandler(manager, this);
        this.toggleRemoteSurfaceFix = false;
    }

    public void startRemoteVideo() {
        if (remoteVideo == null) return;
        if (remoteVideoHandler == null) return;
        SipSurfaceUtil.surfaceToBottom(localVideo);
        this.remoteVideo.getHolder().addCallback(this.remoteVideoHandler);
    }

    public void unsetRemoteVideo() {
        this.remoteVideo = null;
        this.remoteVideoHandler = null;
    }

    @Override
    public void getSize(int w, int h) {
//        this.w = w;
//        this.h = h;
        this.w = 480;
        this.h = 640;
        toggleSurfaceRemoteFit();
    }

    public void toggleSurfaceRemoteFit() {
        if (toggleRemoteSurfaceFix) {
            SipSurfaceUtil.resizeFixWidth(remoteVideo, w, h);
        } else {
            SipSurfaceUtil.resizeFixHeight(remoteVideo, w, h);
        }
        toggleRemoteSurfaceFix = !toggleRemoteSurfaceFix;
    }

    public void surfaceRemoteFitWidth() {
        if (remoteVideo == null) return;
        if (w == 0 || h == 0) return;
        SipSurfaceUtil.resizeFixWidth(remoteVideo, w, h);
    }

    public void surfaceRemoteFitHeight() {
        if (remoteVideo == null) return;
        if (w == 0 || h == 0) return;
        SipSurfaceUtil.resizeFixHeight(remoteVideo, w, h);
    }
}
