package com.volcengine.vertcdemo.interactivelivedemo.common;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ss.video.rtc.demo.basic_module.ui.CommonDialog;

import java.lang.ref.WeakReference;

public final class InviteResultDialog extends CommonDialog {

    private static final int AUTO_HIDE_DELAY_MS = 4000;
    public static boolean sHasInstanceShowing = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public InviteResultDialog(Context context) {
        super(context);
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
