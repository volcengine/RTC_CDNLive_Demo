package com.volcengine.vertcdemo.interactivelive.feature.liveroommain;

import static com.ss.bytertc.engine.handler.IRTCEngineEventHandler.NetworkQuality.NETWORK_QUALITY_EXCELLENT;
import static com.ss.bytertc.engine.handler.IRTCEngineEventHandler.NetworkQuality.NETWORK_QUALITY_GOOD;
import static com.volcengine.vertcdemo.interactivelive.view.LiveRoomControlsLayout.STATUS_DISABLE;
import static com.volcengine.vertcdemo.interactivelive.view.LiveRoomControlsLayout.STATUS_NORMAL;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.INVITE_REPLY_ACCEPT;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.INVITE_REPLY_REJECT;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.LINK_MIC_STATUS_HOST_INTERACTING;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.LINK_MIC_STATUS_OTHER;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.LIVE_PERMIT_TYPE_ACCEPT;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.LIVE_PERMIT_TYPE_REJECT;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.MEDIA_STATUS_OFF;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.MEDIA_STATUS_ON;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.USER_ROLE_AUDIENCE;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.USER_ROLE_HOST;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.USER_STATUS_AUDIENCE_INTERACTING;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.USER_STATUS_CO_HOSTING;
import static com.volcengine.vertcdemo.interactivelive.core.LiveDataManager.USER_STATUS_OTHER;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
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
import com.volcengine.vertcdemo.interactivelive.bean.JoinLiveRoomResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveAnchorPermitAudienceResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveInviteResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveReconnectResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveReplyResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveRoomInfo;
import com.volcengine.vertcdemo.interactivelive.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelive.bean.ReconnectInfo;
import com.volcengine.vertcdemo.interactivelive.event.GiftEvent;
import com.volcengine.vertcdemo.interactivelive.view.AddGuestsDialog;
import com.volcengine.vertcdemo.interactivelive.view.AudienceGroupLayout;
import com.volcengine.vertcdemo.interactivelive.view.AudienceSettingDialog;
import com.volcengine.vertcdemo.interactivelive.view.AvatarView;
import com.volcengine.vertcdemo.interactivelive.view.GiftDialog;
import com.volcengine.vertcdemo.interactivelive.view.InviteResultDialog;
import com.volcengine.vertcdemo.interactivelive.view.LiveCoHostDialog;
import com.volcengine.vertcdemo.interactivelive.view.LiveRoomControlsLayout;
import com.volcengine.vertcdemo.interactivelive.view.LiveSettingDialog;
import com.volcengine.vertcdemo.interactivelive.view.LiveVideoLayout;
import com.volcengine.vertcdemo.interactivelive.view.RequestInteractDialog;
import com.volcengine.vertcdemo.interactivelive.core.LiveDataManager;
import com.volcengine.vertcdemo.interactivelive.core.LivePlayerManager;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;
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
import com.volcengine.vertcdemo.interactivelive.event.InviteAudienceEvent;
import com.volcengine.vertcdemo.interactivelive.event.LinkMicStatusEvent;
import com.volcengine.vertcdemo.interactivelive.event.LiveFinishEvent;
import com.volcengine.vertcdemo.interactivelive.event.LiveHasBlockEvent;
import com.volcengine.vertcdemo.interactivelive.event.LiveKickUserEvent;
import com.volcengine.vertcdemo.interactivelive.event.LiveReconnectEvent;
import com.volcengine.vertcdemo.interactivelive.event.LiveRoomUserEvent;
import com.volcengine.vertcdemo.interactivelive.event.LocalKickUserEvent;
import com.volcengine.vertcdemo.interactivelive.event.MediaChangedEvent;
import com.volcengine.vertcdemo.interactivelive.event.NetworkConnectEvent;
import com.volcengine.vertcdemo.interactivelive.event.NetworkQualityEvent;
import com.volcengine.vertcdemo.interactivelive.event.UpdatePullStreamEvent;
import com.volcengine.vertcdemo.interactivelive.event.UserTemporaryLeaveEvent;
import com.volcengine.vertcdemo.interactivelive.feature.createroom.effect.EffectDialog;

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
    private static final String EXTRA_RTM_TOKEN = "rtmToken";
    private static final String EXTRA_RTC_TOKEN = "rtcToken";
    private static final String EXTRA_RTC_ROOM_ID = "rtcRoomId";
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

    private long mLastApplyTs = 0; // ????????????????????????????????????????????????????????????4S?????????????????????
    private String mPushUrl;
    private String mRTCToken;
    private String mRTMToken;
    private String mRTCRoomId;
    private String mLinkId;
    private LiveUserInfo mSelfInfo; //???????????????
    private LiveUserInfo mHostInfo; //?????????????????????????????????????????????
    private LiveUserInfo mCoHostInfo; //????????????????????????????????????????????????
    private LiveRoomInfo mLiveRoomInfo; //????????????
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

    //??????????????????
    private final IRequestCallback<JoinLiveRoomResponse> mJoinRoomCallback =
            new IRequestCallback<JoinLiveRoomResponse>() {
                @Override
                public void onSuccess(JoinLiveRoomResponse data) {
                    mRoomStatus = data.liveRoomInfo.status;
                    mGuestList.clear();
                    mLiveRoomInfo = data.liveRoomInfo;
                    mSelfInfo = data.liveUserInfo;
                    mRTMToken = data.rtmToken;
                    mHostInfo = data.liveHostUserInfo;
                    updateOnlineGuestList(null);
                    initByJoinResponse(data.liveRoomInfo, data.liveUserInfo, data.liveRoomInfo.audienceCount,
                            data.liveRoomInfo.streamPullStreamList);
                }

                @Override
                public void onError(int errorCode, String message) {
                    String msg;
                    if (errorCode != 200) {
                        msg = "??????????????????????????????????????????";
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

    //????????????????????????
    private final IRequestCallback<LiveInviteResponse> mInviteHostResponse =
            new IRequestCallback<LiveInviteResponse>() {
                @Override
                public void onSuccess(LiveInviteResponse data) {
                    showToast("????????????????????????????????????");
                }

                @Override
                public void onError(int errorCode, String message) {
                    if (errorCode == 622) {
                        showToast("????????????????????????????????????");
                    } else {
                        showToast(errorCodeToMessage(errorCode, message));
                    }
                }
            };

    //??????????????????
    private final IRequestCallback<LiveInviteResponse> mAnchorReplyInviteCallback =
            new IRequestCallback<LiveInviteResponse>() {

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

                    LiveRTCManager.ins().startCaptureVideo(mSelfInfo.isCameraOn());
                    LiveRTCManager.ins().startCaptureAudio(mSelfInfo.isMicOn());
                    mLiveVideoLayout.setLiveUserInfo(mSelfInfo, mCoHostInfo);

                    LiveRTCManager.ins().startForwardStreamToRooms(data.rtcRoomId,
                            mCoHostInfo.userId, data.rtcToken, mRTCRoomId,
                            mSelfInfo.userId, mPushUrl);
                }

                @Override
                public void onError(int errorCode, String message) {
                    showToast(errorCodeToMessage(errorCode, message));
                }
            };


    //????????????????????????
    private final IRequestCallback<LiveInviteResponse> mGuestApplyResponse =
            new IRequestCallback<LiveInviteResponse>() {
                @Override
                public void onSuccess(LiveInviteResponse data) {
                    mLastApplyTs = System.currentTimeMillis();
                    showToast("??????????????????????????????????????????????????????");
                    mLiveRoomControls.setAddGuestBtnStatus(LiveRoomControlsLayout.STATUS_NORMAL);
                }

                @Override
                public void onError(int errorCode, String message) {
                    if (errorCode == 622) {
                        showToast("??????????????????????????????????????????????????????");
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

    //??????????????????
    private final IRequestCallback<LiveResponse> mInteractResponse =
            new IRequestCallback<LiveResponse>() {
                @Override
                public void onSuccess(LiveResponse data) {

                }

                @Override
                public void onError(int errorCode, String message) {
                    showToast(errorCodeToMessage(errorCode, message));
                }
            };

    //???????????????????????????
    private final IRequestCallback<LiveResponse> mLeaveLiveCallback =
            new IRequestCallback<LiveResponse>() {
                @Override
                public void onSuccess(LiveResponse data) {

                }

                @Override
                public void onError(int errorCode, String message) {

                }
            };

    //????????????????????????
    private final IRequestCallback<LiveResponse> mFinishLiveCallback =
            new IRequestCallback<LiveResponse>() {
                @Override
                public void onSuccess(LiveResponse data) {

                }

                @Override
                public void onError(int errorCode, String message) {

                }
            };

    //????????? ??????????????????????????????
    private final IRequestCallback<LiveReplyResponse> mReplyInviteCallbackByAudience =
            new IRequestCallback<LiveReplyResponse>() {
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

    private final IRequestCallback<LiveAnchorPermitAudienceResponse> mReplyInviteCallbackByHost =
            new IRequestCallback<LiveAnchorPermitAudienceResponse>() {

                @Override
                public void onSuccess(LiveAnchorPermitAudienceResponse data) {
                    mRoomStatus = ROOM_STATUS_GUEST_INTERACT;
                    updateOnlineGuestList(data.userList);
                    if (mGuestList.size() == 1) {
                        showToast("???????????????????????????????????????");
                        LiveRTCManager.ins().joinRoom(data.rtcRoomId, mSelfInfo.userId, data.rtcToken);
                        LiveRTCManager.ins().updateLiveTranscodingWithAudience(data.rtcRoomId,
                                mSelfInfo.userId, mPushUrl, sortUserList(data.userList));

                    } else {
                        List<LiveUserInfo> audienceUserList = new ArrayList<>(mGuestList);
                        if (mHostInfo != null) {
                            audienceUserList.add(mHostInfo.getDeepCopy());
                        }
                        LiveRTCManager.ins().updateLiveTranscodingWithAudience(data.rtcRoomId, mSelfInfo.userId,
                                mPushUrl, sortUserList(audienceUserList));
                    }

                    for (LiveUserInfo info : mGuestList) {
                        SolutionDemoEventManager.post(new InviteAudienceEvent(info.userId,
                                LiveDataManager.INVITE_REPLY_ACCEPT));
                    }
                }

                @Override
                public void onError(int errorCode, String message) {
                    showToast(errorCodeToMessage(errorCode, message));
                }
            };

    //????????????
    private final IRequestCallback<LiveReconnectResponse> mLiveReconnectCallback =
            new IRequestCallback<LiveReconnectResponse>() {
                @Override
                public void onSuccess(LiveReconnectResponse data) {
                    if (data.recoverInfo == null) {
                        SolutionToast.show("???????????????????????????????????????");
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

    private final IRequestCallback<LiveResponse> mLiveUpdateResolution =
            new IRequestCallback<LiveResponse>() {
                @Override
                public void onSuccess(LiveResponse data) {

                }

                @Override
                public void onError(int errorCode, String message) {

                }
            };

    private final IMainOption mMainOption = new IMainOption() {

        @Override
        public void onGiftClick() {
            GiftDialog dialog = new GiftDialog(LiveRoomMainActivity.this, mLiveRoomInfo.roomId);
            dialog.show();
        }

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
        mLiveChatRv.setLayoutManager(new LinearLayoutManager(LiveRoomMainActivity.this,
                RecyclerView.VERTICAL, false));
        mLiveChatRv.setAdapter(mLiveChatAdapter);

        if (hasLayouted) {
            return;
        }
        hasLayouted = true;

        if (!checkArgs()) {
            showToast("????????????");
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
            mRTMToken = intent.getStringExtra(EXTRA_RTM_TOKEN);
            mRTCToken = intent.getStringExtra(EXTRA_RTC_TOKEN);
            mRTCRoomId = intent.getStringExtra(EXTRA_RTC_ROOM_ID);
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
                showToast("???????????????????????????");
            }
            if (!isLeaveByKickOut) {
                LiveRTCManager.ins().getRTMClient().requestLeaveLiveRoom(roomId, mLeaveLiveCallback);
            }
        } else if (mSelfInfo.role == USER_ROLE_HOST) {
            if (mRoomStatus == ROOM_STATUS_GUEST_INTERACT) {
                showToast("?????????????????????");
            }
            if (!isLeaveByKickOut) {
                LiveRTCManager.ins().getRTMClient().requestFinishLive(roomId, mFinishLiveCallback);
            }
        }

        LiveRTCManager.ins().leaveRTMRoom();
        LiveRTCManager.ins().leaveRoom();
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

        LiveRTCManager.ins().joinRTMRoom(mLiveRoomInfo.roomId, mSelfInfo.userId, mRTMToken);
        if (mSelfInfo.role != USER_ROLE_HOST) {
            LiveRTCManager.ins().startCaptureAudio(false);
            LiveRTCManager.ins().startCaptureVideo(false);
        }

        //UI
        mAvatarView.setUserName(liveRoomInfo == null ? null : liveRoomInfo.anchorUserName);
        mLiveRoomControls.setRole(liveUserInfo.role, liveUserInfo.linkMicStatus);
        setAudienceCount(audienceCount);

        if (liveUserInfo.role == USER_ROLE_HOST) {
            LiveRTCManager.ins().setSingleLiveInfo(mRTCRoomId, mSelfInfo.userId, mPushUrl);
            LiveRTCManager.ins().joinRoom(mRTCRoomId, mSelfInfo.userId, mRTCToken);
            if (mRoomStatus == ROOM_STATUS_CO_HOST) {
                updateOnlineGuestList(null);
                mLiveVideoLayout.setLiveUserInfo(mSelfInfo, mCoHostInfo);
            } else if (mRoomStatus == ROOM_STATUS_GUEST_INTERACT) {
                mLiveVideoLayout.setLiveUserInfo(mSelfInfo, null);
                updateOnlineGuestList();
            } else {
                mLiveVideoLayout.setLiveUserInfo(liveUserInfo, null);
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
            // ??????????????????????????????????????????
            addOrRemoveBlock(liveRoomInfo != null && (mHostInfo.linkMicStatus == LINK_MIC_STATUS_HOST_INTERACTING));
            updateOnlineGuestList(null);
            mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);
            mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);

            //??????????????????256 * 256
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
        dialog.setMessage("??????????????????");
        dialog.setNegativeListener((v) -> dialog.dismiss());
        dialog.setPositiveListener((v) -> {
            dialog.dismiss();
            LiveRTCManager.ins().getRTMClient().finishHostLink(mLinkId, mLiveRoomInfo.roomId, mInteractResponse);
        });
        dialog.show();
    }

    private void openInviteCoHostDialog() {
        if (mRoomStatus == ROOM_STATUS_GUEST_INTERACT) {
            showToast("??????????????????????????????????????????");
            return;
        }
        LiveCoHostDialog liveCoHostDialog = new LiveCoHostDialog(this, info ->
                LiveRTCManager.ins().getRTMClient().inviteHostByHost(mLiveRoomInfo.roomId, mLiveRoomInfo.anchorUserId,
                        info.roomId, info.userId, "", mInviteHostResponse));
        liveCoHostDialog.show();
    }

    /**
     * ??????????????????????????????
     */
    private void onGuestClick() {
        if (mSelfInfo == null) {
            return;
        }
        if (mSelfInfo.role == USER_ROLE_AUDIENCE) {
            if (mSelfInfo.linkMicStatus == LiveDataManager.LINK_MIC_STATUS_OTHER) {
                if (mRoomStatus == ROOM_STATUS_CO_HOST) {
                    SolutionToast.show("??????????????????????????????????????????");
                    return;
                }
                RequestInteractDialog dialog = new RequestInteractDialog(
                        LiveRoomMainActivity.this, mLastApplyTs,
                        () -> {
                            LiveRTCManager.ins().getRTMClient().requestLinkByAudience(mLiveRoomInfo.roomId, mGuestApplyResponse);
                            mLiveRoomControls.setAddGuestBtnStatus(LiveRoomControlsLayout.STATUS_NORMAL);
                        });
                dialog.show();
            } else if (mSelfInfo.linkMicStatus == LiveDataManager.LINK_MIC_STATUS_AUDIENCE_INTERACTING) {
                final CommonDialog dialog = new CommonDialog(this);
                dialog.setCancelable(true);
                dialog.setMessage("???????????????????????????");
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
                showToast("??????????????????????????????????????????");
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
                showToast("????????????????????????????????????");
                SolutionDemoEventManager.post(new InviteAudienceEvent(info.userId,
                        LiveDataManager.INVITE_REPLY_WAITING));
            }

            @Override
            public void onError(int errorCode, String message) {
                if (errorCode == 622) {
                    showToast("????????????????????????????????????");
                } else {
                    SolutionToast.show(errorCodeToMessage(errorCode, message));
                    SolutionDemoEventManager.post(new InviteAudienceEvent(info.userId,
                            LiveDataManager.INVITE_REPLY_TIMEOUT));
                }
            }
        };
        LiveRTCManager.ins().getRTMClient().inviteAudienceByHost(mLiveRoomInfo.roomId,
                mLiveRoomInfo.anchorUserId, mLiveRoomInfo.roomId, info.userId, "", callback);
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
            AudienceSettingDialog settingDialog = new AudienceSettingDialog(
                    this, mSelfInfo.status, mLiveRoomInfo.roomId);
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
            dialog.setMessage("??????????????????");
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
                message = "????????????????????????????????????";
                break;
            case 402:
                message = "????????????????????????????????????";
                break;
            case 481:
                message = "????????????????????????????????????";
                break;
            case 482:
                message = "??????????????????????????????";
                break;
            case 472:
            case 483:
                message = "??????????????????";
                break;
            case 611:
                message = "???????????????";
                break;
            case 630:
                message = "?????????????????????";
                break;
            case 632:
            case 642:
                message = "?????????????????????????????????";
                break;
            case 638:
            case 645:
                message = "?????????????????????????????????";
                break;
            case 643:
                message = "?????????????????????????????????????????????";
                break;
            case 634:
            case 644:
                message = "???????????????";
                break;
            default:
                message = originMessage;
        }
        return message;
    }

    private void showToast(String message) {
        SolutionToast.show(message);
    }

    private void addChatMessage(CharSequence message) {
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
     * ??????????????? ?????? ???????????????????????????????????????????????????????????????
     *
     * @param userList ????????????????????????????????????
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
     * ????????????????????????????????????????????????
     *
     * @param add ??????
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

    private List<String> sortUserList(List<LiveUserInfo> userInfos) {
        List<String> userIdList = new ArrayList<>();
        if (userInfos != null) {
            for (LiveUserInfo info : userInfos) {
                if (info.role == LiveDataManager.USER_ROLE_HOST &&
                        TextUtils.equals(SolutionDataManager.ins().getUserId(), info.userId)) {
                    userIdList.add(0, info.userId);
                } else {
                    userIdList.add(info.userId);
                }
            }
        }
        return userIdList;
    }

    public static void startFromList(Activity activity, String roomId, String hostId, int requestCode) {
        Intent intent = new Intent(activity, LiveRoomMainActivity.class);
        intent.putExtra(EXTRA_REFER, REFER_LIST);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.putExtra(EXTRA_HOST_ID, hostId);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startFromCreate(Activity activity, LiveRoomInfo roomInfo, LiveUserInfo userInfo,
                                       String pushUrl, String rtmToken, String rtcToken, String rtcRoomId) {
        Intent intent = new Intent(activity, LiveRoomMainActivity.class);
        intent.putExtra(EXTRA_REFER, REFER_CREATE);
        intent.putExtra(EXTRA_ROOM_INFO, GsonUtils.gson().toJson(roomInfo));
        intent.putExtra(EXTRA_USER_INFO, GsonUtils.gson().toJson(userInfo));
        intent.putExtra(EXTRA_PUSH_URL, pushUrl);
        intent.putExtra(EXTRA_RTM_TOKEN, rtmToken);
        intent.putExtra(EXTRA_RTC_TOKEN, rtcToken);
        intent.putExtra(EXTRA_RTC_ROOM_ID, rtcRoomId);
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
            chatMessage = String.format(Locale.US, "%s ???????????????", event.audienceUserName);
        } else {
            chatMessage = String.format(Locale.US, "%s ???????????????", event.audienceUserName);
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
            showToast("?????????????????????????????????");
            LiveRTCManager.ins().startCaptureVideo(false);
        }
        if (event.mic == LiveDataManager.MEDIA_STATUS_OFF) {
            showToast("?????????????????????");
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
        //ui ???????????????????????????????????????????????????
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
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????
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
            showToast("??????????????????");
        } else {
            showToast("??????????????????");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveReconnectEvent(LiveReconnectEvent event) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdatePullStreamEvent(UpdatePullStreamEvent event) {
        //TT????????????????????????
        if (mSelfInfo.role == USER_ROLE_AUDIENCE) {
            playLiveStream(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveKickUserEvent(LiveKickUserEvent event) {
        showToast("??????ID???????????????????????????????????????");
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
        // ???????????????????????????????????????
        if (mSelfInfo.role != USER_ROLE_HOST) {
            if (mRoomStatus == LINK_MIC_STATUS_OTHER) {
                addOrRemoveBlock(false);
            } else if (mRoomStatus == LINK_MIC_STATUS_HOST_INTERACTING) {
                addOrRemoveBlock(true);
            }
        }
    }

    // ???????????? ????????? ??????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkInviteEvent(AudienceLinkInviteEvent event) {
        final CommonDialog dialog = new InviteResultDialog(this);
        dialog.setCancelable(true);
        dialog.setMessage("?????????????????????????????????");
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

    // ???????????? ????????? ?????????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkApplyEvent(AudienceLinkApplyEvent event) {
        final CommonDialog dialog = new InviteResultDialog(this);
        dialog.setCancelable(true);
        dialog.setMessage(String.format("??????%s????????????????????????", event.applicant.userName));
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

    // ???????????? ????????? ??????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkReplyEvent(AudienceLinkReplyEvent event) {
        if (event.replyType == INVITE_REPLY_REJECT) {
            showToast(String.format("??????%s??????????????????", event.userInfo.userName));
        } else if (event.replyType == INVITE_REPLY_ACCEPT) {
            updateOnlineGuestList(event.rtcUserList);
            if (mGuestList.size() == 1) {
                showToast("???????????????????????????????????????");
                LiveRTCManager.ins().joinRoom(event.rtcRoomId, mSelfInfo.userId, event.rtcToken);
                LiveRTCManager.ins().updateLiveTranscodingWithAudience(event.rtcRoomId,
                        mSelfInfo.userId, mPushUrl, sortUserList(event.rtcUserList));
            } else {
                List<LiveUserInfo> audienceUserList = new ArrayList<>(mGuestList);
                if (mHostInfo != null) {
                    audienceUserList.add(mHostInfo.getDeepCopy());
                }
                LiveRTCManager.ins().updateLiveTranscodingWithAudience(event.rtcRoomId,
                        mSelfInfo.userId, mPushUrl, sortUserList(audienceUserList));
            }
        }
    }

    // ???????????? ????????? ??????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkPermitEvent(AudienceLinkPermitEvent event) {
        mLastApplyTs = 0;
        if (event.permitType == LIVE_PERMIT_TYPE_REJECT) {
            showToast("?????????????????????????????????????????????");
            mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);
            SolutionDemoEventManager.post(new InviteAudienceEvent(
                    SolutionDataManager.ins().getUserId(),
                    LiveDataManager.INVITE_REPLY_REJECT));
        } else if (event.permitType == LIVE_PERMIT_TYPE_ACCEPT) {
            showToast("??????????????????????????????????????????????????????");
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

    // ???????????? ????????? ????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnchorLinkInviteEvent(AnchorLinkInviteEvent event) {
        final CommonDialog dialog = new InviteResultDialog(this);
        dialog.setCancelable(true);
        dialog.setMessage(String.format("%s??????????????????????????????????????????", event.userInfo.userName));
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

    // ???????????? ????????? ??????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnchorLinkReplyEvent(AnchorLinkReplyEvent event) {
        if (!TextUtils.equals(event.userInfo.userId, mSelfInfo.userId) && event.replyType == INVITE_REPLY_REJECT) {
            mRoomStatus = ROOM_STATUS_LIVE;
            showToast("?????????????????????????????????????????????");
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
            mLiveVideoLayout.setLiveUserInfo(mSelfInfo, mCoHostInfo);
            LiveRTCManager.ins().startForwardStreamToRooms(event.rtcRoomId, mCoHostInfo.userId,
                    event.rtcToken, mRTCRoomId, mSelfInfo.userId, mPushUrl);
        }
    }

    // ???????????? ?????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkStatusEvent(AudienceLinkStatusEvent event) {
        mLinkId = event.linkerId;
        if (event.isJoin) {
            mRoomStatus = ROOM_STATUS_GUEST_INTERACT;
            updateOnlineGuestList(event.userList);
            if (mSelfInfo.role == USER_ROLE_HOST) {
                if (mGuestList.size() == 1) {
                    showToast("???????????????????????????????????????");
                    LiveRTCManager.ins().updateLiveTranscodingWithAudience(event.rtcRoomId, mSelfInfo.userId,
                            mPushUrl, sortUserList(event.userList));

                } else {
                    List<LiveUserInfo> audienceUserList = new ArrayList<>(mGuestList);
                    if (mHostInfo != null) {
                        audienceUserList.add(mHostInfo.getDeepCopy());
                    }
                    LiveRTCManager.ins().updateLiveTranscodingWithAudience(event.rtcRoomId, mSelfInfo.userId,
                            mPushUrl, sortUserList(audienceUserList));

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
                showToast(String.format("%s????????????????????????", userName));
                updateOnlineGuestList(event.userList);
                List<LiveUserInfo> audienceUserList = new ArrayList<>(mGuestList);
                if (mHostInfo != null) {
                    audienceUserList.add(mHostInfo.getDeepCopy());
                }
                LiveRTCManager.ins().updateLiveTranscodingWithAudience(event.rtcRoomId,
                        mSelfInfo.userId, mPushUrl, sortUserList(audienceUserList));

            } else {
                if (!TextUtils.equals(event.userId, mSelfInfo.userId)) {
                    updateOnlineGuestList(event.userList);
                } else {
                    showToast("???????????????????????????");
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

    // ?????????????????????????????????????????????????????????????????????
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

    // ???????????? ?????????????????? ?????????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceLinkFinishEvent(AudienceLinkFinishEvent event) {
        mRoomStatus = ROOM_STATUS_LIVE;
        mSelfInfo.linkMicStatus = LiveDataManager.LINK_MIC_STATUS_OTHER;
        mLiveRoomControls.setCoHostBtnStatus(STATUS_NORMAL);
        mLiveRoomControls.setAddGuestBtnStatus(STATUS_NORMAL);
        mLiveRoomControls.setRole(mSelfInfo.role, LiveDataManager.LINK_MIC_STATUS_OTHER);
        updateOnlineGuestList(null);
        if (mSelfInfo.role == USER_ROLE_HOST) {
            LiveRTCManager.ins().updateLiveTranscodingWithAudience(mRTCRoomId,
                    mLiveRoomInfo.anchorUserId, mPushUrl, null);
        } else {
            LiveRTCManager.ins().leaveRoom();
            showToast("???????????????????????????");
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

    // ???????????? ????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnchorLinkFinishEvent(AnchorLinkFinishEvent event) {
        mRoomStatus = ROOM_STATUS_LIVE;
        showToast("?????????????????????");
        LiveRTCManager.ins().updateLiveTranscodingWithHost(false, mPushUrl,
                mRTCRoomId, mSelfInfo.userId, null, null);
        LiveRTCManager.ins().stopLiveTranscodingWithHost();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGiftEvent(GiftEvent event) {
        String giftName = null;
        ImageSpan imageSpan = null;
        int size = (int) Utilities.dip2Px(18);
        if (TextUtils.equals(event.giftType, "flower")) {
            giftName = getString(R.string.scene_interactive_live_gift_panel_flower);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flower_icon);
            imageSpan = new ImageSpan(LiveRoomMainActivity.this,
                    imageScale(bitmap, size, size));
        } else if (TextUtils.equals(event.giftType, "rocket")) {
            giftName = getString(R.string.scene_interactive_live_gift_panel_rocket);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rocket_icon);
            imageSpan = new ImageSpan(LiveRoomMainActivity.this,
                    imageScale(bitmap, size, size));
        }
        if (!TextUtils.isEmpty(giftName) && imageSpan != null) {
            String text = String.format("%s??????%s", event.userName, giftName);
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(text);
            ssb.append("  ");
            ssb.setSpan(imageSpan, ssb.length() - 2, ssb.length() - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            addChatMessage(ssb);
        }
    }

    private Bitmap imageScale(Bitmap bitmap, int w, int h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) w) / src_w;
        float scale_h = ((float) h) / src_h;
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        return Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix, true);
    }
}
