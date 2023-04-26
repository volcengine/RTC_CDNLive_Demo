// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.interactivelive.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizResponse;

import java.util.List;

/**
 * 获取正在直播主播列表接口返回的数据模型
 */
public class GetActiveAnchorListResponse implements RTSBizResponse {

    @SerializedName("anchor_list")
    public List<LiveUserInfo> anchorList;

    @Override
    public String toString() {
        return "GetActiveHostListResponse{" +
                "anchorList=" + anchorList +
                '}';
    }
}
