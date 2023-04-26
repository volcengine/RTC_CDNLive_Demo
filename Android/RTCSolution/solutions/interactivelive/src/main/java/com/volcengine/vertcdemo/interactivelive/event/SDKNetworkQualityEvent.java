// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.event;

import com.ss.bytertc.engine.type.NetworkQualityStats;

/**
 * SDK 网络质量变化事件
 */
public class SDKNetworkQualityEvent {

    public String userId;
    /**
     * {@link NetworkQualityStats}
     */
    public int quality;

    public SDKNetworkQualityEvent(String userId, int quality) {
        this.userId = userId;
        this.quality = quality;
    }
}
