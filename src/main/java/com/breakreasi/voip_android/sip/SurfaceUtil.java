package com.breakreasi.voip_android.sip;

import android.view.SurfaceView;
import android.view.ViewGroup;

import org.pjsip.pjsua2.VideoWindow;
import org.pjsip.pjsua2.VideoWindowInfo;

public class SurfaceUtil {
    public static void resizeSurface(SurfaceView surfaceView, VideoWindow videoWindow) {
        try {
            VideoWindowInfo vwi = videoWindow.getInfo();
            int videoWidth = (int) vwi.getSize().getW();
            int videoHeight = (int) vwi.getSize().getW();
            resizeSurface(surfaceView, videoWidth, videoHeight);
        } catch (Exception ignored) {
        }
    }

    public static void resizeSurface(SurfaceView surfaceView, float videoWidth, float videoHeight) {
        float videoRatio = (float) videoWidth / (float) videoHeight;
        surfaceView.post(() -> {
            ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
//            float screenRatio = (float) lp.width / (float) lp.height;
            lp.width = (int) (lp.height * videoRatio);
            surfaceView.setLayoutParams(lp);
            surfaceView.invalidate();
            surfaceView.requestLayout();
        });
    }
}
