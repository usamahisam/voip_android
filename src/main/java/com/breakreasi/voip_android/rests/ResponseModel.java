package com.breakreasi.voip_android.rests;


import androidx.annotation.Nullable;

/** @noinspection ALL */
public class ResponseModel {
    final int status;
    final String msg;

    public ResponseModel(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    /** @noinspection unused*/
    public int getStatus() {
        return status;
    }

    /** @noinspection unused*/
    public String getMsg() {
        return msg;
    }
}
