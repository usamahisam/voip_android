package com.breakreasi.voip_android.sip;

import android.view.SurfaceView;
import android.view.ViewGroup;

import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.VideoWindowInfo;

public class SurfaceUtil {
    public static void resizeSurface(SurfaceView surfaceView, VideoWindow videoWindow, boolean forceWidth) {
        try {
            VideoWindowInfo vwi = videoWindow.getInfo();
            int videoWidth = (int) vwi.getSize().getW();
            int videoHeight = (int) vwi.getSize().getW();
            resizeSurface(surfaceView, videoWidth, videoHeight, forceWidth);
        } catch (Exception ignored) {
        }
    }

    public static void resizeSurface(SurfaceView surfaceView, float videoWidth, float videoHeight, boolean forceWidth) {
        float videoRatio = (float) videoWidth / (float) videoHeight;
        surfaceView.post(() -> {
            ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
//            float screenRatio = (float) lp.width / (float) lp.height;
            if (forceWidth) {
                lp.width = (int) (lp.height * videoRatio);
            } else {
                lp.height = (int) (lp.width * videoRatio);
            }
            surfaceView.setLayoutParams(lp);
            surfaceView.invalidate();
            surfaceView.requestLayout();
        });
    }

    public static void surfaceToTop(SurfaceView surfaceView) {
        surfaceView.post(() -> {
            surfaceView.setZOrderOnTop(true);
            surfaceView.setZOrderMediaOverlay(true);
            surfaceView.bringToFront();
            surfaceView.invalidate();
            surfaceView.requestLayout();
        });
    }

    public static void surfaceToBottom(SurfaceView surfaceView) {
        surfaceView.post(() -> {
            surfaceView.setZOrderOnTop(false);
            surfaceView.setZOrderMediaOverlay(false);
            surfaceView.invalidate();
            surfaceView.requestLayout();
        });
    }
}
