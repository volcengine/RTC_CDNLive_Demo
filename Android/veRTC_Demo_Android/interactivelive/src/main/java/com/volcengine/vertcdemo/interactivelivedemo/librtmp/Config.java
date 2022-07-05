package com.volcengine.vertcdemo.interactivelivedemo.librtmp;

public class Config {
    /**
     * 视频的帧率
     */
    public final int fps;
    /**
     * 视频的最小宽度
     */
    public final int minWidth;
    /**
     * 视频的最大宽度
     */
    public final int maxWidth;
    /**
     * RTMP连接超时时长
     */
    public final int timeOut;
    /**
     * 发布的url
     */
    public final String publishUrl;
    /**
     * 音频的format
     */
    public final int audioSampleRate;
    /**
     * 音频你的channel
     */
    public final int channelConfig;
    /**
     * 视频编码的比特率
     */
    public final int bitrate;

    public int frameWidth;
    public int frameHeight;

    private Config(int fps,
                   int minWidth,
                   int maxWidth,
                   int timeOut,
                   String url,
                   int sampleRate,
                   int channelConfig,
                   int bitrate,
                   int frameWidth,
                   int frameHeight

    ) {
        this.timeOut = timeOut;
        this.fps = fps;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.publishUrl = url;
        this.audioSampleRate = sampleRate;
        this.channelConfig = channelConfig;
        this.bitrate = bitrate;
        this.frameHeight = frameHeight;
        this.frameWidth = frameWidth;
    }

    public static class Builder {
        private int fps;
        private int minWidth;
        private int maxWidth;
        private int timeOut;
        private String url;
        private int frameWidth;
        private int frameHeight;

        private int audioSampleRate = 44100;
        private int audioChannel = 2;

        private int bitrate = 1600 * 1000 * 8;

        public Builder() {
            fps = 30;
            minWidth = 320;
            maxWidth = 720;
            frameWidth = 720;
            frameHeight = 1280;
            timeOut = 1000;
        }

        /**
         * 设置帧率。
         *
         * @param fps 帧率
         * @return {@link Builder}
         */
        public Builder setFps(int fps) {
            this.fps = fps;
            return this;
        }

        /**
         * 设置最大宽度。
         *
         * @param maxWidth 最大宽度
         * @return {@link Builder}
         */
        public Builder setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        /**
         * 设置最小宽度。
         *
         * @param minWidth 最小宽度
         * @return {@link Builder}
         */
        public Builder setMinWidth(int minWidth) {
            this.minWidth = minWidth;
            return this;
        }

        /**
         * 设置发布的url。
         *
         * @param url 发布的url
         * @return {@link Builder}
         */
        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setFrameWidth(int frameWidth) {
            this.frameWidth = frameWidth;
            return this;
        }

        public Builder setFrameHeight(int frameHeight) {
            this.frameHeight = frameHeight;
            return this;
        }

        /**
         * 设置AudioChannel。
         *
         * @param audioChannel audioChannel
         */
        public Builder setAudioChannel(int audioChannel) {
            this.audioChannel = audioChannel;
            return this;
        }

        /**
         * 设置音频采样率
         *
         * @param simpleRate simpleRate
         */
        public Builder setAudioSampleRate(int simpleRate) {
            this.audioSampleRate = simpleRate;
            return this;
        }

        /**
         * 设置编码比特率。
         *
         * @param bitrate 视频编码比特
         */
        public Builder setBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        /**
         * 建造Config
         *
         * @return {@link Config}
         */
        public Config build() {
            Config config = new Config(fps, minWidth, maxWidth, timeOut,
                    url, audioSampleRate, audioChannel, bitrate, frameWidth, frameHeight
            );
            return config;
        }
    }

}
