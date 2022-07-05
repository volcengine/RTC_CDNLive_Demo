package com.volcengine.vertcdemo.interactivelivedemo.librtmp;

import android.media.MediaCodec;
import android.util.Log;

import com.laifeng.sopcastsdk.configuration.AudioConfiguration;
import com.laifeng.sopcastsdk.stream.packer.rtmp.RtmpPacker;
import com.laifeng.sopcastsdk.stream.sender.rtmp.RtmpSender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

class MediaPublisher {
    private static final String TAG = "MediaPublisher";

    private Config mConfig;

    private RtmpSender mSender;
    private RtmpPacker mPacker;

    public static final int NAL_SLICE = 1;
    public static final int NAL_SLICE_DPA = 2;
    public static final int NAL_SLICE_DPB = 3;
    public static final int NAL_SLICE_DPC = 4;
    public static final int NAL_SLICE_IDR = 5;
    public static final int NAL_SEI = 6;
    public static final int NAL_SPS = 7;
    public static final int NAL_PPS = 8;
    public static final int NAL_AUD = 9;
    public static final int NAL_FILLER = 12;

    private final LinkedBlockingQueue<Runnable> mQueue = new LinkedBlockingQueue<>();
    private Thread mAudioEncoderThread;

    private MediaEncoder mMediaEncoder;

    public boolean mIsPublish;
    private boolean mAudioLoop;

    public static MediaPublisher newInstance(Config config) {
        return new MediaPublisher(config);
    }

    private MediaPublisher(Config config) {
        this.mConfig = config;

        init();
        initEncoders();
        startEncoder();
    }

    /**
     * 初始化视频采集器，音频采集器，视频编码器，音频编码器
     */
    public void init() {
        mMediaEncoder = MediaEncoder.newInstance(mConfig);
        mSender = new RtmpSender();
        mSender.setSenderListener(new RtmpSender.OnSenderListener() {
            @Override
            public void onConnecting() {
                Log.e(TAG, "sender listener onConnecting");
            }

            @Override
            public void onConnected() {
                Log.e(TAG, "sender listener onConnected");
            }

            @Override
            public void onDisConnected() {
                Log.e(TAG, "sender listener onDisConnected");
            }

            @Override
            public void onPublishFail() {
                Log.e(TAG, "sender listener onPublishFail");
            }

            @Override
            public void onNetGood() {
                Log.e(TAG, "sender listener onNetGood");
            }

            @Override
            public void onNetBad() {
                Log.e(TAG, "sender listener onNetBad");
            }
        });

        mPacker = new RtmpPacker();
        mPacker.initAudioParams(AudioConfiguration.DEFAULT_FREQUENCY, AudioConfiguration.DEFAULT_FREQUENCY * 2, true);
        mPacker.setPacketListener((data, packetType) -> {
            if (mSender != null) {
                mSender.onData(data, packetType);
            }
        });

        setListener();

        mAudioEncoderThread = new Thread("rtmp-publish-thread") {
            @Override
            public void run() {
                while (mAudioLoop && !Thread.interrupted()) {
                    try {
                        Runnable runnable = mQueue.take();
                        int size = mQueue.size();
                        runnable.run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        mAudioLoop = true;
        mAudioEncoderThread.start();
    }

    /**
     * 初始化编码器
     */
    public void initEncoders() {
        try {
            mMediaEncoder.initAudioEncoder(mConfig.audioSampleRate, mConfig.channelConfig, mConfig.bitrate);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "初始化音频编码器失败");
        }

        try {
            mMediaEncoder.initVideoEncoder(mConfig.frameWidth,
                    mConfig.frameHeight, mConfig.fps);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始编码
     */
    public void startEncoder() {
        mMediaEncoder.start();
    }

    /**
     * 发布
     */
    public void starPublish(String pushUrl) {
        if (mIsPublish) {
            return;
        }

        Runnable runnable = () -> {
            mSender.setAddress(pushUrl);
            mSender.setVideoParams(mConfig.frameWidth, mConfig.frameHeight);
            mSender.setAudioParams(mConfig.audioSampleRate, 16, true);
            mSender.connect();
            mSender.start();

            mPacker.start();
            Log.e(TAG, "startPublisher: url:" + pushUrl);
            mIsPublish = true;
        };
        mQueue.add(runnable);
    }


    /**
     * 停止发布
     */
    public void stopPublish() {
        Runnable runnable = () -> {
            mPacker.stop();
            mSender.stop();

            mAudioLoop = false;
            mIsPublish = false;

            stopEncoder();

            mAudioEncoderThread.interrupt();

        };

        mQueue.add(runnable);
    }

    /**
     * 停止编码
     */
    public void stopEncoder() {
        mMediaEncoder.stop();
    }

    /**
     * 释放
     */
    public void release() {
        Log.i(TAG, "release: ");

        mMediaEncoder.release();

        mAudioLoop = false;
        if (mAudioEncoderThread != null) {
            mAudioEncoderThread.interrupt();
        }
    }

    public void putVideoData(byte[] data) {
        if (mIsPublish) {
            mMediaEncoder.putVideoData(data);
        }
    }

    public void putAudioData(byte[] data) {
        if (mIsPublish) {
            mMediaEncoder.putAudioData(data);
        }
    }

    private void setListener() {
        mMediaEncoder.setCallback(new MediaEncoder.Callback() {
            @Override
            public void onVideoData(ByteBuffer bb, MediaCodec.BufferInfo info) {
                mPacker.onVideoData(bb, info);
            }

            @Override
            public void onAudioData(ByteBuffer bb, MediaCodec.BufferInfo info) {
                mPacker.onAudioData(bb, info);
            }
        });
    }
}
