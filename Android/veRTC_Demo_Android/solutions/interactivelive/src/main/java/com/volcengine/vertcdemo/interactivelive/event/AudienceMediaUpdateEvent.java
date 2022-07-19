package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;

public class AudienceMediaUpdateEvent implements RTMBizInform {
    @SerializedName("guest_room_id")
    public String guestRoomId;
    @SerializedName("guest_user_id")
    public String guestUserId;
    @SerializedName("mic")
    public int mic;
    @SerializedName("camera")
    public int camera;

    @Override
    public String toString() {
        return "AudienceMediaUpdateEvent{" +
                "guestRoomId='" + guestRoomId + '\'' +
                ", guestUserId='" + guestUserId + '\'' +
                ", mic=" + mic +
                ", camera=" + camera +
                '}';
    }
}
