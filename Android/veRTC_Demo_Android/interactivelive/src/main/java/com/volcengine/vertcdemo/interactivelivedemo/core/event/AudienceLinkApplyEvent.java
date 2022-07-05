package com.volcengine.vertcdemo.interactivelivedemo.core.event;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveUserInfo;

public class AudienceLinkApplyEvent implements RTMBizInform {
    @SerializedName("applicant")
    public LiveUserInfo applicant;
    @SerializedName("linker_id")
    public String linkerId;
    @SerializedName("extra")
    public String extra;

    @Override
    public String toString() {
        return "AudienceLinkApplyEvent{" +
                "applicant=" + applicant +
                ", linkerId='" + linkerId + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }
}
