package com.volcengine.vertcdemo.interactivelivedemo.core;

import static com.ss.bytertc.engine.RTCEngine.RemoteUserPriority.REMOTE_USER_PRIORITY_HIGH;
import static com.ss.bytertc.engine.RTCEngine.SubscribeMediaType.RTC_SUBSCRIBE_MEDIA_TYPE_AUDIO_AND_VIDEO;
import static com.ss.bytertc.engine.RTCEngine.SubscribeMediaType.RTC_SUBSCRIBE_MEDIA_TYPE_VIDEO_ONLY;
import static com.ss.bytertc.engine.VideoCanvas.RENDER_MODE_HIDDEN;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.TextureView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.ss.bytertc.engine.IAudioFrameObserver;
import com.ss.bytertc.engine.IRTCRoom;
import com.ss.bytertc.engine.MultiRoomConfig;
import com.ss.bytertc.engine.RTCEngine;
import com.ss.bytertc.engine.RTCRoomConfig;
import com.ss.bytertc.engine.SubscribeVideoConfig;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.VideoCanvas;
import com.ss.bytertc.engine.VideoStreamDescription;
import com.ss.bytertc.engine.data.CameraId;
import com.ss.bytertc.engine.data.MirrorType;
import com.ss.bytertc.engine.data.RemoteStreamKey;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.bytertc.engine.live.ByteRTCStreamMixingEvent;
import com.ss.bytertc.engine.live.ByteRTCStreamMixingType;
import com.ss.bytertc.engine.live.ByteRTCTranscoderErrorCode;
import com.ss.bytertc.engine.live.ILiveTranscodingObserver;
import com.ss.bytertc.engine.live.LiveTranscoding;
import com.ss.bytertc.engine.utils.IAudioFrame;
import com.ss.bytertc.engine.video.IVideoSink;
import com.ss.bytertc.engine.video.VideoCaptureConfig;
import com.ss.video.rtc.demo.basic_module.utils.AppExecutors;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.rtm.RTCEventHandlerWithRTM;
import com.volcengine.vertcdemo.core.net.rtm.RtmInfo;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.MediaChangedEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.NetworkConnectEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.NetworkQualityEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.UpdatePullStreamEvent;

