package com.volcengine.vertcdemo.interactivelive.view;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.volcengine.vertcdemo.common.BaseDialog;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.core.LiveDataManager;
import com.volcengine.vertcdemo.interactivelive.event.InviteAudienceEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 观众申请上麦
 */
public class RequestInteractDialog extends BaseDialog implements View.OnClickListener {

    private final long mLastApplyTs;
    private Button mRequestConnectBtn;
    private TextView mWaitHostResponseTv;
    private final RequestInteractClickListener listener;
    private int mInviteStatus = LiveDataManager.INVITE_REPLY_TIMEOUT;

    public RequestInteractDialog(Context context, long lastApplyTs, RequestInteractClickListener listener) {
        super(context);
        mLastApplyTs = lastApplyTs;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_request_interact_layout);
        super.onCreate(savedInstanceState);

        mRequestConnectBtn = findViewById(R.id.request_interact_btn);
        mWaitHostResponseTv = findViewById(R.id.wait_host_response_tv);

        mRequestConnectBtn.setOnClickListener(this);

        updateButton(mLastApplyTs);
    }

    private void updateButton(long lastApplyTs) {
        if (System.currentTimeMillis() - lastApplyTs > 4000) {
            mRequestConnectBtn.setText("发起申请");
            mRequestConnectBtn.setAlpha(1f);
            mRequestConnectBtn.setClickable(true);
        } else {
            mRequestConnectBtn.setText("申请中");
            mRequestConnectBtn.setAlpha(0.5f);
            mRequestConnectBtn.setClickable(false);
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInviteAudienceEvent(InviteAudienceEvent event) {
        if (!TextUtils.equals(event.userId, SolutionDataManager.ins().getUserId())) {
            return;
        }
        updateButton(mLastApplyTs);
        if (event.inviteReply == LiveDataManager.INVITE_REPLY_WAITING) {
            mRequestConnectBtn.setText("申请中");
            mRequestConnectBtn.setAlpha(0.5f);
        } else {
            dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mRequestConnectBtn) {
            listener.onClick();
            dismiss();
        }
    }

    public interface RequestInteractClickListener {
        void onClick();
    }
}
