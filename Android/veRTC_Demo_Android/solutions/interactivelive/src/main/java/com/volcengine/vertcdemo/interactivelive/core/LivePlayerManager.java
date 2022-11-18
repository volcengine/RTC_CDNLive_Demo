package com.volcengine.vertcdemo.interactivelive.core;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.text.TextUtils;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.ss.video.rtc.demo.basic_module.utils.Utilities;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

// 封装的ijkPlayer flv播放器
public class LivePlayerManager {

    private static LivePlayerManager mInstance;

    public static LivePlayerManager ins() {
        if (mInstance == null) {
            mInstance = new LivePlayerManager(Utilities.getApplicationContext());
        }
        return mInstance;
    }

    private IMediaPlayer mVEMediaPlayer = null;
    private TextureView mTextureView;
    private Surface mVESurface;
    private String mSourcePath = "";

    private final IMediaPlayer.OnErrorListener mOnErrorListener = (iMediaPlayer, i, i1) -> {
        playLive(mSourcePath);
        return false;
    };

    private LivePlayerManager(Context context) {
        createSurfaceView(context);
    }

    /**
     * 获取用来播放的view
     * @return
     */
    public TextureView getPlayView() {
        return mTextureView;
    }

    public void playLive(String url) {
        if (TextUtils.equals(url, mSourcePath)) {
            return;
        }
        mSourcePath = url;
        load();
    }

    public void stopPull() {
        if (mVEMediaPlayer != null) {
            mVEMediaPlayer.stop();
        }
    }

    private void load() {
        //每次都要重新创建IMediaPlayer
        createPlayer();
        try {
            mVEMediaPlayer.setDataSource(mSourcePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //给mediaPlayer设置视图
        mVEMediaPlayer.setSurface(mVESurface);
        mVEMediaPlayer.prepareAsync();
    }

    private void createPlayer() {
        if (mVEMediaPlayer != null) {
            mVEMediaPlayer.stop();
            mVEMediaPlayer.setDisplay(null);
            mVEMediaPlayer.release();
        }
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);

        mVEMediaPlayer = ijkMediaPlayer;

        mVEMediaPlayer.setOnErrorListener(mOnErrorListener);
    }

    private void createSurfaceView(Context context) {
        //生成一个新的surface view
        mTextureView = new TextureView(context);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                mVESurface = new Surface(surface);
                load();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                surface.release();
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        });
    }
}
