package com.breakreasi.voip_android.sip;

import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.VideoWindowHandle;

public class VideoSurfaceHandler implements SurfaceHolder.Callback {

    private final SurfaceHolder holder;
    private VideoWindow videoWindow = null;
    private boolean active = false;

    public VideoSurfaceHandler(SurfaceHolder holder_) {
        holder = holder_;
    }

    public void setVideoWindow(VideoWindow vw) {
        videoWindow = vw;
        active = true;
        setSurfaceHolder(holder);
    }

    public void resetVideoWindow() {
        active = false;
        videoWindow = null;
    }

    private void setSurfaceHolder(SurfaceHolder holder) {
//        if (!active) return;
//        if (videoWindow == null) return;
//        if (holder == null) return;
        try {
            holder.setKeepScreenOn(true);
            VideoWindowHandle wh = new VideoWindowHandle();
            wh.getHandle().setWindow(holder.getSurface());
            videoWindow.setWindow(wh);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int w, int h)
    {
        setSurfaceHolder(holder);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) { }


    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder)
    {
        setSurfaceHolder(null);
    }
}
