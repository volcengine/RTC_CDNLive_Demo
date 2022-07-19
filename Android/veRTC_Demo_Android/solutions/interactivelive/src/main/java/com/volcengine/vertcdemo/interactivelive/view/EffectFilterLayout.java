package com.volcengine.vertcdemo.interactivelive.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;

import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelive.feature.createroom.effect.IEffectItemChangedListener;

import java.util.HashMap;

public class EffectFilterLayout extends LinearLayout implements View.OnClickListener {

    private View mSelectedBtn;
    private IEffectItemChangedListener mEffectItemChangedListener;

    public static final HashMap<Integer, Integer> sSeekBarProgressMap = new HashMap<>();

    public EffectFilterLayout(Context context, IEffectItemChangedListener listener) {
        super(context);
        this.mEffectItemChangedListener = listener;
        initView();
    }

    public EffectFilterLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EffectFilterLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.dialog_effect_filter, this, true);

        ImageView noSelectedBtn = findViewById(R.id.no_select);
        noSelectedBtn.setOnClickListener(this);
        mSelectedBtn = noSelectedBtn;
        ImageView lanDiaoBtn = findViewById(R.id.effect_landiao);
        lanDiaoBtn.setOnClickListener(this);
        ImageView lengYangBtn = findViewById(R.id.effect_lengyang);
        lengYangBtn.setOnClickListener(this);
        ImageView liaNaiBtn = findViewById(R.id.effect_lianai);
        liaNaiBtn.setOnClickListener(this);
        ImageView yeSeBtn = findViewById(R.id.effect_yese);
        yeSeBtn.setOnClickListener(this);

        if (mLastId == R.id.effect_landiao) {
            updateUI(lanDiaoBtn);
        } else if (mLastId == R.id.effect_lengyang) {
            updateUI(lengYangBtn);
        } else if (mLastId == R.id.effect_lianai) {
            updateUI(liaNaiBtn);
        } else if (mLastId == R.id.effect_yese) {
            updateUI(yeSeBtn);
        } else {
            updateUI(noSelectedBtn);
        }
    }

    public int getSelectedId() {
        return mLastId;
    }

    private void updateUI(View view) {
        if (view.getId() == R.id.no_select) {
            view.setBackgroundResource(R.drawable.effect_btn_selected_bg);
        } else {
            view.setBackgroundResource(R.drawable.effect_selected_rec_bg);
        }
        if (view != mSelectedBtn) {
            mSelectedBtn.setBackgroundResource(R.drawable.effect_btn_normal_bg);
        }
        mSelectedBtn = view;
    }

    private static int mLastId = R.id.no_select;

    @Override
    public void onClick(View v) {
        mLastId = v.getId();
        if (v == mSelectedBtn) {
            return;
        }
        if (v.getId() == R.id.no_select) {
            resetEffect();
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

    public int getEffectProgress(@IdRes int id) {
        if (sSeekBarProgressMap.containsKey(id)) {
            Integer res = sSeekBarProgressMap.get(id);
            return res == null ? 0  : res;
        }
        return 0;
    }


    public void resetEffect() {
        LiveRTCManager.ins().updateColorFilterIntensity(0);
        for (int key : sSeekBarProgressMap.keySet()) {
            sSeekBarProgressMap.put(key, 0);
        }
    }

}
