package com.breakreasi.voip_android.sip;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;

import org.pjsip.pjsua2.MediaCoordinate;
import org.pjsip.pjsua2.MediaSize;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.VideoWindowInfo;

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
//        resizeSurface(videoWindow, localVideoView);
    }

    public void startRemoteVideo(VideoWindow videoWindow) {
        remoteVideoHandler.setVideoWindow(videoWindow);
        resizeSurface(videoWindow, remoteVideoView);
    }

    public void resizeSurface(VideoWindow videoWindow, SurfaceView surfaceView) {
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        try {
            VideoWindowInfo vwi = videoWindow.getInfo();
            int videoWidth = (int) vwi.getSize().getW();
            int videoHeight = (int) vwi.getSize().getW();
            surfaceView.post(() -> {
                ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
                float videoRatio = (float) videoWidth / (float) videoHeight;
                float screenRatio = (float) screenWidth / (float) screenHeight;
                lp.height = screenHeight;
                lp.width = (int) (screenHeight * videoRatio);
                Log.d("xcodex resizeSurface", ": " + lp.width + " " + lp.height);
                surfaceView.setLayoutParams(lp);
                surfaceView.invalidate();
                surfaceView.requestLayout();
            });
        } catch (Exception ignored) {

        }
    }
}
