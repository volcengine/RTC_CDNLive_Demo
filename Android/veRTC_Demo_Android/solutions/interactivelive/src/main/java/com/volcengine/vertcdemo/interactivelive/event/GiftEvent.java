package com.volcengine.vertcdemo.interactivelive.event;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;

public class GiftEvent implements RTMBizInform {

    public String userName;

    public String giftType;
    @SerializedName("message")
    public String message;

    public GiftEvent(String userName, String giftType) {
        this.userName = userName;
        this.giftType = giftType;
    }

    public GiftEvent(String message) {
        this.message = message;
    }

    public String getUserNameByMessage() {
        if (TextUtils.isEmpty(message)) {
            return null;
        }
        int index = message.lastIndexOf("送出");
        if (index < 0 || index >= message.length()) {
            return null;
        }
        return message.substring(0, index).trim();
    }

    public String getGiftTypeByMessage() {
        if (TextUtils.isEmpty(message)) {
            return null;
        }
        if (message.endsWith("鲜花")) {
            return "flower";
        } else if (message.endsWith("火箭")) {
            return "rocket";
        } else {
            return null;
        }
    }
}
