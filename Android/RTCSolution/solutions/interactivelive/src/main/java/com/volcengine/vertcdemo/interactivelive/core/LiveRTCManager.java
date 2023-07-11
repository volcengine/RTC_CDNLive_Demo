// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.core;

import static com.ss.bytertc.engine.VideoCanvas.RENDER_MODE_HIDDEN;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.google.gson.JsonObject;
import com.ss.bytertc.engine.RTCRoom;
import com.ss.bytertc.engine.RTCRoomConfig;
import com.ss.bytertc.engine.RTCVideo;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.VideoCanvas;
import com.ss.bytertc.engine.VideoEncoderConfig;
import com.ss.bytertc.engine.data.CameraId;
import com.ss.bytertc.engine.data.ForwardStreamEventInfo;
import com.ss.bytertc.engine.data.ForwardStreamInfo;
import com.ss.bytertc.engine.data.ForwardStreamStateInfo;
import com.ss.bytertc.engine.data.MirrorType;
import com.ss.bytertc.engine.data.RemoteStreamKey;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.bytertc.engine.live.ByteRTCStreamMixingEvent;
import com.ss.bytertc.engine.live.ByteRTCStreamMixingType;
import com.ss.bytertc.engine.live.ByteRTCTranscoderErrorCode;
import com.ss.bytertc.engine.live.IMixedStreamObserver;
import com.ss.bytertc.engine.live.MixedStreamConfig;
import com.ss.bytertc.engine.live.MixedStreamConfig.MixedStreamLayoutRegionConfig;
import com.ss.bytertc.engine.live.MixedStreamConfig.MixedStreamVideoConfig;
import com.ss.bytertc.engine.live.MixedStreamConfig.MixedStreamAudioConfig;
import com.ss.bytertc.engine.live.MixedStreamConfig.MixedStreamLayoutConfig;
import com.ss.bytertc.engine.live.MixedStreamConfig.MixedStreamRenderMode;
import com.ss.bytertc.engine.live.MixedStreamType;
import com.ss.bytertc.engine.type.ChannelProfile;
import com.ss.bytertc.engine.type.MediaStreamType;
import com.ss.bytertc.engine.type.NetworkQualityStats;
import com.ss.bytertc.engine.video.VideoCaptureConfig;
import com.ss.bytertc.engine.video.VideoFrame;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.rts.RTCRoomEventHandlerWithRTS;
import com.volcengine.vertcdemo.core.net.rts.RTCVideoEventHandlerWithRTS;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.interactivelive.event.LocalUpdatePullStreamEvent;
import com.volcengine.vertcdemo.interactivelive.event.MediaChangedEvent;
import com.volcengine.vertcdemo.interactivelive.event.SDKNetworkConnectEvent;
import com.volcengine.vertcdemo.interactivelive.event.SDKNetworkQualityEvent;
import com.volcengine.vertcdemo.interactivelive.event.SDKReconnectToRoomEvent;
import com.volcengine.vertcdemo.protocol.IEffect;
import com.volcengine.vertcdemo.protocol.ProtocolUtil;
import com.volcengine.vertcdemo.utils.AppUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * RTC 接口封装类
 */

/**
 * {en}
 * RTC interface encapsulation class
 */
public class LiveRTCManager {
    public static final String KEY_SEI_KEY_SOURCE = "kLiveCoreSEIKEYSource";
    public static final String KEY_SEI_VALUE_SOURCE_NONE = "kLiveCoreSEIValueSourceNone";
    public static final String KEY_SEI_VALUE_SOURCE_CO_HOST = "kLiveCoreSEIValueSourceCoHost";

    private static final String TAG = "LiveRTCManager";

    // 本地摄像头状态
    private boolean mIsCameraOn = true;
    // 本地麦克风状态
    private boolean mIsMicOn = true;
    // 是否是前置摄像头
    private boolean mIsFront = true;
    // 是否是客户端合流转推
    private boolean mIsClientTranscoding = false;
    // 是否正在推流
    private boolean mIsTranscoding = false;
    // 是否正在pk
    private boolean mIsPk = false;
    // 合流转推参数
    private MixedStreamConfig mMixedStreamConfig = null;

    // 主播的视频采集参数
    private final LiveSettingConfig mHostConfig = new LiveSettingConfig(
            720, 1280, 15, 1600);

    // 嘉宾的视频采集参数
    private final LiveSettingConfig mGuestConfig = new LiveSettingConfig(
            256, 256, 15, 124);

    private String mPlayLiveResolution = RESO720;
    private final HashSet<String> mPlayLiveResolutionSet = new HashSet<String>() {{
        add(RESO480);
        add(RESO540);
        add(RESO720);
        add(RESO1080);
    }};

    @StringDef({RESO540, RESO720, RESO1080})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PLAY_LIVE_RESOLUTION {
    }

    public static final String RESO480 = "480";
    public static final String RESO540 = "540";
    public static final String RESO720 = "720";
    public static final String RESO1080 = "1080";

    // RTC引擎对象
    private RTCVideo mRTCVideo;
    // RTC 房间对象
    private RTCRoom mRTCRoom;
    private LiveRTSClient mRTSClient;
    // RTC 房间ID
    private String mRoomId;

    private static LiveRTCManager sInstance = new LiveRTCManager();

    // 用户id和RTC视频渲染view的映射
    private final Map<String, TextureView> mUidViewMap = new HashMap<>();

