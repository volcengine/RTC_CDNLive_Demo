package com.volcengine.vertcdemo.interactivelivedemo.common;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.volcengine.vertcdemo.interactivelive.R;

public class AvatarView extends ConstraintLayout {
    public AvatarView(Context context) {
        super(context);
        initView();
    }

    public AvatarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AvatarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.user_avator_view, this, true);
        setBackgroundResource(R.drawable.button_black_rec_bg);
    }

    public void setUserName(String name) {
        if (TextUtils.isEmpty(name)) {
            ((TextView) findViewById(R.id.live_user_avatar)).setText("");
            ((TextView) findViewById(R.id.live_user_name)).setText("");
            return;
        }
        ((TextView) findViewById(R.id.live_user_avatar)).setText(name.substring(0,1));
        ((TextView) findViewById(R.id.live_user_name)).setText(name);
    }
}
