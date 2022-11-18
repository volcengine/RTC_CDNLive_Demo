package com.volcengine.vertcdemo.interactivelive.feature.createroom;

import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.bean.CreateLiveRoomResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveRoomInfo;
import com.volcengine.vertcdemo.interactivelive.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelive.core.LiveConstants;
import com.volcengine.vertcdemo.interactivelive.core.LiveDataManager;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelive.feature.liveroommain.LiveRoomMainActivity;
import com.volcengine.vertcdemo.interactivelive.view.LiveSettingDialog;
import com.volcengine.vertcdemo.utils.DebounceClickListener;

public class CreateLiveRoomActivity extends BaseActivity {

    private View mBackBtn;
    private View mSwitchCameraBtn;
    private View mVideoEffectBtn;
    private View mVideoSettingBtn;
    private View mStartLiveBtn;
    private TextureView mLocalVideoView;

    private boolean mEnableStartLive = false;
    private LiveRoomInfo mRoomInfo;
    private LiveUserInfo mSelfInfo;
    private String mPushUrl;
    private String mRTMToken;
    private String mRTCRoomId;
    private String mRTCToken;

    private long mLastClickStartLiveTs = 0;

    private final IRequestCallback<CreateLiveRoomResponse> mCreateLiveRoomCallback = new IRequestCallback<CreateLiveRoomResponse>() {
        @Override
        public void onSuccess(CreateLiveRoomResponse data) {
            mEnableStartLive = true;
            mRoomInfo = data.liveRoomInfo;
            mSelfInfo = data.userInfo;
            mPushUrl = data.streamPushUrl;
            mRTMToken = data.rtmToken;
            mRTCRoomId = data.rtcRoomId;
            mRTCToken = data.rtcToken;
            LiveRTCManager.ins().setLocalVideoView(mLocalVideoView);
            LiveRTCManager.ins().startCaptureVideo(true);
            LiveRTCManager.ins().startCaptureAudio(true);
        }

        @Override
        public void onError(int errorCode, String message) {
            showToast(message);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_live_room);
        requestCreateLiveRoom();
        LiveRTCManager.ins().turnOnCamera(true);
        LiveRTCManager.ins().turnOnMic(true);
        LiveRTCManager.ins().switchCamera(true);
        LiveRTCManager.ins().setResolution(LiveDataManager.USER_ROLE_HOST,
                LiveRTCManager.ins().getWidth(LiveDataManager.USER_ROLE_HOST),
                LiveRTCManager.ins().getHeight(LiveDataManager.USER_ROLE_HOST));
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        super.onGlobalLayoutCompleted();

        mStartLiveBtn = findViewById(R.id.start_live);
        mStartLiveBtn.setOnClickListener(DebounceClickListener.create(this::onClick));
        mBackBtn = findViewById(R.id.exit_create_live);
        mBackBtn.setOnClickListener(DebounceClickListener.create(this::onClick));
        mSwitchCameraBtn = findViewById(R.id.switch_camera_iv);
        mSwitchCameraBtn.setOnClickListener(DebounceClickListener.create(this::onClick));
        mVideoEffectBtn = findViewById(R.id.effect_iv);
        mVideoEffectBtn.setOnClickListener(DebounceClickListener.create(this::onClick));
        mVideoSettingBtn = findViewById(R.id.settings_iv);
        mVideoSettingBtn.setOnClickListener(DebounceClickListener.create(this::onClick));
        FrameLayout mLocalVideoViewContainer = findViewById(R.id.preview_view_container);
        mLocalVideoView = LiveRTCManager.ins().getUserRenderView(SolutionDataManager.ins().getUserId());
        Utilities.removeFromParent(mLocalVideoView);
        mLocalVideoViewContainer.removeAllViews();
        mLocalVideoViewContainer.addView(mLocalVideoView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void onClick(@NonNull View v) {
        if (v == mBackBtn) {
            onBackPressed();
        } else if (v == mSwitchCameraBtn) {
            switchCamera();
        } else if (v == mVideoEffectBtn) {
            openVideoEffectDialog();
        } else if (v == mVideoSettingBtn) {
            openVideoVideoSettingDialog();
        } else if (v == mStartLiveBtn) {
            startLive();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LiveRTCManager.ins().startCaptureVideo(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LiveRTCManager.ins().startCaptureVideo(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LiveRTCManager.ins().removeAllUserRenderView();
    }

    @Override
    protected void setupStatusBar() {
        WindowUtils.setLayoutFullScreen(getWindow());
    }

    private void switchCamera() {
        LiveRTCManager.ins().switchCamera();
    }

    private void openVideoEffectDialog() {
        LiveRTCManager.ins().openEffectDialog(this);
    }

    private void openVideoVideoSettingDialog() {
        LiveSettingDialog settingDialog = new LiveSettingDialog(this, false, null);
        settingDialog.show();
    }

    private void startLive() {
        if (!mEnableStartLive) {
            showToast("获取直播信息失败，无法开始直播");
            return;
        }
        long now = System.currentTimeMillis();
        if (now - mLastClickStartLiveTs <= LiveConstants.CLICK_RESET_INTERVAL) {
            return;
        }
        mLastClickStartLiveTs = now;
        LiveRTCManager.ins().getRTSClient().requestStartLive(mRoomInfo.roomId, new IRequestCallback<LiveUserInfo>() {
            @Override
            public void onSuccess(LiveUserInfo userInfo) {
                LiveRoomMainActivity.startFromCreate(CreateLiveRoomActivity.this,
                        mRoomInfo, mSelfInfo, mPushUrl, mRTMToken, mRTCToken, mRTCRoomId);
                finish();
            }

            @Override
            public void onError(int errorCode, String message) {
                showToast("创建直播失败");
            }
        });
    }

    private void requestCreateLiveRoom() {
        LiveRTCManager.ins().getRTSClient().requestCreateLiveRoom(mCreateLiveRoomCallback);
    }

    private void showToast(String toast) {
        SolutionToast.show(toast);
    }
}
