package com.volcengine.vertcdemo.interactivelivedemo.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizResponse;

import java.util.List;

public class GetActiveHostListResponse implements RTMBizResponse {

    @SerializedName("anchor_list")
    public List<LiveUserInfo> anchorList;

    @Override
    public String toString() {
        return "GetActiveHostListResponse{" +
                "anchorList=" + anchorList +
                '}';
    }
}
