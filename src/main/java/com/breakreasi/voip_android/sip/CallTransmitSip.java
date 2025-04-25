package com.breakreasi.voip_android.sip;

import android.content.Context;
import android.view.SurfaceView;

import org.pjsip.pjsua2.VideoWindow;

public class CallTransmitSip {
    private Context mContext;
    private SurfaceView localVideoView, remoteVideoView;
    private VideoSurfaceHandler localVideoHandler, remoteVideoHandler;

    public CallTransmitSip(Context context) {
        this.mContext = context;
    }

    public void localViewRenderer(SurfaceView localVideoView) {
        this.localVideoView = localVideoView;
        this.localVideoHandler = new VideoSurfaceHandler(localVideoView.getHolder());
        this.localVideoView.getHolder().addCallback(this.localVideoHandler);
    }

    public void remoteViewRenderer(SurfaceView remoteVideoView) {
        this.remoteVideoView = remoteVideoView;
        this.remoteVideoHandler = new VideoSurfaceHandler(remoteVideoView.getHolder());
        this.remoteVideoView.getHolder().addCallback(this.remoteVideoHandler);
    }

    public void startLocalVideo(VideoWindow videoWindow) {
        localVideoHandler.setVideoWindow(videoWindow);
    }

    public void startRemoteVideo(VideoWindow videoWindow) {
        remoteVideoHandler.setVideoWindow(videoWindow);
    }
}