    // RTS对象，用来实现业务服务器的长链接
    private RTCRoom mRTSRoom = null;
    // RTS 长链接回调
    private final RTCRoomEventHandlerWithRTS mRTSRoomEventHandler = new RTCRoomEventHandlerWithRTS() {
    };

    private final RTCVideoEventHandlerWithRTS mRTCVideoEventHandler = new RTCVideoEventHandlerWithRTS() {

        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
        }

        @Override
        public void onError(int err) {
            super.onError(err);
        }


        /**
         * @param type 网络类型
         * -1： 网络连接类型未知
         * 0： 网络连接已断开
         * 1： 网络类型为 LAN
         * 2： 网络类型为 Wi-Fi（包含热点）
         * 3： 网络类型为 2G 移动网络
         * 4： 网络类型为 3G 移动网络
         * 5： 网络类型为 4G 移动
         */
        @Override
        public void onNetworkTypeChanged(int type) {
            super.onNetworkTypeChanged(type);
            SolutionDemoEventManager.post(new SDKNetworkConnectEvent(type != 0));
        }
    };

    /**
     * RTC 房间事件回调
     */
    /**
     * {en}
     * RTC room event callback
     */
    private final RTCRoomEventHandlerWithRTS mRTCRoomEventHandler = new RTCRoomEventHandlerWithRTS() {

        @Override
        public void onRoomStateChanged(String roomId, String uid, int state, String extraInfo) {
            super.onRoomStateChanged(roomId, uid, state, extraInfo);
            Log.d(TAG, String.format("onRoomStateChanged: %s, %s, %d, %s", roomId, uid, state, extraInfo));

            mRoomId = roomId;
            if (isFirstJoinRoomSuccess(state, extraInfo)) {
                if (mSingleLiveInfo != null) {
                    startPushMixedStreamToCDN(roomId, uid, mSingleLiveInfo.pushUrl);
                }
            } else if (isReconnectSuccess(state, extraInfo)) {
                SolutionDemoEventManager.post(new SDKReconnectToRoomEvent());
            }
        }

        @Override
        public void onUserJoined(UserInfo userInfo, int elapsed) {
            super.onUserJoined(userInfo, elapsed);
            String uid = userInfo.getUid();
            Log.d(TAG, String.format("onUserJoined : uid: %s ", uid));
            if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(mRoomId)) {
                TextureView renderView = getUserRenderView(uid);
                setRemoteVideoView(uid, mRoomId, renderView);
            }
        }

        @Override
        public void onNetworkQuality(NetworkQualityStats localQuality, NetworkQualityStats[] remoteQualities) {
            super.onNetworkQuality(localQuality, remoteQualities);
            // 发送本地网络状态统计
            SolutionDemoEventManager.post(new SDKNetworkQualityEvent(
                    SolutionDataManager.ins().getUserId(), localQuality.txQuality));
            // 发送远端网络状态统计
            if (remoteQualities == null) {
                return;
            }
            for (NetworkQualityStats stats : remoteQualities) {
                SolutionDemoEventManager.post(new SDKNetworkQualityEvent(stats.uid, stats.rxQuality));
            }
        }

        @Override
        public void onUserPublishStream(String uid, MediaStreamType type) {
            super.onUserPublishStream(uid, type);

            // 主播连麦时，需要更新合流转推参数
            if (mCoHostInfo != null && TextUtils.equals(uid, mCoHostInfo.coHostUserId)) {
                adjustResolutionWhenPK(true, mCoHostVideoWidth, mCoHostVideoHeight);
                updateLiveTranscodingWithHost(true,
                        mCoHostInfo.pushUrl, mCoHostInfo.selfRoomId, mCoHostInfo.selfUserId,
                        mCoHostInfo.coHostRoomId, mCoHostInfo.coHostUserId);

                TextureView view = LiveRTCManager.ins().getUserRenderView(uid);
                if (view != null) {
                    setRemoteVideoView(uid, mCoHostInfo.selfRoomId, view);
                }
            } else {
                updateLiveTranscodingWithAudience(mSingleLiveInfo.selfRoomId, mSingleLiveInfo.selfUserId,
                        mSingleLiveInfo.pushUrl, mAudienceUserIdList);
            }
        }

        @Override
        public void onForwardStreamStateChanged(ForwardStreamStateInfo[] stateInfos) {
            super.onForwardStreamStateChanged(stateInfos);
        }

        @Override
        public void onForwardStreamEvent(ForwardStreamEventInfo[] eventInfos) {
            super.onForwardStreamEvent(eventInfos);
            if (eventInfos != null && eventInfos.length > 0) {
                for (ForwardStreamEventInfo info : eventInfos) {
                    Log.d(TAG, String.format("onForwardStreamEvent: %s", info));
                }
            }
        }
    };

    // 保存连麦主播的信息
    private MixedStreamInfo mCoHostInfo;
    // 保存当前主播用户的信息
    private MixedStreamInfo mSingleLiveInfo;
    private List<String> mAudienceUserIdList;

    private static class MixedStreamInfo {
        public String pushUrl;
        public String selfRoomId;
        public String selfUserId;
        public String coHostRoomId;
        public String coHostUserId;

        private MixedStreamInfo() {
        }

        public MixedStreamInfo(String pushUrl, String selfRoomId, String selfUserId, String coHostRoomId,
                               String coHostUserId) {
            this.pushUrl = pushUrl;
            this.selfRoomId = selfRoomId;
            this.selfUserId = selfUserId;
            this.coHostRoomId = coHostRoomId;
            this.coHostUserId = coHostUserId;
        }
    }

    private static class LiveSettingConfig {
        public int width;
        public int height;
        public int frameRate;
        public int bitRate;

        public LiveSettingConfig() {
        }

        public LiveSettingConfig(int width, int height, int frameRate, int bitRate) {
            this.width = width;
            this.height = height;
            this.frameRate = frameRate;
            this.bitRate = bitRate;
        }
    }

    public static LiveRTCManager ins() {
        if (sInstance == null) {
            sInstance = new LiveRTCManager();
        }
        return sInstance;
    }

    public void rtcConnect(RTSInfo rtsInfo) {
        initEngine(rtsInfo.appId, rtsInfo.bid);
        mRTSClient = new LiveRTSClient(mRTCVideo, rtsInfo);
        mRTCVideoEventHandler.setBaseClient(mRTSClient);
        mRTCRoomEventHandler.setBaseClient(mRTSClient);
        mRTSRoomEventHandler.setBaseClient(mRTSClient);
        mRTSClient.login(rtsInfo.rtsToken, (resultCode, message) ->
                Log.d(TAG, String.format("notifyLoginResult: %d  %s", resultCode, message)));
    }

    public void initEngine(String appId, String bid) {
        Log.d(TAG, String.format("createEngine: appId: %s", appId));
        destroyEngine();
        mRTCVideo = RTCVideo.createRTCVideo(AppUtil.getApplicationContext(), appId, mRTCVideoEventHandler, null, null);
        mRTCVideo.setBusinessId(bid);
        mRTCVideo.setLocalVideoMirrorType(MirrorType.MIRROR_TYPE_RENDER_AND_ENCODER);

        VideoCaptureConfig captureConfig = new VideoCaptureConfig(
                mHostConfig.width, mHostConfig.height, mHostConfig.frameRate);
        mRTCVideo.setVideoCaptureConfig(captureConfig);

        VideoEncoderConfig config = new VideoEncoderConfig();
        config.width = mHostConfig.width;
        config.height = mHostConfig.height;
        config.frameRate = mHostConfig.frameRate;
        config.maxBitrate = mHostConfig.bitRate;
        mRTCVideo.setVideoEncoderConfig(config);
        Log.d(TAG, "setVideoEncoderConfig: " + config);

        initVideoEffect();
    }

    public void destroyEngine() {
        Log.d(TAG, "destroyEngine");
        if (mRTCRoom != null) {
            mRTCRoom.destroy();
        }
        if (mRTSClient != null) {
            mRTSClient.logout();
            mRTSClient = null;
        }
        if (mRTCVideo != null) {
            RTCVideo.destroyRTCVideo();
            mRTCVideo = null;
        }

        mIsFront = true;
        mIsMicOn = true;
        mIsCameraOn = true;
        mIsTranscoding = false;
    }

    public LiveRTSClient getRTSClient() {
        return mRTSClient;
    }

    public void clearRTSEventListener() {
        if (mRTSClient != null) {
            mRTSClient.removeEventListener();
        }
    }

    public boolean isCameraOn() {
        return mIsCameraOn;
    }

    public boolean isMicOn() {
        return mIsMicOn;
    }

    public void joinRoom(String roomId, String userId, String token) {
        Log.d(TAG, String.format("joinRoom: %s %s %s", roomId, userId, token));
        if (mRTCVideo == null) {
            return;
        }
        mRTCRoom = mRTCVideo.createRTCRoom(roomId);
        mRTCRoom.setRTCRoomEventHandler(mRTCRoomEventHandler);
        mRTCRoomEventHandler.setBaseClient(mRTSClient);
        UserInfo userInfo = new UserInfo(userId, null);
        RTCRoomConfig roomConfig = new RTCRoomConfig(ChannelProfile.CHANNEL_PROFILE_COMMUNICATION,
                true, true, true);
        mRTCRoom.joinRoom(token, userInfo, roomConfig);
    }

    public void startCaptureVideo(boolean isStart) {
        if (mRTCVideo != null) {
            if (isStart) {
                mRTCVideo.startVideoCapture();
            } else {
                mRTCVideo.stopVideoCapture();
            }
        }
        mIsCameraOn = isStart;
        Log.d(TAG, "startCaptureVideo : " + isStart);
    }

    public void startCaptureAudio(boolean isStart) {
        if (mRTCVideo != null) {
            if (isStart) {
                mRTCVideo.startAudioCapture();
            } else {
                mRTCVideo.stopAudioCapture();
            }
        }
        mIsMicOn = isStart;
        Log.d(TAG, "startCaptureAudio : " + isStart);
    }

    /**
     * 其余情况离房，需要重置状态
     */
    /**
     * {en}
     * Leave the room in other cases, you need to reset the status
     */
    public void leaveRoom() {
        Log.d(TAG, "leaveRoom");
        stopPushMixedStreamToCDN();
        if (mRTCRoom != null) {
            mRTCRoom.leaveRoom();
            mRTCRoom.destroy();
        }
        mSingleLiveInfo = null;
        mCoHostInfo = null;
        mIsFront = true;
        mIsMicOn = true;
        mIsCameraOn = true;
        mIsTranscoding = false;
        mRoomId = null;
    }

    public void switchCamera(boolean isFront) {
        if (mRTCVideo != null) {
            mRTCVideo.switchCamera(isFront ? CameraId.CAMERA_ID_FRONT : CameraId.CAMERA_ID_BACK);
            mRTCVideo.setLocalVideoMirrorType(isFront ? MirrorType.MIRROR_TYPE_RENDER_AND_ENCODER : MirrorType.MIRROR_TYPE_NONE);
        }
        mIsFront = isFront;
    }

    public void switchCamera() {
        switchCamera(!mIsFront);
    }

    public void turnOnMic(boolean isMicOn) {
        if (mRTCVideo != null) {
            if (isMicOn) {
                mRTCVideo.startAudioCapture();
            } else {
                mRTCVideo.stopAudioCapture();
            }
        }
        Log.d(TAG, "turnOnMic : " + isMicOn);
        mIsMicOn = isMicOn;
        postMediaStatus();
    }

    public void turnOnMic() {
        turnOnMic(!mIsMicOn);
    }

    public void publishAudio() {
        if (mRTCRoom == null) {
            return;
        }
        mRTCRoom.publishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
        mIsMicOn = true;
        postMediaStatus();
    }

    public void unPublishAudio() {
        if (mRTCRoom == null) {
            return;
        }
        mRTCRoom.unpublishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
        mIsMicOn = false;
        postMediaStatus();
    }

    public void turnOnCamera(boolean isCameraOn) {
        if (mRTCVideo != null) {
            if (isCameraOn) {
                mRTCVideo.startVideoCapture();
            } else {
                mRTCVideo.stopVideoCapture();
            }
        }
        mIsCameraOn = isCameraOn;
        Log.d(TAG, "turnOnCamera : " + isCameraOn);
        postMediaStatus();
    }

    private void postMediaStatus() {
        MediaChangedEvent event = new MediaChangedEvent();
        String selfUid = SolutionDataManager.ins().getUserId();
        event.userId = selfUid;
        event.operatorUserId = selfUid;
        event.mic = mIsMicOn ? LiveDataManager.MEDIA_STATUS_ON : LiveDataManager.MEDIA_STATUS_OFF;
        event.camera = mIsCameraOn ? LiveDataManager.MEDIA_STATUS_ON : LiveDataManager.MEDIA_STATUS_OFF;
        SolutionDemoEventManager.post(event);
    }

    public void turnOnCamera() {
        turnOnCamera(!mIsCameraOn);
    }

    public void setLocalVideoView(@NonNull TextureView surfaceView) {
        if (mRTCVideo == null) {
            return;
        }
        Log.d(TAG, "setLocalVideoView");
        VideoCanvas videoCanvas = new VideoCanvas(surfaceView, RENDER_MODE_HIDDEN);
        mRTCVideo.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, videoCanvas);
    }

    public TextureView getUserRenderView(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        TextureView view = mUidViewMap.get(userId);
        if (view == null) {
            view = new TextureView(AppUtil.getApplicationContext());
            mUidViewMap.put(userId, view);
        }
        return view;
    }

    public void removeAllUserRenderView() {
        mUidViewMap.clear();
    }

    public void setRemoteVideoView(String userId, String roomId, TextureView textureView) {
        Log.d(TAG, String.format("setRemoteVideoView : %s %s", userId, roomId));
        if (mRTCVideo != null) {
            VideoCanvas canvas = new VideoCanvas(textureView, RENDER_MODE_HIDDEN);
            RemoteStreamKey remoteStreamKey = new RemoteStreamKey(roomId, userId, StreamIndex.STREAM_INDEX_MAIN);
            mRTCVideo.setRemoteVideoCanvas(remoteStreamKey, canvas);
        }
    }

    /**
     * mute 远端音频
     * @param uid 用户id
     * @param mute 是否静音
     */
    /**
     * {en}
     * mute remote audio
     *
     * @param uid  user id
     * @param mute Whether to mute
     */
    public void muteRemoteAudio(String uid, boolean mute) {
        Log.d(TAG, "muteRemoteAudio uid:" + uid + ",mute:" + mute);
        if (mRTCRoom != null) {
            if (mute) {
                mRTCRoom.unsubscribeStream(uid, MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
            } else {
                mRTCRoom.subscribeStream(uid, MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
            }
        }
        updateLiveTranscodingWhenMuteCoHost(uid, mute);
    }

    public void setLiveTranscodingType(boolean isClient) {
        mIsClientTranscoding = isClient;
    }

    public boolean getLiveTranscodingType() {
        return mIsClientTranscoding;
    }

    public void setFrameRate(@LiveDataManager.LiveRoleType int role, int frameRate) {
        LiveSettingConfig config = role == LiveDataManager.USER_ROLE_HOST
                ? mHostConfig
                : mGuestConfig;
        config.frameRate = frameRate;
        updateVideoConfig(config.width, config.height, config.frameRate, config.bitRate);
    }

    public void setResolution(@LiveDataManager.LiveRoleType int role, int width, int height) {
        LiveSettingConfig config = role == LiveDataManager.USER_ROLE_HOST
                ? mHostConfig
                : mGuestConfig;
        config.width = width;
        config.height = height;
        updateVideoConfig(config.width, config.height, config.frameRate, config.bitRate);
    }

    public void setBitrate(@LiveDataManager.LiveRoleType int role, int bitRate) {
        LiveSettingConfig config = role == LiveDataManager.USER_ROLE_HOST
                ? mHostConfig
                : mGuestConfig;
        config.bitRate = bitRate;
        updateVideoConfig(config.width, config.height, config.frameRate, config.bitRate);
    }

    public int getBitrate(@LiveDataManager.LiveRoleType int role) {
        return role == LiveDataManager.USER_ROLE_HOST
                ? mHostConfig.bitRate
                : mGuestConfig.bitRate;
    }

    public int getFrameRate(@LiveDataManager.LiveRoleType int role) {
        return role == LiveDataManager.USER_ROLE_HOST
                ? mHostConfig.frameRate
                : mGuestConfig.frameRate;
    }

    public int getWidth(@LiveDataManager.LiveRoleType int role) {
        return role == LiveDataManager.USER_ROLE_HOST
                ? mHostConfig.width
                : mGuestConfig.width;
    }

    public int getHeight(@LiveDataManager.LiveRoleType int role) {
        return role == LiveDataManager.USER_ROLE_HOST
                ? mHostConfig.height
                : mGuestConfig.height;
    }

    public boolean isLiveTranscoding() {
        return mIsTranscoding;
    }

    private void updateVideoConfig(int width, int height, int frameRate, int bitRate) {
        if (mRTCVideo != null && !mIsTranscoding) {
            VideoEncoderConfig config = new VideoEncoderConfig();
            config.width = width;
            config.height = height;
            config.frameRate = frameRate;
            config.maxBitrate = bitRate;
            Log.d(TAG, String.format("updateVideoConfig: %d-%d %d %d",
                    width, height, frameRate, bitRate));
            mRTCVideo.setVideoEncoderConfig(config);
            Log.d(TAG, "setVideoEncoderConfig: " + config);

            VideoCaptureConfig captureConfig = new VideoCaptureConfig(width, height, frameRate);
            mRTCVideo.setVideoCaptureConfig(captureConfig);
        }
    }

    public void setPlayLiveStreamResolution(@PLAY_LIVE_RESOLUTION String resolution) {
        if (mPlayLiveResolutionSet.contains(resolution)) {
            mPlayLiveResolution = resolution;
            SolutionDemoEventManager.post(new LocalUpdatePullStreamEvent());
        }
    }

    public String getPlayLiveStreamResolution() {
        return mPlayLiveResolution;
    }

    public void joinRTSRoom(String rtsRoomId, String userId, String token) {
        Log.d(TAG, String.format("joinRTSRoom: %s  %s  %s", rtsRoomId, userId, token));
        if (mRTCVideo == null) {
            return;
        }
        mRTSRoom = mRTCVideo.createRTCRoom(rtsRoomId);
        mRTSRoom.setRTCRoomEventHandler(mRTSRoomEventHandler);
        UserInfo userInfo = new UserInfo(userId, null);
        RTCRoomConfig roomConfig = new RTCRoomConfig(ChannelProfile.CHANNEL_PROFILE_COMMUNICATION,
                false, false, false);
        mRTSRoom.joinRoom(token, userInfo, roomConfig);
    }

    public void leaveRTSRoom() {
        if (mRTSRoom == null) {
            return;
        }
        mRTSRoom.leaveRoom();
        mRTSRoom.destroy();
        mRTSRoom = null;
    }

    private final IMixedStreamObserver mMixedStreamObserver = new IMixedStreamObserver() {
        /**
         * 客户端是否具有推流能力。
         * @return false：不具备推流能力（默认值）
         */
        @Override
        public boolean isSupportClientPushStream() {
            return false;
        }

        /**
         * 转推直播状态回调
         * @param eventType 转推直播任务状态
         * @param taskId 转推直播任务 ID
         * @param error 转推直播错误码
         * @param mixType 转推直播类型
         */
        @Override
        public void onMixingEvent(ByteRTCStreamMixingEvent eventType, String taskId,
                                  ByteRTCTranscoderErrorCode error, MixedStreamType mixType) {
            Log.d(TAG, String.format("onMixingEvent: %s %s", eventType, error));
        }

        @Override
        public void onMixingAudioFrame(String taskId, byte[] audioFrame,
                                       int frameNum, long timeStampMs) {

        }

        @Override
        public void onMixingVideoFrame(String taskId, VideoFrame videoFrame) {

        }

        @Override
        public void onMixingDataFrame(String taskId, byte[] dataFrame, long time) {

        }

        @Override
        public void onCacheSyncVideoFrames(String taskId, String[] userIds,
                                           VideoFrame[] videoFrame, byte[][] dataFrame, int count) {

        }

    };

    public void setSingleLiveInfo(String roomId, String userId, String pushUrl) {
        mSingleLiveInfo = new MixedStreamInfo(pushUrl, roomId, userId, null, null);
    }

    /**
     * 开启合流转推
     * @param roomId 房间id
     * @param userId 用户id
     * @param liveUrl rtmp 推流地址
     */
    /**
     * {en}
     * Turn on live transcoding
     *
     * @param roomId  room id
     * @param userId  user id
     * @param liveUrl rtmp streaming address
     */
    private void startPushMixedStreamToCDN(String roomId, String userId, String liveUrl) {
        Log.d(TAG, String.format("startPushMixedStreamToCDN: %s  %s  %s", roomId, userId, liveUrl));
        mRoomId = roomId;
        MixedStreamConfig config = MixedStreamConfig.defaultMixedStreamConfig();
        config.setRoomID(roomId);
        config.setUserID(userId);
        config.setPushURL(liveUrl);
        config.setExpectedMixingType(ByteRTCStreamMixingType.STREAM_MIXING_BY_SERVER);

        // 设置合流视频参数，具体参数根据情况而定
        MixedStreamVideoConfig videoConfig = config.getVideoConfig();
        videoConfig.setWidth(mHostConfig.width);
        videoConfig.setHeight(mHostConfig.height);
        videoConfig.setFps(mHostConfig.frameRate);
        videoConfig.setBitrate(mHostConfig.bitRate);
        config.setVideoConfig(videoConfig);

        // 设置合流音频参数，具体参数根据情况而定
        MixedStreamAudioConfig audioConfig = config.getAudioConfig();
        audioConfig.setSampleRate(44100);
        audioConfig.setChannels(2);
        config.setAudioConfig(audioConfig);

        // 设置合流视频布局参数
        MixedStreamLayoutRegionConfig localRegionConfig = new MixedStreamLayoutRegionConfig();
        localRegionConfig.setUserID(userId);
        localRegionConfig.setIsLocalUser(true);
        localRegionConfig.setRoomID(roomId);
        localRegionConfig.setLocationX(0);
        localRegionConfig.setLocationY(0);
        localRegionConfig.setWidthProportion(1);
        localRegionConfig.setHeightProportion(1);
        localRegionConfig.setAlpha(1);
        localRegionConfig.setZOrder(0);
        localRegionConfig.setRenderMode(MixedStreamRenderMode.MIXED_STREAM_RENDER_MODE_HIDDEN);

        MixedStreamLayoutRegionConfig[] regions = new MixedStreamLayoutRegionConfig[]{localRegionConfig};
        MixedStreamLayoutConfig layoutConfig = new MixedStreamLayoutConfig();
        JsonObject json = new JsonObject();
        json.addProperty(KEY_SEI_KEY_SOURCE, KEY_SEI_VALUE_SOURCE_NONE);
        layoutConfig.setUserConfigExtraInfo(json.toString());
        layoutConfig.setRegions(regions);
        config.setLayout(layoutConfig);
        // 开始合流任务，taskid使用空字符串即可
        mRTCVideo.startPushMixedStreamToCDN("", config, mMixedStreamObserver);
    }

    /**
     * 停止合流转推
     */
    /**
     * {en}
     * stop live transcoding
     */
    public void stopPushMixedStreamToCDN() {
        Log.d(TAG, "stopLiveTranscoding");
        if (mRTCVideo != null) {
            mRTCVideo.stopPushStreamToCDN("");
        }
        mMixedStreamConfig = null;
    }

    /**
     * PK时调整编码分辨率，调整为单独直播时一半
     * @param adjust true表示调整，false表示恢复
     */
    /**
     * {en}
     * Adjust the encoding resolution during PK, and adjust it to half of the live broadcast alone
     *
     * @param adjust true means adjustment, false means recovery
     */
    private void adjustResolutionWhenPK(boolean adjust, int coHostWidth, int coHostHeight) {
        if (mRTCVideo == null) {
            return;
        }
        VideoEncoderConfig config = new VideoEncoderConfig();
        config.frameRate = mHostConfig.frameRate;
        if (adjust) {
            config.width = (Math.max(mHostConfig.width, coHostWidth)) / 2;
            config.height = (Math.max(mHostConfig.height, coHostHeight)) / 2;
            config.maxBitrate = mHostConfig.bitRate / 4;
        } else {
            config.width = mHostConfig.width;
            config.height = mHostConfig.height;
            config.maxBitrate = mHostConfig.bitRate;
        }
        Log.d(TAG, "setVideoEncoderConfig: " + config);
        mRTCVideo.setVideoEncoderConfig(config);
    }

    /**
     * 更新合流转推参数
     * @param isStart 是否是pk开始
     * @param selfRoomId 房间id
     * @param selfUserId 自己的用户Id
     * @param coHostRoomId 对方房间id
     * @param coHostUserId 对方主播的用户Id
     */
    /**
     * {en}
     * Update live transcoding parameters
     *
     * @param isStart      Whether it is pk start
     * @param selfRoomId   room id
     * @param selfUserId   user Id
     * @param coHostRoomId The other party's room id
     * @param coHostUserId The host's user ID
     */
    public void updateLiveTranscodingWithHost(boolean isStart, String liveUrl, String selfRoomId,
                                              String selfUserId, String coHostRoomId, String coHostUserId) {

        mIsTranscoding = isStart;
        mIsPk = isStart;
        adjustResolutionWhenPK(isStart, mCoHostVideoWidth, mCoHostVideoHeight);

        MixedStreamConfig config = MixedStreamConfig.defaultMixedStreamConfig();
        config.setRoomID(selfRoomId);
        config.setPushURL(liveUrl);
        config.setExpectedMixingType(ByteRTCStreamMixingType.STREAM_MIXING_BY_SERVER);
        // 设置合流视频参数，具体参数根据情况而定
        // Set the merged live transcoding parameters, the specific parameters depend on the situation
        MixedStreamVideoConfig videoConfig = config.getVideoConfig();
        videoConfig.setWidth(mHostConfig.width);
        videoConfig.setHeight(mHostConfig.height);
        videoConfig.setFps(mHostConfig.frameRate);
        videoConfig.setBitrate(mHostConfig.bitRate);
        // 设置合流音频参数，具体参数根据情况而定
        MixedStreamAudioConfig audioConfig = config.getAudioConfig();
        audioConfig.setSampleRate(44100);
        audioConfig.setChannels(2);

        MixedStreamLayoutConfig layoutConfig = new MixedStreamLayoutConfig();
        if (isStart){
            MixedStreamLayoutRegionConfig localRegion = new MixedStreamLayoutRegionConfig();
            localRegion.setUserID(selfUserId);
            localRegion.setIsLocalUser(true);
            localRegion.setRoomID(selfRoomId);
            localRegion.setLocationX(0);
            localRegion.setLocationY(0.25);
            localRegion.setWidthProportion(0.5);
            localRegion.setHeightProportion(0.5);
            localRegion.setAlpha(1);
            localRegion.setZOrder(0);

            MixedStreamLayoutRegionConfig hostRegion = new MixedStreamLayoutRegionConfig();
            hostRegion.setUserID(coHostUserId);
            hostRegion.setIsLocalUser(false);
            hostRegion.setRoomID(selfRoomId);
            hostRegion.setLocationX(0.5);
            hostRegion.setLocationY(0.25);
            hostRegion.setWidthProportion(0.5);
            hostRegion.setHeightProportion(0.5);
            hostRegion.setAlpha(1);
            hostRegion.setZOrder(0);

            JsonObject json = new JsonObject();
            json.addProperty(KEY_SEI_KEY_SOURCE, KEY_SEI_VALUE_SOURCE_CO_HOST);
            layoutConfig.setUserConfigExtraInfo(json.toString());
            layoutConfig.setRegions(new MixedStreamLayoutRegionConfig[]{localRegion,hostRegion});
            config.setLayout(layoutConfig);
        }else {
            MixedStreamLayoutRegionConfig localRegion = new MixedStreamLayoutRegionConfig();
            localRegion.setUserID(selfUserId);
            localRegion.setIsLocalUser(true);
            localRegion.setRoomID(selfRoomId);
            localRegion.setLocationX(0);
            localRegion.setLocationY(0);
            localRegion.setWidthProportion(1);
            localRegion.setHeightProportion(1);
            localRegion.setAlpha(1);
            localRegion.setZOrder(0);

            JsonObject json = new JsonObject();
            json.addProperty(KEY_SEI_KEY_SOURCE, KEY_SEI_VALUE_SOURCE_NONE);
            layoutConfig.setUserConfigExtraInfo(json.toString());
            layoutConfig.setRegions(new MixedStreamLayoutRegionConfig[]{localRegion});
            config.setLayout(layoutConfig);
        }

        mMixedStreamConfig = config;
        mRTCVideo.updatePushMixedStreamToCDN("", mMixedStreamConfig);
    }


    /**
     * 当 mute 对方主播时，需要修改合流转推参数
     * @param userId 用户userId
     * @param isMute 是否是静音
     */
    /**
     * {en}
     * When mute the host of the other party, you need to modify the live transcoding parameters
     *
     * @param userId user userId
     * @param isMute whether it is mute
     */
    public void updateLiveTranscodingWhenMuteCoHost(String userId, boolean isMute) {
        if (TextUtils.isEmpty(userId)) {
            Log.d(TAG, "muteCoHost() failed, userId is empty");
            return;
        }
        if (mMixedStreamConfig == null || !mIsPk || mCoHostInfo == null) {
            Log.d(TAG, "muteCoHost() failed, LiveTranscoding params error");
            return;
        }
        MixedStreamLayoutConfig layout = mMixedStreamConfig.getLayout();
        if (layout == null) {
            Log.d(TAG, "muteCoHost() failed, layout is null");
            return;
        }
        MixedStreamLayoutRegionConfig[] regions = layout.getRegions();
        if (regions == null) {
            Log.d(TAG, "muteCoHost() failed, regions is null");
            return;
        }
        for (MixedStreamLayoutRegionConfig region : regions) {
            if (region != null && !region.getIsLocalUser() && TextUtils.equals(userId, mCoHostInfo.coHostUserId)) {
                region.setMediaType(isMute
                        ? MixedStreamConfig.MixedStreamMediaType.MIXED_STREAM_MEDIA_TYPE_VIDEO_ONLY
                        : MixedStreamConfig.MixedStreamMediaType.MIXED_STREAM_MEDIA_TYPE_AUDIO_AND_VIDEO);
                break;
            }
        }
        if (mRTCVideo != null) {
            mRTCVideo.updatePushMixedStreamToCDN("", mMixedStreamConfig);
        }
    }

    /**
     * 更新观众连麦的布局
     * @param roomId 房间id
     * @param selfUserId 主播的用户id
     * @param liveUrl 直播的url
     * @param audienceUserIdList 观众id列表，传null意味着结束共享
     */
    /**
     * {en}
     * Update the layout of the audience's params
     *
     * @param roomId             room id
     * @param selfUserId         the anchor's user id
     * @param liveUrl            live URL
     * @param audienceUserIdList audience id list, passing null means end sharing
     */
    public void updateLiveTranscodingWithAudience(String roomId, String selfUserId, String liveUrl,
                                                  List<String> audienceUserIdList) {

        mAudienceUserIdList = audienceUserIdList;

        MixedStreamConfig config = MixedStreamConfig.defaultMixedStreamConfig();
        config.setRoomID(roomId);
        config.setPushURL(liveUrl);
        config.setExpectedMixingType(ByteRTCStreamMixingType.STREAM_MIXING_BY_SERVER);

        MixedStreamVideoConfig videoConfig =  config.getVideoConfig();
        videoConfig.setWidth(mHostConfig.width);
        videoConfig.setHeight(mHostConfig.height);
        videoConfig.setFps(mHostConfig.frameRate);
        videoConfig.setBitrate(mHostConfig.bitRate);

        MixedStreamAudioConfig audioConfig =  config.getAudioConfig();
        audioConfig.setSampleRate(44100);
        audioConfig.setChannels(2);

        MixedStreamLayoutConfig layoutConfig = new MixedStreamLayoutConfig();
        List<MixedStreamLayoutRegionConfig> regions = new ArrayList<>();
        {
            MixedStreamLayoutRegionConfig localRegion = new MixedStreamLayoutRegionConfig();
            localRegion.setUserID(selfUserId);
            localRegion.setRoomID(roomId);
            localRegion.setLocationX(0);
            localRegion.setLocationY(0);
            localRegion.setWidthProportion(1);
            localRegion.setHeightProportion(1);
            localRegion.setAlpha(1);
            localRegion.setZOrder(0);
            regions.add(localRegion);
        }
        if (audienceUserIdList != null && audienceUserIdList.size() > 1) {
            mIsTranscoding = true;
            for (int i = 1; i < audienceUserIdList.size(); i++) {
                MixedStreamLayoutRegionConfig region = new MixedStreamLayoutRegionConfig();
                region.setUserID(audienceUserIdList.get(i));
                region.setRoomID(roomId);
                float screenWidth = 365;
                float screenHeight = 667;
                float itemHeight = 80;
                float itemSpace = 6;
                float itemRightSpace = 52;
                float itemTopSpace = 500;
                int index = i - 1;
                float regionHeight = itemHeight / screenHeight;
                float regionWidth = regionHeight * screenHeight / screenWidth;
                float regionY = (itemTopSpace - (itemHeight + itemSpace) * index) / screenHeight;
                float regionX = 1 - (regionHeight * screenHeight + itemRightSpace) / screenWidth;
                region.setLocationX(regionX);
                region.setLocationY(regionY);
                region.setWidthProportion(regionWidth);
                region.setHeightProportion(regionHeight);
                region.setZOrder(1);
                region.setAlpha(1);
                regions.add(region);
            }
        } else {
            mIsTranscoding = false;
        }
        JsonObject json = new JsonObject();
        json.addProperty(KEY_SEI_KEY_SOURCE, KEY_SEI_VALUE_SOURCE_NONE);
        layoutConfig.setUserConfigExtraInfo(json.toString());
        layoutConfig.setRegions(regions.toArray(new MixedStreamLayoutRegionConfig[]{}));
        config.setLayout(layoutConfig);

        mRTCVideo.updatePushMixedStreamToCDN("", config);
    }

    /**
     * 将自己的视频流推送到其他房间
     * @param coHostRoomId 对方房间的roomId
     * @param coHostUserId 对方的userId
     * @param token 用于向该房间转发媒体流的 Token
     * @param selfRoomId 自己房间的roomId
     * @param selfUserId 自己的userId
     * @param pushUrl 推流地址
     */
    /**
     * {en}
     * Push your own video stream to other rooms
     *
     * @param coHostRoomId The roomId of the other party's room
     * @param coHostUserId The other party's userId
     * @param token        Token used to forward media stream to this room
     * @param selfRoomId   the roomId of your own room
     * @param selfUserId   own userId
     * @param pushUrl      push URL
     */
    public void startForwardStreamToRooms(String coHostRoomId, String coHostUserId, String token,
                                          String selfRoomId, String selfUserId, String pushUrl) {
        mCoHostInfo = new MixedStreamInfo(pushUrl, selfRoomId, selfUserId, coHostRoomId, coHostUserId);

        ForwardStreamInfo forwardStreamInfo = new ForwardStreamInfo(coHostRoomId, token);
        if (mRTCRoom != null) {
            mRTCRoom.stopForwardStreamToRooms();
            int res = mRTCRoom.startForwardStreamToRooms(Collections.singletonList(forwardStreamInfo));
            Log.d(TAG, "startForwardStreamToRooms: " + res);
        }
    }

    private int mCoHostVideoWidth; // 对方主播视频的宽
    private int mCoHostVideoHeight; // 对方主播视频的高

    /**
     * 设置对方主播视频分辨率
     * @param coHostVideoWidth  对方主播视频的宽
     * @param coHostVideoHeight  对方主播视频的高
     */
    /**
     * {en}
     * Set the host video resolution of the other anchor
     *
     * @param coHostVideoWidth  The width of the host's video
     * @param coHostVideoHeight The height of the host's video
     */
    public void setCoHostVideoConfig(int coHostVideoWidth, int coHostVideoHeight) {
        mCoHostVideoWidth = coHostVideoWidth;
        mCoHostVideoHeight = coHostVideoHeight;
    }

    /**
     * 停止自己的视频流推送到其他房间
     */
    /**
     * {en}
     * Stop pushing your own video stream to other rooms
     */
    public void stopLiveTranscodingWithHost() {
        Log.d(TAG, "stopLiveTranscodingWithHost");
        mCoHostInfo = null;
        if (mRTCRoom != null) {
            mRTCRoom.stopForwardStreamToRooms();
        }
    }

    /**
     * 初始化视频美颜
     */
    /**
     * {en}
     * Initialize video beauty
     */
    private void initVideoEffect() {
        IEffect effect = ProtocolUtil.getIEffect();
        if (effect != null) {
            effect.initWithRTCVideo(mRTCVideo);
        }
    }

    /**
     * 打开美颜对话框
     * @param context 上下文对象
     */
    /**
     * {en}
     * Open the beauty dialog
     *
     * @param context context object
     */
    public void openEffectDialog(Context context) {
        IEffect effect = ProtocolUtil.getIEffect();
        if (effect != null) {
            effect.showEffectDialog(context, null);
        }
    }
}
