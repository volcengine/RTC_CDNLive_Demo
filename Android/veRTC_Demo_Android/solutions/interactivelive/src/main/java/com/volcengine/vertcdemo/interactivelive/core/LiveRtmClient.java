package com.volcengine.vertcdemo.interactivelive.core;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.ss.bytertc.engine.RTCEngine;
import com.ss.video.rtc.demo.basic_module.utils.AppExecutors;
import com.volcengine.vertcdemo.common.AbsBroadcast;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.rtm.RTMBaseClient;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizResponse;
import com.volcengine.vertcdemo.core.net.rtm.RtmInfo;
import com.volcengine.vertcdemo.interactivelive.bean.CreateLiveRoomResponse;
import com.volcengine.vertcdemo.interactivelive.bean.GetActiveHostListResponse;
import com.volcengine.vertcdemo.interactivelive.bean.GetAudienceListResponse;
import com.volcengine.vertcdemo.interactivelive.bean.JoinLiveRoomResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveAnchorPermitAudienceResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveInviteResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveReconnectResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveReplyResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveRoomListResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelive.event.AnchorLinkFinishEvent;
import com.volcengine.vertcdemo.interactivelive.event.AnchorLinkInviteEvent;
import com.volcengine.vertcdemo.interactivelive.event.AnchorLinkReplyEvent;
import com.volcengine.vertcdemo.interactivelive.event.AudienceLinkApplyEvent;
import com.volcengine.vertcdemo.interactivelive.event.AudienceLinkFinishEvent;
import com.volcengine.vertcdemo.interactivelive.event.AudienceLinkInviteEvent;
import com.volcengine.vertcdemo.interactivelive.event.AudienceLinkPermitEvent;
import com.volcengine.vertcdemo.interactivelive.event.AudienceLinkReplyEvent;
import com.volcengine.vertcdemo.interactivelive.event.AudienceLinkStatusEvent;
import com.volcengine.vertcdemo.interactivelive.event.AudienceMediaUpdateEvent;
import com.volcengine.vertcdemo.interactivelive.event.GiftEvent;
import com.volcengine.vertcdemo.interactivelive.event.LinkMicStatusEvent;
import com.volcengine.vertcdemo.interactivelive.event.LiveFinishEvent;
import com.volcengine.vertcdemo.interactivelive.event.LiveRoomUserEvent;
import com.volcengine.vertcdemo.interactivelive.event.MediaChangedEvent;

import java.util.UUID;

public class LiveRtmClient extends RTMBaseClient {

    private static final String CMD_LIVE_GET_ACTIVE_LIVE_ROOM_LIST = "liveGetActiveLiveRoomList";
    private static final String CMD_LIVE_CLEAR_USER = "liveClearUser";
    private static final String CMD_LIVE_RECONNECT = "liveReconnect";
    private static final String CMD_LIVE_UPDATE_MEDIA_STATUS = "liveUpdateMediaStatus";

    private static final String CMD_LIVE_CREATE_LIVE = "liveCreateLive";
    private static final String CMD_LIVE_START_LIVE = "liveStartLive";
    private static final String CMD_LIVE_UPDATE_RESOLUTION = "liveUpdateResolution";
    private static final String CMD_LIVE_GET_ACTIVE_ANCHOR_LIST = "liveGetActiveAnchorList";
    private static final String CMD_LIVE_GET_AUDIENCE_LIST = "liveGetAudienceList";
    private static final String CMD_LIVE_MANAGE_GUEST_MEDIA = "liveManageGuestMedia";
    private static final String CMD_LIVE_FINISH_LIVE = "liveFinishLive";

    private static final String CMD_LIVE_JOIN_LIVE_ROOM = "liveJoinLiveRoom";
    private static final String CMD_LIVE_LEAVE_LIVE_ROOM = "liveLeaveLiveRoom";

    private static final String CMD_LIVE_AUDIENCE_LINK_MIC_INVITE = "liveAudienceLinkmicInvite";
    private static final String CMD_LIVE_AUDIENCE_LINK_MIC_PERMIT = "liveAudienceLinkmicPermit";
    private static final String CMD_LIVE_AUDIENCE_LINK_MIC_KICK = "liveAudienceLinkmicKick";
    private static final String CMD_LIVE_AUDIENCE_LINK_MIC_FINISH = "liveAudienceLinkmicFinish";
    private static final String CMD_LIVE_ANCHOR_LINK_MIC_INVITE = "liveAnchorLinkmicInvite";
    private static final String CMD_LIVE_ANCHOR_LINK_MIC_REPLY = "liveAnchorLinkmicReply";
    private static final String CMD_LIVE_ANCHOR_LINK_MIC_FINISH = "liveAnchorLinkmicFinish";
    private static final String CMD_LIVE_AUDIENCE_LINK_MIC_APPLY = "liveAudienceLinkmicApply";
    private static final String CMD_LIVE_AUDIENCE_LINK_MIC_REPLY = "liveAudienceLinkmicReply";
    private static final String CMD_LIVE_AUDIENCE_LINK_MIC_LEAVE = "liveAudienceLinkmicLeave";
    private static final String CMD_LIVE_SEND_MESSAGE = "liveSendMessage";

