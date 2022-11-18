package com.volcengine.vertcdemo.interactivelive.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizResponse;

import java.util.List;

public class LiveAnchorPermitAudienceResponse implements RTSBizResponse {

    @SerializedName("rtc_room_id")
    public String rtcRoomId;
    @SerializedName("rtc_token")
    public String rtcToken;
    @SerializedName("linker_id")
    public String linkerId;
    @SerializedName("rtc_user_list")
    public List<LiveUserInfo> userList;

    @Override
    public String toString() {
        return "LiveAnchorPermitAudienceResponse{" +
                "rtcRoomId='" + rtcRoomId + '\'' +
                ", rtcToken='" + rtcToken + '\'' +
                ", linkerId='" + linkerId + '\'' +
                ", userList=" + userList +
                '}';
    }
}
