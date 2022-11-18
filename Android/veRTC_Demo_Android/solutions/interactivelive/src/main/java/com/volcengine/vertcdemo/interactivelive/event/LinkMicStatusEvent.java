package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizInform;
import com.volcengine.vertcdemo.interactivelive.bean.LiveUserInfo;

import java.util.List;

public class LinkMicStatusEvent implements RTSBizInform {
    @SerializedName("linkmic_status")
    public int linkMicStatus;

    @Override
    public String toString() {
        return "LinkMicStatusEvent{" +
                "linkMicStatus=" + linkMicStatus +
                '}';
    }
}
