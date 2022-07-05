package com.volcengine.vertcdemo.interactivelivedemo.core.event;

// 本地踢人广播
public class LocalKickUserEvent {

    public String userId;

    public LocalKickUserEvent(String userId) {
        this.userId = userId;
    }
}
