package com.volcengine.vertcdemo.interactivelivedemo.common;

import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.MEDIA_STATUS_OFF;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.MEDIA_STATUS_ON;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;

import com.volcengine.vertcdemo.common.BaseDialog;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveResponse;
import com.volcengine.vertcdemo.interactivelivedemo.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelivedemo.core.ViewUtils;

import java.util.Locale;

public class LiveSettingDialog extends BaseDialog implements
        SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private ViewGroup mConfluenceLayout;
    private ViewGroup mVideoSettingLayout;
    private ImageView mSwitchCameraBtn;
    private ImageView mMicBtn;
    private ImageView mCameraBtn;
    private RadioGroup mFrameRateLayout;
    private RadioGroup mResolutionLayout;
    private SeekBar mBitRateSeekBar;
    private TextView mBitRateTv;
    private Switch mTranscodingSwitch;
    private boolean isInLive = false;
    private String mRoomId;
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
        mConfluenceLayout = findViewById(R.id.transcoding_layout);
        mVideoSettingLayout = findViewById(R.id.video_setting_layout);
        mSwitchCameraBtn = findViewById(R.id.switch_camera_iv);
        mSwitchCameraBtn.setOnClickListener(this);
        mMicBtn = findViewById(R.id.mic_iv);
        mMicBtn.setOnClickListener(this);
        mCameraBtn = findViewById(R.id.camera_iv);
        mCameraBtn.setOnClickListener(this);
        mTranscodingSwitch = findViewById(R.id.transcoding_switch);
        mTranscodingSwitch.setChecked(LiveRTCManager.ins().getLiveTranscodingType());
        mTranscodingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LiveRTCManager.ins().setLiveTranscodingType(isChecked);
            }
        });
        updateCameraAndMicView();

        mFrameRateLayout = findViewById(R.id.frame_rate_rg);
        mFrameRateLayout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.frame_rate_15) {
                    LiveRTCManager.ins().setFrameRate(15);
                } else if (checkedId == R.id.frame_rate_20) {
                    LiveRTCManager.ins().setFrameRate(20);
                }
            }
        });
        int frameRateCheckedId;
        if (LiveRTCManager.ins().getFrameRate() == 15) {
            frameRateCheckedId = R.id.frame_rate_15;
        } else {
            frameRateCheckedId = R.id.frame_rate_20;
        }
        mFrameRateLayout.check(frameRateCheckedId);

        mResolutionLayout = findViewById(R.id.resolution_rg);
        mVideoSettingGroup = findViewById(R.id.video_setting_group);
        int rCheckedId;
        if (LiveRTCManager.ins().getWidth() == 540) {
            rCheckedId = R.id.resolution_540;
        } else if (LiveRTCManager.ins().getWidth() == 1080) {
            rCheckedId = R.id.resolution_1080;
        } else {
            rCheckedId = R.id.resolution_720;
        }
        mResolutionLayout.check(rCheckedId);
        mResolutionLayout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
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
                    LiveRTCManager.ins().setResolution(width, height);
                    if (isInLive && !TextUtils.isEmpty(mRoomId)) {
                        LiveRTCManager.ins().getRTMClient().updateResolution(mRoomId, width, height,
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
            }
        });
        mBitRateSeekBar = findViewById(R.id.bit_rate_seekbar);
        mBitRateTv = findViewById(R.id.bit_rate_tv);
        currentBitRate = LiveRTCManager.ins().getBitrate();
        updateMinMaxBitrate(LiveRTCManager.ins().getWidth());
        updateBitRate();
        mBitRateSeekBar.setOnSeekBarChangeListener(this);

        if (isInLive) {
            mVideoSettingLayout.setVisibility(View.VISIBLE);
            mConfluenceLayout.setVisibility(View.GONE);
        } else {
            mVideoSettingLayout.setVisibility(View.GONE);
            mConfluenceLayout.setVisibility(View.VISIBLE);
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
        } else if (currentBitRate > maxBitRate){
            currentBitRate = maxBitRate;
        }

        float progress = 100 * (currentBitRate - minBitRate) * 1f / (maxBitRate - minBitRate);
        mBitRateSeekBar.setProgress((int) progress);
        LiveRTCManager.ins().setBitrate(currentBitRate);
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
            LiveRTCManager.ins().setBitrate(currentBitRate);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.switch_camera_iv) {
            LiveRTCManager.ins().switchCamera();
        } else if (id == R.id.mic_iv) {
            LiveRTCManager.ins().turnOnMic();
            updateMediaStatus();
        } else if (id == R.id.camera_iv) {
            LiveRTCManager.ins().turnOnCamera();
            updateMediaStatus();
        }
        updateCameraAndMicView();
    }

    private void updateMediaStatus() {
        if (isInLive && !TextUtils.isEmpty(mRoomId)) {
            int micStatus = LiveRTCManager.ins().isMicOn() ? MEDIA_STATUS_ON : MEDIA_STATUS_OFF;
            int cameraStatus = LiveRTCManager.ins().isCameraOn() ? MEDIA_STATUS_ON : MEDIA_STATUS_OFF;
            LiveRTCManager.ins().getRTMClient().updateMediaStatus(mRoomId, micStatus, cameraStatus, null);
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
