package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;
import com.volcengine.vertcdemo.interactivelive.bean.LiveUserInfo;

import java.util.List;

public class AudienceLinkPermitEvent implements RTMBizInform {

    @SerializedName("linker_id")
    public String linkerId;
    @SerializedName("permit_type")
    public int permitType;
    @SerializedName("rtc_room_id")
    public String rtcRoomId;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("rtc_token")
    public String rtcToken;
    @SerializedName("rtc_user_list")
    public List<LiveUserInfo> rtcUserList;

    @Override
    public String toString() {
        return "AudienceLinkPermitEvent{" +
                "linkerId='" + linkerId + '\'' +
                ", permitType=" + permitType +
                ", rtcRoomId='" + rtcRoomId + '\'' +
                ", userId='" + userId + '\'' +
                ", rtcToken='" + rtcToken + '\'' +
                ", rtcUserList=" + rtcUserList +
                '}';
    }
}
