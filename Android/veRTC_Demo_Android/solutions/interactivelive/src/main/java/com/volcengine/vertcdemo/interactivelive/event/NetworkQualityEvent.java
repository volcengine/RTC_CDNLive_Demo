package com.volcengine.vertcdemo.interactivelive.event;

public class NetworkQualityEvent {

    public String userId;
    /**
     * {@link com.ss.bytertc.engine.handler.IRTCEngineEventHandler.NetworkQuality}
     */
    public int quality;

    public NetworkQualityEvent(String userId, int quality) {
        this.userId = userId;
        this.quality = quality;
    }
}
