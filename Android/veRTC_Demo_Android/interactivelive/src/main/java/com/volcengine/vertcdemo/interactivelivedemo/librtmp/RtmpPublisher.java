package com.volcengine.vertcdemo.interactivelivedemo.librtmp;

public class RtmpPublisher {
    private MediaPublisher mMediaPublisher;
    private int mFrameWidth;
    private int mFrameHeight;
    private String mUrl = null;
    private int mBitrateKbps;

    public void startPublish(String url, int frameWidth, int frameHeight, int bitKbps) {
        Config config = new Config.Builder()
                .setFrameWidth(frameWidth)
                .setFrameHeight(frameHeight)
                .setBitrate(bitKbps * 1024 * 8)
                .build();
        mFrameWidth = frameWidth;
        mFrameHeight = frameHeight;
        mBitrateKbps = bitKbps;
        mUrl = url;
        mMediaPublisher = MediaPublisher.newInstance(config);
        mMediaPublisher.starPublish(url);
    }

    public void updateVideoConfig(int frameWidth, int frameHeight) {
        if (mUrl == null) {
            return;
        }
        if (mFrameHeight != frameHeight || mFrameWidth != frameWidth) {
            Config config = new Config.Builder()
                    .setFrameWidth(frameWidth)
                    .setFrameHeight(frameHeight)
                    .build();

            mFrameWidth = frameWidth;
            mFrameHeight = frameHeight;

            MediaPublisher publisher = MediaPublisher.newInstance(config);
            publisher.starPublish(mUrl);

            MediaPublisher oldPublisher = mMediaPublisher;
            mMediaPublisher = publisher;

            if (oldPublisher != null) {
                oldPublisher.stopPublish();
            }
        }
    }

    public void stopPublish() {
        mMediaPublisher.stopPublish();
        mMediaPublisher.release();
        mMediaPublisher = null;
    }

    public void putVideoData(byte[] data) {
        if (mMediaPublisher != null) {
            mMediaPublisher.putVideoData(data);
        }
    }

    public void putAudioData(byte[] data) {
        if (mMediaPublisher != null) {
            mMediaPublisher.putAudioData(data);
        }
    }
}