    private static final String ON_AUDIENCE_JOIN_ROOM = "liveOnAudienceJoinRoom";
    private static final String ON_AUDIENCE_LEAVE_ROOM = "liveOnAudienceLeaveRoom";
    private static final String ON_FINISH_LIVE = "liveOnFinishLive";
    private static final String ON_LINK_MIC_STATUS = "liveOnLinkmicStatus";
    private static final String ON_AUDIENCE_LINK_MIC_JOIN = "liveOnAudienceLinkmicJoin";
    private static final String ON_AUDIENCE_LINK_MIC_LEAVE = "liveOnAudienceLinkmicLeave";
    private static final String ON_AUDIENCE_LINK_MIC_FINISH = "liveOnAudienceLinkmicFinish";
    private static final String ON_MEDIA_CHANGE = "liveOnMediaChange";
    private static final String ON_AUDIENCE_LINK_MIC_INVITE = "liveOnAudienceLinkmicInvite";
    private static final String ON_AUDIENCE_LINK_MIC_APPLY = "liveOnAudienceLinkmicApply";
    private static final String ON_AUDIENCE_LINK_MIC_REPLY = "liveOnAudienceLinkmicReply";
    private static final String ON_AUDIENCE_LINK_MIC_PERMIT = "liveOnAudienceLinkmicPermit";
    private static final String ON_AUDIENCE_LINK_MIC_KICK = "liveOnAudienceLinkmicKick";
    private static final String ON_ANCHOR_LINK_MIC_INVITE = "liveOnAnchorLinkmicInvite";
    private static final String ON_ANCHOR_LINK_MIC_REPLY = "liveOnAnchorLinkmicReply";
    private static final String ON_ANCHOR_LINK_MIC_FINISH = "liveOnAnchorLinkmicFinish";
    private static final String ON_MANAGER_GUEST_MEDIA = "liveOnManageGuestMedia";
    private static final String ON_MESSAGE_SEND = "liveOnMessageSend";

    public LiveRtmClient(@NonNull RTCEngine engine, @NonNull RtmInfo rtmInfo) {
        super(engine, rtmInfo);
        initEventListener();
    }

    private JsonObject getCommonParams(String cmd) {
        JsonObject params = new JsonObject();
        params.addProperty("app_id", mRtmInfo.appId);
        params.addProperty("user_id", SolutionDataManager.ins().getUserId());
        params.addProperty("user_name", SolutionDataManager.ins().getUserName());
        params.addProperty("event_name", cmd);
        params.addProperty("request_id", UUID.randomUUID().toString());
        params.addProperty("device_id", SolutionDataManager.ins().getDeviceId());
        return params;
    }

