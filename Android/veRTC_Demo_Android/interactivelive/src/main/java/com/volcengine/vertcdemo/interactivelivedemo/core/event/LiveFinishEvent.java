package com.volcengine.vertcdemo.interactivelivedemo.core.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;
import com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager;

public class LiveFinishEvent implements RTMBizInform {

    @SerializedName("room_id")
    public String roomId;
    @SerializedName("type")
    @LiveDataManager.LiveFinishType
    public int type;

    @Override
    public String toString() {
        return "LiveFinishEvent{" +
                "roomId='" + roomId + '\'' +
                ", type=" + type +
                '}';
    }
}
