package com.volcengine.vertcdemo.interactivelive.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizResponse;
import com.volcengine.vertcdemo.interactivelive.feature.liveroommain.LiveRoomMainActivity;

import java.util.List;

public class LiveReconnectResponse implements RTMBizResponse {

    @SerializedName("user")
    public LiveUserInfo userInfo;
    @SerializedName("reconnect_info")
    public ReconnectInfo recoverInfo;
    @LiveRoomMainActivity.RoomStatus
    @SerializedName("interact_status")
    public int interactStatus;
    @SerializedName("interact_user_list")
    public List<LiveUserInfo> interactUserList;

    @Override
    public String toString() {
        return "LiveReconnectResponse{" +
                "userInfo=" + userInfo +
                ", recoverInfo=" + recoverInfo +
                ", interactStatus=" + interactStatus +
                ", interactUserList=" + interactUserList +
                '}';
    }
}
