package com.volcengine.vertcdemo.interactivelive.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;
import com.volcengine.vertcdemo.utils.DebounceClickListener;
import com.volcengine.vertcdemo.utils.Utils;

import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.MEDIA_STATUS_ON;

public class AudienceLayout extends FrameLayout {

    private LiveUserInfo mUserInfo;
    private AudienceGroupLayout.OnUserClickListener mOnUserClickListener;
    private ViewGroup mVideoContainer;
    private TextView mNetStatus;
    private TextView mUserName;
    private TextView mNamePrefix;
    private View mNamePrefixBg;

    public AudienceLayout(Context context) {
        super(context);
        initView();
    }

    public AudienceLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AudienceLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_audience_list, this, true);
        mVideoContainer = view.findViewById(R.id.audience_video_view_container);
        mNamePrefixBg = view.findViewById(R.id.audience_background);
        mNetStatus = view.findViewById(R.id.net_status_tv);
        mNamePrefix = view.findViewById(R.id.audience_name_prefix);
        mUserName = view.findViewById(R.id.audience_name);
        bind(null, null);
        setOnClickListener(DebounceClickListener.create(v -> {
            if (mUserInfo != null && mOnUserClickListener != null) {
                mOnUserClickListener.onClick(mUserInfo);
            }
        }));
    }

    /**
     * 用户数据和UI绑定
     * @param userInfo 用户信息
     * @param userClickListener 用户点击事件
     */
    public void bind(LiveUserInfo userInfo, AudienceGroupLayout.OnUserClickListener userClickListener) {
        if (userInfo == null) {
            setNetStatus(null , true);
            mVideoContainer.removeAllViews();
            mNamePrefixBg.setVisibility(GONE);
            mUserName.setText("");
            mNamePrefix.setText("");
        } else {
            if (userInfo.cameraStatus == MEDIA_STATUS_ON) {
                mNamePrefixBg.setVisibility(GONE);
                mNamePrefix.setVisibility(GONE);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                TextureView renderView = LiveRTCManager.ins().getUserRenderView(userInfo.userId);
                Utils.attachViewToViewGroup(mVideoContainer, renderView, params);
                if (TextUtils.equals(userInfo.userId, SolutionDataManager.ins().getUserId())) {
                    LiveRTCManager.ins().setLocalVideoView(renderView);
                } else {
                    LiveRTCManager.ins().setRemoteVideoView(userInfo.userId,userInfo.roomId, renderView);
                }
            } else {
                mVideoContainer.removeAllViews();
                mNamePrefixBg.setVisibility(VISIBLE);
                mNamePrefix.setVisibility(VISIBLE);
                mNamePrefix.setText(TextUtils.isEmpty(userInfo.userName) ? "" : userInfo.userName.substring(0, 1));
            }
            Drawable micRes;
            if (userInfo.micStatus == MEDIA_STATUS_ON) {
                micRes = null;
            } else {
                micRes = getContext().getResources().getDrawable(R.drawable.mic_off_red);
                micRes.setBounds(0, 0, (int) Utilities.dip2Px(10), (int) Utilities.dip2Px(10));
            }
            mUserName.setCompoundDrawables(micRes, null, null, null);
            mUserName.setText(userInfo.userName);
        }
        mUserInfo = userInfo == null ? null : userInfo.getDeepCopy();
        mOnUserClickListener = userClickListener;
    }

    /**
     * 网络状态显示
     * @param uid 用户id
     * @param isGood 网络状态
     */
    public void setNetStatus(String uid, boolean isGood) {
        if (mUserInfo == null || !TextUtils.equals(uid, mUserInfo.userId)) {
            return;
        }
        Drawable res = isGood ? getContext().getResources().getDrawable(R.drawable.net_status_good) :
                getContext().getResources().getDrawable(R.drawable.net_status_bad);
        mNetStatus.setCompoundDrawablesWithIntrinsicBounds(res, null, null, null);
        mNetStatus.setText(isGood ? "网络良好" : "网络卡顿");
    }
}
