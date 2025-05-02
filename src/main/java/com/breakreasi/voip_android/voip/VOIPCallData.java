package com.breakreasi.voip_android.voip;

public class VOIPCallData {
    String displayName;
    String phone;
    boolean isVideo;

    public VOIPCallData(String displayName, String phone, boolean isVideo) {
        this.displayName = displayName;
        this.phone = phone;
        this.isVideo = isVideo;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isVideo() {
        return isVideo;
    }
}