    private void initEventListener() {
        putEventListener(new AbsBroadcast<>(ON_AUDIENCE_JOIN_ROOM, LiveRoomUserEvent.class, (data) -> {
            data.isJoin = true;
            SolutionDemoEventManager.post(data);
        }));
        putEventListener(new AbsBroadcast<>(ON_AUDIENCE_LEAVE_ROOM, LiveRoomUserEvent.class, (data) -> {
            data.isJoin = false;
            SolutionDemoEventManager.post(data);
        }));
        putEventListener(new AbsBroadcast<>(ON_FINISH_LIVE, LiveFinishEvent.class, (data) -> {
            SolutionDemoEventManager.post(data);
            if (data.type == LiveDataManager.LIVE_FINISH_TYPE_TIMEOUT) {
                SolutionToast.show("???????????????????????????20mins");
            } else if (data.type == LiveDataManager.LIVE_FINISH_TYPE_IRREGULARITY) {
                SolutionToast.show("?????????????????????????????????????????????");
            } else if (data.type == LiveDataManager.LIVE_FINISH_TYPE_NORMAL) {
                SolutionToast.show("?????????????????????");
            }
        }));
        putEventListener(new AbsBroadcast<>(ON_LINK_MIC_STATUS, LinkMicStatusEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_AUDIENCE_LINK_MIC_JOIN, AudienceLinkStatusEvent.class,
                (data) -> {
                    data.isJoin = true;
                    SolutionDemoEventManager.post(data);
                }));
        putEventListener(new AbsBroadcast<>(ON_AUDIENCE_LINK_MIC_LEAVE, AudienceLinkStatusEvent.class,
                (data) -> {
                    data.isJoin = false;
                    SolutionDemoEventManager.post(data);
                }));
        putEventListener(new AbsBroadcast<>(ON_AUDIENCE_LINK_MIC_FINISH, AudienceLinkFinishEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_MEDIA_CHANGE, MediaChangedEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_AUDIENCE_LINK_MIC_INVITE, AudienceLinkInviteEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_AUDIENCE_LINK_MIC_APPLY, AudienceLinkApplyEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_AUDIENCE_LINK_MIC_REPLY, AudienceLinkReplyEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_AUDIENCE_LINK_MIC_PERMIT, AudienceLinkPermitEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_AUDIENCE_LINK_MIC_KICK, AudienceLinkFinishEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_ANCHOR_LINK_MIC_INVITE, AnchorLinkInviteEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_ANCHOR_LINK_MIC_REPLY, AnchorLinkReplyEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_ANCHOR_LINK_MIC_FINISH, AnchorLinkFinishEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_MANAGER_GUEST_MEDIA, AudienceMediaUpdateEvent.class,
                SolutionDemoEventManager::post));
        putEventListener(new AbsBroadcast<>(ON_MESSAGE_SEND, GiftEvent.class, (data) ->
                SolutionDemoEventManager.post(new GiftEvent(data.getUserNameByMessage(),
                        data.getGiftTypeByMessage()))));
    }

    private <T extends RTMBizResponse> void sendServerMessageOnNetwork(String roomId, JsonObject content, Class<T> resultClass, IRequestCallback<T> callback) {
        String cmd = content.get("event_name").getAsString();
        if (TextUtils.isEmpty(cmd)) {
            return;
        }
        AppExecutors.networkIO().execute(() -> sendServerMessage(cmd, roomId, content, resultClass, callback));
    }

    private void putEventListener(AbsBroadcast<? extends RTMBizInform> absBroadcast) {
        mEventListeners.put(absBroadcast.getEvent(), absBroadcast);
    }

    public void removeEventListener() {
        mEventListeners.remove(ON_AUDIENCE_JOIN_ROOM);
        mEventListeners.remove(ON_AUDIENCE_LEAVE_ROOM);
        mEventListeners.remove(ON_FINISH_LIVE);
        mEventListeners.remove(ON_LINK_MIC_STATUS);
        mEventListeners.remove(ON_AUDIENCE_LINK_MIC_JOIN);
        mEventListeners.remove(ON_AUDIENCE_LINK_MIC_LEAVE);
        mEventListeners.remove(ON_AUDIENCE_LINK_MIC_FINISH);
        mEventListeners.remove(ON_MEDIA_CHANGE);
        mEventListeners.remove(ON_AUDIENCE_LINK_MIC_INVITE);
        mEventListeners.remove(ON_AUDIENCE_LINK_MIC_APPLY);
        mEventListeners.remove(ON_AUDIENCE_LINK_MIC_REPLY);
        mEventListeners.remove(ON_AUDIENCE_LINK_MIC_PERMIT);
        mEventListeners.remove(ON_AUDIENCE_LINK_MIC_KICK);
        mEventListeners.remove(ON_ANCHOR_LINK_MIC_INVITE);
        mEventListeners.remove(ON_ANCHOR_LINK_MIC_REPLY);
        mEventListeners.remove(ON_ANCHOR_LINK_MIC_FINISH);
        mEventListeners.remove(ON_MANAGER_GUEST_MEDIA);
        mEventListeners.remove(ON_MESSAGE_SEND);
    }

