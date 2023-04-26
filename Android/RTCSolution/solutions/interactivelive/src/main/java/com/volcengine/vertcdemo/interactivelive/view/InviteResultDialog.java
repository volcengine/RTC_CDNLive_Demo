// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.volcengine.vertcdemo.common.SolutionCommonDialog;
import com.volcengine.vertcdemo.interactivelive.R;

import java.lang.ref.WeakReference;

/**
 * 邀请结果对话框
 * 功能：
 * 1.不重复展示
 * 2.自动消失
 */
public final class InviteResultDialog extends SolutionCommonDialog {

    private static final int AUTO_HIDE_DELAY_MS = 4000;
    public static boolean sHasInstanceShowing = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public InviteResultDialog(Context context) {
        super(context);
        setPositiveBtnText(R.string.accept);
        setNegativeBtnText(R.string.decline);
    }

    @Override
    public void show() {
        if (sHasInstanceShowing) {
            return;
        }
        sHasInstanceShowing = true;
        super.show();
        final WeakReference<Dialog> weakReference = new WeakReference<>(InviteResultDialog.this);
        mHandler.postDelayed(() -> {
            Dialog dialog = weakReference.get();
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }, AUTO_HIDE_DELAY_MS);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        sHasInstanceShowing = false;
    }
}
