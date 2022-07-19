package com.volcengine.vertcdemo.interactivelive.feature.createroom.effect;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.volcengine.vertcdemo.common.BaseDialog;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.view.EffectBeautyLayout;
import com.volcengine.vertcdemo.interactivelive.view.EffectFilterLayout;
import com.volcengine.vertcdemo.interactivelive.view.EffectStickerLayout;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EffectDialog extends BaseDialog implements IEffectItemChangedListener{

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private SeekBar mSeekbar;
    private EffectBeautyLayout mBeautyLayout;
    private EffectFilterLayout mFilterLayout;
    private EffectStickerLayout mStickerLayout;

    private final String mExternalResourcePath;
    public static final String[] TAB_NAMES = {"美颜" ,"滤镜" ,"贴纸"};
    private final ArrayList<String> mEffectPathList = new ArrayList<>();

    public EffectDialog(@NonNull Context context) {
        super(context);
        mExternalResourcePath = context.getExternalFilesDir("assets").getAbsolutePath();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.effect_dialog_layout);
        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setDimAmount(0);
        initUI();
    }

    public void initUI() {
        mViewPager = findViewById(R.id.effect_vp);
        TabViewPageAdapter adapter = new TabViewPageAdapter(Arrays.asList(TAB_NAMES), generateTabViews());
        mViewPager.setAdapter(adapter);

        mTabLayout = findViewById(R.id.effect_tab);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (TextUtils.equals(tab.getText(), "贴纸")) {
                    mSeekbar.setVisibility(View.GONE);
                } else if (TextUtils.equals(tab.getText(), "美颜")) {
                    mSeekbar.setProgress(mBeautyLayout.getEffectProgress(mBeautyLayout.getSelectedId()));
                    mSeekbar.setVisibility(mBeautyLayout.getSelectedId() == R.id.no_select ? View.GONE : View.VISIBLE);
                } else if (TextUtils.equals(tab.getText(), "滤镜")) {
                    mSeekbar.setProgress(mFilterLayout.getEffectProgress(mFilterLayout.getSelectedId()));
                    mSeekbar.setVisibility(mFilterLayout.getSelectedId() == R.id.no_select ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mSeekbar = findViewById(R.id.effect_seekbar);
        mSeekbar.setVisibility(mBeautyLayout.getSelectedId() == R.id.no_select ? View.GONE : View.VISIBLE);
        int currentProgress = mBeautyLayout.getEffectProgress(mBeautyLayout.getSelectedId());
        mSeekbar.setProgress(currentProgress);

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int viewId = -1;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                float value = seekBar.getProgress() / 100f;
                View currentView = adapter.getPrimaryItem();
                int tabPos = mTabLayout.getSelectedTabPosition();
                if (tabPos == 0) {
                    EffectBeautyLayout effectBeautyLayout = (EffectBeautyLayout) currentView;
                    viewId = effectBeautyLayout.getSelectedId();
                    if (viewId == R.id.effect_whiten) {
                        LiveRTCManager.ins().updateVideoEffectNode(getByteComposePath(), "whiten", value);
                    } else if (viewId == R.id.effect_smooth) {
                        LiveRTCManager.ins().updateVideoEffectNode(getByteComposePath(), "smooth", value);
                    } else if (viewId == R.id.effect_big_eye) {
                        LiveRTCManager.ins().updateVideoEffectNode(getByteShapePath(), "Internal_Deform_Eye", value);
                    } else if (viewId == R.id.effect_sharp) {
                        LiveRTCManager.ins().updateVideoEffectNode(getByteShapePath(), "Internal_Deform_Overall", value);
                    }
                } else if (tabPos == 1) {
                    viewId = ((EffectFilterLayout) currentView).getSelectedId();
                    if (viewId == R.id.effect_landiao) {
                        LiveRTCManager.ins().setVideoEffectColorFilter(getByteColorFilterPath() + "Filter_47_S5");
                    } else if (viewId == R.id.effect_lengyang) {
                        LiveRTCManager.ins().setVideoEffectColorFilter(getByteColorFilterPath() + "Filter_30_Po8");
                    } else if (viewId == R.id.effect_lianai) {
                        LiveRTCManager.ins().setVideoEffectColorFilter(getByteColorFilterPath() + "Filter_24_Po2");
                    } else if (viewId == R.id.effect_yese ) {
                        LiveRTCManager.ins().setVideoEffectColorFilter(getByteColorFilterPath() + "Filter_35_L3");
                    }
                    LiveRTCManager.ins().updateColorFilterIntensity(viewId == R.id.no_select ? 0 : value);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int tabPos = mTabLayout.getSelectedTabPosition();
                if (tabPos == 0) {
                    EffectBeautyLayout.sSeekBarProgressMap.put(viewId, seekBar.getProgress());
                } else if (tabPos == 1) {
                    EffectFilterLayout.sSeekBarProgressMap.clear();
                    EffectFilterLayout.sSeekBarProgressMap.put(viewId, seekBar.getProgress());
                }
            }
        });
    }

    public List<View> generateTabViews() {
        List<View> mViews = new ArrayList<>();
        for (String tabName : TAB_NAMES) {
            switch (tabName) {
                case "美颜" :
                    mViews.add(mBeautyLayout = new EffectBeautyLayout(getContext(), this));
                    break;
                case "滤镜" :
                    mViews.add(mFilterLayout = new EffectFilterLayout(getContext(), this));
                    break;
                case "贴纸" :
                    mViews.add(mStickerLayout = new EffectStickerLayout(getContext(), this));
                    break;
                default:
            }
        }
        return mViews;
    }

    public String getByteStickerPath() {
        File stickerPath = new File(mExternalResourcePath + "/resource/", "cvlab/StickerResource.bundle");
        return stickerPath.getAbsolutePath()+"/";
    }

    public String getByteComposePath() {
        File composerPath = new File(mExternalResourcePath + "/resource/", "cvlab/ComposeMakeup.bundle");
        return composerPath.getAbsolutePath()+"/ComposeMakeup/beauty_Android_live";
    }

    public String getByteShapePath() {
        File composerPath = new File(mExternalResourcePath+ "/resource/", "cvlab/ComposeMakeup.bundle");
        return composerPath.getAbsolutePath()+"/ComposeMakeup/reshape_live";
    }

    public String getByteColorFilterPath() {
        File filterPath = new File(mExternalResourcePath + "/resource/", "cvlab/FilterResource.bundle");
        return filterPath.getAbsolutePath() + "/Filter/";
    }


    @Override
    public void onChanged(View newItem, View lastItem) {
        if (newItem.getId() == R.id.no_select) {
            mSeekbar.setVisibility(View.GONE);
        } else if (mTabLayout.getSelectedTabPosition() != 2) {
            mSeekbar.setVisibility(View.VISIBLE);
        }

        if (mTabLayout.getSelectedTabPosition() == 0) {
            int currentProgress = mBeautyLayout.getEffectProgress(newItem.getId());
            mSeekbar.setProgress(currentProgress);
            mBeautyLayout.updateStatusByValue();

            if (newItem.getId() == R.id.no_select) {
                LiveRTCManager.ins().updateVideoEffectNode(getByteComposePath(), "whiten", 0);
                LiveRTCManager.ins().updateVideoEffectNode(getByteComposePath(), "smooth", 0);
                LiveRTCManager.ins().updateVideoEffectNode(getByteShapePath(), "Internal_Deform_Eye", 0);
                LiveRTCManager.ins().updateVideoEffectNode(getByteShapePath(), "Internal_Deform_Overall", 0);
            } else {
                for (Map.Entry<Integer, Integer> entry : EffectBeautyLayout.sSeekBarProgressMap.entrySet()) {
                    float value = entry.getValue() == null ? 0 : entry.getValue();
                    int id = newItem.getId();
                    if (id == R.id.effect_whiten) {
                        LiveRTCManager.ins().updateVideoEffectNode(getByteComposePath(), "whiten", value / 100);
                    } else if (id == R.id.effect_smooth) {
                        LiveRTCManager.ins().updateVideoEffectNode(getByteComposePath(), "smooth", value / 100);
                    } else if (id == R.id.effect_big_eye) {
                        LiveRTCManager.ins().updateVideoEffectNode(getByteShapePath(), "Internal_Deform_Eye", value / 100);
                    } else if (id == R.id.effect_sharp) {
                        LiveRTCManager.ins().updateVideoEffectNode(getByteShapePath(), "Internal_Deform_Overall", value / 100);
                    }
                }
            }
        } else if (mTabLayout.getSelectedTabPosition() == 1) {
            int currentProgress = mFilterLayout.getEffectProgress(newItem.getId());
            mSeekbar.setProgress(currentProgress);
            for (Map.Entry<Integer, Integer> entry : EffectFilterLayout.sSeekBarProgressMap.entrySet()) {
                if (entry.getKey() != newItem.getId()) {
                    entry.setValue(0);
                }
            }

            if (newItem.getId() == R.id.effect_landiao) {
                LiveRTCManager.ins().setVideoEffectColorFilter(getByteColorFilterPath() + "Filter_47_S5");
            } else if (newItem.getId() == R.id.effect_lengyang) {
                LiveRTCManager.ins().setVideoEffectColorFilter(getByteColorFilterPath() + "Filter_30_Po8");
            } else if (newItem.getId() == R.id.effect_lianai) {
                LiveRTCManager.ins().setVideoEffectColorFilter(getByteColorFilterPath() + "Filter_24_Po2");
            } else if (newItem.getId() == R.id.effect_yese ) {
                LiveRTCManager.ins().setVideoEffectColorFilter(getByteColorFilterPath() + "Filter_35_L3");
            }
            LiveRTCManager.ins().updateColorFilterIntensity((float)currentProgress / 100);
        } else if (mTabLayout.getSelectedTabPosition() == 2) {
            int id = newItem.getId();
            if (id == R.id.effect_shaonvmanhua) {
                LiveRTCManager.ins().setStickerNodes("shenxiangaoguang");
            } else if (id == R.id.effect_manhuanansheng) {
                LiveRTCManager.ins().setStickerNodes("manhuanansheng");
            } else if (id == R.id.effect_suixingshan) {
                LiveRTCManager.ins().setStickerNodes("suixingshan");
            } else if (id == R.id.effect_fuguyanjing) {
                LiveRTCManager.ins().setStickerNodes("fuguxiangzuanyanjing");
            } else if (id == R.id.no_select) {
                LiveRTCManager.ins().setStickerNodes("");
            }
        }
    }
}