    //????????????
    public void requestLiveRoomList(IRequestCallback<LiveRoomListResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_GET_ACTIVE_LIVE_ROOM_LIST);
        sendServerMessageOnNetwork("", params, LiveRoomListResponse.class, callback);
    }

    public void requestLiveClearUser(IRequestCallback<LiveResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_CLEAR_USER);
        sendServerMessageOnNetwork("", params, LiveResponse.class, callback);
    }

    public void requestLiveReconnect(IRequestCallback<LiveReconnectResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_RECONNECT);
        sendServerMessageOnNetwork("", params, LiveReconnectResponse.class, callback);
    }

    public void updateMediaStatus(String roomId, @LiveDataManager.MediaStatus int micStatus, @LiveDataManager.MediaStatus int cameraStatus,
                                  IRequestCallback<LiveResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_UPDATE_MEDIA_STATUS);
        params.addProperty("room_id", roomId);
        params.addProperty("mic", micStatus);
        params.addProperty("camera", cameraStatus);
        sendServerMessageOnNetwork(roomId, params, LiveResponse.class, callback);
    }

    //????????????
    public void requestCreateLiveRoom(IRequestCallback<CreateLiveRoomResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_CREATE_LIVE);
        sendServerMessageOnNetwork("", params, CreateLiveRoomResponse.class, callback);
    }

    public void updateResolution(String roomId, int width, int height, IRequestCallback<LiveResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_UPDATE_RESOLUTION);
        params.addProperty("room_id", roomId);
        params.addProperty("width", width);
        params.addProperty("height", height);
        sendServerMessageOnNetwork(roomId, params, LiveResponse.class, callback);
    }

    public void requestStartLive(String roomId, IRequestCallback<LiveUserInfo> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_START_LIVE);
        params.addProperty("room_id", roomId);
        sendServerMessageOnNetwork(roomId, params, LiveUserInfo.class, callback);
    }

    public void requestActiveHostList(IRequestCallback<GetActiveHostListResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_GET_ACTIVE_ANCHOR_LIST);
        sendServerMessageOnNetwork("", params, GetActiveHostListResponse.class, callback);
    }

    public void requestAudienceList(String roomId, IRequestCallback<GetAudienceListResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_GET_AUDIENCE_LIST);
        params.addProperty("room_id", roomId);
        sendServerMessageOnNetwork(roomId, params, GetAudienceListResponse.class, callback);
    }

    public void requestManageGuest(String hostRoomId, String hostUserId, String guestRoomId, String guestUserId,
                                   @LiveDataManager.MediaStatus int camera, @LiveDataManager.MediaStatus int mic,
                                   IRequestCallback<LiveResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_MANAGE_GUEST_MEDIA);
        params.addProperty("host_room_id", hostRoomId);
        params.addProperty("host_user_id", hostUserId);
        params.addProperty("guest_room_id", guestRoomId);
        params.addProperty("guest_user_id", guestUserId);
        params.addProperty("mic", mic);
        params.addProperty("camera", camera);
        sendServerMessageOnNetwork(hostRoomId, params, LiveResponse.class, callback);
    }

    public void requestFinishLive(String roomId, IRequestCallback<LiveResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_FINISH_LIVE);
        params.addProperty("room_id", roomId);
        sendServerMessageOnNetwork(roomId, params, LiveResponse.class, callback);
    }

    //????????????
    public void requestJoinLiveRoom(String roomId, IRequestCallback<JoinLiveRoomResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_JOIN_LIVE_ROOM);
        params.addProperty("room_id", roomId);
        sendServerMessageOnNetwork(roomId, params, JoinLiveRoomResponse.class, callback);
    }

    public void requestLeaveLiveRoom(String roomId, IRequestCallback<LiveResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_LEAVE_LIVE_ROOM);
        params.addProperty("room_id", roomId);
        sendServerMessageOnNetwork(roomId, params, LiveResponse.class, callback);
    }

    /**
     * ????????????????????????
     */
    public void inviteAudienceByHost(String hostRoomId, String hostUserId, String audienceRoomId,
                                     String audienceUserId, String extra, IRequestCallback<LiveInviteResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_AUDIENCE_LINK_MIC_INVITE);
        params.addProperty("host_room_id", hostRoomId);
        params.addProperty("host_user_id", hostUserId);
        params.addProperty("audience_room_id", audienceRoomId);
        params.addProperty("audience_user_id", audienceUserId);
        params.addProperty("extra", extra);
        sendServerMessageOnNetwork(hostRoomId, params, LiveInviteResponse.class, callback);
    }

    /**
     * ??????????????????????????????
     */
    public void replyAudienceRequestByHost(String linkerId, String hostRoomId, String hostUserId,
                                           String audienceRoomId, String audienceUserId, int permitType,
                                           IRequestCallback<LiveAnchorPermitAudienceResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_AUDIENCE_LINK_MIC_PERMIT);
        params.addProperty("linker_id", linkerId);
        params.addProperty("host_room_id", hostRoomId);
        params.addProperty("host_user_id", hostUserId);
        params.addProperty("audience_room_id", audienceRoomId);
        params.addProperty("audience_user_id", audienceUserId);
        params.addProperty("permit_type", permitType);
        sendServerMessageOnNetwork(hostRoomId, params, LiveAnchorPermitAudienceResponse.class, callback);
    }

    /**
     * ???????????????????????????
     */
    public void kickAudienceByHost(String hostRoomId, String hostUserId, String audienceRoomId,
                                   String audienceUserId, IRequestCallback<LiveResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_AUDIENCE_LINK_MIC_KICK);
        params.addProperty("host_room_id", hostRoomId);
        params.addProperty("host_user_id", hostUserId);
        params.addProperty("audience_room_id", audienceRoomId);
        params.addProperty("audience_user_id", audienceUserId);
        sendServerMessageOnNetwork(hostRoomId, params, LiveResponse.class, callback);
    }

    /**
     * ??????????????????????????????
     */
    public void finishAudienceLinkByHost(String roomId, IRequestCallback<LiveResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_AUDIENCE_LINK_MIC_FINISH);
        params.addProperty("room_id", roomId);
        sendServerMessageOnNetwork(roomId, params, LiveResponse.class, callback);
    }

    /**
     * ????????????????????????????????????
     */
    public void inviteHostByHost(String inviterRoomId, String inviterUserId, String inviteeRoomId,
                                 String inviteeUserId, String extra, IRequestCallback<LiveInviteResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_ANCHOR_LINK_MIC_INVITE);
        params.addProperty("inviter_room_id", inviterRoomId);
        params.addProperty("inviter_user_id", inviterUserId);
        params.addProperty("invitee_room_id", inviteeRoomId);
        params.addProperty("invitee_user_id", inviteeUserId);
        params.addProperty("extra", extra);
        sendServerMessageOnNetwork(inviterRoomId, params, LiveInviteResponse.class, callback);
    }

    /**
     * ??????????????????????????????
     */
    public void replyHostInviteeByHost(String linkerId, String inviterRoomId, String inviterUserId,
                                       String inviteeRoomId, String inviteeUserId, int replyType,
                                       IRequestCallback<LiveInviteResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_ANCHOR_LINK_MIC_REPLY);
        params.addProperty("linker_id", linkerId);
        params.addProperty("inviter_room_id", inviterRoomId);
        params.addProperty("inviter_user_id", inviterUserId);
        params.addProperty("invitee_room_id", inviteeRoomId);
        params.addProperty("invitee_user_id", inviteeUserId);
        params.addProperty("reply_type", replyType);
        sendServerMessageOnNetwork(inviterRoomId, params, LiveInviteResponse.class, callback);
    }

    /**
     * ????????????????????????
     */
    public void finishHostLink(String linkerId, String roomId, IRequestCallback<LiveResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_ANCHOR_LINK_MIC_FINISH);
        params.addProperty("linker_id", linkerId);
        params.addProperty("room_id", roomId);
        sendServerMessageOnNetwork(roomId, params, LiveResponse.class, callback);
    }

    /**
     * ??????????????????
     */
    public void requestLinkByAudience(String roomId, IRequestCallback<LiveInviteResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_AUDIENCE_LINK_MIC_APPLY);
        params.addProperty("room_id", roomId);
        sendServerMessageOnNetwork(roomId, params, LiveInviteResponse.class, callback);
    }

    /**
     * ??????????????????????????????
     */
    public void replyHostInviterByAudience(String linkerId, String roomId, int replyType,
                                           IRequestCallback<LiveReplyResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_AUDIENCE_LINK_MIC_REPLY);
        params.addProperty("linker_id", linkerId);
        params.addProperty("room_id", roomId);
        params.addProperty("reply_type", replyType);
        sendServerMessageOnNetwork(roomId, params, LiveReplyResponse.class, callback);
    }

    /**
     * ?????????????????????
     */
    public void finishAudienceLinkByAudience(String linkerId, String roomId, IRequestCallback<LiveResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_AUDIENCE_LINK_MIC_LEAVE);
        params.addProperty("linker_id", linkerId);
        params.addProperty("room_id", roomId);
        sendServerMessageOnNetwork(roomId, params, LiveResponse.class, callback);
    }

    /**
     * ????????????
     */
    public void sendMessage(String roomId, String userName, String giftType, IRequestCallback<LiveResponse> callback) {
        JsonObject params = getCommonParams(CMD_LIVE_SEND_MESSAGE);
        params.addProperty("room_id", roomId);
        String giftName = null;
        if (TextUtils.equals(giftType, "flower")) {
            giftName = "??????";
        } else if (TextUtils.equals(giftType, "rocket")) {
            giftName = "??????";
        }
        if (TextUtils.isEmpty(giftName)) {
            return;
        }
        String message = String.format("%s??????%s", userName, giftName);
        params.addProperty("message", message);
        sendServerMessageOnNetwork(roomId, params, LiveResponse.class, callback);
    }
}
