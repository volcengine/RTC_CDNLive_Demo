package com.volcengine.vertcdemo.interactivelive.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelive.feature.createroom.effect.IEffectItemChangedListener;

public class EffectStickerLayout extends LinearLayout implements View.OnClickListener {

    private ImageView mNoSelectedBtn;
    private ImageView mShenxianBtn;
    private ImageView mXiaoxiongBtn;
    private ImageView mSuixingshanBtn;
    private ImageView mFuguyanjingBtn;

    private View mSelectedBtn;
    private IEffectItemChangedListener mEffectItemChangedListener;


    public EffectStickerLayout(Context context, IEffectItemChangedListener listener) {
        super(context);
        this.mEffectItemChangedListener = listener;
        initView();
    }

    public EffectStickerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EffectStickerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.dialog_effect_sticker, this, true);

        mNoSelectedBtn = findViewById(R.id.no_select);
        mNoSelectedBtn.setOnClickListener(this);
        mSelectedBtn = mNoSelectedBtn;
        mShenxianBtn = findViewById(R.id.effect_shaonvmanhua);
        mShenxianBtn.setOnClickListener(this);
        mXiaoxiongBtn = findViewById(R.id.effect_manhuanansheng);
        mXiaoxiongBtn.setOnClickListener(this);
        mSuixingshanBtn = findViewById(R.id.effect_suixingshan);
        mSuixingshanBtn.setOnClickListener(this);
        mFuguyanjingBtn = findViewById(R.id.effect_fuguyanjing);
        mFuguyanjingBtn.setOnClickListener(this);

        setSelectedPath(LiveRTCManager.ins().getStickerPath());
    }

    private void setSelectedPath(String path) {
        if (TextUtils.equals(path, "shenxiangaoguang")) {
            updateUI(mShenxianBtn);
        } else if (TextUtils.equals(path, "meiyouxiaoxiong")) {
            updateUI(mXiaoxiongBtn);
        } else if (TextUtils.equals(path, "suixingshan")) {
            updateUI(mSuixingshanBtn);
        } else if (TextUtils.equals(path, "fuguxiangzuanyanjing")) {
            updateUI(mFuguyanjingBtn);
        } else {
            updateUI(mNoSelectedBtn);
        }
    }

    private void updateUI(View view) {
        if (view.getId() == R.id.no_select) {
            view.setBackgroundResource(R.drawable.effect_btn_selected_bg);
        } else {
            view.setBackgroundResource(R.drawable.effect_selected_rec_bg);
        }
        mSelectedBtn.setBackgroundResource(R.drawable.effect_btn_normal_bg);
        mSelectedBtn = view;
    }

    public int getSelectedId() {
        if (mSelectedBtn != null) {
            return mSelectedBtn.getId();
        }
        return -1;
    }

    @Override
    public void onClick(View v) {
        if (v == mSelectedBtn) {
            return;
        }
        if (v.getId() == R.id.no_select) {
            v.setBackgroundResource(R.drawable.effect_btn_selected_bg);
        } else {
            v.setBackgroundResource(R.drawable.effect_selected_rec_bg);
        }
        mSelectedBtn.setBackgroundResource(R.drawable.effect_btn_normal_bg);
        if (mEffectItemChangedListener != null) {
            mEffectItemChangedListener.onChanged(v, mSelectedBtn);
        }

        mSelectedBtn = v;
    }
}
