package com.volcengine.vertcdemo.interactivelive.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rts.RTSBizResponse;

import java.util.List;

public class GetActiveHostListResponse implements RTSBizResponse {

    @SerializedName("anchor_list")
    public List<LiveUserInfo> anchorList;

    @Override
    public String toString() {
        return "GetActiveHostListResponse{" +
                "anchorList=" + anchorList +
                '}';
    }
}
