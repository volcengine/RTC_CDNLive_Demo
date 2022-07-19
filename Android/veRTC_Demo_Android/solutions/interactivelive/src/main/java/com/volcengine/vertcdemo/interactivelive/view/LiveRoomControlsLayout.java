package com.volcengine.vertcdemo.interactivelive.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.core.LiveDataManager;
import com.volcengine.vertcdemo.interactivelive.feature.liveroommain.IMainOption;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LiveRoomControlsLayout extends LinearLayout implements View.OnClickListener {

    private ImageView mGiftBtn;
    private ImageView mCoHostBtn;
    private ImageView mAddAudienceBtn;
    private ImageView mEffectBtn;
    private ImageView mSettingBtn;
    private ImageView mHangUpBtn;

    private IMainOption mainOption;

    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_DISABLE = 1;
    public static final int STATUS_IN_PROCESS = 2;

    @IntDef({STATUS_DISABLE, STATUS_IN_PROCESS, STATUS_NORMAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ButtonStatus {}

    public LiveRoomControlsLayout(Context context) {
        super(context);
        initView();
    }

    public LiveRoomControlsLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LiveRoomControlsLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_live_room_controls, this, true);
        mGiftBtn = findViewById(R.id.live_room_gift);
        mGiftBtn.setOnClickListener(this);
        mCoHostBtn = findViewById(R.id.live_room_pk);
        mCoHostBtn.setOnClickListener(this);
        mAddAudienceBtn = findViewById(R.id.live_room_lianmai);
        mAddAudienceBtn.setOnClickListener(this);
        mEffectBtn = findViewById(R.id.live_room_effect);
        mEffectBtn.setOnClickListener(this);
        mSettingBtn = findViewById(R.id.live_room_setting);
        mSettingBtn.setOnClickListener(this);
        mHangUpBtn = findViewById(R.id.live_room_exit);
        mHangUpBtn.setOnClickListener(this);
    }

    public void setRole(@LiveDataManager.LiveRoleType int role,
                        @LiveDataManager.LiveLinkMicStatus int status) {
        if (role == LiveDataManager.USER_ROLE_HOST) {
            mCoHostBtn.setVisibility(VISIBLE);
            mGiftBtn.setVisibility(GONE);
        } else {
            mCoHostBtn.setVisibility(GONE);
            mGiftBtn.setVisibility(VISIBLE);
        }
        if (role == LiveDataManager.USER_ROLE_HOST || status == LiveDataManager.LINK_MIC_STATUS_AUDIENCE_INTERACTING) {
            mEffectBtn.setVisibility(VISIBLE);
        } else {
            mEffectBtn.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (mainOption == null) {
            return;
        }
        if (v == mGiftBtn) {
            mainOption.onGiftClick();
        } else if (v == mCoHostBtn) {
            mainOption.onCoHostClick();
        } else if (v == mAddAudienceBtn) {
            mainOption.onAddGuestClick();
        } else if (v == mEffectBtn) {
            mainOption.onVideoEffectClick();
        } else if (v == mSettingBtn) {
            mainOption.onSettingClick();
        } else if (v == mHangUpBtn) {
            mainOption.onExitClick();
        }
    }

    public void setMainOption(IMainOption mainOption) {
        this.mainOption = mainOption;
    }

    public void setAddGuestBtnStatus(@ButtonStatus int status) {
        mAddAudienceBtn.setAlpha(1f);
        mAddAudienceBtn.setClickable(true);
        if (status == STATUS_NORMAL) {
            mAddAudienceBtn.setImageResource(R.drawable.icon_live_lianmai);
        } else if (status == STATUS_DISABLE) {
            mAddAudienceBtn.setClickable(true);
            mAddAudienceBtn.setImageResource(R.drawable.icon_live_lianmai_disable);
        } else if (status == STATUS_IN_PROCESS) {
            mAddAudienceBtn.setClickable(false);
            mAddAudienceBtn.setAlpha(0.5f);
        }
    }

    public void setCoHostBtnStatus(@ButtonStatus int status) {
        mCoHostBtn.setAlpha(1f);
        mCoHostBtn.setClickable(true);
        if (status == STATUS_NORMAL) {
            mCoHostBtn.setImageResource(R.drawable.icon_live_pk);
        } else if (status == STATUS_DISABLE) {
            mCoHostBtn.setImageResource(R.drawable.icon_live_pk_disable);
            mCoHostBtn.setClickable(true);
        } else if (status == STATUS_IN_PROCESS) {
            mCoHostBtn.setAlpha(0.5f);
            mCoHostBtn.setClickable(false);
        }
    }
}
