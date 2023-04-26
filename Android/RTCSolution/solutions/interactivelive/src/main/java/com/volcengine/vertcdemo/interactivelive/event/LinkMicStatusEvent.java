// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizInform;

/**
 * 连线状态更新事件
 */
public class LinkMicStatusEvent implements RTSBizInform {
    @SerializedName("linkmic_status")
    public int linkMicStatus;

    @Override
    public String toString() {
        return "LinkMicStatusEvent{" +
                "linkMicStatus=" + linkMicStatus +
                '}';
    }
}
