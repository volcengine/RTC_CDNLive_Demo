package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;
import com.volcengine.vertcdemo.interactivelive.bean.LiveUserInfo;

import java.util.List;

public class AudienceLinkStatusEvent implements RTMBizInform {

    @SerializedName("linker_id")
    public String linkerId;
    @SerializedName("rtc_room_id")
    public String rtcRoomId;
    @SerializedName("user_list")
    public List<LiveUserInfo> userList;
    @SerializedName("user_id")
    public String userId;

    public boolean isJoin = true;

    @Override
    public String toString() {
        return "AudienceLinkStatusEvent{" +
                "linkerId='" + linkerId + '\'' +
                ", rtcRoomId='" + rtcRoomId + '\'' +
                ", userList=" + userList +
                ", userId='" + userId + '\'' +
                ", isJoin=" + isJoin +
                '}';
    }
}
