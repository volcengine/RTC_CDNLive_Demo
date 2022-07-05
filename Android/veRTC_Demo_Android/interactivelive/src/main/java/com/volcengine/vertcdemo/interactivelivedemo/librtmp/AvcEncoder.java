package com.volcengine.vertcdemo.interactivelivedemo.librtmp;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

class AvcEncoder {
    private final static String TAG = "AvcEncoder";

    /**
     * decoder timeout time
     */
    private static final int TIMEOUT_USEC = 12000;

    /**
     * 编码器
     */
    private MediaCodec mVideoEncoder;
    int mWidth;
    int mHeight;
    int mFps;

    public boolean mRunning = false;
    private long presentationTimeUs = 0;

    private MediaEncoder.Callback mCallback;
    private Thread mDecoderThread;

    private final LinkedBlockingQueue<byte[]> mQueue = new LinkedBlockingQueue<>();

    @SuppressLint("NewApi")
    public AvcEncoder(int width, int height, int fps, int bitrate) {

        mWidth = width;
        mHeight = height;
        mFps = fps;

        MediaCodecInfo mediaCodecInfo = getMediaCodecInfoByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        int colorFormat = getColorFormat(mediaCodecInfo);
        Log.e(TAG, "AvcEncoder colorFormat:" + colorFormat + ", mediaCodecInfo.getName:" + mediaCodecInfo.getName());
//        int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar;
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);

        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
        mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
        mediaFormat.setInteger("level", MediaCodecInfo.CodecProfileLevel.AVCLevel41); // Level 4.1
        try {
            mVideoEncoder = MediaCodec.createByCodecName(mediaCodecInfo.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mVideoEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    private static int getColorFormat(MediaCodecInfo mediaCodecInfo) {
        MediaCodecInfo.CodecCapabilities codecCapabilities =
                mediaCodecInfo.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC);
        boolean has420P = false;
        boolean has420pp = false;
        boolean hasNv12 = false;
        for (int i = 0; i < codecCapabilities.colorFormats.length; i++) {
            int format = codecCapabilities.colorFormats[i];
            Log.e(TAG, "get color format:" + format);
            if (format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
                has420P = true;
            } else if (format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar) {
                has420pp = true;
            } else if (format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
                hasNv12 = true;
            }
        }

         /*
         if (has420pp) {
             return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar;
         } else if (has420P) {
             return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
         }
          */
        if (hasNv12) {
            return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        } else {
            return MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;
        }
    }

    private static MediaCodecInfo getMediaCodecInfoByType(String mimeType) {
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    @SuppressLint("NewApi")
    private void stopEncoder() {
        try {
            mVideoEncoder.stop();
            mVideoEncoder.release();

            mDecoderThread.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void stopVideoEncode() {
        mRunning = false;
        try {
            stopEncoder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startVideoEncoder() {
        mVideoEncoder.start();
        mDecoderThread = new Thread(() -> {
            mRunning = true;
            byte[] input = null;
            long pts = 0;
            if (presentationTimeUs == 0) {
                presentationTimeUs = System.currentTimeMillis() * 1000;
            }

            while (mRunning) {
                if (mQueue.size() > 0) {
                    input = mQueue.poll();
                }
                if (input != null) {
                    try {
                        ByteBuffer[] inputBuffers = mVideoEncoder.getInputBuffers();
                        ByteBuffer[] outputBuffers = mVideoEncoder.getOutputBuffers();
                        int inputBufferIndex = mVideoEncoder.dequeueInputBuffer(-1);
                        if (inputBufferIndex >= 0) {
                            pts = new Date().getTime() * 1000 - presentationTimeUs;
                            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                            inputBuffer.clear();
                            inputBuffer.put(input);
                            mVideoEncoder.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
                        }

                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                        int outputBufferIndex = mVideoEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                        while (outputBufferIndex >= 0) {
                            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                            if (mCallback != null) {
                                mCallback.onVideoData(outputBuffer, bufferInfo);
                            }
                            mVideoEncoder.releaseOutputBuffer(outputBufferIndex, false);
                            outputBufferIndex = mVideoEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Log.e(TAG, "AvcEncoder got exception, msg:" + t.getMessage(), t);
                    }
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mDecoderThread.start();
    }

    public void addFrame(byte[] data) {
        if (!mRunning) {
            return;
        }

        mQueue.add(data);
    }

    public void setCallback(MediaEncoder.Callback callback) {
        mCallback = callback;
    }
}
