// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizInform;
import com.volcengine.vertcdemo.interactivelive.bean.LiveUserInfo;

/**
 * 主播连线邀请事件
 */
public class AnchorLinkInviteEvent implements RTSBizInform {
    @SerializedName("inviter")
    public LiveUserInfo userInfo;
    @SerializedName("linker_id")
    public String linkerId;
    @SerializedName("extra")
    public String extra;

    @Override
    public String toString() {
        return "AudienceLinkInviteEvent{" +
                "userInfo=" + userInfo +
                ", linkerId='" + linkerId + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }
}
