// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;

/**
 * 用户暂时离开事件
 */
public class UserTemporaryLeaveEvent {

    @SerializedName("user_id")
    public String userId;
    @SerializedName("user_name")
    public String userName;

    @Override
    public String toString() {
        return "UserTemporaryLeaveEvent{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
