package com.volcengine.vertcdemo.interactivelivedemo.core.event;

import com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager;

/**
 * 本地通知邀请结果的通知
 */
public class InviteAudienceEvent {

    public String userId;
    @LiveDataManager.InviteReply
    public int inviteReply;

    public InviteAudienceEvent(String userId, @LiveDataManager.InviteReply int inviteReply) {
        this.userId = userId;
        this.inviteReply = inviteReply;
    }
}
