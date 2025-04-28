package com.breakreasi.voip_android;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.util.Log;
import android.view.SurfaceView;

import com.breakreasi.voip_android.sip.CallSip;
import com.breakreasi.voip_android.sip.SipManager;
import com.breakreasi.voip_android.sip.SipManagerCallback;
import com.breakreasi.voip_android.sip.SurfaceUtil;
import com.breakreasi.voip_android.sip.VideoSurfaceHandler;

import org.pjsip.pjsua2.AccountInfo;
import org.pjsip.pjsua2.VideoWindow;

import java.util.ArrayList;
import java.util.List;

public class VOIP implements SipManagerCallback {
    private Context context;
    private VOIPType type;
    private List<VOIPCallback> callbacks;
    private String username, password, to;
    private boolean isVideo;
    private String authSessionFor;
    private CameraManager cm;
    private AudioManager am;
    private SipManager sip;
    private CallSip sipCall;
    private SurfaceView localVideo, remoteVideo;
    private VideoSurfaceHandler localVideoHandler, remoteVideoHandler;

    public VOIP(Context context) {
        this.context = context;
        this.callbacks = new ArrayList<>();
    }

    public void setType(VOIPType type) {
        this.type = type;
    }

    public void init(VOIPType type) {
        setType(type);
        cm = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (this.type == VOIPType.SIP
        && sip == null) {
            sip = new SipManager(context, cm, am);
            sip.registerListener(this);
        }
    }

    public void auth(String username, String password) {
        auth(username, password, "");
    }

    public void auth(String username, String password, String authSessionFor) {
        this.username = username;
        this.password = password;
        this.authSessionFor = authSessionFor;
        if (this.type == VOIPType.SIP) {
            sip.register(username, password, true);
        }
    }

    public void makeCall(String to, boolean isVideo) {
        this.to = to;
        this.isVideo = isVideo;
        if (this.type == VOIPType.SIP) {
            auth(username, password, "makeCall");
        }
    }

    public void accept() {
        if (this.type == VOIPType.SIP) {
            sipCall.accept();
        }
    }

    public void hangup() {
        if (this.type == VOIPType.SIP) {
            sipCall.decline();
        }
    }

    public void setLocalVideo(SurfaceView localVideo) {
        if (this.type == VOIPType.SIP) {
            this.localVideo = localVideo;
            this.localVideoHandler = new VideoSurfaceHandler(localVideo.getHolder());
            this.localVideo.getHolder().addCallback(this.localVideoHandler);
        }
    }

    public void unsetLocalVideo() {
        if (this.type == VOIPType.SIP) {
            this.localVideo = null;
            this.localVideoHandler = null;
        }
    }

    public void setRemoteVideo(SurfaceView remoteVideo) {
        if (this.type == VOIPType.SIP) {
            this.remoteVideo = remoteVideo;
            this.remoteVideoHandler = new VideoSurfaceHandler(remoteVideo.getHolder());
            this.remoteVideo.getHolder().addCallback(this.remoteVideoHandler);
        }
    }

    public void unsetRemoteVideo() {
        if (this.type == VOIPType.SIP) {
            this.remoteVideo = null;
            this.remoteVideoHandler = null;
        }
    }

    public void setCallback(VOIPCallback callback) {
        if (callback != null && !callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public void unsetCallback(VOIPCallback callback) {
        callbacks.remove(callback);
    }

    public void notifyCallbacks(String status) {
        for (VOIPCallback callback : new ArrayList<>(callbacks)) { // pakai copy biar aman kalau ada unregister di dalam callback
            callback.onStatus(status);
        }
    }

    @Override
    public void onSipAccountInfo(AccountInfo accountInfo, String status) {
        if (status.equals("success")) {
            if (authSessionFor.equals("makeCall")) {
                sipCall = sip.initCall();
                sipCall.call(to, isVideo);
            }
        }
//        if (status.contains("Unauthorized")) {
//        }
    }

    @Override
    public void onSipCall(CallSip call, String status) {
        if (status.contains("incoming")) {
            sipCall = call;
        }
        notifyCallbacks(status);
    }

    @Override
    public void onSipVideo(VideoWindow videoWindow, String status) {
        if (status.equals("video_local")) {
            Log.d("VANVBSNVABS", "local");
            if (localVideoHandler != null) {
                localVideoHandler.setVideoWindow(videoWindow);
                SurfaceUtil.resizeSurface(localVideo, videoWindow);
            }
        } else if (status.equals("video_remote")) {
            Log.d("VANVBSNVABS", "remote");
            if (remoteVideoHandler != null) {
                remoteVideoHandler.setVideoWindow(videoWindow);
                SurfaceUtil.resizeSurface(localVideo, videoWindow);
            }
        }
    }
}