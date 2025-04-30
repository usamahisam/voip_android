package com.breakreasi.voip_android.sip;

import android.util.Log;
import android.view.SurfaceHolder;
import androidx.annotation.NonNull;
import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.VideoWindowHandle;

public class VideoSurfaceHandler implements SurfaceHolder.Callback {

    private final SurfaceHolder holder;
    private VideoWindow videoWindow = null;
    private boolean active = false;

    public VideoSurfaceHandler(SurfaceHolder holder_) {
        this.holder = holder_;
    }

    public void setVideoWindow(VideoWindow vw) {
        // If there is already a video window, reset the current one first
        if (videoWindow != null) {
            resetVideoWindow();
        }

        videoWindow = vw;
        active = true;
        setSurfaceHolder(holder);
    }

    public void resetVideoWindow() {
        active = false;
        if (videoWindow != null) {
            // Clean up and release the previous video window
            try {
                VideoWindowHandle wh = new VideoWindowHandle();
                wh.getHandle().setWindow(null);  // Detach the current surface
                videoWindow.setWindow(wh);       // Reset the video window
            } catch (Exception ignored) {
            }
            videoWindow = null;
        }
    }

    private void setSurfaceHolder(SurfaceHolder holder) {
        if (!active || videoWindow == null || holder == null) {
            return;
        }

        try {
            holder.setKeepScreenOn(true);
            VideoWindowHandle wh = new VideoWindowHandle();
            wh.getHandle().setWindow(holder.getSurface()); // Set the new surface to the window
            videoWindow.setWindow(wh);                      // Attach the window to the new surface
        } catch (Exception ignored) {
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int w, int h) {
        setSurfaceHolder(holder);  // Update surface when it changes
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // Nothing to do here, it's handled in surfaceChanged
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        // Detach the surface and release the video window
        setSurfaceHolder(null);
    }
}
