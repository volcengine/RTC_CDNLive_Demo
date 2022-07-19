package com.volcengine.vertcdemo.interactivelive.view;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.volcengine.vertcdemo.common.BaseDialog;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.bean.LiveResponse;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;

public class GiftDialog extends BaseDialog {

    private final String mRoomId;
    private final IRequestCallback<LiveResponse> mCallback = new IRequestCallback<LiveResponse>() {
        @Override
        public void onSuccess(LiveResponse data) {

        }

        @Override
        public void onError(int errorCode, String message) {
            SolutionToast.show(message);
        }
    };

    public GiftDialog(@NonNull Context context, String roomId) {
        super(context);
        mRoomId = roomId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_gift_layout);
        super.onCreate(savedInstanceState);

        findViewById(R.id.flower_click).setOnClickListener((v) -> sendGift("flower"));
        findViewById(R.id.rocket_click).setOnClickListener((v) -> sendGift("rocket"));
    }

    private void sendGift(String giftType) {
        String userName = SolutionDataManager.ins().getUserName();
        LiveRTCManager.ins().getRTMClient().sendMessage(mRoomId, userName, giftType, mCallback);
        dismiss();
    }
}
