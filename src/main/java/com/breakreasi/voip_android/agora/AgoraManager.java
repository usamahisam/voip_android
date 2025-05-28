package com.breakreasi.voip_android.agora;

import android.content.Context;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.breakreasi.voip_android.rests.ResponseInitConfigModel;
import com.breakreasi.voip_android.rests.ResponseModel;
import com.breakreasi.voip_android.rests.Rests;
import com.breakreasi.voip_android.sip.SipSurfaceUtil;

import java.util.Random;

import io.agora.rtc2.Constants;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgoraManager {

    private Context context;
    private RtcEngine rtcEngine;
    private boolean isVideo;
    private String channel;
    private int uid;
    private String agoraId;
    private String userToken;
    private SurfaceView localVideo, remoteVideo;
    private final AgoraEngineEventListener eventListener;
    private final Rests rests;

    public AgoraManager(Context context) {
        this.context = context;
        eventListener = new AgoraEngineEventListener();
        rests = new Rests(context);
    }

    public static long generateRandom(int length) {
        Random random = new Random();
        char[] digits = new char[length];
        digits[0] = (char) (random.nextInt(9) + '1');
        for (int i = 1; i < length; i++) {
            digits[i] = (char) (random.nextInt(10) + '0');
        }
        return Long.parseLong(new String(digits));
    }

    public void auth(String channel, String userToken, boolean isVideo) {
        this.channel = channel;
        this.uid = (int) generateRandom(8);
        this.userToken = userToken;
        if (agoraId != null) {
            eventListener.onAgoraStatus("success");
            startEngine(isVideo);
            return;
        }
        rests.initConfig(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseInitConfigModel> call, @NonNull Response<ResponseInitConfigModel> response) {
                if (response.code() == 200 || response.code() == 201) {
                    ResponseInitConfigModel body = response.body();
                    if (body != null) {
                        agoraId = body.getAgora_app_id();
                        eventListener.onAgoraStatus("success");
                        startEngine(isVideo);
                        return;
                    }
                }
                logout();
                eventListener.onAgoraStatus("failed");
            }
            @Override
            public void onFailure(@NonNull Call<ResponseInitConfigModel> call, @NonNull Throwable t) {
                logout();
                eventListener.onAgoraStatus("failed");
            }
        });
    }

    private String getAgoraId() {
        return agoraId;
    }

    private void logout() {
        agoraId = null;
    }

    public void startEngine(boolean isV) {
        isVideo = isV;
        if (rtcEngine != null) {
            destroyEngine();
        }
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = context;
            config.mAppId = getAgoraId();
            config.mEventHandler = eventListener;
            config.mAutoRegisterAgoraExtensions = false;
            rtcEngine = RtcEngine.create(config);
            rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION_1v1);
            rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            if (isVideo) {
                rtcEngine.enableVideo();
                rtcEngine.setDefaultAudioRoutetoSpeakerphone(true);
                rtcEngine.setEnableSpeakerphone(true);
            } else {
                rtcEngine.enableAudio();
                rtcEngine.disableVideo();
                rtcEngine.setDefaultAudioRoutetoSpeakerphone(false);
                rtcEngine.setEnableSpeakerphone(false);
            }
        } catch (Exception ignored) {
        }
    }

    public RtcEngine rtcEngine() {
        return rtcEngine;
    }

    public void registerEventListener(AgoraIEventListener listener) {
        eventListener.registerEventListener(listener);
    }

    public void removeEventListener(AgoraIEventListener listener) {
        eventListener.removeEventListener(listener);
    }

    public String getDisplayName() {
        return channel;
    }

    public void joinChannel() {
        if (rtcEngine == null) return;
        rtcEngine.joinChannel(null, channel, "", uid);
    }

    public void setVideoConfiguration() {
        if (rtcEngine == null) return;
        rtcEngine.setVideoEncoderConfiguration(
                new VideoEncoderConfiguration(
                        VideoEncoderConfiguration.VD_480x360,
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                        VideoEncoderConfiguration.COMPATIBLE_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE)
        );
    }

    public boolean getIsVideo() {
        return isVideo;
    }

    public void setRemoteVideo(SurfaceView surfaceView) {
        remoteVideo = surfaceView;
    }

    public void setLocalVideo(SurfaceView surfaceView) {
        localVideo = surfaceView;
    }

    public void startLocalVideo() {
        if (rtcEngine == null) return;
        rtcEngine.setupLocalVideo(new VideoCanvas(localVideo, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    public void startRemoteVideo(int rUid) {
        if (localVideo != null) {
            SipSurfaceUtil.surfaceToTop(localVideo);
        }
        if (rtcEngine == null) return;
        rtcEngine.setupRemoteVideo(new VideoCanvas(remoteVideo, VideoCanvas.RENDER_MODE_HIDDEN, rUid));
    }

    public void startPreview() {
        if (rtcEngine == null) return;
        rtcEngine.startPreview();
    }

    public void unsetLocalVideo() {
        if (rtcEngine == null) return;
        rtcEngine.setupLocalVideo(null);
    }

    public void unsetRemoteVideo() {
        if (rtcEngine == null) return;
        rtcEngine.setupRemoteVideo(null);
    }

    public void accept() {
        if (rtcEngine == null) return;
        rests.acceptCall(userToken, channel, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel> call, @NonNull Response<ResponseModel> response) {
                if (response.code() == 200 || response.code() == 201) {
                    setVideoConfiguration();
                    startPreview();
                    joinChannel();
                }
            }
            @Override
            public void onFailure(@NonNull Call<ResponseModel> call, @NonNull Throwable t) {
            }
        });
    }

    public void hangup() {
        if (rtcEngine != null) {
            rtcEngine.stopPreview();
            rtcEngine.leaveChannel();
        }
        rests.rejectCall(userToken, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel> call, @NonNull Response<ResponseModel> response) {
            }
            @Override
            public void onFailure(@NonNull Call<ResponseModel> call, @NonNull Throwable t) {
            }
        });
    }

    public void destroyEngine() {
        new Thread(() -> {
            try {
                if (rtcEngine != null) {
                    RtcEngine.destroy();
                    rtcEngine = null;
                }
            } catch (Exception ignored) {
            }
        }).start();
    }
}
