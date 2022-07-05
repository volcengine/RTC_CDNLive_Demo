package com.volcengine.vertcdemo.interactivelivedemo.core;

import android.util.Log;

import com.ss.bytertc.engine.utils.IAudioFrame;
import com.volcengine.vertcdemo.interactivelivedemo.librtmp.RtmpPublisher;

import org.webrtc.VideoFrame;
import org.webrtc.YuvHelper;

import java.nio.ByteBuffer;

public class LivePushManager {

    private static final String TAG = "LivePushManager";

    private final RtmpPublisher mMediaPublisher;

    private final ByteBuffer mByteBuffer = ByteBuffer.allocateDirect(1080 * 1920 * 2);
    private ByteBuffer mDesY;
    private ByteBuffer mDesU;
    private ByteBuffer mDesV;
    private byte[] mResult;
    private byte[] mNv12Result;
    private boolean mIsPushing = false;

    private static final LivePushManager mInstance = new LivePushManager();

    private LivePushManager() {
        mMediaPublisher = new RtmpPublisher();
    }

    public static LivePushManager ins() {
        return mInstance;
    }

    public void startPush(String url, int frameWidth, int frameHeight, int bitrateKbps) {
        Log.d(TAG, "startPush: " + url);
        mIsPushing = true;
        mMediaPublisher.startPublish(url, frameWidth, frameHeight, bitrateKbps);
    }

    public void stopPush() {
        Log.d(TAG, "stopPush");
        if (mIsPushing) {
            mMediaPublisher.stopPublish();
        }
        mIsPushing = false;
    }

    public void pushVideoFrame(VideoFrame videoFrame) {
        Log.d(TAG, "pushVideoFrame: webrtc VideoFrame");
        if (mIsPushing) {
            mMediaPublisher.putVideoData(videoFrameTo420PByte(videoFrame));
        }
    }

    public void pushVideoFrame(com.ss.bytertc.engine.video.VideoFrame videoFrame) {
        Log.d(TAG, "pushVideoFrame: rtc VideoFrame");
        if (mIsPushing) {
            mMediaPublisher.putVideoData(yuvFrameTo420PByte(videoFrame));
        }
    }

    public void pushAudioFrame(byte[] audioFrame) {
        Log.d(TAG, "pushAudioFrame");
        if (mIsPushing) {
            mMediaPublisher.putAudioData(audioFrame);
        }
    }

    public void pushAudioFrame(IAudioFrame audioFrame) {
        Log.d(TAG, "pushAudioFrame");
        if (mIsPushing) {
            mMediaPublisher.putAudioData(audioFrame.getDataBuffer().array());
        }
    }

    public byte[] yuvFrameTo420PByte(com.ss.bytertc.engine.video.VideoFrame frame) {
        mByteBuffer.clear();

        int length = frame.getHeight() * frame.getWidth() * 3 / 2;
        if (mResult == null || mResult.length != length) {
            mResult = new byte[length];
        }

        if (mNv12Result == null || mNv12Result.length != length) {
            mNv12Result = new byte[length];
        }

        ByteBuffer y = frame.getPlaneData(0);
        ByteBuffer u = frame.getPlaneData(1);
        ByteBuffer v = frame.getPlaneData(2);
        y.rewind();
        u.rewind();
        v.rewind();

        if (mDesY == null || mDesY.capacity() != y.capacity()) {
            mDesY = ByteBuffer.allocateDirect(y.capacity());
        }
        if (mDesU == null || mDesU.capacity() != u.capacity()) {
            mDesU = ByteBuffer.allocateDirect(u.capacity());
        }
        if (mDesV == null || mDesV.capacity() != v.capacity()) {
            mDesV = ByteBuffer.allocateDirect(v.capacity());
        }
        YuvHelper.I420Rotate(y, frame.getPlaneLineSize(0),
                u, frame.getPlaneLineSize(1),
                v, frame.getPlaneLineSize(2),
                mDesY, frame.getHeight(), mDesU, (frame.getHeight() + 1) / 2,
                mDesV, (frame.getHeight() + 1) / 2,
                frame.getWidth(),
                frame.getHeight(),
                frame.getRotation().value());

        mDesY.rewind();
        mDesU.rewind();
        mDesV.rewind();
        mDesY.get(mResult, 0, frame.getHeight() * frame.getWidth());
        mDesU.get(mResult, frame.getHeight() * frame.getWidth(), frame.getHeight() * frame.getWidth() / 4);
        mDesV.get(mResult, frame.getHeight() * frame.getWidth() * 5 / 4, frame.getHeight() * frame.getWidth() / 4);

        mDesY.clear();
        mDesU.clear();
        mDesV.clear();
        mByteBuffer.clear();

        int width = frame.getWidth();
        int height = frame.getHeight();
        if (frame.getRotation().value() == 90 || frame.getRotation().value() == 270) {
            width = frame.getHeight();
            height = frame.getWidth();
        }

        swapYV12toNV12(mResult, mNv12Result, width, height);
        frame.release();

        return mNv12Result;
    }

