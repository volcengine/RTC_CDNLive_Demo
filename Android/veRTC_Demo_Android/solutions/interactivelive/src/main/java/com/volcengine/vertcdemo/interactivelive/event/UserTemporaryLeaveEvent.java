package com.volcengine.vertcdemo.interactivelive.event;

import com.google.gson.annotations.SerializedName;

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
