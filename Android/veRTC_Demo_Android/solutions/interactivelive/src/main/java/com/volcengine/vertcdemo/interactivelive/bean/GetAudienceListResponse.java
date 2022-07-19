package com.volcengine.vertcdemo.interactivelive.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizResponse;

import java.util.List;

public class GetAudienceListResponse implements RTMBizResponse {

    @SerializedName("audience_list")
    public List<LiveUserInfo> audienceList;

    @Override
    public String toString() {
        return "GetAudienceListResponse{" +
                "audienceList=" + audienceList +
                '}';
    }
}
