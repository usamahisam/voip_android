package com.breakreasi.voip_android.sip;

public class SipCallData {
    String displayName;
    String phone;

    public SipCallData(String displayName, String phone) {
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
