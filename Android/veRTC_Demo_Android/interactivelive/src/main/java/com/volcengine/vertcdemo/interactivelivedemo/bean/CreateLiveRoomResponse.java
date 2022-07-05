package com.volcengine.vertcdemo.interactivelivedemo.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizResponse;

public class CreateLiveRoomResponse implements RTMBizResponse {

    @SerializedName("live_room_info")
    public LiveRoomInfo liveRoomInfo;
    @SerializedName("user_info")
    public LiveUserInfo userInfo;
    @SerializedName("stream_push_url")
    public String streamPushUrl;
    @SerializedName("rtm_token")
    public String rtmToken;

    @Override
    public String toString() {
        return "CreateLiveRoomResponse{" +
                "liveRoomInfo=" + liveRoomInfo +
                ", userInfo=" + userInfo +
                ", streamPushUrl='" + streamPushUrl + '\'' +
                ", rtmToken='" + rtmToken + '\'' +
                '}';
    }
}
