// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizResponse;

import java.util.List;

/**
 * 获取观众列表接口返回的数据模型
 */
public class GetAudienceListResponse implements RTSBizResponse {

    @SerializedName("audience_list")
    public List<LiveUserInfo> audienceList;

    @Override
    public String toString() {
        return "GetAudienceListResponse{" +
                "audienceList=" + audienceList +
                '}';
    }
}
