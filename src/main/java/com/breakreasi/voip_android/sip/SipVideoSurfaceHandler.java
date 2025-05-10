package com.breakreasi.voip_android.sip;

import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

public class SipVideoSurfaceHandler implements SurfaceHolder.Callback {
    private SipManager manager;
    private String surfaceStatus;
    private SipIncomingVideo callback;

    public SipVideoSurfaceHandler(SipManager manager) {
        this.manager = manager;
        this.callback = null;
        this.surfaceStatus = "local";
    }

    public SipVideoSurfaceHandler(SipManager manager, SipIncomingVideo callback) {
        this.manager = manager;
        this.callback = callback;
        this.surfaceStatus = "incoming";
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Surface surface = holder.getSurface();
        if (surfaceStatus.equals("incoming")) {
            manager.getCall().setIncomingVideoCallback(callback);
            manager.getCall().setIncomingVideoFeed(surface);
        } else if (surfaceStatus.equals("local")) {
            manager.getCall().startPreviewVideoFeed(surface);
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }
}
