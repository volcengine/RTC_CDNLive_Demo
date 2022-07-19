package com.volcengine.vertcdemo.interactivelive.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizResponse;

import java.util.List;

public class LiveRoomListResponse implements RTMBizResponse {

    @SerializedName("live_room_list")
    public List<LiveRoomInfo> liveRoomList;
    @SerializedName("user")
    public LiveUserInfo user;
    @SerializedName("reconnect_info")
    public ReconnectInfo recoverInfo;
    @SerializedName("interact_status")
    public int interactStatus;
    @SerializedName("interact_user_list")
    public List<LiveUserInfo> interactUserList;

    @Override
    public String toString() {
        return "LiveRoomListResponse{" +
                "liveRoomList=" + liveRoomList +
                ", user=" + user +
                ", recoverInfo=" + recoverInfo +
                ", interact_status=" + interactStatus +
                ", interactUserList=" + interactUserList +
                '}';
    }
}
