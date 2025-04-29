package com.breakreasi.voip_android.sip;

import android.view.SurfaceView;

public class SipVideo {
    private SipManager manager;
    private SurfaceView localVideo, remoteVideo;
    private VideoSurfaceHandler localVideoHandler, remoteVideoHandler;

    public SipVideo(SipManager manager) {
        this.manager = manager;
    }

    public void setLocalVideo(SurfaceView localVideo) {
        this.localVideo = localVideo;
        this.localVideoHandler = new VideoSurfaceHandler(localVideo.getHolder());
        this.localVideo.getHolder().addCallback(this.localVideoHandler);
    }

    public SurfaceView getLocalVideo() {
        return localVideo;
    }

    public VideoSurfaceHandler getLocalVideoHandler() {
        return localVideoHandler;
    }

    public void unsetLocalVideo() {
        this.localVideo = null;
        this.localVideoHandler = null;
    }

    public void setRemoteVideo(SurfaceView remoteVideo) {
        this.remoteVideo = remoteVideo;
        this.remoteVideoHandler = new VideoSurfaceHandler(remoteVideo.getHolder());
        this.remoteVideo.getHolder().addCallback(this.remoteVideoHandler);
    }

    public SurfaceView getRemoteVideo() {
        return remoteVideo;
    }

    public VideoSurfaceHandler getRemoteVideoHandler() {
        return remoteVideoHandler;
    }

    public void unsetRemoteVideo() {
        this.remoteVideo = null;
        this.remoteVideoHandler = null;
    }
}
