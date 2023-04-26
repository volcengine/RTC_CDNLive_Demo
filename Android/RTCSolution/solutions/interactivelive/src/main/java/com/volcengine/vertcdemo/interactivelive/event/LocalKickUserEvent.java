// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.event;

/**
 * 本地踢人事件
 */
public class LocalKickUserEvent {

    public String userId;

    public LocalKickUserEvent(String userId) {
        this.userId = userId;
    }
}
