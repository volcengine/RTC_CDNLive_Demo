// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;

/**
 * 重连事件
 */
public class LiveReconnectEvent {

    @SerializedName("user_id")
    public String userId;
    @SerializedName("user_name")
    public String userName;
    @SerializedName("rtc_room_id")
    public String rtcRoomId;
    @SerializedName("room_id")
    public String roomId;

    @Override
    public String toString() {
        return "LiveReconnectEvent{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", rtcRoomId='" + rtcRoomId + '\'' +
                ", roomId='" + roomId + '\'' +
                '}';
    }
}
