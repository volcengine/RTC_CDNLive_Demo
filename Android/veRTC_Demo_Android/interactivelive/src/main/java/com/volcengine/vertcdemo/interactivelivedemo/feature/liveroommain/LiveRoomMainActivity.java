package com.volcengine.vertcdemo.interactivelivedemo.feature.liveroommain;

import static com.ss.bytertc.engine.handler.IRTCEngineEventHandler.NetworkQuality.NETWORK_QUALITY_EXCELLENT;
import static com.ss.bytertc.engine.handler.IRTCEngineEventHandler.NetworkQuality.NETWORK_QUALITY_GOOD;
import static com.volcengine.vertcdemo.interactivelivedemo.common.LiveRoomControlsLayout.STATUS_DISABLE;
import static com.volcengine.vertcdemo.interactivelivedemo.common.LiveRoomControlsLayout.STATUS_NORMAL;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.INVITE_REPLY_ACCEPT;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.INVITE_REPLY_REJECT;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.LINK_MIC_STATUS_HOST_INTERACTING;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.LINK_MIC_STATUS_OTHER;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.LIVE_PERMIT_TYPE_ACCEPT;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.LIVE_PERMIT_TYPE_REJECT;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.MEDIA_STATUS_OFF;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.MEDIA_STATUS_ON;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.USER_ROLE_AUDIENCE;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.USER_ROLE_HOST;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.USER_STATUS_AUDIENCE_INTERACTING;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.USER_STATUS_CO_HOSTING;
import static com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager.USER_STATUS_OTHER;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.ui.CommonDialog;
import com.ss.video.rtc.demo.basic_module.utils.GsonUtils;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SocketConnectEvent;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelivedemo.bean.JoinLiveRoomResponse;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveAnchorPermitAudienceResponse;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveInviteResponse;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveReconnectResponse;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveReplyResponse;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveResponse;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveRoomInfo;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelivedemo.bean.ReconnectInfo;
import com.volcengine.vertcdemo.interactivelivedemo.common.AddGuestsDialog;
import com.volcengine.vertcdemo.interactivelivedemo.common.AudienceGroupLayout;
import com.volcengine.vertcdemo.interactivelivedemo.common.AudienceSettingDialog;
import com.volcengine.vertcdemo.interactivelivedemo.common.AvatarView;
import com.volcengine.vertcdemo.interactivelivedemo.common.InviteResultDialog;
import com.volcengine.vertcdemo.interactivelivedemo.common.LiveCoHostDialog;
import com.volcengine.vertcdemo.interactivelivedemo.common.LiveRoomControlsLayout;
import com.volcengine.vertcdemo.interactivelivedemo.common.LiveSettingDialog;
import com.volcengine.vertcdemo.interactivelivedemo.common.LiveVideoLayout;
import com.volcengine.vertcdemo.interactivelivedemo.common.RequestInteractDialog;
import com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager;
import com.volcengine.vertcdemo.interactivelivedemo.core.LivePlayerManager;
import com.volcengine.vertcdemo.interactivelivedemo.core.LivePushManager;
import com.volcengine.vertcdemo.interactivelivedemo.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AnchorLinkFinishEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AnchorLinkInviteEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AnchorLinkReplyEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AudienceLinkApplyEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AudienceLinkFinishEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AudienceLinkInviteEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AudienceLinkPermitEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AudienceLinkReplyEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AudienceLinkStatusEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AudienceMediaUpdateEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.InviteAudienceEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.LinkMicStatusEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.LiveFinishEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.LiveHasBlockEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.LiveKickUserEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.LiveReconnectEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.LiveRoomUserEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.LocalKickUserEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.MediaChangedEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.NetworkConnectEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.NetworkQualityEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.UpdatePullStreamEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.UserTemporaryLeaveEvent;
import com.volcengine.vertcdemo.interactivelivedemo.feature.createroom.effect.EffectDialog;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LiveRoomMainActivity extends BaseActivity {

    private static final String TAG = "LiveRoomMainActivity";

    private static final String EXTRA_REFER = "refer";
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_HOST_ID = "hostId";
    private static final String EXTRA_ROOM_INFO = "roomInfo";
    private static final String EXTRA_USER_INFO = "userInfo";
    private static final String EXTRA_PUSH_URL = "pushUrl";
    private static final String EXTRA_TOKEN = "rtcToken";
    private static final String EXTRA_RECONNECT_INFO = "reconnectInfo";
    private static final String EXTRA_INTERACT_STATUS = "interactStatus";
    private static final String EXTRA_INTERACT_USERS = "interactUsers";

    private static final String REFER_LIST = "list";
    private static final String REFER_CREATE = "create";
    private static final String REFER_RECONNECT = "reconnect";

    @RoomStatus
    private int mRoomStatus = ROOM_STATUS_LIVE;

    @IntDef({ROOM_STATUS_LIVE, ROOM_STATUS_GUEST_INTERACT, ROOM_STATUS_CO_HOST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RoomStatus {
    }

    public static final int ROOM_STATUS_LIVE = 0;
    public static final int ROOM_STATUS_GUEST_INTERACT = 3;
    public static final int ROOM_STATUS_CO_HOST = 4;

    private long mLastApplyTs = 0; // 观众身份下，记录每次发起连麦申请的时间，4S内不能再次申请
    private String mPushUrl;
    private String mRTCToken;
    private String mLinkId;
    private LiveUserInfo mSelfInfo; //自己的信息
    private LiveUserInfo mHostInfo; //自己不是主播时，主播的用户信息
    private LiveUserInfo mCoHostInfo; //自己是主播时，别的主播的用户信息
    private LiveRoomInfo mLiveRoomInfo; //房间信息
    private final List<LiveUserInfo> mGuestList = new LinkedList<>();
    private int mAudienceCount = 0;
    private final Map<String, String> mPullStreamMap = new HashMap<>();

    private AvatarView mAvatarView;
    private LiveRoomControlsLayout mLiveRoomControls;
    private TextView mAudienceCountTv;
    private FrameLayout mLiveStreamContainer;
    private LiveVideoLayout mLiveVideoLayout;
    private AudienceGroupLayout mGuestListLayout;
    private RecyclerView mLiveChatRv;
    private View mTopTip;

    private LiveChatAdapter mLiveChatAdapter;

    private boolean hasLayouted = false;
    private boolean isLeaveByKickOut = false;

    //加入房间回调
    private final IRequestCallback<JoinLiveRoomResponse> mJoinRoomCallback = new IRequestCallback<JoinLiveRoomResponse>() {
        @Override
        public void onSuccess(JoinLiveRoomResponse data) {
            mRoomStatus = data.liveRoomInfo.status;
            mGuestList.clear();
            mLiveRoomInfo = data.liveRoomInfo;
            mSelfInfo = data.liveUserInfo;
            mRTCToken = data.rtmToken;
            mHostInfo = data.liveHostUserInfo;
            updateOnlineGuestList(null);
            initByJoinResponse(data.liveRoomInfo, data.liveUserInfo, data.liveRoomInfo.audienceCount,
                    data.liveRoomInfo.streamPullStreamList);
        }

        @Override
        public void onError(int errorCode, String message) {
            String msg;
            if (errorCode != 200) {
                msg = "加入房间失败，回到房间列表页";
            } else {
                msg = message;
            }
            CommonDialog dialog = new CommonDialog(LiveRoomMainActivity.this);
            dialog.setCancelable(false);
            dialog.setMessage(msg);
            dialog.setPositiveListener((v) -> finish());
            dialog.show();
        }
    };

    //邀请主播连线回调
    private final IRequestCallback<LiveInviteResponse> mInviteHostResponse = new IRequestCallback<LiveInviteResponse>() {
        @Override
        public void onSuccess(LiveInviteResponse data) {
            showToast("已发出邀请，等待对方应答");
        }

        @Override
        public void onError(int errorCode, String message) {
            if (errorCode == 622) {
                showToast("已发出邀请，等待对方应答");
            } else {
                showToast(errorCodeToMessage(errorCode, message));
            }
        }
    };

    //响应邀请回调
    private final IRequestCallback<LiveInviteResponse> mAnchorReplyInviteCallback = new IRequestCallback<LiveInviteResponse>() {

        @Override
        public void onSuccess(LiveInviteResponse data) {
            mRoomStatus = ROOM_STATUS_CO_HOST;
            for (LiveUserInfo info : data.userList) {
                if (!TextUtils.equals(info.userId, SolutionDataManager.ins().getUserId())) {
                    mCoHostInfo = info;
                    break;
                }
            }
            mLiveRoomControls.setCoHostBtnStatus(STATUS_DISABLE);
            LiveRTCManager.ins().joinRoom(data.rtcRoomId, mSelfInfo.userId, data.rtcToken);
            LiveRTCManager.ins().startCaptureVideo(mSelfInfo.isCameraOn());
            LiveRTCManager.ins().startCaptureAudio(mSelfInfo.isMicOn());
            mLiveVideoLayout.setLiveUserInfo(mSelfInfo, mCoHostInfo);
            LiveRTCManager.ins().startLiveTranscoding(mPushUrl, data.userList, data.rtcRoomId, true);
        }

        @Override
        public void onError(int errorCode, String message) {
            showToast(errorCodeToMessage(errorCode, message));
        }
    };


    //观众申请连线回调
    private final IRequestCallback<LiveInviteResponse> mGuestApplyResponse = new IRequestCallback<LiveInviteResponse>() {
        @Override
        public void onSuccess(LiveInviteResponse data) {
            mLastApplyTs = System.currentTimeMillis();
            showToast("您已向主播发起连麦申请，等待主播应答");
            mLiveRoomControls.setAddGuestBtnStatus(LiveRoomControlsLayout.STATUS_NORMAL);
        }

        @Override
        public void onError(int errorCode, String message) {
            if (errorCode == 622) {
                showToast("您已向主播发起连麦申请，等待主播应答");
            } else {
                mLastApplyTs = 0;
                showToast(errorCodeToMessage(errorCode, message));
                SolutionDemoEventManager.post(new InviteAudienceEvent(
                        SolutionDataManager.ins().getUserId(),
                        LiveDataManager.INVITE_REPLY_TIMEOUT));
                mLiveRoomControls.setAddGuestBtnStatus(LiveRoomControlsLayout.STATUS_NORMAL);
            }
        }
    };

    //结束互动回调
    private final IRequestCallback<LiveResponse> mInteractResponse = new IRequestCallback<LiveResponse>() {
        @Override
        public void onSuccess(LiveResponse data) {

        }

        @Override
        public void onError(int errorCode, String message) {
            showToast(errorCodeToMessage(errorCode, message));
        }
    };

    //观众离开直播间回调
    private final IRequestCallback<LiveResponse> mLeaveLiveCallback = new IRequestCallback<LiveResponse>() {
        @Override
        public void onSuccess(LiveResponse data) {

        }

        @Override
        public void onError(int errorCode, String message) {

        }
    };

    //主播结束直播回调
    private final IRequestCallback<LiveResponse> mFinishLiveCallback = new IRequestCallback<LiveResponse>() {
        @Override
        public void onSuccess(LiveResponse data) {

        }

        @Override
        public void onError(int errorCode, String message) {

        }
    };

    //观众端 观众响应主播邀请回调
    private final IRequestCallback<LiveReplyResponse> mReplyInviteCallbackByAudience = new IRequestCallback<LiveReplyResponse>() {
        @Override
        public void onSuccess(LiveReplyResponse data) {
            stopPlayLiveStream();

            updateOnlineGuestList(data.rtcUserList);
            LiveRTCManager.ins().joinRoom(data.rtcRoomId, mSelfInfo.userId, data.rtcToken);
            LiveRTCManager.ins().startCaptureVideo(mSelfInfo.isCameraOn());
            LiveRTCManager.ins().startCaptureAudio(mSelfInfo.isMicOn());

            mRoomStatus = ROOM_STATUS_GUEST_INTERACT;
            mSelfInfo.status = USER_STATUS_AUDIENCE_INTERACTING;
            mSelfInfo.linkMicStatus = LiveDataManager.LINK_MIC_STATUS_AUDIENCE_INTERACTING;
            mLiveVideoLayout.setLiveUserInfo(mHostInfo, null);
            mLiveRoomControls.setAddGuestBtnStatus(STATUS_DISABLE);
            mLiveRoomControls.setRole(mSelfInfo.role, LiveDataManager.LINK_MIC_STATUS_AUDIENCE_INTERACTING);
        }

        @Override
        public void onError(int errorCode, String message) {
            showToast(errorCodeToMessage(errorCode, message));
        }
    };

    private final IRequestCallback<LiveAnchorPermitAudienceResponse> mReplyInviteCallbackByHost = new IRequestCallback<LiveAnchorPermitAudienceResponse>() {

        @Override
        public void onSuccess(LiveAnchorPermitAudienceResponse data) {
            mRoomStatus = ROOM_STATUS_GUEST_INTERACT;
            updateOnlineGuestList(data.userList);
            if (mGuestList.size() == 1) {
                showToast("点击观众画面可进行麦位管理");
                LiveRTCManager.ins().joinRoom(data.rtcRoomId, mSelfInfo.userId, data.rtcToken);
                LiveRTCManager.ins().startLiveTranscoding(mPushUrl, data.userList, data.rtcRoomId, false);
            } else {
                List<LiveUserInfo> audienceUserList = new ArrayList<>(mGuestList);
                if (mHostInfo != null) {
                    audienceUserList.add(mHostInfo.getDeepCopy());
                }
                LiveRTCManager.ins().updateLiveTranscoding(mPushUrl, audienceUserList, data.rtcRoomId, false);
            }

            for (LiveUserInfo info : mGuestList) {
                SolutionDemoEventManager.post(new InviteAudienceEvent(info.userId, LiveDataManager.INVITE_REPLY_ACCEPT));
            }
        }

        @Override
        public void onError(int errorCode, String message) {
            showToast(errorCodeToMessage(errorCode, message));
        }
    };

    //重连回调
    private final IRequestCallback<LiveReconnectResponse> mLiveReconnectCallback = new IRequestCallback<LiveReconnectResponse>() {
        @Override
        public void onSuccess(LiveReconnectResponse data) {
            if (data.recoverInfo == null) {
                SolutionToast.show("房间已解散，回到房间列表页");
                finish();
            } else {
                mSelfInfo = data.userInfo;
                mRoomStatus = data.interactStatus;
                setAudienceCount(data.recoverInfo.audienceCount);
                updateOnlineGuestList(data.interactUserList);
                initByJoinResponse(data.recoverInfo.liveRoomInfo, data.userInfo,
                        data.recoverInfo.audienceCount, data.recoverInfo.streamPullUrl);
            }
        }

        @Override
        public void onError(int errorCode, String message) {
            showToast(errorCodeToMessage(errorCode, message));
        }
    };

    private final IRequestCallback<LiveResponse> mLiveUpdateResolution = new IRequestCallback<LiveResponse>() {
        @Override
        public void onSuccess(LiveResponse data) {

        }

        @Override
        public void onError(int errorCode, String message) {

        }
    };

    private final IMainOption mMainOption = new IMainOption() {
        @Override
        public void onCoHostClick() {
            if (mRoomStatus == ROOM_STATUS_CO_HOST) {
                openFinishCoHostDialog();
            } else {
                openInviteCoHostDialog();
            }
        }

        @Override
        public void onAddGuestClick() {
            onGuestClick();
        }

        @Override
        public void onVideoEffectClick() {
            openVideoEffectDialog();
        }

        @Override
        public void onSettingClick() {
            openSettingDialog();
        }

        @Override
        public void onExitClick() {
            openLeaveDialog();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room_main);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        super.onGlobalLayoutCompleted();
        mAvatarView = findViewById(R.id.host_avatar);
        mLiveRoomControls = findViewById(R.id.main_controls);
        mLiveRoomControls.setMainOption(mMainOption);
        mAudienceCountTv = findViewById(R.id.audience_num_tv);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_audience);
        drawable.setBounds(0, 0,
                (int) Utilities.dip2Px(22), (int) Utilities.dip2Px(20));
        mAudienceCountTv.setCompoundDrawables(drawable, null, null, null);
        mLiveStreamContainer = findViewById(R.id.live_stream_container);
        mLiveVideoLayout = findViewById(R.id.main_container);
        mGuestListLayout = findViewById(R.id.audience_list_rv);
        mTopTip = findViewById(R.id.main_disconnect_tip);

        mLiveChatAdapter = new LiveChatAdapter();
        mLiveChatRv = findViewById(R.id.message_rv);
        mLiveChatRv.setLayoutManager(new LinearLayoutManager(LiveRoomMainActivity.this, RecyclerView.VERTICAL, false));
        mLiveChatRv.setAdapter(mLiveChatAdapter);

        if (hasLayouted) {
            return;
        }
        hasLayouted = true;

        if (!checkArgs()) {
            showToast("参数错误");
        }

        SolutionDemoEventManager.register(this);
    }

    private boolean checkArgs() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        String refer = intent.getStringExtra(EXTRA_REFER);
        if (TextUtils.equals(REFER_CREATE, refer)) {
            LiveRoomInfo roomInfo = GsonUtils.gson().fromJson(intent.getStringExtra(EXTRA_ROOM_INFO), LiveRoomInfo.class);
            LiveUserInfo userInfo = GsonUtils.gson().fromJson(intent.getStringExtra(EXTRA_USER_INFO), LiveUserInfo.class);
            mPushUrl = intent.getStringExtra(EXTRA_PUSH_URL);
            mRTCToken = intent.getStringExtra(EXTRA_TOKEN);
            if (roomInfo != null && userInfo != null) {
                mSelfInfo = userInfo;
                mLiveRoomInfo = roomInfo;
                LiveRTCManager.ins().startCaptureAudio(true);
                LiveRTCManager.ins().startCaptureVideo(true);
                initByJoinResponse(mLiveRoomInfo, mSelfInfo, 0, null);
                return true;
            } else {
                return false;
            }
        } else if (TextUtils.equals(REFER_LIST, refer)) {
            String roomId = intent.getStringExtra(EXTRA_ROOM_ID);
            String hostId = intent.getStringExtra(EXTRA_HOST_ID);
            if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(hostId)) {
                return false;
            } else {
                LiveRTCManager.ins().getRTMClient().requestJoinLiveRoom(roomId, mJoinRoomCallback);
                return true;
            }
        } else if (TextUtils.equals(refer, REFER_RECONNECT)) {
            ReconnectInfo reconnectInfo = GsonUtils.gson().fromJson(
                    intent.getStringExtra(EXTRA_RECONNECT_INFO), ReconnectInfo.class);
            LiveUserInfo userInfo = GsonUtils.gson().fromJson(
                    intent.getStringExtra(EXTRA_USER_INFO), LiveUserInfo.class);
            int status = intent.getIntExtra(EXTRA_INTERACT_STATUS, ROOM_STATUS_LIVE);
            String usersJson = intent.getStringExtra(EXTRA_INTERACT_USERS);
            if (reconnectInfo != null && userInfo != null) {
                mLiveRoomInfo = reconnectInfo.liveRoomInfo;
                mSelfInfo = userInfo;
                mRoomStatus = status;
                setAudienceCount(reconnectInfo.audienceCount);
                mPushUrl = reconnectInfo.streamPushUrl;
                mRTCToken = reconnectInfo.rtcToken;
                LiveRTCManager.ins().turnOnMic(mSelfInfo.isMicOn());
                LiveRTCManager.ins().turnOnCamera(mSelfInfo.isCameraOn());
                if (mSelfInfo.status != USER_STATUS_OTHER) {
                    LiveRTCManager.ins().startCaptureAudio(mSelfInfo.isMicOn());
                    LiveRTCManager.ins().startCaptureVideo(mSelfInfo.isCameraOn());
                    mGuestList.clear();
                    if (!TextUtils.isEmpty(usersJson)) {
                        List<LiveUserInfo> users = GsonUtils.gson().fromJson(usersJson,
                                new TypeToken<List<LiveUserInfo>>() {
                                }.getType());
                        if (status == ROOM_STATUS_CO_HOST && users != null) {
                            for (LiveUserInfo info : users) {
                                if (!TextUtils.equals(info.userId, SolutionDataManager.ins().getUserId())) {
                                    mCoHostInfo = info;
                                    break;
                                }
                            }
                        } else if (status == ROOM_STATUS_GUEST_INTERACT) {
                            updateOnlineGuestList(users);
                        }
                    }
                }
                initByJoinResponse(mLiveRoomInfo, mSelfInfo, mAudienceCount, reconnectInfo.streamPullUrl);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        openLeaveDialog();
    }

    @Override
    public void finish() {
        super.finish();
        SolutionDemoEventManager.unregister(this);

        if (mLiveRoomInfo == null || mSelfInfo == null) {
            return;
        }
        String roomId = mLiveRoomInfo.roomId;
        if (mSelfInfo.role == USER_ROLE_AUDIENCE) {
            if (mRoomStatus == ROOM_STATUS_GUEST_INTERACT) {
                showToast("主播已和你断开连线");
            }
            if (!isLeaveByKickOut) {
                LiveRTCManager.ins().getRTMClient().requestLeaveLiveRoom(roomId, mLeaveLiveCallback);
            }
        } else if (mSelfInfo.role == USER_ROLE_HOST) {
            if (mRoomStatus == ROOM_STATUS_GUEST_INTERACT) {
                showToast("主播已断开连线");
            }
            if (!isLeaveByKickOut) {
                LiveRTCManager.ins().getRTMClient().requestFinishLive(roomId, mFinishLiveCallback);
            }
        }

        LiveRTCManager.ins().leaveRTMRoom();
        LiveRTCManager.ins().leaveRoom();
        LivePushManager.ins().stopPush();
        LivePlayerManager.ins().stopPull();
    }

    public void initByJoinResponse(LiveRoomInfo liveRoomInfo, LiveUserInfo liveUserInfo,
                                   int audienceCount, Map<String, String> pullStreamMap) {
        mLiveRoomInfo = liveRoomInfo;
        mSelfInfo = liveUserInfo;
        mAudienceCount = audienceCount;
        mPullStreamMap.clear();
        if (pullStreamMap != null) {
            mPullStreamMap.putAll(pullStreamMap);
        }

        LiveRTCManager.ins().joinRTMRoom(mLiveRoomInfo.roomId, mSelfInfo.userId, mRTCToken);
        if (mSelfInfo.role != USER_ROLE_HOST) {
            LiveRTCManager.ins().startCaptureAudio(false);
            LiveRTCManager.ins().startCaptureVideo(false);
        }

        //UI
        mAvatarView.setUserName(liveRoomInfo == null ? null : liveRoomInfo.anchorUserName);
        mLiveRoomControls.setRole(liveUserInfo.role, liveUserInfo.linkMicStatus);
        setAudienceCount(audienceCount);

        if (liveUserInfo.role == USER_ROLE_HOST) {
            if (mRoomStatus == ROOM_STATUS_CO_HOST) {
                updateOnlineGuestList(null);
                mLiveVideoLayout.setLiveUserInfo(mSelfInfo, mCoHostInfo);
            } else if (mRoomStatus == ROOM_STATUS_GUEST_INTERACT) {
                mLiveVideoLayout.setLiveUserInfo(mSelfInfo, null);
                updateOnlineGuestList();
            } else {
                mLiveVideoLayout.setLiveUserInfo(liveUserInfo, null);
                LiveRTCManager.ins().startHostLive(mPushUrl);
                updateOnlineGuestList(null);
            }

            LiveRTCManager.ins().getRTMClient().updateResolution(mLiveRoomInfo.roomId, LiveRTCManager.ins().getWidth(),
                    LiveRTCManager.ins().getHeight(), mLiveUpdateResolution);
        } else {
            if (mHostInfo.isCameraOn()) {
                playLiveStream(true);
            } else {
                mLiveVideoLayout.setLiveUserInfo(mHostInfo, null);
                stopPlayLiveStream();
            }
            // 观众端进房时需要调整色块遮罩
            addOrRemoveBlock(liveRoomInfo != null && (mHostInfo.linkMicStatus == LINK_MIC_STATUS_HOST_INTERACTING));
            updateOnlineGuestList(null);
            mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);
            mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);

            //观众永远都是256 * 256
            LiveRTCManager.ins().setResolution(256, 256);
        }
    }

    private void setAudienceCount(int count) {
        mAudienceCount = count;
        mAudienceCountTv.setText(String.format(Locale.US, "%d", mAudienceCount));
    }

    private void openFinishCoHostDialog() {
        final CommonDialog dialog = new CommonDialog(this);
        dialog.setCancelable(true);
        dialog.setMessage("确认断开连线");
        dialog.setNegativeListener((v) -> dialog.dismiss());
        dialog.setPositiveListener((v) -> {
            dialog.dismiss();
            LiveRTCManager.ins().getRTMClient().finishHostLink(mLinkId, mLiveRoomInfo.roomId, mInteractResponse);
        });
        dialog.show();
    }

    private void openInviteCoHostDialog() {
        if (mRoomStatus == ROOM_STATUS_GUEST_INTERACT) {
            showToast("观众连线中，无法发起主播连线");
            return;
        }
        LiveCoHostDialog liveCoHostDialog = new LiveCoHostDialog(this, info ->
                LiveRTCManager.ins().getRTMClient().inviteHostByHost(mLiveRoomInfo.roomId, mLiveRoomInfo.anchorUserId,
                info.roomId, info.userId, "", mInviteHostResponse));
        liveCoHostDialog.show();
    }

    /**
     * 观众连麦操作按钮点击
     */
    private void onGuestClick() {
        if (mSelfInfo == null) {
            return;
        }
        if (mSelfInfo.role == USER_ROLE_AUDIENCE) {
            if (mSelfInfo.linkMicStatus == LiveDataManager.LINK_MIC_STATUS_OTHER) {
                if (mRoomStatus == ROOM_STATUS_CO_HOST) {
                    SolutionToast.show("主播连线中，无法发起观众连线");
                    return;
                }
                RequestInteractDialog dialog = new RequestInteractDialog(LiveRoomMainActivity.this, mLastApplyTs, () -> {
                    LiveRTCManager.ins().getRTMClient().requestLinkByAudience(mLiveRoomInfo.roomId, mGuestApplyResponse);
                    mLiveRoomControls.setAddGuestBtnStatus(LiveRoomControlsLayout.STATUS_NORMAL);
                });
                dialog.show();
            } else if (mSelfInfo.linkMicStatus == LiveDataManager.LINK_MIC_STATUS_AUDIENCE_INTERACTING) {
                final CommonDialog dialog = new CommonDialog(this);
                dialog.setCancelable(true);
                dialog.setMessage("是否与主播断开连接");
                dialog.setNegativeListener((v) -> dialog.dismiss());
                dialog.setPositiveListener((v) -> {
                    dialog.dismiss();
                    LiveRTCManager.ins().getRTMClient().finishAudienceLinkByAudience("",
                            mLiveRoomInfo.roomId, mInteractResponse);
                });
                dialog.show();
            }
        } else if (mSelfInfo.role == USER_ROLE_HOST) {
            if (mRoomStatus == ROOM_STATUS_CO_HOST) {
                showToast("主播连线中，无法发起观众连线");
                return;
            }
            final AddGuestsDialog dialog = new AddGuestsDialog(this, mLiveRoomInfo.roomId,
                    this::inviteAudienceByHost);
            dialog.show();
        }
    }

    private void inviteAudienceByHost(LiveUserInfo info) {
        if (info == null) {
            return;
        }
        IRequestCallback<LiveInviteResponse> callback = new IRequestCallback<LiveInviteResponse>() {
            @Override
            public void onSuccess(LiveInviteResponse data) {
                showToast("已发出邀请，等待对方应答");
                SolutionDemoEventManager.post(new InviteAudienceEvent(info.userId,
                        LiveDataManager.INVITE_REPLY_WAITING));
            }

            @Override
            public void onError(int errorCode, String message) {
                if (errorCode == 622) {
                    showToast("已发出邀请，等待对方应答");
                } else {
                    SolutionToast.show(errorCodeToMessage(errorCode, message));
                    SolutionDemoEventManager.post(new InviteAudienceEvent(info.userId,
                            LiveDataManager.INVITE_REPLY_TIMEOUT));
                }
            }
        };
        LiveRTCManager.ins().getRTMClient().inviteAudienceByHost(mLiveRoomInfo.roomId, mLiveRoomInfo.anchorUserId,
                mLiveRoomInfo.roomId, info.userId, "", callback);
    }

    private void openVideoEffectDialog() {
        new EffectDialog(this).show();
    }

    private void openSettingDialog() {
        if (mLiveRoomInfo == null || mSelfInfo == null) {
            showToast("");
            return;
        }
        if (mSelfInfo.role == USER_ROLE_HOST) {
            LiveSettingDialog settingDialog = new LiveSettingDialog(this, true, mLiveRoomInfo.roomId);
            settingDialog.show();
        } else {
            AudienceSettingDialog settingDialog = new AudienceSettingDialog(this, mSelfInfo.status, mLiveRoomInfo.roomId);
            settingDialog.show();
        }
    }

    private void openLeaveDialog() {
        if (mLiveRoomInfo == null || mSelfInfo == null) {
            finish();
            return;
        }
        if (mSelfInfo.role == USER_ROLE_HOST) {
            final CommonDialog dialog = new CommonDialog(this);
            dialog.setCancelable(true);
            dialog.setMessage("确认结束直播");
            dialog.setNegativeListener((v) -> dialog.dismiss());
            dialog.setPositiveListener((v) -> finish());
            dialog.show();
        } else {
            finish();
        }
    }

    private String errorCodeToMessage(int code, String originMessage) {
        String message;
        switch (code) {
            case 401:
                message = "主播邀请中，无法发起连线";
                break;
            case 402:
                message = "主播连线中，无法发起连线";
                break;
            case 481:
                message = "主播邀请中，无法发起连麦";
                break;
            case 482:
                message = "当前申请人数已达上限";
                break;
            case 472:
            case 483:
                message = "当前麦位已满";
                break;
            case 611:
                message = "连麦已过期";
                break;
            case 630:
                message = "主播正在连线中";
                break;
            case 632:
            case 642:
                message = "主播正在发起双主播连线";
                break;
            case 638:
            case 645:
                message = "正在等待被邀主播的应答";
                break;
            case 643:
                message = "与观众连线中，无法发起主播连线";
                break;
            case 634:
            case 644:
                message = "主播连线中";
                break;
            default:
                message = originMessage;
        }
        return message;
    }

    private void showToast(String message) {
        SolutionToast.show(message);
    }

    private void addChatMessage(String message) {
        mLiveChatAdapter.addChatMsg(message);
        mLiveChatRv.post(() -> mLiveChatRv.smoothScrollToPosition(mLiveChatAdapter.getItemCount()));
    }

    private void playLiveStream() {
        playLiveStream(true);
    }

    private void playLiveStream(boolean forceUpdate) {
        if (mPullStreamMap.isEmpty()) {
            Log.d(TAG, "playLiveStream: pullStream map is empty");
            return;
        }
        mLiveVideoLayout.setVisibility(View.GONE);
        if (forceUpdate) {
            mLiveStreamContainer.removeAllViews();
            TextureView renderView = LivePlayerManager.ins().getPlayView();
            Utilities.removeFromParent(renderView);
            int height = WindowUtils.getScreenWidth(this) * 16 / 9;
            int y = (WindowUtils.getScreenHeight(this) - height) / 2;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, height);
            params.topMargin = y;

            mLiveStreamContainer.addView(renderView, params);
        }

        String url = mPullStreamMap.get(LiveRTCManager.ins().getPlayLiveStreamResolution());
        Log.d(TAG, "playLiveStream: play: " + url);
        LivePlayerManager.ins().playLive(url);
    }

    private void stopPlayLiveStream() {
        mLiveStreamContainer.removeAllViews();
        LivePlayerManager.ins().stopPull();
        mLiveVideoLayout.setVisibility(View.VISIBLE);
    }

    private void updateOnlineGuestList() {
        mGuestListLayout.setUserList(mLiveRoomInfo.roomId, mLiveRoomInfo.anchorUserId,
                mSelfInfo.role == USER_ROLE_HOST, mGuestList);
    }

    /**
     * 该方法会用 入参 中的列表刷新房主、连麦主播、自己的状态信息
     * @param userList 用来刷新房间内用户的数据
     */
    private void updateOnlineGuestList(List<LiveUserInfo> userList) {
        mGuestList.clear();
        if (userList != null) {
            for (LiveUserInfo userInfo : userList) {
                if (!TextUtils.equals(userInfo.userId, mLiveRoomInfo.anchorUserId)) {
                    if (mRoomStatus == ROOM_STATUS_CO_HOST) {
                        mCoHostInfo = userInfo;
                    } else {
                        mGuestList.add(userInfo);
                    }
                } else {
                    mHostInfo = userInfo;
                }
                if (TextUtils.equals(userInfo.userId, mSelfInfo.userId)) {
                    mSelfInfo = userInfo.getDeepCopy();
                    if (mSelfInfo.role == USER_ROLE_HOST && mRoomStatus == ROOM_STATUS_CO_HOST) {
                        mSelfInfo.status = USER_STATUS_CO_HOSTING;
                        mSelfInfo.linkMicStatus = LINK_MIC_STATUS_HOST_INTERACTING;
                    } else {
                        mSelfInfo.status = USER_STATUS_AUDIENCE_INTERACTING;
                        mSelfInfo.linkMicStatus = LiveDataManager.LINK_MIC_STATUS_AUDIENCE_INTERACTING;
                    }
                }
            }
        }
        mGuestListLayout.setUserList(mLiveRoomInfo.roomId, mLiveRoomInfo.anchorUserId,
                mSelfInfo.role == USER_ROLE_HOST, mGuestList);
    }

    private void showTopTip() {
        mTopTip.setVisibility(View.VISIBLE);
    }

    private void hideTopTip() {
        mTopTip.setVisibility(View.GONE);
    }

    /**
     * 观众端观看直播时是否增加色块遮罩
     * @param add 添加
     */
    private void addOrRemoveBlock(boolean add) {
        int childViewCount = mLiveStreamContainer.getChildCount();
        if (add && childViewCount == 1) {
            int height = WindowUtils.getScreenWidth(this) * 16 / 9;
            int blockHeight = (mLiveStreamContainer.getHeight() - height / 2) / 2;
            View topView = new View(this);
            topView.setBackgroundColor(Color.parseColor("#272E3B"));
            FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, blockHeight);
            mLiveStreamContainer.addView(topView, topParams);

            View bottomView = new View(this);
            bottomView.setBackgroundColor(Color.parseColor("#272E3B"));
            FrameLayout.LayoutParams bottomParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, blockHeight);
            bottomParams.topMargin = mLiveStreamContainer.getHeight() - blockHeight;
            mLiveStreamContainer.addView(bottomView, bottomParams);
        } else if (!add && childViewCount != 1) {
            LinkedList<View> removingViews = new LinkedList<>();
            for (int i = 0; i < mLiveStreamContainer.getChildCount(); i++) {
                View view = mLiveStreamContainer.getChildAt(i);
                if (view instanceof SurfaceView || view instanceof TextureView) {
                    continue;
                }
                removingViews.add(view);
            }
            for (View view : removingViews) {
                mLiveStreamContainer.removeView(view);
            }
        }
    }

    public static void startFromList(Activity activity, String roomId, String hostId, int requestCode) {
        Intent intent = new Intent(activity, LiveRoomMainActivity.class);
        intent.putExtra(EXTRA_REFER, REFER_LIST);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.putExtra(EXTRA_HOST_ID, hostId);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startFromCreate(Activity activity, LiveRoomInfo roomInfo, LiveUserInfo userInfo,
                                       String pushUrl, String rtcToken) {
        Intent intent = new Intent(activity, LiveRoomMainActivity.class);
        intent.putExtra(EXTRA_REFER, REFER_CREATE);
        intent.putExtra(EXTRA_ROOM_INFO, GsonUtils.gson().toJson(roomInfo));
        intent.putExtra(EXTRA_USER_INFO, GsonUtils.gson().toJson(userInfo));
        intent.putExtra(EXTRA_PUSH_URL, pushUrl);
        intent.putExtra(EXTRA_TOKEN, rtcToken);
        activity.startActivity(intent);
    }

    public static void startFromReconnect(Activity activity, ReconnectInfo reconnectInfo,
                                          LiveUserInfo selfInfo, int interactStatus,
                                          List<LiveUserInfo> interactUsers) {
        Intent intent = new Intent(activity, LiveRoomMainActivity.class);
        intent.putExtra(EXTRA_REFER, REFER_RECONNECT);
        intent.putExtra(EXTRA_USER_INFO, GsonUtils.gson().toJson(selfInfo));
        intent.putExtra(EXTRA_RECONNECT_INFO, GsonUtils.gson().toJson(reconnectInfo));
        intent.putExtra(EXTRA_INTERACT_STATUS, interactStatus);
        intent.putExtra(EXTRA_INTERACT_USERS, GsonUtils.gson().toJson(interactUsers));
        activity.startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveRoomUserEvent(LiveRoomUserEvent event) {
        String chatMessage;
        if (event.isJoin) {
            chatMessage = String.format(Locale.US, "%s 加入了房间", event.audienceUserName);
        } else {
            chatMessage = String.format(Locale.US, "%s 离开了房间", event.audienceUserName);
        }
        addChatMessage(chatMessage);

        setAudienceCount(event.audienceCount);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveFinishEvent(LiveFinishEvent event) {
        if (TextUtils.equals(event.roomId, mLiveRoomInfo.roomId)) {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceMediaUpdateEvent(AudienceMediaUpdateEvent event) {
        if (event.camera == LiveDataManager.MEDIA_STATUS_OFF) {
            showToast("你的摄像头已被主播关闭");
            LiveRTCManager.ins().startCaptureVideo(false);
        }
        if (event.mic == LiveDataManager.MEDIA_STATUS_OFF) {
            showToast("你已被主播禁麦");
            LiveRTCManager.ins().startCaptureAudio(false);
        }
        int mic = LiveRTCManager.ins().isMicOn() ? MEDIA_STATUS_ON : MEDIA_STATUS_OFF;
        int camera = LiveRTCManager.ins().isCameraOn() ? MEDIA_STATUS_ON : MEDIA_STATUS_OFF;
        LiveRTCManager.ins().getRTMClient().updateMediaStatus(event.guestRoomId, mic, camera,
                new IRequestCallback<LiveResponse>() {
                    @Override
                    public void onSuccess(LiveResponse data) {

                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        showToast(errorCodeToMessage(errorCode, message));
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaChangedEvent(MediaChangedEvent event) {
        //ui 连麦主播界面变化，观众列表状态变化
        if (mSelfInfo.role == USER_ROLE_HOST) {
            if (mRoomStatus == ROOM_STATUS_CO_HOST) {
                if (mCoHostInfo != null && TextUtils.equals(event.userId, mCoHostInfo.userId)) {
                    mCoHostInfo.micStatus = event.mic;
                    mCoHostInfo.cameraStatus = event.camera;
                    mLiveVideoLayout.setLiveUserInfo(mSelfInfo, mCoHostInfo);
                } else if (TextUtils.equals(event.userId, mSelfInfo.userId)) {
                    mSelfInfo.micStatus = event.mic;
                    mSelfInfo.cameraStatus = event.camera;
                    mLiveVideoLayout.setLiveUserInfo(mSelfInfo, mCoHostInfo);
                }
            } else {
                if (TextUtils.equals(event.userId, mSelfInfo.userId)) {
                    mSelfInfo.micStatus = event.mic;
                    mSelfInfo.cameraStatus = event.camera;
                    LiveRTCManager.ins().startCaptureAudio(event.mic == MEDIA_STATUS_ON);
                    LiveRTCManager.ins().startCaptureVideo(event.camera == MEDIA_STATUS_ON);
                    mLiveVideoLayout.setLiveUserInfo(mSelfInfo, null);
                }
                if (mRoomStatus == ROOM_STATUS_GUEST_INTERACT) {
                    for (LiveUserInfo userInfo : mGuestList) {
                        if (TextUtils.equals(event.userId, userInfo.userId)) {
                            userInfo.micStatus = event.mic;
                            userInfo.cameraStatus = event.camera;
                            break;
                        }
                    }
                    updateOnlineGuestList();
                }
            }
        } else {
            boolean isSelfInteract = false;
            for (LiveUserInfo userInfo : mGuestList) {
                if (TextUtils.equals(userInfo.userId, mSelfInfo.userId)) {
                    isSelfInteract = true;
                    break;
                }
            }
            if (mHostInfo != null && TextUtils.equals(event.userId, mHostInfo.userId)) {
                if (event.camera == LiveDataManager.MEDIA_STATUS_ON && !isSelfInteract) {
                    // 只有当主播摄像头上次是关闭，且本次打开的情况下，触发拉流控件的强制刷新
                    playLiveStream(!mHostInfo.isCameraOn());
                } else {
                    stopPlayLiveStream();
                }
                mHostInfo.micStatus = event.mic;
                mHostInfo.cameraStatus = event.camera;
                mLiveVideoLayout.setLiveUserInfo(mHostInfo, null);
            }
            if (mRoomStatus == ROOM_STATUS_GUEST_INTERACT) {
                for (LiveUserInfo userInfo : mGuestList) {
                    if (TextUtils.equals(event.userId, userInfo.userId)) {
                        userInfo.micStatus = event.mic;
                        userInfo.cameraStatus = event.camera;
                        break;
                    }
                }
                updateOnlineGuestList();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserTemporaryLeaveEvent(UserTemporaryLeaveEvent event) {
        if (TextUtils.equals(event.userId, mLiveRoomInfo.anchorUserId) || mCoHostInfo != null) {
            showToast("主播暂时离开");
        } else {
            showToast("嘉宾暂时离开");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveReconnectEvent(LiveReconnectEvent event) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdatePullStreamEvent(UpdatePullStreamEvent event) {
        //TT拉流失败问题解决
        if (mSelfInfo.role == USER_ROLE_AUDIENCE) {
            playLiveStream(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveKickUserEvent(LiveKickUserEvent event) {
        showToast("相同ID用户已登录，您已被强制下线");
        isLeaveByKickOut = true;
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSocketConnectEvent(SocketConnectEvent event) {
        if (event.status == SocketConnectEvent.ConnectStatus.RECONNECTED) {
            LiveRTCManager.ins().getRTMClient().requestLiveReconnect(mLiveReconnectCallback);
        }
        if (event.status == SocketConnectEvent.ConnectStatus.DISCONNECTED
                || event.status == SocketConnectEvent.ConnectStatus.CONNECTING) {
            showTopTip();
        } else {
            hideTopTip();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkConnectEvent(NetworkConnectEvent event) {
        if (event.isConnect) {
            hideTopTip();
        } else {
            showTopTip();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkQualityEvent(NetworkQualityEvent event) {
        boolean isGood = event.quality == NETWORK_QUALITY_EXCELLENT
                || event.quality == NETWORK_QUALITY_GOOD;
        mLiveVideoLayout.updateNetStatus(event.userId, isGood);
        mGuestListLayout.updateNetStatus(event.userId, isGood);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLinkMicStatusEvent(LinkMicStatusEvent event) {
        mRoomStatus = event.linkMicStatus;
        // 不是主播，需要调整遮罩位置
        if (mSelfInfo.role != USER_ROLE_HOST) {
            if (mRoomStatus == LINK_MIC_STATUS_OTHER) {
                addOrRemoveBlock(false);
            } else if (mRoomStatus == LINK_MIC_STATUS_HOST_INTERACTING) {
                addOrRemoveBlock(true);
            }
        }
    }

    // 观众连麦 观众端 主播邀请观众上麦通知
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkInviteEvent(AudienceLinkInviteEvent event) {
        final CommonDialog dialog = new InviteResultDialog(this);
        dialog.setCancelable(true);
        dialog.setMessage("是否接受主播的连麦邀请");
        dialog.setNegativeListener((v) -> {
            LiveRTCManager.ins().getRTMClient().replyHostInviterByAudience(event.linkerId, mLiveRoomInfo.roomId,
                    LiveDataManager.LIVE_PERMIT_TYPE_REJECT, new IRequestCallback<LiveReplyResponse>() {
                        @Override
                        public void onSuccess(LiveReplyResponse data) {

                        }

                        @Override
                        public void onError(int errorCode, String message) {

                        }
                    });
            dialog.dismiss();
        });
        dialog.setPositiveListener((v) -> {
            LiveRTCManager.ins().getRTMClient().replyHostInviterByAudience(event.linkerId, mLiveRoomInfo.roomId,
                    LiveDataManager.LIVE_PERMIT_TYPE_ACCEPT, mReplyInviteCallbackByAudience);
            dialog.dismiss();
        });
        dialog.show();
    }

    // 观众连麦 主播端 观众向主播申请上麦通知
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkApplyEvent(AudienceLinkApplyEvent event) {
        final CommonDialog dialog = new InviteResultDialog(this);
        dialog.setCancelable(true);
        dialog.setMessage(String.format("观众%s向你发来连线邀请", event.applicant.userName));
        dialog.setNegativeListener((v) -> {
            LiveRTCManager.ins().getRTMClient().replyAudienceRequestByHost(event.linkerId, mLiveRoomInfo.roomId,
                    mLiveRoomInfo.anchorUserId, mLiveRoomInfo.roomId, event.applicant.userId,
                    LiveDataManager.LIVE_PERMIT_TYPE_REJECT, new IRequestCallback<LiveAnchorPermitAudienceResponse>() {
                        @Override
                        public void onSuccess(LiveAnchorPermitAudienceResponse data) {

                        }

                        @Override
                        public void onError(int errorCode, String message) {

                        }
                    });
            dialog.dismiss();
        });
        dialog.setPositiveListener((v) -> {
            LiveRTCManager.ins().getRTMClient().replyAudienceRequestByHost(event.linkerId, mLiveRoomInfo.roomId,
                    mLiveRoomInfo.anchorUserId, mLiveRoomInfo.roomId, event.applicant.userId,
                    LiveDataManager.LIVE_PERMIT_TYPE_ACCEPT, mReplyInviteCallbackByHost);
            dialog.dismiss();
        });
        dialog.show();
    }

    // 观众连麦 主播端 观众回复主播邀请结果
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkReplyEvent(AudienceLinkReplyEvent event) {
        if (event.replyType == INVITE_REPLY_REJECT) {
            showToast(String.format("观众%s拒绝与你连麦", event.userInfo.userName));
        } else if (event.replyType == INVITE_REPLY_ACCEPT) {
            updateOnlineGuestList(event.rtcUserList);
            if (mGuestList.size() == 1) {
                showToast("点击观众画面可进行麦位管理");
                LiveRTCManager.ins().joinRoom(event.rtcRoomId, mSelfInfo.userId, event.rtcToken);
                LiveRTCManager.ins().startLiveTranscoding(mPushUrl, event.rtcUserList, event.rtcRoomId, false);
            } else {
                List<LiveUserInfo> audienceUserList = new ArrayList<>(mGuestList);
                if (mHostInfo != null) {
                    audienceUserList.add(mHostInfo.getDeepCopy());
                }
                LiveRTCManager.ins().updateLiveTranscoding(mPushUrl, audienceUserList, event.rtcRoomId, false);
            }
        }
    }

    // 观众连麦 观众端 主播确认观众申请结果
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkPermitEvent(AudienceLinkPermitEvent event) {
        mLastApplyTs = 0;
        if (event.permitType == LIVE_PERMIT_TYPE_REJECT) {
            showToast("主播暂时有点事，拒绝了你的邀请");
            mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);
            SolutionDemoEventManager.post(new InviteAudienceEvent(
                    SolutionDataManager.ins().getUserId(),
                    LiveDataManager.INVITE_REPLY_REJECT));
        } else if (event.permitType == LIVE_PERMIT_TYPE_ACCEPT) {
            showToast("主播接受了您的连麦申请，即将开始连麦");
            stopPlayLiveStream();

            SolutionDemoEventManager.post(new InviteAudienceEvent(
                    SolutionDataManager.ins().getUserId(),
                    LiveDataManager.INVITE_REPLY_ACCEPT));

            updateOnlineGuestList(event.rtcUserList);
            LiveRTCManager.ins().joinRoom(event.rtcRoomId, mSelfInfo.userId, event.rtcToken);
            LiveRTCManager.ins().startCaptureVideo(mSelfInfo.isCameraOn());
            LiveRTCManager.ins().startCaptureAudio(mSelfInfo.isMicOn());

            mRoomStatus = ROOM_STATUS_GUEST_INTERACT;
            mSelfInfo.status = USER_STATUS_AUDIENCE_INTERACTING;
            mSelfInfo.linkMicStatus = LiveDataManager.LINK_MIC_STATUS_AUDIENCE_INTERACTING;
            mLiveVideoLayout.setLiveUserInfo(mHostInfo, null);
            mLiveRoomControls.setAddGuestBtnStatus(STATUS_DISABLE);
            mLiveRoomControls.setRole(mSelfInfo.role, LiveDataManager.LINK_MIC_STATUS_AUDIENCE_INTERACTING);
        }
    }

    // 主播连麦 主播端 收到主播连麦邀请
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnchorLinkInviteEvent(AnchorLinkInviteEvent event) {
        final CommonDialog dialog = new InviteResultDialog(this);
        dialog.setCancelable(true);
        dialog.setMessage(String.format("%s邀请你进行主播连线，是否接受", event.userInfo.userName));
        dialog.setNegativeListener((v) -> {
            LiveRTCManager.ins().getRTMClient().replyHostInviteeByHost(event.linkerId, event.userInfo.roomId,
                    event.userInfo.userId, mLiveRoomInfo.roomId, mLiveRoomInfo.anchorUserId,
                    LiveDataManager.LIVE_PERMIT_TYPE_REJECT, new IRequestCallback<LiveInviteResponse>() {
                        @Override
                        public void onSuccess(LiveInviteResponse data) {

                        }

                        @Override
                        public void onError(int errorCode, String message) {

                        }
                    });
            dialog.dismiss();
        });
        dialog.setPositiveListener((v) -> {
            mLinkId = event.linkerId;
            LiveRTCManager.ins().getRTMClient().replyHostInviteeByHost(event.linkerId, event.userInfo.roomId,
                    event.userInfo.userId, mLiveRoomInfo.roomId, mLiveRoomInfo.anchorUserId,
                    LiveDataManager.LIVE_PERMIT_TYPE_ACCEPT, mAnchorReplyInviteCallback);
            dialog.dismiss();
        });
        dialog.show();
    }

    // 主播连麦 主播端 收到主播连麦邀请结果
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnchorLinkReplyEvent(AnchorLinkReplyEvent event) {
        if (!TextUtils.equals(event.userInfo.userId, mSelfInfo.userId) && event.replyType == INVITE_REPLY_REJECT) {
            mRoomStatus = ROOM_STATUS_LIVE;
            showToast("主播暂时有点事，拒绝了你的邀请");
            mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);
        } else if (event.replyType == INVITE_REPLY_ACCEPT) {
            mRoomStatus = ROOM_STATUS_CO_HOST;
            mLinkId = event.linkerId;
            for (LiveUserInfo info : event.rtcUserList) {
                if (!TextUtils.equals(info.userId, SolutionDataManager.ins().getUserId())) {
                    mCoHostInfo = info;
                    break;
                }
            }
            mLiveRoomControls.setCoHostBtnStatus(STATUS_DISABLE);
            LiveRTCManager.ins().joinRoom(event.rtcRoomId, mSelfInfo.userId, event.rtcToken);
            mLiveVideoLayout.setLiveUserInfo(mSelfInfo, mCoHostInfo);
            LiveRTCManager.ins().startLiveTranscoding(mPushUrl, event.rtcUserList, event.rtcRoomId, true);
        }
    }

    // 观众连麦 观众加入或离开
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkStatusEvent(AudienceLinkStatusEvent event) {
        mLinkId = event.linkerId;
        if (event.isJoin) {
            mRoomStatus = ROOM_STATUS_GUEST_INTERACT;
            updateOnlineGuestList(event.userList);
            if (mSelfInfo.role == USER_ROLE_HOST) {
                if (mGuestList.size() == 1) {
                    showToast("点击观众画面可进行麦位管理");
                    LiveRTCManager.ins().startLiveTranscoding(mPushUrl, event.userList, event.rtcRoomId, false);
                } else {
                    List<LiveUserInfo> audienceUserList = new ArrayList<>(mGuestList);
                    if (mHostInfo != null) {
                        audienceUserList.add(mHostInfo.getDeepCopy());
                    }
                    LiveRTCManager.ins().updateLiveTranscoding(mPushUrl, audienceUserList, event.rtcRoomId, false);
                }
                for (LiveUserInfo info : mGuestList) {
                    SolutionDemoEventManager.post(new InviteAudienceEvent(info.userId, LiveDataManager.INVITE_REPLY_ACCEPT));
                }
            }
        } else {
            if (mSelfInfo.role == USER_ROLE_HOST) {
                String userName = "";
                for (LiveUserInfo userInfo : mGuestList) {
                    if (TextUtils.equals(userInfo.userId, event.userId)) {
                        userName = userInfo.userName;
                        break;
                    }
                }
                showToast(String.format("%s断开了与您的连麦", userName));
                updateOnlineGuestList(event.userList);
                List<LiveUserInfo> audienceUserList = new ArrayList<>(mGuestList);
                if (mHostInfo != null) {
                    audienceUserList.add(mHostInfo.getDeepCopy());
                }
                LiveRTCManager.ins().updateLiveTranscoding(mPushUrl, audienceUserList, event.rtcRoomId, false);
            } else {
                if (!TextUtils.equals(event.userId, mSelfInfo.userId)) {
                    updateOnlineGuestList(event.userList);
                } else {
                    showToast("主播已和你断开连接");
                    updateOnlineGuestList(null);
                    mLiveRoomControls.setRole(mSelfInfo.role, LiveDataManager.LINK_MIC_STATUS_OTHER);
                    mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);
                    mSelfInfo.linkMicStatus = LiveDataManager.LINK_MIC_STATUS_OTHER;
                    playLiveStream(true);
                    updateOnlineGuestList(null);
                    LiveRTCManager.ins().leaveRoom();
                }
            }
        }
    }

    // 本地接口请求成功后需要发送本地广播更新用户视图
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocalKickUserEvent(LocalKickUserEvent event) {
        for (int i = mGuestList.size() - 1; i >= 0; i--) {
            LiveUserInfo userInfo = mGuestList.get(i);
            if (TextUtils.equals(event.userId, userInfo.userId)) {
                mGuestList.remove(i);
            }
        }
        updateOnlineGuestList();
    }

    // 观众连麦 观众自己下麦 或者主播端所有嘉宾下麦
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkFinishEvent(AudienceLinkFinishEvent event) {
        mRoomStatus = ROOM_STATUS_LIVE;
        mSelfInfo.linkMicStatus = LiveDataManager.LINK_MIC_STATUS_OTHER;
        mLiveRoomControls.setCoHostBtnStatus(STATUS_NORMAL);
        mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);
        mLiveRoomControls.setRole(mSelfInfo.role, LiveDataManager.LINK_MIC_STATUS_OTHER);
        updateOnlineGuestList(null);
        if (mSelfInfo.role == USER_ROLE_HOST) {
            LiveRTCManager.ins().leaveRoomWhenFinishCoHost();
            LiveRTCManager.ins().startHostLive(mPushUrl);
        } else {
            LiveRTCManager.ins().leaveRoom();
            showToast("主播已和你断开连线");
            mLiveRoomControls.setRole(mSelfInfo.role, LiveDataManager.LINK_MIC_STATUS_OTHER);
            mLiveRoomControls.setCoHostBtnStatus(STATUS_NORMAL);
            updateOnlineGuestList(null);
            if (mHostInfo.isCameraOn()) {
                playLiveStream(true);
            } else {
                stopPlayLiveStream();
                mLiveVideoLayout.setLiveUserInfo(mHostInfo, null);
            }
            mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);
            mLiveRoomControls.setCoHostBtnStatus(STATUS_NORMAL);
        }
    }

    // 主播连麦 连麦结束
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnchorLinkFinishEvent(AnchorLinkFinishEvent event) {
        mRoomStatus = ROOM_STATUS_LIVE;
        showToast("主播已断开连线");
        LiveRTCManager.ins().leaveRoomWhenFinishCoHost();
        LiveRTCManager.ins().startHostLive(mPushUrl);
        updateOnlineGuestList(null);
        mLiveVideoLayout.setLiveUserInfo(mSelfInfo, null);
        mLiveRoomControls.setRole(USER_ROLE_HOST, LiveDataManager.LINK_MIC_STATUS_OTHER);
        mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);
        mLiveRoomControls.setCoHostBtnStatus(STATUS_NORMAL);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveHasBlockEvent(LiveHasBlockEvent event) {
        addOrRemoveBlock(event.hasBlock);
    }
}
