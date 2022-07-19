package com.volcengine.vertcdemo.interactivelive.event;

/**
 * RTC SDK网络连接事件
 */
public class NetworkConnectEvent {

    public boolean isConnect;

    public NetworkConnectEvent(boolean isConnect) {
        this.isConnect = isConnect;
    }
}
