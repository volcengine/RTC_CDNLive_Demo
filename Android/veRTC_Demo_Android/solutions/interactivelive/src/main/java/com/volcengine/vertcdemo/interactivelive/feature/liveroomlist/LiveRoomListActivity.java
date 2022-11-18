package com.volcengine.vertcdemo.interactivelive.feature.liveroomlist;

import static com.volcengine.vertcdemo.core.net.rts.RTSInfo.KEY_RTM;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.vertcdemo.joinrtsparams.bean.JoinRTSRequest;
import com.vertcdemo.joinrtsparams.common.JoinRTSManager;
import com.volcengine.vertcdemo.common.IAction;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.ServerResponse;
import com.volcengine.vertcdemo.core.net.rts.RTSBaseClient;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelive.bean.LiveResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveRoomInfo;
import com.volcengine.vertcdemo.interactivelive.bean.LiveRoomListResponse;
import com.volcengine.vertcdemo.interactivelive.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelive.bean.ReconnectInfo;
import com.volcengine.vertcdemo.interactivelive.core.LiveConstants;
import com.volcengine.vertcdemo.interactivelive.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelive.feature.createroom.CreateLiveRoomActivity;
import com.volcengine.vertcdemo.interactivelive.feature.liveroommain.LiveRoomMainActivity;
import com.volcengine.vertcdemo.utils.DebounceClickListener;

import java.util.List;

public class LiveRoomListActivity extends BaseActivity {

    private static final String TAG = "LiveRoomListActivity";

    public static final int JOIN_LIVE_ROOM_REQUEST_CODE = 1000;
    public static final String JOIN_LIVE_ROOM_RESULT_MESSAGE = "result_message";
    public static final String EXTRA_II_RTM_INFO = "il_rtm_info";

    private RTSInfo mRtmInfo;

    private ImageView mRefreshBtn;
    private View mCreateLiveBtn;
    private TextView mNoLiveTv;
    private LiveRoomListAdapter mLiveRoomListAdapter;
    private long mLastClickCreateTs = 0;
    private long mLastClickJoinTs = 0;
    private long mLastClickGetListTs = 0;

    private final IRequestCallback<LiveRoomListResponse> mRequestListRoomList = new IRequestCallback<LiveRoomListResponse>() {

        @Override
        public void onSuccess(LiveRoomListResponse data) {
            setRoomList(data.liveRoomList);
            ReconnectInfo reconnectInfo = data.recoverInfo;
            LiveUserInfo userInfo = data.user;
            if (reconnectInfo != null && userInfo != null) {
                reconnectInfo.liveRoomInfo.status = data.interactStatus;

                LiveRoomMainActivity.startFromReconnect(LiveRoomListActivity.this,
                        reconnectInfo, userInfo, data.interactStatus, data.interactUserList);
            }
        }

        @Override
        public void onError(int errorCode, String message) {
            showToast(message);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room_list);

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mRtmInfo = intent.getParcelableExtra(RTSInfo.KEY_RTM);
        if (mRtmInfo == null || !mRtmInfo.isValid()) {
            finish();
        }
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        super.onGlobalLayoutCompleted();
        // title
        ((TextView) findViewById(R.id.title_bar_title_tv)).setText("互动直播");
        ImageView backArrow = findViewById(R.id.title_bar_left_iv);
        backArrow.setImageResource(R.drawable.back_arrow);
        backArrow.setOnClickListener(v -> finish());
        mRefreshBtn = findViewById(R.id.title_bar_right_iv);
        mRefreshBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mRefreshBtn.setImageResource(R.drawable.refresh);
        mRefreshBtn.setOnClickListener(DebounceClickListener.create(this::onClick));

        mNoLiveTv = findViewById(R.id.no_live_tv);
        RecyclerView liveListRv = findViewById(R.id.live_list_rv);
        liveListRv.setLayoutManager(new LinearLayoutManager(this));
        mLiveRoomListAdapter = new LiveRoomListAdapter(info -> enterLiveRoom(info.roomId, info.anchorUserId));
        liveListRv.setAdapter(mLiveRoomListAdapter);
        mCreateLiveBtn = findViewById(R.id.live_room_list_create_room);
        mCreateLiveBtn.setOnClickListener(DebounceClickListener.create(this::onClick));

        LiveRTCManager.ins().rtcConnect(mRtmInfo);

        LiveRTCManager.ins().getRTSClient().login(mRtmInfo.rtmToken, (resultCode, message) -> {
            if (resultCode == RTSBaseClient.LoginCallBack.SUCCESS) {
                requestRoomList();
            } else {
                SafeToast.show("Login Rtm Fail Error:" + resultCode + ",Message:" + message);
            }
        });
    }

