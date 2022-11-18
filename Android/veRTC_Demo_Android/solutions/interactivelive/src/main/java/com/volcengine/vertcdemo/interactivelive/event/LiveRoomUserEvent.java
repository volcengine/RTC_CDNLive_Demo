package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizInform;

public class LiveRoomUserEvent implements RTSBizInform {

    public boolean isJoin;
    @SerializedName("audience_user_id")
    public String audienceUserId;
    @SerializedName("audience_user_name")
    public String audienceUserName;
    @SerializedName("audience_count")
    public int audienceCount;

    @Override
    public String toString() {
        return "LiveRoomUserEvent{" +
                "audienceUserId='" + audienceUserId + '\'' +
                ", audienceUserName='" + audienceUserName + '\'' +
                ", audienceCount=" + audienceCount +
                '}';
    }
}
