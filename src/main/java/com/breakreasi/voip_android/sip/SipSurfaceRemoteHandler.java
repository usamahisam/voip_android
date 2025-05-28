package com.breakreasi.voip_android.sip;

import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

public class SipSurfaceRemoteHandler implements SurfaceHolder.Callback {
    private SipManager manager;
    private SipIncomingVideo callback;

    public SipSurfaceRemoteHandler(SipManager manager, SipIncomingVideo callback) {
        this.manager = manager;
        this.callback = callback;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Surface surface = holder.getSurface();
        manager.getCall().setIncomingVideoCallback(callback);
        manager.getCall().setIncomingVideoFeed(surface);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }
}
