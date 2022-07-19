package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;

public class AudienceLinkFinishEvent implements RTMBizInform {

    @SerializedName("rtc_room_id")
    public String rtcRoomId;

    @Override
    public String toString() {
        return "AudienceLinkFinishEvent{" +
                "rtcRoomId='" + rtcRoomId + '\'' +
                '}';
    }
}
