package com.volcengine.vertcdemo.interactivelive.core;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;

public class ViewUtils {

    public static void setGroupAlpha(@NonNull Group group, @NonNull float alpha) {
        ViewGroup rootView = (ViewGroup) group.getParent();
        for (int id : group.getReferencedIds()) {
            rootView.findViewById(id).setAlpha(alpha);
        }
    }

    public static void setGroupEnable(@NonNull Group group, boolean enable) {
        ViewGroup rootView = (ViewGroup) group.getParent();
        for (int id : group.getReferencedIds()) {
            rootView.findViewById(id).setEnabled(enable);
            rootView.findViewById(id).setClickable(enable);
        }
    }

    public static void handleViewClick(View view) {
        if (view == null) {
            return;
        }

        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500L);
    }
}
