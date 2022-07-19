package com.volcengine.vertcdemo.interactivelive.event;

import com.volcengine.vertcdemo.interactivelive.core.LiveDataManager;

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
