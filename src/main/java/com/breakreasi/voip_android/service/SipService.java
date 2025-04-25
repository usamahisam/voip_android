package com.breakreasi.voip_android.service;

import android.content.Context;
import android.hardware.camera2.CameraManager;

import androidx.annotation.NonNull;

import com.breakreasi.voip_android.sip.SipManager;

public class SipService {
    public Context mContext;
    private SipManager manager;

    public SipService(Context context, CameraManager cm) {
        this.mContext = context;
        this.manager = new SipManager(context, cm);
    }

    @NonNull
    public SipManager getManager() {
        return manager;
    }

    public boolean getLogin() {
        if (manager == null) {
            return false;
        }
        return manager.getLogin();
    }

    public void destroy() {
        manager.destroy();
    }
}
