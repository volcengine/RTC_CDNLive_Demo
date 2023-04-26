// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.event;

/**
 * 直播画面是否有遮罩事件
 */
public class LiveHasBlockEvent {

    public boolean hasBlock;

    public LiveHasBlockEvent(boolean hasBlock) {
        this.hasBlock = hasBlock;
    }
}
