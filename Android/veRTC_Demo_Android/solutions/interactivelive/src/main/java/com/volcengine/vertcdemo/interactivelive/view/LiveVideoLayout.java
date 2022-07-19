package com.volcengine.vertcdemo.interactivelive.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelive.core.LiveDataManager;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;

import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.MEDIA_STATUS_ON;

public class LiveVideoLayout extends ConstraintLayout {

    private FrameLayout mLocalVideoContainer;
    private FrameLayout mLocalVideoInConnection;
    private FrameLayout mCoHostVideoInConnection;
    private ConstraintLayout mCoHostLayout;
    private ImageView mCoHostAudioIv;
    private AvatarView mCoHostAvatar;
    private TextView mDefaultNetStatusTv;
    private TextView mDefaultMicStatusTv;
    private TextView mLocalNetStatusTv;
    private TextView mCoHostNetStatusTv;
    private TextView mLocalCameraStatusTv;
    private TextView mLocalMicStatusTv;
    private TextView mCoHostCameraStatusTv;
    private TextView mCoHostMicStatusTv;
    private TextView mLocalDefaultHead;
    private TextView mSelfDefaultHead;
    private TextView mCoHostDefaultHead;

    private LiveUserInfo mSelfUserInfo;
    private LiveUserInfo mCoHostUserInfo;
    private boolean mMuteRemoteUser = false;


    public LiveVideoLayout(@NonNull Context context) {
        super(context);
        initUI();
    }

