package com.volcengine.vertcdemo.interactivelive.view;

import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.MEDIA_STATUS_OFF;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.MEDIA_STATUS_ON;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;

import com.volcengine.vertcdemo.common.BaseDialog;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.bean.LiveResponse;
import com.volcengine.vertcdemo.interactivelive.core.LiveDataManager;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelive.core.ViewUtils;
import com.volcengine.vertcdemo.utils.DebounceClickListener;

import java.util.Locale;

public class LiveSettingDialog extends BaseDialog implements
        SeekBar.OnSeekBarChangeListener {

    private ViewGroup mVideoSettingLayout;
    private ImageView mSwitchCameraBtn;
    private ImageView mMicBtn;
    private ImageView mCameraBtn;
    private RadioGroup mFrameRateLayout;
    private RadioGroup mResolutionLayout;
    private SeekBar mBitRateSeekBar;
    private TextView mBitRateTv;
    private final boolean isInLive;
    private final String mRoomId;
    private Group mVideoSettingGroup;

    private int minBitRate = 800;
    private int maxBitRate = 1200;
    private int currentBitRate = 0;

    public LiveSettingDialog(@NonNull Context context, boolean isInLive, String roomId) {
        super(context);
        this.isInLive = isInLive;
        this.mRoomId = roomId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_setting_dialog);
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        mVideoSettingLayout = findViewById(R.id.video_setting_layout);
        mSwitchCameraBtn = findViewById(R.id.switch_camera_iv);
        mSwitchCameraBtn.setOnClickListener(DebounceClickListener.create(v -> LiveRTCManager.ins().switchCamera()));
        mMicBtn = findViewById(R.id.mic_iv);
        mMicBtn.setOnClickListener(DebounceClickListener.create(v -> {
            boolean turnOn = LiveRTCManager.ins().isMicOn();
            if (turnOn) {
                LiveRTCManager.ins().unPublishAudio();
            } else {
                LiveRTCManager.ins().publishAudio();
            }
            updateMediaStatus();
            updateCameraAndMicView();
        }));
        mCameraBtn = findViewById(R.id.camera_iv);
        mCameraBtn.setOnClickListener(DebounceClickListener.create(v -> {
            LiveRTCManager.ins().turnOnCamera();
            updateMediaStatus();
            updateCameraAndMicView();
        }));
        updateCameraAndMicView();

        mFrameRateLayout = findViewById(R.id.frame_rate_rg);
        mFrameRateLayout.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.frame_rate_15) {
                LiveRTCManager.ins().setFrameRate(LiveDataManager.USER_ROLE_HOST, 15);
            } else if (checkedId == R.id.frame_rate_20) {
                LiveRTCManager.ins().setFrameRate(LiveDataManager.USER_ROLE_HOST, 20);
            }
        });
        int frameRateCheckedId;
        if (LiveRTCManager.ins().getFrameRate(LiveDataManager.USER_ROLE_HOST) == 15) {
            frameRateCheckedId = R.id.frame_rate_15;
        } else {
            frameRateCheckedId = R.id.frame_rate_20;
        }
        mFrameRateLayout.check(frameRateCheckedId);

        mResolutionLayout = findViewById(R.id.resolution_rg);
        mVideoSettingGroup = findViewById(R.id.video_setting_group);
        int rCheckedId;
        if (LiveRTCManager.ins().getWidth(LiveDataManager.USER_ROLE_HOST) == 540) {
            rCheckedId = R.id.resolution_540;
        } else if (LiveRTCManager.ins().getWidth(LiveDataManager.USER_ROLE_HOST) == 1080) {
            rCheckedId = R.id.resolution_1080;
        } else {
            rCheckedId = R.id.resolution_720;
        }
        mResolutionLayout.check(rCheckedId);
        mResolutionLayout.setOnCheckedChangeListener((group, checkedId) -> {
            int width = 0;
            int height = 0;
            if (checkedId == R.id.resolution_540) {
                width = 540;
                height = 960;
            } else if (checkedId == R.id.resolution_720) {
                width = 720;
                height = 1280;
            } else if (checkedId == R.id.resolution_1080) {
                width = 1080;
                height = 1920;
            }
            if (width != 0) {
                updateMinMaxBitrate(width);
                LiveRTCManager.ins().setResolution(LiveDataManager.USER_ROLE_HOST, width, height);
                if (isInLive && !TextUtils.isEmpty(mRoomId)) {
                    LiveRTCManager.ins().getRTSClient().updateResolution(mRoomId, width, height,
                            new IRequestCallback<LiveResponse>() {
                                @Override
                                public void onSuccess(LiveResponse data) {

                                }

                                @Override
                                public void onError(int errorCode, String message) {

                                }
                            });
                }
            }
            updateBitRate();
        });
        mBitRateSeekBar = findViewById(R.id.bit_rate_seekbar);
        mBitRateTv = findViewById(R.id.bit_rate_tv);
        currentBitRate = LiveRTCManager.ins().getBitrate(LiveDataManager.USER_ROLE_HOST);
        updateMinMaxBitrate(LiveRTCManager.ins().getWidth(LiveDataManager.USER_ROLE_HOST));
        updateBitRate();
        mBitRateSeekBar.setOnSeekBarChangeListener(this);

        if (isInLive) {
            mVideoSettingLayout.setVisibility(View.VISIBLE);
        } else {
            mVideoSettingLayout.setVisibility(View.GONE);
            LiveRTCManager.ins().setLiveTranscodingType(LiveRTCManager.ins().getLiveTranscodingType());
        }

        if (LiveRTCManager.ins().isLiveTranscoding()) {
            disableVideoSetting();
        } else {
            enableVideoSetting();
        }
    }

    private void updateBitRate() {
        if (currentBitRate < minBitRate) {
            currentBitRate = minBitRate;
        } else if (currentBitRate > maxBitRate) {
            currentBitRate = maxBitRate;
        }

        float progress = 100 * (currentBitRate - minBitRate) * 1f / (maxBitRate - minBitRate);
        mBitRateSeekBar.setProgress((int) progress);
        LiveRTCManager.ins().setBitrate(LiveDataManager.USER_ROLE_HOST, currentBitRate);
        mBitRateTv.setText(String.format(Locale.US, "%d kbps", currentBitRate));
    }

    private void updateCameraAndMicView() {
        mCameraBtn.setImageResource(LiveRTCManager.ins().isCameraOn() ? R.drawable.camera_on : R.drawable.camera_off_red);
        mMicBtn.setImageResource(LiveRTCManager.ins().isMicOn() ? R.drawable.mic_on : R.drawable.mic_off_red);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            currentBitRate = (int) (minBitRate + (progress / 100f) * (maxBitRate - minBitRate));
            mBitRateTv.setText(String.format(Locale.US, "%d kbps", currentBitRate));
            LiveRTCManager.ins().setBitrate(LiveDataManager.USER_ROLE_HOST, currentBitRate);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void updateMediaStatus() {
        if (isInLive && !TextUtils.isEmpty(mRoomId)) {
            int micStatus = LiveRTCManager.ins().isMicOn() ? MEDIA_STATUS_ON : MEDIA_STATUS_OFF;
            int cameraStatus = LiveRTCManager.ins().isCameraOn() ? MEDIA_STATUS_ON : MEDIA_STATUS_OFF;
            LiveRTCManager.ins().getRTSClient().updateMediaStatus(mRoomId, micStatus, cameraStatus, null);
        }
    }

    private void disableVideoSetting() {
        ViewUtils.setGroupAlpha(mVideoSettingGroup, 0.5f);
        ViewUtils.setGroupEnable(mVideoSettingGroup, false);
    }

    private void enableVideoSetting() {
        ViewUtils.setGroupAlpha(mVideoSettingGroup, 1f);
        ViewUtils.setGroupEnable(mVideoSettingGroup, true);
    }

    private void updateMinMaxBitrate(int currentWidth) {
        if (currentWidth == 540) {
            minBitRate = 500;
            maxBitRate = 1520;
        } else if (currentWidth == 1080) {
            minBitRate = 1000;
            maxBitRate = 3800;
        } else {
            minBitRate = 800;
            maxBitRate = 1900;
        }
    }
}
