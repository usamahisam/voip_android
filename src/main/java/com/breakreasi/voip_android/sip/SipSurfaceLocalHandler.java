package com.breakreasi.voip_android.sip;

import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

public class SipSurfaceLocalHandler implements SurfaceHolder.Callback {
    private SipManager manager;

    public SipSurfaceLocalHandler(SipManager manager) {
        this.manager = manager;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        manager.getCall().startPreviewVideoFeed(holder.getSurface());
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }
}
