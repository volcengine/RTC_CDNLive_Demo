package com.volcengine.vertcdemo.interactivelivedemo.feature.createroom;

import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelivedemo.bean.CreateLiveRoomResponse;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveRoomInfo;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelivedemo.common.LiveSettingDialog;
import com.volcengine.vertcdemo.interactivelivedemo.core.LiveConstants;
import com.volcengine.vertcdemo.interactivelivedemo.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelivedemo.feature.createroom.effect.EffectDialog;
import com.volcengine.vertcdemo.interactivelivedemo.feature.liveroommain.LiveRoomMainActivity;

public class CreateLiveRoomActivity extends BaseActivity implements View.OnClickListener {

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
    private String mRTCToken;

    private long mLastClickStartLiveTs = 0;

    private final IRequestCallback<CreateLiveRoomResponse> mCreateLiveRoomCallback = new IRequestCallback<CreateLiveRoomResponse>() {
        @Override
        public void onSuccess(CreateLiveRoomResponse data) {
            mEnableStartLive = true;
            mRoomInfo = data.liveRoomInfo;
            mSelfInfo = data.userInfo;
            mPushUrl = data.streamPushUrl;
            mRTCToken = data.rtmToken;
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
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        super.onGlobalLayoutCompleted();

        mStartLiveBtn = findViewById(R.id.start_live);
        mStartLiveBtn.setOnClickListener(this);
        mBackBtn = findViewById(R.id.exit_create_live);
        mBackBtn.setOnClickListener(this);
        mSwitchCameraBtn = findViewById(R.id.switch_camera_iv);
        mSwitchCameraBtn.setOnClickListener(this);
        mVideoEffectBtn = findViewById(R.id.effect_iv);
        mVideoEffectBtn.setOnClickListener(this);
        mVideoSettingBtn = findViewById(R.id.settings_iv);
        mVideoSettingBtn.setOnClickListener(this);
        FrameLayout mLocalVideoViewContainer = findViewById(R.id.preview_view_container);
        mLocalVideoView = LiveRTCManager.ins().getUserRenderView(SolutionDataManager.ins().getUserId());
        Utilities.removeFromParent(mLocalVideoView);
        mLocalVideoViewContainer.removeAllViews();
        mLocalVideoViewContainer.addView(mLocalVideoView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onClick(View v) {
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

    private void switchCamera() {
        LiveRTCManager.ins().switchCamera();
    }

    private void openVideoEffectDialog() {
        EffectDialog effectDialog = new EffectDialog(this);
        effectDialog.show();
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
        LiveRTCManager.ins().getRTMClient().requestStartLive(mRoomInfo.roomId, new IRequestCallback<LiveUserInfo>() {
            @Override
            public void onSuccess(LiveUserInfo userInfo) {
                LiveRoomMainActivity.startFromCreate(
                        CreateLiveRoomActivity.this, mRoomInfo, mSelfInfo, mPushUrl, mRTCToken);
                finish();
            }

            @Override
            public void onError(int errorCode, String message) {
                showToast("创建直播失败");
            }
        });
    }

    private void requestCreateLiveRoom() {
        LiveRTCManager.ins().getRTMClient().requestCreateLiveRoom(mCreateLiveRoomCallback);
    }

    private void showToast(String toast) {
        SolutionToast.show(toast);
    }
}
