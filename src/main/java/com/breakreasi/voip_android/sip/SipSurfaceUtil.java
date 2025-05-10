package com.breakreasi.voip_android.sip;

import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class SipSurfaceUtil {

    public static void resizeFixWidth(SurfaceView surfaceView, int videoWidth, int videoHeight) {
        int fixedWidth = surfaceView.getContext().getResources().getDisplayMetrics().widthPixels;
        surfaceView.post(() -> {
            float aspectRatio = (float) videoHeight / videoWidth; // height per 1 width
            int adjustedHeight = (int) (fixedWidth * aspectRatio);
            ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
            lp.width = fixedWidth;
            lp.height = adjustedHeight;
            surfaceView.setLayoutParams(lp);
            surfaceView.invalidate();
            surfaceView.requestLayout();
        });
    }

    public static void resizeFixHeight(SurfaceView surfaceView, int videoWidth, int videoHeight) {
        int fixedHeight = surfaceView.getContext().getResources().getDisplayMetrics().heightPixels;
        surfaceView.post(() -> {
            float aspectRatio = (float) videoWidth / videoHeight; // width per 1 height
            int adjustedWidth = (int) (fixedHeight * aspectRatio);
            ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
            lp.width = adjustedWidth;
            lp.height = fixedHeight;
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
