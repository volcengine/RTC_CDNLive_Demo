package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;

public class LiveKickUserEvent {

    @SerializedName("linker_id")
    public String linkerId;
    @SerializedName("rtc_room_id")
    public String rtcRoomId;
    @SerializedName("user_name")
    public String userName;

    @Override
    public String toString() {
        return "LiveKickUserEvent{" +
                "linkerId='" + linkerId + '\'' +
                ", rtcRoomId='" + rtcRoomId + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