    public LiveVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initUI();
    }

    public LiveVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initUI();
    }

    private void initUI() {
        LayoutInflater.from(getContext()).inflate(R.layout.live_user_video_layout, this, true);
        mLocalVideoContainer = findViewById(R.id.local_video_container);
        mLocalVideoInConnection = findViewById(R.id.local_video_in_connection);
        mCoHostVideoInConnection = findViewById(R.id.guest_video_in_connection);
        mCoHostLayout = findViewById(R.id.co_host_layout);
        mCoHostAudioIv = findViewById(R.id.guest_audio);
        mCoHostAudioIv.setOnClickListener(v -> {
            if (mCoHostUserInfo != null) {
                mMuteRemoteUser = !mMuteRemoteUser;
                mCoHostAudioIv.setImageResource(mMuteRemoteUser ? R.drawable.guest_audio_off : R.drawable.guest_audio_on);
                LiveRTCManager.ins().muteRemoteAudio(mCoHostUserInfo.userId, mMuteRemoteUser);
            }
        });
        mCoHostAvatar = findViewById(R.id.guest_avatar);
        mDefaultNetStatusTv = findViewById(R.id.local_default_network);
        mDefaultMicStatusTv = findViewById(R.id.local_default_mic_status_tv);
        mLocalNetStatusTv = findViewById(R.id.local_net_status_tv);
        mCoHostNetStatusTv = findViewById(R.id.guest_net_status_tv);
        mLocalCameraStatusTv = findViewById(R.id.local_camera_status_tv);
        mLocalMicStatusTv = findViewById(R.id.local_mic_status_tv);
        mCoHostCameraStatusTv = findViewById(R.id.guest_camera_status_tv);
        mCoHostMicStatusTv = findViewById(R.id.guest_mic_status_tv);
        mLocalDefaultHead = findViewById(R.id.local_default_head_tv);
        mSelfDefaultHead = findViewById(R.id.self_default_head_tv);
        mCoHostDefaultHead = findViewById(R.id.co_host_default_head_tv);
    }

    private void setSelfVideoView(TextureView selfView) {
        mLocalVideoContainer.removeAllViews();
        mLocalVideoInConnection.removeAllViews();
        mCoHostVideoInConnection.removeAllViews();

        mCoHostLayout.setVisibility(GONE);
        if (selfView != null) {
            mLocalVideoContainer.addView(selfView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    public void setLiveUserInfo(LiveUserInfo selfUserInfo, LiveUserInfo coHostUserInfo) {
        if (coHostUserInfo == null) {
            mMuteRemoteUser = false;
            if (selfUserInfo != null) {
                mLocalDefaultHead.setText(selfUserInfo.userName.substring(0, 1));
                mDefaultMicStatusTv.setVisibility(selfUserInfo.micStatus == MEDIA_STATUS_ON ? INVISIBLE : VISIBLE);
                if (selfUserInfo.cameraStatus == MEDIA_STATUS_ON) {
                    if (mSelfUserInfo == null
                            || !TextUtils.equals(mSelfUserInfo.userId, selfUserInfo.userId)
                            || mSelfUserInfo.cameraStatus == LiveDataManager.MEDIA_STATUS_OFF
                            || mCoHostUserInfo != null) {
                        TextureView view = LiveRTCManager.ins().getUserRenderView(selfUserInfo.userId);
                        Utilities.removeFromParent(view);
                        if (TextUtils.equals(selfUserInfo.userId, SolutionDataManager.ins().getUserId())) {
                            LiveRTCManager.ins().setLocalVideoView(view);
                        } else {
                            LiveRTCManager.ins().setRemoteVideoView(selfUserInfo.userId, view);
                        }
                        setSelfVideoView(view);
                        mLocalDefaultHead.setVisibility(GONE);
                    }
                } else {
                    mLocalVideoContainer.removeAllViews();
                    mLocalDefaultHead.setVisibility(VISIBLE);
                }
            } else {
                mLocalDefaultHead.setText("");
                mLocalVideoContainer.removeAllViews();
                mLocalVideoInConnection.removeAllViews();
            }
            mCoHostLayout.setVisibility(GONE);
            mLocalVideoInConnection.removeAllViews();
            mCoHostVideoInConnection.removeAllViews();
        } else {
            mCoHostAudioIv.setImageResource(mMuteRemoteUser ? R.drawable.guest_audio_off : R.drawable.guest_audio_on);

            mDefaultNetStatusTv.setVisibility(INVISIBLE);
            mDefaultMicStatusTv.setVisibility(INVISIBLE);

            mSelfDefaultHead.setText(selfUserInfo.userName.substring(0, 1));
            mCoHostDefaultHead.setText(coHostUserInfo.userName.substring(0, 1));
            mCoHostLayout.setVisibility(VISIBLE);
            if (selfUserInfo.isCameraOn() && (mSelfUserInfo == null
                    || mCoHostUserInfo == null
                    || !TextUtils.equals(mSelfUserInfo.userId, selfUserInfo.userId)
                    || !mSelfUserInfo.isCameraOn())) {
                TextureView view = LiveRTCManager.ins().getUserRenderView(selfUserInfo.userId);
                Utilities.removeFromParent(view);
                if (TextUtils.equals(selfUserInfo.userId, SolutionDataManager.ins().getUserId())) {
                    LiveRTCManager.ins().setLocalVideoView(view);
                } else {
                    LiveRTCManager.ins().setRemoteVideoView(coHostUserInfo.userId, view);
                }
                mLocalVideoInConnection.addView(view, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else if (!mSelfUserInfo.isCameraOn()) {
                mLocalVideoInConnection.removeAllViews();
            }

            if (coHostUserInfo.isCameraOn() && (mCoHostUserInfo == null
                    || !TextUtils.equals(mCoHostUserInfo.userId, coHostUserInfo.userId)
                    || !mCoHostUserInfo.isCameraOn())) {
                TextureView coHostView = LiveRTCManager.ins().getUserRenderView(coHostUserInfo.userId);
                Utilities.removeFromParent(coHostView);
                mCoHostVideoInConnection.addView(coHostView, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                LiveRTCManager.ins().setRemoteVideoView(coHostUserInfo.userId, coHostView);
            } else if (!coHostUserInfo.isCameraOn()) {
                mCoHostVideoInConnection.removeAllViews();
            }
            mCoHostAvatar.setUserName(coHostUserInfo.userName);
            mCoHostMicStatusTv.setVisibility(coHostUserInfo.micStatus == 1 ? GONE : VISIBLE);
            mCoHostCameraStatusTv.setVisibility(coHostUserInfo.cameraStatus == 1 ? GONE : VISIBLE);

            mLocalCameraStatusTv.setVisibility(selfUserInfo.cameraStatus == 1 ? GONE : VISIBLE);
            mLocalMicStatusTv.setVisibility(selfUserInfo.micStatus == 1 ? GONE : VISIBLE);

            mSelfDefaultHead.setVisibility(selfUserInfo.cameraStatus == 1 ? GONE : VISIBLE);
            mCoHostDefaultHead.setVisibility(coHostUserInfo.cameraStatus == 1 ? GONE : VISIBLE);
        }

        mSelfUserInfo = selfUserInfo == null ? null : selfUserInfo.getDeepCopy();
        mCoHostUserInfo = coHostUserInfo == null ? null : coHostUserInfo.getDeepCopy();

        if (selfUserInfo != null && coHostUserInfo == null) {
            updateNetStatus(selfUserInfo.userId, true);
        } else {
            mDefaultNetStatusTv.setVisibility(INVISIBLE);
            mLocalDefaultHead.setVisibility(INVISIBLE);
        }
    }

    public void updateNetStatus(String uid, boolean isGood) {
        TextView tv;
        if (mCoHostUserInfo != null && mSelfUserInfo != null) {
            if (TextUtils.equals(uid, mSelfUserInfo.userId)) {
                tv = mLocalNetStatusTv;
            } else if (TextUtils.equals(uid, mCoHostUserInfo.userId)) {
                tv = mCoHostNetStatusTv;
            } else {
                return;
            }
        } else {
            if (mSelfUserInfo != null && TextUtils.equals(uid, mSelfUserInfo.userId)) {
                tv = mDefaultNetStatusTv;
            } else {
                return;
            }
        }

        tv.setVisibility(VISIBLE);
        if (isGood) {
            tv.setText("网络良好");
            tv.setCompoundDrawablesWithIntrinsicBounds(
                    getContext().getResources().getDrawable(R.drawable.net_status_good), null, null, null);
        } else {
            tv.setText("网络卡顿");
            tv.setCompoundDrawablesWithIntrinsicBounds(
                    getContext().getResources().getDrawable(R.drawable.net_status_bad), null, null, null);
        }
    }
}
