package com.volcengine.vertcdemo.interactivelivedemo.core.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveUserInfo;

import java.util.List;

public class LinkMicStatusEvent implements RTMBizInform {
    @SerializedName("linkmic_status")
    public int linkMicStatus;
    @SerializedName("linkmic_user_list")
    public List<LiveUserInfo> linkMicUsers;

    @Override
    public String toString() {
        return "LinkMicStatusEvent{" +
                "linkMicStatus=" + linkMicStatus +
                ", linkMicUsers=" + linkMicUsers +
                '}';
    }
}
