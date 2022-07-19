package com.volcengine.vertcdemo.interactivelive.core;

import static com.ss.bytertc.engine.RTCEngine.RemoteUserPriority.REMOTE_USER_PRIORITY_HIGH;
import static com.ss.bytertc.engine.RTCEngine.SubscribeMediaType.RTC_SUBSCRIBE_MEDIA_TYPE_AUDIO_AND_VIDEO;
import static com.ss.bytertc.engine.RTCEngine.SubscribeMediaType.RTC_SUBSCRIBE_MEDIA_TYPE_VIDEO_ONLY;
import static com.ss.bytertc.engine.VideoCanvas.RENDER_MODE_HIDDEN;
import static com.volcengine.vertcdemo.utils.FileUtils.copyAssetFolder;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.ss.bytertc.engine.IRTCRoom;
import com.ss.bytertc.engine.MultiRoomConfig;
import com.ss.bytertc.engine.RTCEngine;
import com.ss.bytertc.engine.RTCRoomConfig;
import com.ss.bytertc.engine.SubscribeVideoConfig;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.VideoCanvas;
import com.ss.bytertc.engine.VideoStreamDescription;
import com.ss.bytertc.engine.data.CameraId;
import com.ss.bytertc.engine.data.ForwardStreamEventInfo;
import com.ss.bytertc.engine.data.ForwardStreamInfo;
import com.ss.bytertc.engine.data.ForwardStreamStateInfo;
import com.ss.bytertc.engine.data.MirrorType;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.bytertc.engine.live.ByteRTCStreamMixingEvent;
import com.ss.bytertc.engine.live.ByteRTCStreamMixingType;
import com.ss.bytertc.engine.live.ByteRTCTranscoderErrorCode;
import com.ss.bytertc.engine.live.ILiveTranscodingObserver;
import com.ss.bytertc.engine.live.LiveTranscoding;
import com.ss.bytertc.engine.video.VideoCaptureConfig;
import com.ss.video.rtc.demo.basic_module.utils.AppExecutors;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.rtm.RTCEventHandlerWithRTM;
import com.volcengine.vertcdemo.core.net.rtm.RtmInfo;
import com.volcengine.vertcdemo.interactivelive.event.MediaChangedEvent;
import com.volcengine.vertcdemo.interactivelive.event.NetworkConnectEvent;
import com.volcengine.vertcdemo.interactivelive.event.NetworkQualityEvent;
import com.volcengine.vertcdemo.interactivelive.event.UpdatePullStreamEvent;

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

            if (isFirstJoinRoomSuccess(state, extraInfo)) {
                if (mSingleLiveInfo != null) {
                    LiveRTCManager.ins().startLiveTranscoding(roomId, uid, mSingleLiveInfo.pushUrl);
                }
            }
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

        @Override
        public void onUserPublishStream(String uid, RTCEngine.MediaStreamType type) {
            super.onUserPublishStream(uid, type);

            // 主播连麦
            if (mCoHostInfo != null && TextUtils.equals(uid, mCoHostInfo.coHostUserId)) {
                LiveRTCManager.ins().updateLiveTranscodingWithHost(true,
                        mCoHostInfo.pushUrl, mCoHostInfo.selfRoomId, mCoHostInfo.selfUserId,
                        mCoHostInfo.coHostRoomId, mCoHostInfo.coHostUserId);

                TextureView view = LiveRTCManager.ins().getUserRenderView(uid);
                if (view != null) {
                    LiveRTCManager.ins().setRemoteVideoView(uid, view);
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
        }
    };

    private LiveTranscodingInfo mCoHostInfo;
    private LiveTranscodingInfo mSingleLiveInfo;
    private List<String> mAudienceUserIdList;

    private static class LiveTranscodingInfo {
        public String pushUrl;
        public String selfRoomId;
        public String selfUserId;
        public String coHostRoomId;
        public String coHostUserId;

        private LiveTranscodingInfo() {}

        public LiveTranscodingInfo(String pushUrl, String selfRoomId, String selfUserId, String coHostRoomId,
                                   String coHostUserId) {
            this.pushUrl = pushUrl;
            this.selfRoomId = selfRoomId;
            this.selfUserId = selfUserId;
            this.coHostRoomId = coHostRoomId;
            this.coHostUserId = coHostUserId;
        }
    }

    public static LiveRTCManager ins() {
        if (sInstance == null) {
            sInstance = new LiveRTCManager();
        }
        return sInstance;
    }

    public void rtcConnect(RtmInfo rtmInfo) {
        mRTMInfo = rtmInfo;
        initEngine(rtmInfo.appId, rtmInfo.bid);
        mRTMClient = new LiveRtmClient(mEngine, rtmInfo);
        mIRTCEngineEventHandler.setBaseClient(mRTMClient);
    }

    public void initEngine(String appId, String bid) {
        Log.d(TAG, String.format("createEngine: appId: %s", appId));
        destroyEngine();
        mEngine = RTCEngine.create(Utilities.getApplicationContext(), appId, mIRTCEngineEventHandler);
        mEngine.setBusinessId(bid);
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
            copyAssetFolder(Utilities.getApplicationContext(), "cvlab/LicenseBag.bundle", licensePath.getAbsolutePath());
        }
        File modelPath = new File(getExternalResourcePath(), "cvlab/ModelResource.bundle");
        if (!modelPath.exists()) {
            copyAssetFolder(Utilities.getApplicationContext(), "cvlab/ModelResource.bundle", modelPath.getAbsolutePath());
        }
        File stickerPath = new File(getExternalResourcePath(), "cvlab/StickerResource.bundle");
        if (!stickerPath.exists()) {
            copyAssetFolder(Utilities.getApplicationContext(), "cvlab/StickerResource.bundle", stickerPath.getAbsolutePath());
        }
        File filterPath = new File(getExternalResourcePath(), "cvlab/FilterResource.bundle");
        if (!filterPath.exists()) {
            copyAssetFolder(Utilities.getApplicationContext(), "cvlab/FilterResource.bundle", filterPath.getAbsolutePath());
        }
        File composerPath = new File(getExternalResourcePath(), "cvlab/ComposeMakeup.bundle");
        if (!composerPath.exists()) {
            copyAssetFolder(Utilities.getApplicationContext(), "cvlab/ComposeMakeup.bundle", composerPath.getAbsolutePath());
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
     * 其余情况离房，需要重置状态
     */
    public void leaveRoom() {
        Log.d(TAG, "leaveRoom");
        if (mEngine != null) {
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

    private final ILiveTranscodingObserver mILiveTranscodingObserver = new ILiveTranscodingObserver() {
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
        public void onStreamMixingEvent(ByteRTCStreamMixingEvent eventType, String taskId, ByteRTCTranscoderErrorCode error, ByteRTCStreamMixingType mixType) {
            Log.d(TAG, String.format("onStreamMixingEvent: %s %s", eventType, error));
        }

        @Override
        public void onMixingAudioFrame(String taskId, byte[] audioFrame, int frameNum) {

        }

        @Override
        public void onMixingVideoFrame(String taskId, VideoFrame VideoFrame) {

        }

        @Override
        public void onDataFrame(String taskId, byte[] dataFrame, long time) {

        }
    };

    public void setSingleLiveInfo(String roomId, String userId, String pushUrl) {
        mSingleLiveInfo = new LiveTranscodingInfo(pushUrl, roomId, userId, null, null);
    }

    private void startLiveTranscoding(String roomId, String userId, String liveUrl) {
        Log.d(TAG, String.format("startLiveTranscoding: %s  %s  %s", roomId, userId, liveUrl));
        LiveTranscoding liveTranscoding = LiveTranscoding.getDefualtLiveTranscode();
        // 设置房间id
        liveTranscoding.setRoomId(roomId);
        // 设置推流的直播地址
        liveTranscoding.setUrl(liveUrl);
        // 设置合流模式，0 代表服务端合流
        liveTranscoding.setMixType(ByteRTCStreamMixingType.STREAM_MIXING_BY_SERVER);

        // 设置合流视频参数，具体参数根据情况而定
        LiveTranscoding.VideoConfig videoConfig = liveTranscoding.getVideo();
        videoConfig.setWidth(mFrameWidth);
        videoConfig.setHeight(mFrameHeight);
        videoConfig.setFps(mFrameRate);
        videoConfig.setKBitRate(mBitrate);
        liveTranscoding.setVideo(videoConfig);

        // 设置合流音频参数，具体参数根据情况而定
        LiveTranscoding.AudioConfig audioConfig = liveTranscoding.getAudio();
        audioConfig.setSampleRate(44100);
        audioConfig.setChannels(2);
        liveTranscoding.setAudio(audioConfig);

        // 设置合流视频布局参数
        LiveTranscoding.Layout.Builder layoutBuilder = new LiveTranscoding.Layout.Builder();
        LiveTranscoding.Region selfRegion = new LiveTranscoding.Region();
        selfRegion.uid(userId);
        selfRegion.roomId(roomId);
        selfRegion.position(0, 0);
        selfRegion.size(1, 1);
        selfRegion.alpha(1);
        selfRegion.zorder(0);
        selfRegion.renderMode(LiveTranscoding.TranscoderRenderMode.RENDER_HIDDEN);
        layoutBuilder.addRegion(selfRegion);
        liveTranscoding.setLayout(layoutBuilder.builder());

        // 开始服务端合流任务，taskid使用空字符串即可
        mEngine.startLiveTranscoding("", liveTranscoding, mILiveTranscodingObserver);
    }

    /**
     * @param isStart 是否是pk开始
     * @param selfRoomId 房间id
     * @param selfUserId 自己的用户Id
     * @param coHostRoomId 对方房间id
     * @param coHostUserId 对方主播的用户Id
     */
    public void updateLiveTranscodingWithHost(boolean isStart, String liveUrl, String selfRoomId,
                                               String selfUserId, String coHostRoomId, String coHostUserId) {

        mIsTranscoding = isStart;

        LiveTranscoding liveTranscoding = LiveTranscoding.getDefualtLiveTranscode();
        // 设置房间id
        liveTranscoding.setRoomId(selfRoomId);
        // 设置推流的直播地址
        liveTranscoding.setUrl(liveUrl);
        // 设置合流模式，0 代表服务端合流
        liveTranscoding.setMixType(ByteRTCStreamMixingType.STREAM_MIXING_BY_SERVER);

        // 设置合流视频参数，具体参数根据情况而定
        LiveTranscoding.VideoConfig videoConfig = liveTranscoding.getVideo();
        videoConfig.setWidth(mFrameWidth);
        videoConfig.setHeight(mFrameHeight);
        videoConfig.setFps(mFrameRate);
        videoConfig.setKBitRate(mBitrate);
        liveTranscoding.setVideo(videoConfig);

        // 设置合流音频参数，具体参数根据情况而定
        LiveTranscoding.AudioConfig audioConfig = liveTranscoding.getAudio();
        audioConfig.setSampleRate(44100);
        audioConfig.setChannels(2);
        liveTranscoding.setAudio(audioConfig);

        LiveTranscoding.Layout.Builder layoutBuilder = new LiveTranscoding.Layout.Builder();
        if (isStart) {
            LiveTranscoding.Region selfRegion = new LiveTranscoding.Region();
            selfRegion.uid(selfUserId);
            selfRegion.roomId(selfRoomId);
            selfRegion.position(0, 0.25); // 设置用户视频布局的相对位置，取值范围[0, 1]，(0,0)为左上角，根据实际情况调整
            selfRegion.size(0.5, 0.5); // 设置用户视频相对大小，取值范围[0, 1]，根据实际情况调整
            selfRegion.alpha(1); // 设置透明度，取值范围[0, 1]，0代表完全透明
            selfRegion.zorder(0); // 设置用户视频布局在画布中的层级，取值范围为[0 - 100]。0为底层，值越大越上层。
            layoutBuilder.addRegion(selfRegion);

            LiveTranscoding.Region hostRegion = new LiveTranscoding.Region();
            hostRegion.uid(coHostUserId);
            hostRegion.roomId(selfRoomId);
            hostRegion.position(0.5, 0.25);
            hostRegion.size(0.5, 0.5);
            hostRegion.alpha(1);
            hostRegion.zorder(0);
            layoutBuilder.addRegion(hostRegion);
            liveTranscoding.setLayout(layoutBuilder.builder());
        } else {
            LiveTranscoding.Region selfRegion = new LiveTranscoding.Region();
            selfRegion.uid(selfUserId);
            selfRegion.roomId(selfRoomId);
            selfRegion.position(0, 0);
            selfRegion.size(1, 1);
            selfRegion.alpha(1);
            selfRegion.zorder(0);
            layoutBuilder.addRegion(selfRegion);
        }
        liveTranscoding.setLayout(layoutBuilder.builder());

        mEngine.updateLiveTranscoding("", liveTranscoding);
    }

    /**
     * 更新观众连麦的布局
     * @param roomId 房间id
     * @param selfUserId 主播的用户id
     * @param liveUrl 直播的url
     * @param audienceUserIdList 观众id列表，传null意味着结束共享
     */
    public void updateLiveTranscodingWithAudience(String roomId, String selfUserId, String liveUrl,
                                                   List<String> audienceUserIdList) {

        mAudienceUserIdList = audienceUserIdList;

        LiveTranscoding liveTranscoding = LiveTranscoding.getDefualtLiveTranscode();
        liveTranscoding.setRoomId(roomId);
        liveTranscoding.setUrl(liveUrl);
        liveTranscoding.setMixType(ByteRTCStreamMixingType.STREAM_MIXING_BY_SERVER);

        LiveTranscoding.VideoConfig videoConfig = liveTranscoding.getVideo();
        videoConfig.setWidth(mFrameWidth);
        videoConfig.setHeight(mFrameHeight);
        videoConfig.setFps(mFrameRate);
        videoConfig.setKBitRate(mBitrate);
        liveTranscoding.setVideo(videoConfig);

        LiveTranscoding.AudioConfig audioConfig = liveTranscoding.getAudio();
        audioConfig.setSampleRate(44100);
        audioConfig.setChannels(2);
        liveTranscoding.setAudio(audioConfig);

        LiveTranscoding.Layout.Builder layoutBuilder = new LiveTranscoding.Layout.Builder();
        LiveTranscoding.Region selfRegion = new LiveTranscoding.Region();
        selfRegion.uid(selfUserId);
        selfRegion.roomId(roomId);
        selfRegion.position(0, 0);
        selfRegion.size(1, 1);
        selfRegion.alpha(1);
        selfRegion.zorder(0);
        layoutBuilder.addRegion(selfRegion);

        if (audienceUserIdList != null && audienceUserIdList.size() > 1) {
            mIsTranscoding = true;

            for (int i = 1; i < audienceUserIdList.size(); i++) {
                LiveTranscoding.Region region = new LiveTranscoding.Region();
                region.uid(audienceUserIdList.get(i));
                region.roomId(roomId);
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
                region.alpha(1);
                layoutBuilder.addRegion(region);
            }
        } else {
            mIsTranscoding = false;
        }
        liveTranscoding.setLayout(layoutBuilder.builder());

        mEngine.updateLiveTranscoding("", liveTranscoding);
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
    public void startForwardStreamToRooms(String coHostRoomId, String coHostUserId, String token,
                                          String selfRoomId, String selfUserId, String pushUrl) {
        mCoHostInfo = new LiveTranscodingInfo(pushUrl, selfRoomId, selfUserId, coHostRoomId, coHostUserId);

        ForwardStreamInfo forwardStreamInfo = new ForwardStreamInfo(coHostRoomId, token);
        int res = mEngine.startForwardStreamToRooms(Collections.singletonList(forwardStreamInfo));
        Log.d(TAG, "startForwardStreamToRooms: " + res);
    }

    /**
     * 停止自己的视频流推送到其他房间
     */
    public void stopLiveTranscodingWithHost() {
        mCoHostInfo = null;
        mEngine.stopForwardStreamToRooms();
    }
}
