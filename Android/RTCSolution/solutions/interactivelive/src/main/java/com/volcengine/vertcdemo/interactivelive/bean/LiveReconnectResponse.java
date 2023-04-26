// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizResponse;
import com.volcengine.vertcdemo.interactivelive.feature.liveroommain.LiveRoomMainActivity;

import java.util.List;

/**
 * 重连接口返回的数据模型
 */
public class LiveReconnectResponse implements RTSBizResponse {

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
