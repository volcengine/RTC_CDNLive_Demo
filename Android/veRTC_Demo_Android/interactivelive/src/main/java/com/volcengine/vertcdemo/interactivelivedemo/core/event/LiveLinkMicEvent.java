package com.volcengine.vertcdemo.interactivelivedemo.core.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveUserInfo;

import java.util.List;

public class LiveLinkMicEvent {

    @SerializedName("linkmic_status")
    public int linkMicStatus;
    @SerializedName("interact_user_list")
    public List<LiveUserInfo> interactUserList;

    @Override
    public String toString() {
        return "LiveLinkMicEvent{" +
                "linkMicStatus=" + linkMicStatus +
                ", interactUserList=" + interactUserList +
                '}';
    }
}
