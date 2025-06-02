package com.breakreasi.voip_android.agora;

import java.util.Map;

/** @noinspection ALL */
public interface AgoraIEventListener2 {
    void onAgoraStatus(String status);

    void onJoinChannelSuccess(String channel, int uid, int elapsed);

    void onRejoinChannelSuccess(String channel, int uid, int elapsed);

    void onUserJoined(int uid, int elapsed);

    void onUserOffline(int uid, int reason);

    void onConnectionStateChanged(int status, int reason);

    void onPeersOnlineStatusChanged(Map<String, Integer> map);

    void onError(int err);
}
