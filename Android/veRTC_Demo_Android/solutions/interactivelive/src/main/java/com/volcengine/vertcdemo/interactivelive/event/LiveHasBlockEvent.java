package com.volcengine.vertcdemo.interactivelive.event;

/**
 * 直播画面是否有色块事件
 */
public class LiveHasBlockEvent {

    public boolean hasBlock;

    public LiveHasBlockEvent(boolean hasBlock) {
        this.hasBlock = hasBlock;
    }
}
