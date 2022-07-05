package com.volcengine.vertcdemo.interactivelivedemo.librtmp;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import static android.media.MediaFormat.KEY_BIT_RATE;
import static android.media.MediaFormat.KEY_MAX_INPUT_SIZE;

class MediaEncoder {
    private static final String TAG = "MediaEncoder";
    private Config mConfig;

    private AvcEncoder mAvcEncoder;

    private MediaCodec mAudioEncoder;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private Thread mAudioEncoderThread;
    private boolean mAudioEncoderLoop;
    private long mPresentTime;
    private Callback mCallback;
    private LinkedBlockingQueue<byte[]> mAudioQueue;

    public static MediaEncoder newInstance(Config config) {
        return new MediaEncoder(config);
    }

    private MediaEncoder(Config config) {
        this.mConfig = config;
    }

    /**
     * 设置回调
     *
     * @param callback 回调
     */
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    /**
     * 开始
     */
    public void start() {
        startAudioEncode();
        startVideoEncode();
    }

    /**
     * 停止
     */
    public void stop() {
        stopAudioEncode();
        stopVideoEncode();
    }

    /**
     * 释放
     */
    public void release() {
        releaseAudioEncoder();
        releaseVideoEncoder();
    }

    /**
     * 初始化音频编码器
     *
     * @param sampleRate  音频采样率
     * @param chanelCount 声道数
     * @throws IOException 创建编码器失败
     */
    public void initAudioEncoder(int sampleRate, int chanelCount, int bitrate) throws IOException {
        MediaCodec aencoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                sampleRate, chanelCount);
        format.setInteger(KEY_MAX_INPUT_SIZE, 0);
        format.setInteger(KEY_BIT_RATE, 32 * 8);
        aencoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioQueue = new LinkedBlockingQueue<>();
        mAudioEncoder = aencoder;
    }


    /**
     * 初始化视频编码器。
     *
     * @param width  视频的宽
     * @param height 视频的高
     * @throws IOException 创建编码器失败
     */
    public int initVideoEncoder(int width, int height, int fps) throws IOException {
        mAvcEncoder = new AvcEncoder(width, height, fps, mConfig.bitrate);
        return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
    }

    /**
     * 开始视频编码
     */
    public void startVideoEncode() {
        if (mAvcEncoder == null) {
            throw new RuntimeException("请初始化视频编码器");
        }

        mAvcEncoder.setCallback(mCallback);
        mAvcEncoder.startVideoEncoder();
    }

    /**
     * 开始音频编码
     */
    public void startAudioEncode() {
        if (mAudioEncoder == null) {
            throw new RuntimeException("请初始化音频编码器");
        }

        if (mAudioEncoderLoop) {
            throw new RuntimeException("必须先停止");
        }

        mAudioEncoderThread = new Thread() {
            @Override
            public void run() {
                mAudioEncoder.start();
                mPresentTime = System.currentTimeMillis() * 1000;
                while (mAudioEncoderLoop && !Thread.interrupted()) {
                    try {
                        byte[] data = mAudioQueue.take();
                        encodeAudioData(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };

        mAudioEncoderLoop = true;
        mAudioEncoderThread.start();
    }

    /**
     * 添加视频数据
     *
     * @param data
     */
    public void putVideoData(byte[] data) {
        mAvcEncoder.addFrame(data);
    }

    /**
     * 添加音频数据
     *
     * @param data
     */
    public void putAudioData(byte[] data) {
        if (!mAudioEncoderLoop) {
            return;
        }

        try {
            mAudioQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * stop视频编码
     */
    public void stopVideoEncode() {
        mAvcEncoder.stopVideoEncode();
    }

    /**
     * 停止音频编码
     */
    public void stopAudioEncode() {
        mAudioEncoderLoop = false;
        mAudioEncoderThread.interrupt();
        Log.i(TAG, "run: 停止音频编码");

        try {
            mAudioEncoder.stop();
            mAudioEncoder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放视频编码器
     */
    public void releaseVideoEncoder() {
        mAvcEncoder.stopVideoEncode();
    }

    /**
     * 释放音频编码器
     */
    public void releaseAudioEncoder() {
        mAudioEncoder.release();
    }

    /**
     * 音频解码
     *
     * @param data
     */
    private void encodeAudioData(byte[] data) {
        try {
            ByteBuffer[] inputBuffers = mAudioEncoder.getInputBuffers();
            ByteBuffer[] outputBuffers = mAudioEncoder.getOutputBuffers();
            int inputBufferId = mAudioEncoder.dequeueInputBuffer(-1);
            if (inputBufferId >= 0) {
                ByteBuffer bb = inputBuffers[inputBufferId];
                bb.clear();
                bb.put(data, 0, data.length);
                long pts = new Date().getTime() * 1000 - mPresentTime;
                mAudioEncoder.queueInputBuffer(inputBufferId, 0, data.length, pts, 0);
            }

            int outputBufferId = mAudioEncoder.dequeueOutputBuffer(mBufferInfo, 0);
            if (outputBufferId >= 0) {
                ByteBuffer bb = outputBuffers[outputBufferId];
                if (mCallback != null) {
                    mCallback.onAudioData(bb, mBufferInfo);
                }
                mAudioEncoder.releaseOutputBuffer(outputBufferId, false);
            }
        } catch (Exception e) {
        }
    }

    public interface Callback {
        void onVideoData(ByteBuffer bb, MediaCodec.BufferInfo info);

        void onAudioData(ByteBuffer bb, MediaCodec.BufferInfo info);
    }
}
