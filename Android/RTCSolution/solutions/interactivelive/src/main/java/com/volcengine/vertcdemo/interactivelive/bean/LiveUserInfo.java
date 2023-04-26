// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.bean;

import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.USER_STATUS_OTHER;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.common.GsonUtils;
import com.volcengine.vertcdemo.core.net.rts.RTSBizResponse;
import com.volcengine.vertcdemo.interactivelive.core.LiveDataManager;

import java.util.Map;

/**
 * 用户数据模型
 */
public class LiveUserInfo implements RTSBizResponse {

    @SerializedName("room_id")
    public String roomId;
    @SerializedName("user_id")
    public String userId;
    @SerializedName("user_name")
    public String userName;
    @SerializedName("user_role")
    @LiveDataManager.LiveRoleType
    public int role;
    /**
     * status:
     * 1:其它
     * 2:主播连麦邀请中
     * 3:主播连麦互动中
     * 4:观众连麦邀请中
     * 5:观众连麦互动中
     */
    @LiveDataManager.LiveUserStatus
    public int status = USER_STATUS_OTHER;
    @LiveDataManager.MediaStatus
    @SerializedName("mic")
    public Integer micStatus;
    @LiveDataManager.MediaStatus
    @SerializedName("camera")
    public Integer cameraStatus;
    /**
     * 额外信息 存储宽高
     * 格式:  "extra":"{\"width\":0,\"height\":0}"
     */
    @SerializedName("extra")
    public String extra;
    @LiveDataManager.LiveLinkMicStatus
    @SerializedName("linkmic_status")
    public int linkMicStatus;

    public boolean isMicOn() {
        return micStatus == LiveDataManager.MEDIA_STATUS_ON;
    }

    public boolean isCameraOn() {
        return cameraStatus == LiveDataManager.MEDIA_STATUS_ON;
    }

    /**
     * 获取用户名称第一位字符
     *
     * @return 用户名称第一位字符
     */
    public @NonNull
    String getNamePrefix() {
        if (TextUtils.isEmpty(userName)) {
            return "";
        } else {
            return userName.substring(0, 1);
        }
    }

    /**
     * 获取用户推流视频宽度
     */
    public int getWidth() {
        if (TextUtils.isEmpty(extra)) {
            return 0;
        }
        try {
            Map<String, Double> resp = GsonUtils.gson().fromJson(extra, Map.class);
            Double d = resp.get("width");
            return d == null ? 0 : d.intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取用户推流视频高度
     */
    public int getHeight() {
        if (TextUtils.isEmpty(extra)) {
            return 0;
        }
        try {
            Map<String, Double> resp = GsonUtils.gson().fromJson(extra, Map.class);
            Double d = resp.get("height");
            return d == null ? 0 : d.intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "LiveUserInfo{" +
                "roomId='" + roomId + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", micStatus=" + micStatus +
                ", cameraStatus=" + cameraStatus +
                ", extra='" + extra + '\'' +
                ", linkMicStatus=" + linkMicStatus +
                '}';
    }

    public LiveUserInfo getDeepCopy() {
        LiveUserInfo userInfo = new LiveUserInfo();
        userInfo.userId = userId;
        userInfo.userName = userName;
        userInfo.roomId = roomId;
        userInfo.cameraStatus = cameraStatus;
        userInfo.micStatus = micStatus;
        userInfo.role = role;
        userInfo.status = status;
        userInfo.linkMicStatus = linkMicStatus;
        userInfo.extra = extra;
        return userInfo;
    }
}