import org.webrtc.VideoFrame;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class LiveRTCManager {

    private static final String TAG = "LiveRTCManager";

    private boolean mIsCameraOn = true;
    private boolean mIsMicOn = true;
    private boolean mIsFront = true;
    private boolean mIsClientTranscoding = false;
    private boolean mIsTranscoding = false;
    private int mFrameRate = 15;
    private int mFrameWidth = 720;
    private int mFrameHeight = 1280;
    private int mBitrate = 1600;

    private final ArrayList<String> mEffectPathList = new ArrayList<>();

    private LiveTranscoding mLiveTranscoding;

    private String mPlayLiveResolution = RESO720;
    private final HashSet<String> mPlayLiveResolutionSet = new HashSet<String>() {{
        add(RESO480);
        add(RESO540);
        add(RESO720);
        add(RESO1080);
    }};

    @StringDef({RESO540, RESO720, RESO1080})
    public @interface PLAY_LIVE_RESOLUTION {
    }

    public static final String RESO480 = "480";
    public static final String RESO540 = "540";
    public static final String RESO720 = "720";
    public static final String RESO1080 = "1080";

    private static final int LIVE_TRANSCODING_CLIENT = 1;
    private static final int LIVE_TRANSCODING_SERVER = 0;

    @IntDef({LIVE_TRANSCODING_CLIENT, LIVE_TRANSCODING_SERVER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LIVE_TRANSCODING_TYPE {
    }

    private RTCEngine mEngine;
    private LiveRtmClient mRTMClient;
    private RtmInfo mRTMInfo;
    private static LiveRTCManager sInstance = new LiveRTCManager();

    private final Map<String, TextureView> mUidViewMap = new HashMap<>();

    private final RTCEventHandlerWithRTM mIRTCEngineEventHandler = new RTCEventHandlerWithRTM() {

        @Override
        public void onRoomStateChanged(String roomId, String uid, int state, String extraInfo) {
            super.onRoomStateChanged(roomId, uid, state, extraInfo);
            Log.d(TAG, String.format("onRoomStateChanged: %s, %s, %d, %s", roomId, uid, state, extraInfo));
        }

        @Override
        public void onUserJoined(UserInfo userInfo, int elapsed) {
            super.onUserJoined(userInfo, elapsed);
            String uid = userInfo.getUid();
            Log.d(TAG, String.format("onUserJoined : uid: %s ", uid));
            if (!TextUtils.isEmpty(uid)) {
                TextureView renderView = getUserRenderView(uid);
                setRemoteVideoView(uid, renderView);
            }
        }

        @Override
        public void onWarning(int warn) {
            super.onWarning(warn);
        }

        @Override
        public void onError(int err) {
            super.onError(err);
        }

        @Override
        public void onLocalStreamStats(LocalStreamStats stats) {
            super.onLocalStreamStats(stats);
            SolutionDemoEventManager.post(new NetworkQualityEvent(
                    SolutionDataManager.ins().getUserId(), stats.txQuality));
        }

        @Override
        public void onRemoteStreamStats(RemoteStreamStats stats) {
            super.onRemoteStreamStats(stats);
            SolutionDemoEventManager.post(new NetworkQualityEvent(stats.uid, stats.rxQuality));
        }

        /**
         * RTM 登录结果回调
         * @param uid 登录用户 ID
         * @param error_code 登录结果
         * @param elapsed 从调用 login 接口开始到返回结果所用时长（单位为 ms）。
         */
        @Override
        public void onLoginResult(String uid, int error_code, int elapsed) {
            Log.e(TAG, "onLoginResult error:" + error_code);
            if (error_code == LoginErrorCode.LOGIN_ERROR_CODE_SUCCESS) {
                LiveRTCManager manager = LiveRTCManager.ins();
                RTCEngine engine = manager.mEngine;
                if (engine != null) {
                    engine.setServerParams(manager.mRTMInfo.serverSignature, manager.mRTMInfo.serverUrl);
                }
            } else {
                SafeToast.show("连接失败");
            }
        }

        @Override
        public void onServerParamsSetResult(int error) {
            Log.e(TAG, "onServerParamsSetResult error:" + error);
            LiveRTCManager manager = LiveRTCManager.ins();
            if (manager != null && manager.mRTMClient != null) {
                manager.mRTMClient.onServerParamsSetResult(error);
            }
        }

        @Override
        public void onServerMessageSendResult(long msgId, int error, ByteBuffer message) {
            super.onServerMessageSendResult(msgId, error, message);
            Log.e(TAG, "onServerMessageSendResult error:" + error);
            LiveRTCManager manager = LiveRTCManager.ins();
            if (manager != null && manager.mRTMClient != null) {
                manager.mRTMClient.onServerMessageSendResult(msgId, error);
            }
        }

        @Override
        public void onRoomMessageReceived(String uid, String message) {
            Log.e(TAG, "onRoomMessageReceived message:" + message);
            LiveRTCManager manager = LiveRTCManager.ins();
            if (manager != null && manager.mRTMClient != null) {
                manager.mRTMClient.onMessageReceived(uid, message);
            }
        }

        @Override
        public void onUserMessageReceivedOutsideRoom(String uid, String message) {
            Log.e(TAG, "onUserMessageReceivedOutsideRoom message:" + message);
            LiveRTCManager manager = LiveRTCManager.ins();
            if (manager != null && manager.mRTMClient != null) {
                manager.mRTMClient.onMessageReceived(uid, message);
            }
        }

        @Override
        public void onUserMessageReceived(String uid, String message) {
            Log.e(TAG, "onUserMessageReceived message:" + message);
            LiveRTCManager manager = LiveRTCManager.ins();
            if (manager != null && manager.mRTMClient != null) {
                manager.mRTMClient.onMessageReceived(uid, message);
            }
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
            SolutionDemoEventManager.post(new NetworkConnectEvent(type != 0));
        }
    };

    private final IVideoSink mIVideoSink = new IVideoSink() {

        @Override
        public void onFrame(com.ss.bytertc.engine.video.VideoFrame frame) {
            if (mIsTranscoding) {
                return;
            }
            Log.d("IVideoSink", "setCustomVideoPreprocessor processVideoFrame");
            LivePushManager.ins().pushVideoFrame(frame);
        }

        @Override
        public int getRenderElapse() {
            return 0;
        }
    };

    public static LiveRTCManager ins() {
        if (sInstance == null) {
            sInstance = new LiveRTCManager();
        }
        return sInstance;
    }

    public void rtcConnect(RtmInfo rtmInfo) {
        mRTMInfo = rtmInfo;
        initEngine(rtmInfo.appId);
        mRTMClient = new LiveRtmClient(mEngine, rtmInfo);
        mIRTCEngineEventHandler.setBaseClient(mRTMClient);
    }

    public void initEngine(String appId) {
        Log.d(TAG, String.format("createEngine: appId: %s", appId));
        destroyEngine();
        mEngine = RTCEngine.create(Utilities.getApplicationContext(), appId, mIRTCEngineEventHandler);
        mEngine.setLocalVideoMirrorType(MirrorType.MIRROR_TYPE_RENDER_AND_ENCODER);

        VideoCaptureConfig captureConfig = new VideoCaptureConfig(mFrameWidth, mFrameHeight, 15);
        mEngine.setVideoCaptureConfig(captureConfig);

        VideoStreamDescription description = new VideoStreamDescription();
        description.videoSize = new Pair<>(mFrameWidth, mFrameHeight);
        description.frameRate = mFrameRate;
        description.maxKbps = mBitrate;
        mEngine.setVideoEncoderConfig(Collections.singletonList(description));

        AppExecutors.diskIO().execute(() -> LiveRTCManager.ins().initEffect());
    }

    public void destroyEngine() {
        Log.d(TAG, "destroyEngine");
        RTCEngine.destroyEngine(mEngine);
        mIsFront = true;
        mIsMicOn = true;
        mIsCameraOn = true;
        mIsTranscoding = false;
    }

    public LiveRtmClient getRTMClient() {
        return mRTMClient;
    }

    public void clearRTMEventListener() {
        if (mRTMClient != null) {
            mRTMClient.removeEventListener();
        }
    }

    public void initEffect() {
        if (mEngine != null) {
            initEffectPath();
            int licRes = mEngine.checkVideoEffectLicense(Utilities.getApplicationContext(), getLicensePath());
            mEngine.setVideoEffectAlgoModelPath(getEffectAlgoModelPath());
            int enableRes = mEngine.enableVideoEffect(true);

            mEffectPathList.add(getByteComposePath());
            mEffectPathList.add(getByteShapePath());
            LiveRTCManager.ins().setVideoEffectNodes(mEffectPathList);
            setStickerNodes(mLastStickerPath);

            updateVideoEffectNode();

            setVideoEffectColorFilter(mLastFilter);
            updateColorFilterIntensity(mLastFilterValue);
        }
    }

    private String mLastFilter = "";
    private float mLastFilterValue = 0;
    private Map<String, Map<String, Float>> mEffectValue = new HashMap<>();

    public String getByteStickerPath() {
        File stickerPath = new File(Utilities.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath() + "/resource/", "cvlab/StickerResource.bundle");
        return stickerPath.getAbsolutePath() + "/";
    }

    public String getByteComposePath() {
        File composerPath = new File(Utilities.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath() + "/resource/", "cvlab/ComposeMakeup.bundle");
        return composerPath.getAbsolutePath() + "/ComposeMakeup/beauty_Android_live";
    }

    public String getByteShapePath() {
        File composerPath = new File(Utilities.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath() + "/resource/", "cvlab/ComposeMakeup.bundle");
        return composerPath.getAbsolutePath() + "/ComposeMakeup/reshape_live";
    }

    public String getByteColorFilterPath() {
        File filterPath = new File(Utilities.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath() + "/resource/", "cvlab/FilterResource.bundle");
        return filterPath.getAbsolutePath() + "/Filter/";
    }

    public void initEffectPath() {
        File licensePath = new File(getExternalResourcePath(), "cvlab/LicenseBag.bundle");
        if (!licensePath.exists()) {
            Utils.copyAssetFolder(Utilities.getApplicationContext(), "cvlab/LicenseBag.bundle", licensePath.getAbsolutePath());
        }
        File modelPath = new File(getExternalResourcePath(), "cvlab/ModelResource.bundle");
        if (!modelPath.exists()) {
            Utils.copyAssetFolder(Utilities.getApplicationContext(), "cvlab/ModelResource.bundle", modelPath.getAbsolutePath());
        }
        File stickerPath = new File(getExternalResourcePath(), "cvlab/StickerResource.bundle");
        if (!stickerPath.exists()) {
            Utils.copyAssetFolder(Utilities.getApplicationContext(), "cvlab/StickerResource.bundle", stickerPath.getAbsolutePath());
        }
        File filterPath = new File(getExternalResourcePath(), "cvlab/FilterResource.bundle");
        if (!filterPath.exists()) {
            Utils.copyAssetFolder(Utilities.getApplicationContext(), "cvlab/FilterResource.bundle", filterPath.getAbsolutePath());
        }
        File composerPath = new File(getExternalResourcePath(), "cvlab/ComposeMakeup.bundle");
        if (!composerPath.exists()) {
            Utils.copyAssetFolder(Utilities.getApplicationContext(), "cvlab/ComposeMakeup.bundle", composerPath.getAbsolutePath());
        }
    }


    private String getExternalResourcePath() {
        return Utilities.getApplicationContext().getExternalFilesDir("assets").getAbsolutePath() + "/resource/";
    }

    public boolean isCameraOn() {
        return mIsCameraOn;
    }

    public boolean isMicOn() {
        return mIsMicOn;
    }

    public void setVideoEffectNodes(List<String> pathList) {
        if (mEngine != null) {
            mEngine.setVideoEffectNodes(pathList);
        }
    }

    private String mLastStickerPath = "";

    public void setStickerNodes(String path) {
        if (mEngine != null) {
            ArrayList<String> pathList = new ArrayList<>(mEffectPathList);
            if (!TextUtils.isEmpty(path)) {
                pathList.add(getByteStickerPath() + path);
            }
            mEngine.setVideoEffectNodes(pathList);
        }
        mLastStickerPath = path;
    }

    public String getStickerPath() {
        return mLastStickerPath;
    }

    public void updateVideoEffectNode() {
        if (mEngine != null) {
            for (Map.Entry<String, Map<String, Float>> entry : mEffectValue.entrySet()) {
                String path = entry.getKey();
                Map<String, Float> keyValue = entry.getValue();
                for (Map.Entry<String, Float> temp : keyValue.entrySet()) {
                    updateVideoEffectNode(path, temp.getKey(), temp.getValue());
                }
            }
        }
    }

    public void updateVideoEffectNode(String path, String key, float val) {
        if (mEngine != null) {
            mEngine.updateVideoEffectNode(path, key, val);
        }
        Map<String, Float> keyValue = mEffectValue.get(path);
        if (keyValue == null) {
            keyValue = new HashMap<>();
            mEffectValue.put(path, keyValue);
        }
        keyValue.put(key, val);
    }

    public void setVideoEffectColorFilter(String path) {
        if (mEngine != null) {
            mEngine.setVideoEffectColorFilter(path);
        }
        mLastFilter = path;
    }

    public String getLastFilterPath() {
        return mLastFilter;
    }

    public float getLastFilterValue() {
        return mLastFilterValue;
    }

    public void updateColorFilterIntensity(float intensity) {
        if (mEngine != null) {
            mEngine.setVideoEffectColorFilterIntensity(intensity);
        }
        mLastFilterValue = intensity;
    }

    private String getLicensePath() {
        return new File(getExternalResourcePath(), "cvlab/LicenseBag.bundle").getAbsolutePath() +
                "/rtc_test_20210911_20220831_rtc.vertcdemo.android_4.1.0.1.licbag";
    }

    private String getEffectAlgoModelPath() {
        return new File(getExternalResourcePath(), "cvlab/ModelResource.bundle").getAbsolutePath();
    }

    public void joinRoom(String roomId, String userId, String token) {
        Log.d(TAG, String.format("joinRoom: %s %s %s", roomId, userId, token));
        if (mEngine != null) {
            RTCRoomConfig config = new RTCRoomConfig(
                    RTCEngine.ChannelProfile.CHANNEL_PROFILE_COMMUNICATION,
                    true, true, true);
            mEngine.joinRoom(token, roomId, new UserInfo(userId, null), config);
        }
    }

    public void startCaptureVideo(boolean isStart) {
        if (mEngine != null) {
            if (isStart) {
                mEngine.startVideoCapture();
            } else {
                mEngine.stopVideoCapture();
            }
        }
        mIsCameraOn = isStart;
        Log.d(TAG, "startCaptureVideo : " + isStart);
    }

    public void startCaptureAudio(boolean isStart) {
        if (mEngine != null) {
            if (isStart) {
                mEngine.startAudioCapture();
            } else {
                mEngine.stopAudioCapture();
            }
        }
        mIsMicOn = isStart;
        Log.d(TAG, "startCaptureAudio : " + isStart);
    }

    /**
     * 主播结束直播时离房
     */
    public void leaveRoomWhenFinishCoHost() {
        Log.d(TAG, "leaveRoomWhenFinishCoHost");
        if (mEngine != null) {
            mEngine.leaveRoom();
        }
        mIsTranscoding = false;
    }

    /**
     * 其余情况离房，需要重置状态
     */
    public void leaveRoom() {
        Log.d(TAG, "leaveRoom");
        if (mEngine != null) {
            mEngine.setLocalVideoSink(StreamIndex.STREAM_INDEX_MAIN, null, IVideoSink.PixelFormat.I420);
            mEngine.registerAudioFrameObserver(null);
            mEngine.leaveRoom();
            mEngine.stopVideoCapture();
            mEngine.stopAudioCapture();
        }
        mIsFront = true;
        mIsMicOn = true;
        mIsCameraOn = true;
        mIsTranscoding = false;
    }

    public void switchCamera(boolean isFront) {
        if (mEngine != null) {
            mEngine.switchCamera(isFront ? CameraId.CAMERA_ID_FRONT : CameraId.CAMERA_ID_BACK);
            mEngine.setLocalVideoMirrorType(isFront ? MirrorType.MIRROR_TYPE_RENDER_AND_ENCODER : MirrorType.MIRROR_TYPE_NONE);
        }
        mIsFront = isFront;
    }

    public void switchCamera() {
        switchCamera(!mIsFront);
    }

    public void turnOnMic(boolean isMicOn) {
        if (mEngine != null) {
            if (isMicOn) {
                mEngine.startAudioCapture();
            } else {
                mEngine.stopAudioCapture();
            }
        }
        Log.d(TAG, "turnOnMic : " + isMicOn);
        mIsMicOn = isMicOn;
        postMediaStatus();
    }

    public void turnOnMic() {
        turnOnMic(!mIsMicOn);
    }

    public void turnOnCamera(boolean isCameraOn) {
        if (mEngine != null) {
            if (isCameraOn) {
                mEngine.startVideoCapture();
            } else {
                mEngine.stopVideoCapture();
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
        if (mEngine == null) {
            return;
        }
        VideoCanvas videoCanvas = new VideoCanvas(surfaceView, RENDER_MODE_HIDDEN, "", false);
        mEngine.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, null);
        mEngine.setLocalVideoCanvas(StreamIndex.STREAM_INDEX_MAIN, videoCanvas);
        Log.d(TAG, "setLocalVideoView");
    }

    public TextureView getUserRenderView(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        TextureView view = mUidViewMap.get(userId);
        if (view == null) {
            view = new TextureView(Utilities.getApplicationContext());
            mUidViewMap.put(userId, view);
        }
        return view;
    }

    public void setRemoteVideoView(String userId, TextureView textureView) {
        if (mEngine != null) {
            VideoCanvas canvas = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, userId, false);
            mEngine.setRemoteVideoCanvas(userId, StreamIndex.STREAM_INDEX_MAIN, null);
            mEngine.setRemoteVideoCanvas(userId, StreamIndex.STREAM_INDEX_MAIN, canvas);
            Log.d(TAG, "setRemoteVideoView : " + userId);
        }
    }

    public void muteRemoteAudio(String uid, boolean mute) {
        if (mEngine != null) {
            RTCEngine.SubscribeMediaType type = mute ? RTC_SUBSCRIBE_MEDIA_TYPE_VIDEO_ONLY
                    : RTC_SUBSCRIBE_MEDIA_TYPE_AUDIO_AND_VIDEO;
            SubscribeVideoConfig config = new SubscribeVideoConfig(0, REMOTE_USER_PRIORITY_HIGH.value());
            mEngine.subscribeUserStream(uid, StreamIndex.STREAM_INDEX_MAIN, type, config);
        }
    }

    private final IAudioFrameObserver mIAudioProcessor = new IAudioFrameObserver() {
        @Override
        public void onRecordAudioFrame(IAudioFrame audioFrame) {
            LivePushManager.ins().pushAudioFrame(audioFrame);
        }

        @Override
        public void onPlaybackAudioFrame(IAudioFrame audioFrame) {

        }

        @Override
        public void onRemoteUserAudioFrame(RemoteStreamKey stream_info, IAudioFrame audioFrame) {

        }

        @Override
        public void onMixedAudioFrame(IAudioFrame audioFrame) {

        }
    };

    public void startHostLive(String pushUrl) {
        if (mEngine == null) {
            Log.d(TAG, "startHostLive engine is null");
            return;
        }

        stopLiveTranscoding();

        LivePushManager.ins().startPush(pushUrl, mFrameWidth, mFrameHeight, mBitrate);

        mEngine.setLocalVideoSink(StreamIndex.STREAM_INDEX_MAIN, mIVideoSink, IVideoSink.PixelFormat.I420);
        mEngine.registerAudioFrameObserver(mIAudioProcessor);
    }

    private final ILiveTranscodingObserver mILiveTranscodingObserver = new ILiveTranscodingObserver() {
        @Override
        public boolean isSupportClientPushStream() {
            return mIsClientTranscoding;
        }

        @Override
        public void onStreamMixingEvent(ByteRTCStreamMixingEvent eventType, String taskId, ByteRTCTranscoderErrorCode error, ByteRTCStreamMixingType mixType) {

        }

        @Override
        public void onMixingAudioFrame(String taskId, byte[] audioFrame, int frameNum) {
            LivePushManager.ins().pushAudioFrame(audioFrame);
        }

        @Override
        public void onMixingVideoFrame(String taskId, VideoFrame videoFrame) {
            LivePushManager.ins().pushVideoFrame(videoFrame);
        }

        @Override
        public void onDataFrame(String taskId, byte[] dataFrame, long time) {

        }
    };

    public void startLiveTranscoding(String url, List<LiveUserInfo> users, String rtcRoomId, boolean isCoHost) {
        mIsTranscoding = true;
        if (mEngine == null) {
            Log.d(TAG, "starLiveTranscoding engine is null");
            return;
        }
        Log.d(TAG, String.format("startLiveTranscoding %b, pushUrl : %s", mIsClientTranscoding, url));
        if (!mIsClientTranscoding) {
            LivePushManager.ins().stopPush();
        }
        mLiveTranscoding = LiveTranscoding.getDefualtLiveTranscode();
        mLiveTranscoding.setUrl(url);
        mLiveTranscoding.setMixType(mIsClientTranscoding ? ByteRTCStreamMixingType.STREAM_MIXING_BY_CLIENT
                : ByteRTCStreamMixingType.STREAM_MIXING_BY_SERVER); //1是客户端合流
        LiveTranscoding.VideoConfig videoConfig = mLiveTranscoding.getVideo();
        users = sortUserList(users);
        if (isCoHost) {
            LiveUserInfo localUserInfo = users.get(0);
            LiveUserInfo remoteUserInfo = users.get(1);
            VideoStreamDescription description = new VideoStreamDescription();
            description.frameRate = 15;
            description.maxKbps = 1600;
            if (mIsClientTranscoding) {
                description.videoSize = new Pair<>(remoteUserInfo.getWidth() / 2, remoteUserInfo.getHeight() / 2);
            } else {
                int width = Math.max(localUserInfo.getWidth(), remoteUserInfo.getWidth()) / 2;
                int height = Math.max(localUserInfo.getHeight(), remoteUserInfo.getHeight()) / 2;
                description.videoSize = new Pair<>(width, height);
            }
            mEngine.setVideoEncoderConfig(Collections.singletonList(description));
        }
        videoConfig.setWidth(mFrameWidth);
        videoConfig.setHeight(mFrameHeight);
        videoConfig.setFps(mFrameRate);
        videoConfig.setKBitRate(mBitrate);
        LiveTranscoding.AudioConfig audioConfig = mLiveTranscoding.getAudio();
        audioConfig.setSampleRate(44100);
        audioConfig.setChannels(2);
        mLiveTranscoding.setAudio(audioConfig);
        LiveTranscoding.Layout.Builder builder = new LiveTranscoding.Layout.Builder();
        if (isCoHost) {
            for (int i = 0; i < users.size(); i++) {
                LiveUserInfo info = users.get(i);
                LiveTranscoding.Region region = new LiveTranscoding.Region();
                region.uid(info.userId);
                region.roomId(rtcRoomId);
                region.setLocalUser(TextUtils.equals(info.userId, SolutionDataManager.ins().getUserId()));
                region.position(i * 0.5, 0.25);
                region.size(0.5, 0.5);
                region.alpha(1);
                region.renderMode(LiveTranscoding.TranscoderRenderMode.RENDER_HIDDEN);
                builder.addRegion(region);
            }
        } else {
            for (int i = 0; i < users.size(); i++) {
                LiveUserInfo info = users.get(i);
                LiveTranscoding.Region region = new LiveTranscoding.Region();
                region.uid(info.userId);
                if (i == 0) {
                    region.position(0, 0);
                    region.size(1, 1);
                    region.zorder(0);
                } else {
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
                    region.position(regionX, regionY);
                    region.size(regionWidth, regionHeight);
                    region.zorder(1);
                }
                region.setLocalUser(TextUtils.equals(info.userId, SolutionDataManager.ins().getUserId()));
                region.alpha(1);
                region.roomId(rtcRoomId);
                region.renderMode(LiveTranscoding.TranscoderRenderMode.RENDER_HIDDEN);
                builder.addRegion(region);
            }
        }
        LiveTranscoding.Layout layout = builder.builder();
        layout.setBackgroundColor("#272E3B");
        mLiveTranscoding.setLayout(layout);
        mLiveTranscoding.setRoomId(rtcRoomId);
        mEngine.startLiveTranscoding("", mLiveTranscoding, mILiveTranscodingObserver);
    }

    public void updateLiveTranscoding(String url, List<LiveUserInfo> users, String rtcRoomId, boolean isCoHost) {
        mIsTranscoding = true;
        if (mEngine == null) {
            Log.d(TAG, "updateLiveTranscoding engine is null");
            return;
        }
        Log.d(TAG, "updateLiveTranscoding pushUrl : " + url);
        users = sortUserList(users);
        LiveTranscoding.Layout.Builder builder = new LiveTranscoding.Layout.Builder();
        if (isCoHost) {
            for (int i = 0; i < users.size(); i++) {
                LiveUserInfo info = users.get(i);
                boolean isSelf = TextUtils.equals(info.userId, SolutionDataManager.ins().getUserId());
                LiveTranscoding.Region region = new LiveTranscoding.Region();
                region.uid(info.userId);
                region.roomId(rtcRoomId);
                region.position(i * 0.5, 0.25);
                region.size(0.5, 0.5);
                region.alpha(1);
                region.setLocalUser(isSelf);
                region.renderMode(LiveTranscoding.TranscoderRenderMode.RENDER_HIDDEN);
                builder.addRegion(region);
            }
        } else {
            for (int i = 0; i < users.size(); i++) {
                LiveUserInfo info = users.get(i);
                LiveTranscoding.Region region = new LiveTranscoding.Region();
                region.uid(info.userId);
                if (i == 0) {
                    region.position(0, 0);
                    region.size(1, 1);
                    region.zorder(0);
                } else {
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
                    region.position(regionX, regionY);
                    region.size(regionWidth, regionHeight);
                    region.zorder(1);
                }
                region.setLocalUser(TextUtils.equals(info.userId, SolutionDataManager.ins().getUserId()));
                region.alpha(1);
                region.roomId(rtcRoomId);
                region.renderMode(LiveTranscoding.TranscoderRenderMode.RENDER_HIDDEN);
                builder.addRegion(region);
            }
        }
        LiveTranscoding.Layout layout = builder.builder();
        layout.setBackgroundColor("#272E3B");
        mLiveTranscoding.setLayout(layout);
        mEngine.updateLiveTranscoding("", mLiveTranscoding);
    }

    private List<LiveUserInfo> sortUserList(List<LiveUserInfo> userInfos) {
        List<LiveUserInfo> userInfoList = new ArrayList<>();
        if (userInfos != null) {
            for (LiveUserInfo info : userInfos) {
                if (info.role == LiveDataManager.USER_ROLE_HOST &&
                        TextUtils.equals(SolutionDataManager.ins().getUserId(), info.userId)) {
                    userInfoList.add(0, info);
                } else {
                    userInfoList.add(info);
                }
            }
        }
        return userInfoList;
    }

    public void stopLiveTranscoding() {
        Log.d(TAG, "stopLiveTranscoding");
        mIsTranscoding = false;
        if (mEngine != null) {
            mEngine.stopLiveTranscoding("");
            VideoStreamDescription description = new VideoStreamDescription();
            description.videoSize = new Pair<>(mFrameWidth, mFrameHeight);
            description.frameRate = mFrameRate;
            description.maxKbps = mBitrate;
            mEngine.setVideoEncoderConfig(Collections.singletonList(description));
        }
    }

    public void setLiveTranscodingType(boolean isClient) {
        mIsClientTranscoding = isClient;
    }

    public boolean getLiveTranscodingType() {
        return mIsClientTranscoding;
    }

    public void setFrameRate(int frameRate) {
        mFrameRate = frameRate;
        updateVideoConfig();
    }

    public void setResolution(int width, int height) {
        mFrameWidth = width;
        mFrameHeight = height;
        updateVideoConfig();
    }

    public void setBitrate(int bitrate) {
        mBitrate = bitrate;
        updateVideoConfig();
    }

    public int getBitrate() {
        return mBitrate;
    }

    public int getFrameRate() {
        return mFrameRate;
    }

    public int getWidth() {
        return mFrameWidth;
    }

    public int getHeight() {
        return mFrameHeight;
    }

    public boolean isLiveTranscoding() {
        return mIsTranscoding;
    }

    private void updateVideoConfig() {
        if (mEngine != null && !mIsTranscoding) {
            VideoStreamDescription description = new VideoStreamDescription();
            description.videoSize = new Pair<>(mFrameWidth, mFrameHeight);
            description.frameRate = mFrameRate;
            description.maxKbps = mBitrate;
            Log.d(TAG, String.format("updateVideoConfig: %d-%d %d %d",
                    mFrameWidth, mFrameHeight, mFrameRate, mBitrate));
            mEngine.setVideoEncoderConfig(Collections.singletonList(description));

            VideoCaptureConfig captureConfig = new VideoCaptureConfig(mFrameWidth, mFrameHeight, mFrameRate);
            mEngine.setVideoCaptureConfig(captureConfig);

            LivePushManager.ins().updateVideoConfig(mFrameWidth, mFrameHeight);
        }
    }

    public void setPlayLiveStreamResolution(@PLAY_LIVE_RESOLUTION String resolution) {
        if (mPlayLiveResolutionSet.contains(resolution)) {
            mPlayLiveResolution = resolution;
            SolutionDemoEventManager.post(new UpdatePullStreamEvent());
        }
    }

    public String getPlayLiveStreamResolution() {
        return mPlayLiveResolution;
    }

    private IRTCRoom mRTMRoom = null;
    private final IRtcRoomEventHandlerAdapter mIRtcRoomEventHandlerAdapter = new IRtcRoomEventHandlerAdapter() {

        @Override
        public void onRoomMessageReceived(String uid, String message) {
            super.onRoomMessageReceived(uid, message);
            Log.e(TAG, "onRoomMessageReceived message:" + message);
            LiveRTCManager manager = LiveRTCManager.ins();
            if (manager != null && manager.mRTMClient != null) {
                manager.mRTMClient.onMessageReceived(uid, message);
            }
        }

        @Override
        public void onUserMessageReceived(String uid, String message) {
            super.onUserMessageReceived(uid, message);
            Log.e(TAG, "onUserMessageReceived message:" + message);
            LiveRTCManager manager = LiveRTCManager.ins();
            if (manager != null && manager.mRTMClient != null) {
                manager.mRTMClient.onMessageReceived(uid, message);
            }
        }
    };

    public void joinRTMRoom(String rtmRoomId, String userId, String token) {
        if (mEngine == null) {
            return;
        }
        mRTMRoom = mEngine.createRoom(rtmRoomId);
        mRTMRoom.setRTCRoomEventHandler(mIRtcRoomEventHandlerAdapter);
        MultiRoomConfig multiRoomConfig = new MultiRoomConfig(
                RTCEngine.ChannelProfile.CHANNEL_PROFILE_COMMUNICATION,
                false, false);
        mRTMRoom.joinRoom(token, new UserInfo(userId, null), multiRoomConfig);
    }

    public void leaveRTMRoom() {
        if (mRTMRoom == null) {
            return;
        }
        mRTMRoom.leaveRoom();
        mRTMRoom.destroy();
    }
}
