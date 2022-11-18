package com.volcengine.vertcdemo.interactivelive.view;

import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.MEDIA_STATUS_OFF;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.MEDIA_STATUS_ON;
import static com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager.RESO1080;
import static com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager.RESO540;
import static com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager.RESO720;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.volcengine.vertcdemo.common.BaseDialog;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.core.LiveDataManager;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelive.event.MediaChangedEvent;
import com.volcengine.vertcdemo.utils.DebounceClickListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AudienceSettingDialog extends BaseDialog {

    private ImageView mBackArrow;
    private ImageView mResolutionBtn;
    private ImageView mSwitchCameraBtn;
    private ImageView mMicBtn;
    private ImageView mCameraBtn;
    private TextView mTitle;
    private ConstraintLayout mSettingItems;
    private RadioGroup mResolutionLayout;
    private @LiveDataManager.LiveUserStatus int mUserStatus;
    private final String mRoomId;


    public AudienceSettingDialog(@NonNull Context context, @LiveDataManager.LiveUserStatus int userStatus, String roomId) {
        super(context);
        mUserStatus = userStatus;
        mRoomId = roomId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_audience_setting_layout);
        super.onCreate(savedInstanceState);

        mBackArrow = findViewById(R.id.back_iv);
        mBackArrow.setOnClickListener(DebounceClickListener.create(this::onClick));
        mTitle = findViewById(R.id.dialog_title);
        mTitle.setOnClickListener(DebounceClickListener.create(this::onClick));
        mTitle.setText("设置");
        mSettingItems = findViewById(R.id.setting_items_layout);
        mSettingItems.setOnClickListener(DebounceClickListener.create(this::onClick));
        mResolutionLayout = findViewById(R.id.resolution_layout);
        setResolutionChecked();
        mResolutionLayout.setOnClickListener(DebounceClickListener.create(this::onClick));
        mResolutionLayout.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.resolution_540) {
                LiveRTCManager.ins().setPlayLiveStreamResolution(RESO540);
            } else if (checkedId == R.id.resolution_720) {
                LiveRTCManager.ins().setPlayLiveStreamResolution(RESO720);
            } else if (checkedId == R.id.resolution_1080) {
                LiveRTCManager.ins().setPlayLiveStreamResolution(RESO1080);
            }
        });
        mResolutionBtn = findViewById(R.id.resolution_iv);
        mResolutionBtn.setOnClickListener(DebounceClickListener.create(this::onClick));
        mSwitchCameraBtn = findViewById(R.id.switch_camera_iv);
        mSwitchCameraBtn.setOnClickListener(DebounceClickListener.create(this::onClick));
        mMicBtn = findViewById(R.id.mic_iv);
        mMicBtn.setOnClickListener(DebounceClickListener.create(this::onClick));
        mCameraBtn = findViewById(R.id.camera_iv);
        mCameraBtn.setOnClickListener(DebounceClickListener.create(this::onClick));
        updateButtonStatusByRole();
    }

    private void setResolutionChecked() {
        String resolution = LiveRTCManager.ins().getPlayLiveStreamResolution();
        if (TextUtils.equals(resolution, RESO540)) {
            ((RadioButton) findViewById(R.id.resolution_540)).setChecked(true);
        } else if (TextUtils.equals(resolution, RESO720)) {
            ((RadioButton) findViewById(R.id.resolution_720)).setChecked(true);
        } else if (TextUtils.equals(resolution, RESO1080)) {
            ((RadioButton) findViewById(R.id.resolution_1080)).setChecked(true);
        }
    }

    private void updateCameraAndMicView(boolean cameraStatus, boolean micCamera) {
        mCameraBtn.setImageResource(cameraStatus ? R.drawable.camera_on : R.drawable.camera_off_red);
        mMicBtn.setImageResource(micCamera ? R.drawable.mic_on : R.drawable.mic_off_red);
    }

    private void updateButtonStatusByRole() {
        if (mUserStatus == LiveDataManager.USER_STATUS_AUDIENCE_INTERACTING) {
            mCameraBtn.setAlpha(1F);
            mCameraBtn.setEnabled(true);
            mMicBtn.setAlpha(1F);
            mMicBtn.setEnabled(true);
            mSwitchCameraBtn.setAlpha(1F);
            mSwitchCameraBtn.setEnabled(true);
            mResolutionBtn.setAlpha(0.5F);
            mResolutionBtn.setEnabled(false);
            updateCameraAndMicView(LiveRTCManager.ins().isCameraOn(), LiveRTCManager.ins().isMicOn());
        } else {
            mCameraBtn.setAlpha(0.5F);
            mCameraBtn.setEnabled(false);
            mMicBtn.setAlpha(0.5F);
            mMicBtn.setEnabled(false);
            mSwitchCameraBtn.setAlpha(0.5F);
            mSwitchCameraBtn.setEnabled(false);
            mResolutionBtn.setAlpha(1F);
            mResolutionBtn.setEnabled(true);
            updateCameraAndMicView(true, true);
        }
    }

    public void onClick(@NonNull View v) {
        if (v == mResolutionBtn) {
            mSettingItems.setVisibility(View.GONE);
            mResolutionLayout.setVisibility(View.VISIBLE);
            mBackArrow.setVisibility(View.VISIBLE);
            mTitle.setText("分辨率");
        } else if (v == mBackArrow) {
            mSettingItems.setVisibility(View.VISIBLE);
            mResolutionLayout.setVisibility(View.GONE);
            mBackArrow.setVisibility(View.GONE);
            mTitle.setText("设置");
        } else if (v == mSwitchCameraBtn) {
            LiveRTCManager.ins().switchCamera();
        } else if (v == mMicBtn) {
            LiveRTCManager.ins().turnOnMic();
            updateMediaStatus();
        } else if (v == mCameraBtn) {
            LiveRTCManager.ins().turnOnCamera();
            updateMediaStatus();
        }

        updateCameraAndMicView(LiveRTCManager.ins().isCameraOn(), LiveRTCManager.ins().isMicOn());
    }

    @Override
    public void show() {
        super.show();
        SolutionDemoEventManager.register(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        SolutionDemoEventManager.unregister(this);
    }

    private void updateMediaStatus() {
        if (!TextUtils.isEmpty(mRoomId)) {
            int micStatus = LiveRTCManager.ins().isMicOn() ? MEDIA_STATUS_ON : MEDIA_STATUS_OFF;
            int cameraStatus = LiveRTCManager.ins().isCameraOn() ? MEDIA_STATUS_ON : MEDIA_STATUS_OFF;
            LiveRTCManager.ins().getRTSClient().updateMediaStatus(mRoomId, micStatus, cameraStatus, null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaChangedEvent(MediaChangedEvent event) {
        if (TextUtils.equals(SolutionDataManager.ins().getUserId(), event.userId)) {
            updateCameraAndMicView(LiveRTCManager.ins().isCameraOn(), LiveRTCManager.ins().isMicOn());
        }
    }
}
