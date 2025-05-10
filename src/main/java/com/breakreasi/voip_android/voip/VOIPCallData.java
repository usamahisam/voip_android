package com.breakreasi.voip_android.voip;

public class VOIPCallData {
    String displayName;
    String phone;

    public VOIPCallData(String displayName, String phone) {
        this.displayName = displayName;
        this.phone = phone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPhone() {
        return phone;
    }
}
