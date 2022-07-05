package com.volcengine.vertcdemo.interactivelivedemo.core;

import com.ss.bytertc.engine.RTCEngine;
import com.ss.bytertc.engine.RTCStream;
import com.ss.bytertc.engine.SubscribeConfig;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.data.AVSyncState;
import com.ss.bytertc.engine.data.MuteState;
import com.ss.bytertc.engine.data.RemoteStreamKey;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.bytertc.engine.data.VideoFrameInfo;
import com.ss.bytertc.engine.handler.IRTCEngineEventHandler;
import com.ss.bytertc.engine.handler.IRTCRoomEventHandler;

import java.nio.ByteBuffer;

public class IRtcRoomEventHandlerAdapter extends IRTCRoomEventHandler {

    @Override
    public void onLeaveRoom(IRTCEngineEventHandler.RTCRoomStats rtcRoomStats) {

    }

    @Override
    public void onRoomStateChanged(String roomId, String uid, int state, String extraInfo) {

    }

    @Override
    public void onStreamStateChanged(String roomId, String uid, int state, String extraInfo) {

    }

    @Override
    public void onRoomWarning(int i) {

    }

    @Override
    public void onRoomError(int i) {

    }

    @Override
    public void onAVSyncStateChange(AVSyncState state) {

    }

    @Override
    public void onAudioVolumeIndication(IRTCEngineEventHandler.AudioVolumeInfo[] audioVolumeInfos, int i, int i1) {

    }

    @Override
    public void onRtcStats(IRTCEngineEventHandler.RTCRoomStats rtcRoomStats) {

    }

    @Override
    public void onUserJoined(UserInfo userInfo, int i) {

    }

    @Override
    public void onUserLeave(String s, int i) {

    }

    @Override
    public void onTokenWillExpire() {

    }

    @Override
    public void onUserMuteAudio(String s, MuteState muteState) {

    }

    @Override
    public void onUserPublishStream(String uid, RTCEngine.MediaStreamType type) {

    }

    @Override
    public void onUserUnPublishStream(String uid, RTCEngine.MediaStreamType type, IRTCEngineEventHandler.StreamRemoveReason reason) {

    }

    @Override
    public void onUserPublishScreen(String uid, RTCEngine.MediaStreamType type) {

    }

    @Override
    public void onUserUnPublishScreen(String uid, RTCEngine.MediaStreamType type, IRTCEngineEventHandler.StreamRemoveReason reason) {

    }

    @Override
    public void onUserEnableLocalAudio(String s, boolean b) {

    }

    @Override
    public void onLocalStreamStats(IRTCEngineEventHandler.LocalStreamStats localStreamStats) {

    }

    @Override
    public void onRemoteStreamStats(IRTCEngineEventHandler.RemoteStreamStats remoteStreamStats) {

    }

    @Override
    public void onFirstLocalAudioFrame(StreamIndex streamIndex) {

    }

    @Override
    public void onFirstRemoteAudioFrame(RemoteStreamKey remoteStreamKey) {

    }

    @Override
    public void onStreamRemove(RTCStream rtcStream, IRTCEngineEventHandler.StreamRemoveReason streamRemoveReason) {

    }

    @Override
    public void onStreamAdd(RTCStream rtcStream) {

    }

    @Override
    public void onStreamSubscribed(int i, String s, SubscribeConfig subscribeConfig) {

    }

    @Override
    public void onStreamPublishSuccess(String s, boolean b) {

    }

    @Override
    public void onRoomMessageReceived(String s, String s1) {

    }

    @Override
    public void onRoomBinaryMessageReceived(String s, ByteBuffer byteBuffer) {

    }

    @Override
    public void onUserMessageReceived(String s, String s1) {

    }

    @Override
    public void onUserBinaryMessageReceived(String s, ByteBuffer byteBuffer) {

    }

    @Override
    public void onUserMessageSendResult(long l, int i) {

    }

    @Override
    public void onRoomMessageSendResult(long l, int i) {

    }

    @Override
    public void onFirstLocalVideoFrameCaptured(StreamIndex streamIndex, VideoFrameInfo videoFrameInfo) {

    }

    @Override
    public void onLocalVideoSizeChanged(StreamIndex streamIndex, VideoFrameInfo videoFrameInfo) {

    }

    @Override
    public void onRemoteVideoSizeChanged(RemoteStreamKey remoteStreamKey, VideoFrameInfo videoFrameInfo) {

    }

    @Override
    public void onFirstRemoteVideoFrameRendered(RemoteStreamKey remoteStreamKey, VideoFrameInfo videoFrameInfo) {

    }

    @Override
    public void onFirstRemoteVideoFrameDecoded(RemoteStreamKey remoteStreamKey, VideoFrameInfo frameInfo) {

    }

    @Override
    public void onUserMuteVideo(String s, MuteState muteState) {

    }

    @Override
    public void onUserStartVideoCapture(String s) {

    }

    @Override
    public void onUserStopVideoCapture(String s) {

    }

    @Override
    public void onUserStartAudioCapture(String s) {

    }

    @Override
    public void onUserStopAudioCapture(String s) {

    }

    @Override
    public void onVideoStreamBanned(String s, boolean b) {

    }

    @Override
    public void onAudioStreamBanned(String s, boolean b) {

    }
}