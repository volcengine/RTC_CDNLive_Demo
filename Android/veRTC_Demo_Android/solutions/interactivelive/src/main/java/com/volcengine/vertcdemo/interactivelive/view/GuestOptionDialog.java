package com.volcengine.vertcdemo.interactivelive.view;

import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.MEDIA_STATUS_KEEP;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.MEDIA_STATUS_OFF;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.MEDIA_STATUS_ON;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.volcengine.vertcdemo.common.BaseDialog;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.bean.LiveResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelive.core.LiveDataManager;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelive.event.AudienceLinkFinishEvent;
import com.volcengine.vertcdemo.interactivelive.event.LocalKickUserEvent;
import com.volcengine.vertcdemo.interactivelive.event.MediaChangedEvent;
import com.volcengine.vertcdemo.utils.DebounceClickListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GuestOptionDialog extends BaseDialog {

    private final int mDisableColor = Color.parseColor("#22E5E6EB");
    private final int mEnableColor = Color.parseColor("#FFFFFF");

    private final String mRoomId;
    private final String mHostUserId;
    private final LiveUserInfo mUserInfo;
    private TextView mCloseConnect;
    private TextView mCameraTv;
    private TextView mMicTv;
    private TextView mDismissTv;

    public GuestOptionDialog(@NonNull Context context, LiveUserInfo userInfo, String roomId, String hostUserId) {
        super(context);
        this.mRoomId = roomId;
        this.mUserInfo = userInfo;
        this.mHostUserId = hostUserId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_guest_setting);
        super.onCreate(savedInstanceState);

        mCloseConnect = findViewById(R.id.close_connect_btn);
        mCloseConnect.setOnClickListener(DebounceClickListener.create(v -> LiveRTCManager.ins().getRTSClient()
                .kickAudienceByHost(mRoomId, mHostUserId, mRoomId, mUserInfo.userId,
                        new IRequestCallback<LiveResponse>() {

                            @Override
                            public void onSuccess(LiveResponse data) {
                                dismiss();
                                SolutionDemoEventManager.post(new LocalKickUserEvent(mUserInfo.userId));
                            }

                            @Override
                            public void onError(int errorCode, String message) {

                            }
                        })));
        mCameraTv = findViewById(R.id.camera_btn);
        mCameraTv.setOnClickListener(DebounceClickListener.create(v -> LiveRTCManager.ins().getRTSClient()
                .requestManageGuest(mRoomId, mHostUserId, mRoomId, mUserInfo.userId,
                        MEDIA_STATUS_OFF, MEDIA_STATUS_KEEP,
                        new IRequestCallback<LiveResponse>() {

                            @Override
                            public void onSuccess(LiveResponse data) {
                                mUserInfo.cameraStatus = MEDIA_STATUS_OFF;
                                updateMediaStatus();
                                dismiss();
                            }

                            @Override
                            public void onError(int errorCode, String message) {

                            }
                        })));
        mMicTv = findViewById(R.id.mic_btn);
        mMicTv.setOnClickListener(DebounceClickListener.create(v -> LiveRTCManager.ins().getRTSClient()
                .requestManageGuest(mRoomId, mHostUserId, mRoomId, mUserInfo.userId,
                        MEDIA_STATUS_KEEP, LiveDataManager.MEDIA_STATUS_OFF,
                        new IRequestCallback<LiveResponse>() {

                            @Override
                            public void onSuccess(LiveResponse data) {
                                mUserInfo.cameraStatus = MEDIA_STATUS_ON;
                                updateMediaStatus();
                                dismiss();
                            }

                            @Override
                            public void onError(int errorCode, String message) {

                            }
                        })));
        mDismissTv = findViewById(R.id.dismiss_btn);
        mDismissTv.setOnClickListener(DebounceClickListener.create(v -> dismiss()));
        updateMediaStatus();
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
        if (mUserInfo.cameraStatus == MEDIA_STATUS_ON) {
            mCameraTv.setTextColor(mEnableColor);
        } else {
            mCameraTv.setTextColor(mDisableColor);
        }
        if (mUserInfo.micStatus == MEDIA_STATUS_ON) {
            mMicTv.setTextColor(mEnableColor);
        } else {
            mMicTv.setTextColor(mDisableColor);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaChangedEvent(MediaChangedEvent event) {
        if (TextUtils.equals(event.userId, mUserInfo.userId)) {
            mUserInfo.cameraStatus = event.camera;
            mUserInfo.micStatus = event.mic;
            updateMediaStatus();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkFinishEvent(AudienceLinkFinishEvent event) {
        dismiss();
    }
}
