package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizInform;
import com.volcengine.vertcdemo.interactivelive.core.LiveDataManager;

public class LiveFinishEvent implements RTSBizInform {

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