    @Override
    protected void setupStatusBar() {
        WindowUtils.setLayoutFullScreen(getWindow());
    }

    @Override
    public void finish() {
        super.finish();
        LiveRTCManager.ins().clearRTSEventListener();
        LiveRTCManager.ins().getRTSClient().logout();
        LiveRTCManager.ins().destroyEngine();
    }

    public void onClick(View v) {
        if (v == mRefreshBtn) {
            requestRoomList();
        } else if (v == mCreateLiveBtn) {
            createLiveRoom();
        }
    }

    private void requestRoomList() {
        long now = System.currentTimeMillis();
        if (now - mLastClickGetListTs <= LiveConstants.CLICK_RESET_INTERVAL) {
            return;
        }
        mLastClickGetListTs = now;
        LiveRTCManager.ins().getRTSClient().requestLiveClearUser(new IRequestCallback<LiveResponse>() {
            @Override
            public void onSuccess(LiveResponse data) {
                LiveRTCManager.ins().getRTSClient().requestLiveRoomList(mRequestListRoomList);
            }

            @Override
            public void onError(int errorCode, String message) {
                LiveRTCManager.ins().getRTSClient().requestLiveRoomList(mRequestListRoomList);
            }
        });
    }

    private void setRoomList(List<LiveRoomInfo> roomList) {
        if (roomList == null) {
            return;
        }
        mLiveRoomListAdapter.setLiveRoomList(roomList);
        if (roomList.size() == 0) {
            mNoLiveTv.setVisibility(View.VISIBLE);
        } else {
            mNoLiveTv.setVisibility(View.GONE);
        }
    }

    private void createLiveRoom() {
        long now = System.currentTimeMillis();
        if (now - mLastClickCreateTs <= LiveConstants.CLICK_RESET_INTERVAL) {
            return;
        }
        mLastClickCreateTs = now;
        Intent intent = new Intent(this, CreateLiveRoomActivity.class);
        startActivity(intent);
    }

    private void enterLiveRoom(String roomId, String hostId) {
        if (TextUtils.isEmpty(roomId) || TextUtils.isEmpty(hostId)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - mLastClickJoinTs <= LiveConstants.CLICK_RESET_INTERVAL) {
            return;
        }
        mLastClickJoinTs = now;
        LiveRoomMainActivity.startFromList(this, roomId, hostId, JOIN_LIVE_ROOM_REQUEST_CODE);
    }

    private void showToast(String toast) {
        SolutionToast.show(toast);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JOIN_LIVE_ROOM_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                showToast(data.getStringExtra(JOIN_LIVE_ROOM_RESULT_MESSAGE));
            }
        }
    }

    @Keep
    @SuppressWarnings("unused")
    public static void prepareSolutionParams(Activity activity, IAction<Object> doneAction) {
        Log.d(TAG, "prepareSolutionParams() invoked");
        IRequestCallback<ServerResponse<RTSInfo>> callback = new IRequestCallback<ServerResponse<RTSInfo>>() {
            @Override
            public void onSuccess(ServerResponse<RTSInfo> response) {
                RTSInfo data = response == null ? null : response.getData();
                if (data == null || !data.isValid()) {
                    onError(-1, "");
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClass(Utilities.getApplicationContext(), LiveRoomListActivity.class);
                intent.putExtra(KEY_RTM, data);
                activity.startActivity(intent);
                if (doneAction != null) {
                    doneAction.act(null);
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                if (doneAction != null) {
                    doneAction.act(null);
                }
            }
        };
        JoinRTSRequest request = new JoinRTSRequest();
        request.scenesName = LiveConstants.SOLUTION_NAME_ABBR;
        request.loginToken = SolutionDataManager.ins().getToken();

        JoinRTSManager.setAppInfoAndJoinRTM(request, callback);
    }
}
