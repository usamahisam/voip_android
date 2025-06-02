package com.breakreasi.voip_android.agora;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc2.IRtcEngineEventHandler;

public class AgoraEngineEventListener extends IRtcEngineEventHandler {
    private final List<AgoraIEventListener2> mListeners = new ArrayList<>();

    public void registerEventListener(AgoraIEventListener2 listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeEventListener(AgoraIEventListener2 listener) {
        mListeners.remove(listener);
    }

    public void onAgoraStatus(String status) {
        for (AgoraIEventListener2 listener : mListeners) {
            listener.onAgoraStatus(status);
        }
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        int size = mListeners.size();
        if (size > 0) {
            mListeners.get(size - 1).onJoinChannelSuccess(channel, uid, elapsed);
        }
    }

    @Override
    public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
        int size = mListeners.size();
        if (size > 0) {
            mListeners.get(size - 1).onRejoinChannelSuccess(channel, uid, elapsed);
        }
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        int size = mListeners.size();
        if (size > 0) {
            mListeners.get(size - 1).onUserJoined(uid, elapsed);
        }
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        int size = mListeners.size();
        if (size > 0) {
            mListeners.get(size - 1).onUserOffline(uid, reason);
        }
    }

    @Override
    public void onConnectionStateChanged(int status, int reason) {
        int size = mListeners.size();
        if (size > 0) {
            mListeners.get(size - 1).onConnectionStateChanged(status, reason);
        }
    }
}
