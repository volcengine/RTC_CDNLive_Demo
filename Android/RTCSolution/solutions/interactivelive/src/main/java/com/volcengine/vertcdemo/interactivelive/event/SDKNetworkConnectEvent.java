// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.event;

/**
 * RTC SDK网络连接状态变化事件
 */
public class SDKNetworkConnectEvent {

    public boolean isConnect;

    public SDKNetworkConnectEvent(boolean isConnect) {
        this.isConnect = isConnect;
    }
}