    public byte[] videoFrameTo420PByte(org.webrtc.VideoFrame frame) {
        org.webrtc.VideoFrame.Buffer buffer = frame.getBuffer();
        org.webrtc.VideoFrame.I420Buffer buffer420 = buffer.toI420();
        ByteBuffer y = buffer420.getDataY();
        ByteBuffer u = buffer420.getDataU();
        ByteBuffer v = buffer420.getDataV();

        mByteBuffer.clear();

        int length = buffer420.getHeight() * buffer420.getWidth() * 3 / 2;
        if (mResult == null || mResult.length != length) {
            mResult = new byte[length];
        }
        if (mNv12Result == null || mNv12Result.length != length) {
            mNv12Result = new byte[length];
        }

        if (frame.getRotation() == 0) {
            final int width = frame.getBuffer().getWidth();
            final int height = frame.getBuffer().getHeight();

            buffer420.getDataY().position(0);
            ByteBuffer bufferY = buffer420.getDataY();
            ByteBuffer bufferU = buffer420.getDataU();
            ByteBuffer bufferV = buffer420.getDataV();
            bufferY.position(0);
            bufferU.position(0);
            bufferV.position(0);

            YuvHelper.I420Copy(bufferY, buffer420.getStrideY(), bufferU, buffer420.getStrideU(),
                    bufferV, buffer420.getStrideV(), mByteBuffer, width, height);
            mByteBuffer.position(0);
            mByteBuffer.limit(mByteBuffer.capacity());
            mByteBuffer.get(mResult, 0, width * height * 3 / 2);

            swapYV12toNV12(mResult, mNv12Result, width, height);
        } else {
            if (mDesY == null || mDesY.capacity() != y.capacity()) {
                mDesY = ByteBuffer.allocateDirect(y.capacity());
            }
            if (mDesU == null || mDesU.capacity() != u.capacity()) {
                mDesU = ByteBuffer.allocateDirect(u.capacity());
            }
            if (mDesV == null || mDesV.capacity() != v.capacity()) {
                mDesV = ByteBuffer.allocateDirect(v.capacity());
            }
            YuvHelper.I420Rotate(y, buffer420.getStrideY(),
                    u, buffer420.getStrideU(),
                    v, buffer420.getStrideV(),
                    mDesY, buffer420.getHeight(), mDesU, (buffer420.getHeight() + 1) / 2,
                    mDesV, (buffer420.getHeight() + 1) / 2,
                    buffer420.getWidth(),
                    buffer420.getHeight(),
                    frame.getRotation());

            mDesY.rewind();
            mDesU.rewind();
            mDesV.rewind();
            mDesY.get(mResult, 0, buffer420.getHeight() * buffer420.getWidth());
            mDesU.get(mResult, buffer420.getHeight() * buffer420.getWidth(), buffer420.getHeight() * buffer420.getWidth() / 4);
            mDesV.get(mResult, buffer420.getHeight() * buffer420.getWidth() * 5 / 4, buffer420.getHeight() * buffer420.getWidth() / 4);

            mDesY.clear();
            mDesU.clear();
            mDesV.clear();
            mByteBuffer.clear();

            swapYV12toNV12(mResult, mNv12Result, buffer.getHeight(), buffer.getWidth());
        }
        frame.release();
        buffer420.release();

        return mNv12Result;
    }

    void swapYV12toNV12(byte[] yv12bytes, byte[] nv12bytes, int width, int height) {
        int nLenY = width * height;
        int nLenU = nLenY / 4;

        System.arraycopy(yv12bytes, 0, nv12bytes, 0, width * height);
        for (int i = 0; i < nLenU; i++) {
            nv12bytes[nLenY + 2 * i] = yv12bytes[nLenY + i];
            nv12bytes[nLenY + 2 * i + 1] = yv12bytes[nLenY + nLenU + i];
        }
    }

    public void updateVideoConfig(int frameWidth, int frameHeight) {
        mMediaPublisher.updateVideoConfig(frameWidth, frameHeight);
    }
}
