package com.volcengine.vertcdemo.interactivelivedemo.feature.liveroomlist;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.rtm.RTMBaseClient;
import com.volcengine.vertcdemo.core.net.rtm.RtmInfo;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveResponse;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveRoomInfo;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveRoomListResponse;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelivedemo.bean.ReconnectInfo;
import com.volcengine.vertcdemo.interactivelivedemo.core.LiveConstants;
import com.volcengine.vertcdemo.interactivelivedemo.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelivedemo.feature.createroom.CreateLiveRoomActivity;
import com.volcengine.vertcdemo.interactivelivedemo.feature.liveroommain.LiveRoomMainActivity;

import java.util.List;

public class LiveRoomListActivity extends BaseActivity implements View.OnClickListener {

    public static final int JOIN_LIVE_ROOM_REQUEST_CODE = 1000;
    public static final String JOIN_LIVE_ROOM_RESULT_MESSAGE = "result_message";
    public static final String EXTRA_II_RTM_INFO = "il_rtm_info";

    private RtmInfo mRtmInfo;

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
        mRtmInfo = intent.getParcelableExtra(RtmInfo.KEY_RTM);
        if (mRtmInfo == null || !mRtmInfo.isValid()) {
            finish();
            return;
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
        mRefreshBtn.setOnClickListener(v -> requestRoomList());

        mNoLiveTv = findViewById(R.id.no_live_tv);
        RecyclerView liveListRv = findViewById(R.id.live_list_rv);
        liveListRv.setLayoutManager(new LinearLayoutManager(this));
        mLiveRoomListAdapter = new LiveRoomListAdapter(info -> enterLiveRoom(info.roomId, info.anchorUserId));
        liveListRv.setAdapter(mLiveRoomListAdapter);
        mCreateLiveBtn = findViewById(R.id.live_room_list_create_room);
        mCreateLiveBtn.setOnClickListener(this);

        LiveRTCManager.ins().rtcConnect(mRtmInfo);

        LiveRTCManager.ins().getRTMClient().login(mRtmInfo.rtmToken, (resultCode, message) -> {
            if (resultCode == RTMBaseClient.LoginCallBack.SUCCESS) {
                requestRoomList();
            } else {
                SafeToast.show("Login Rtm Fail Error:" + resultCode + ",Message:" + message);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        LiveRTCManager.ins().clearRTMEventListener();
        LiveRTCManager.ins().getRTMClient().logout();
        LiveRTCManager.ins().destroyEngine();
    }

    @Override
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
        LiveRTCManager.ins().getRTMClient().requestLiveClearUser(new IRequestCallback<LiveResponse>() {
            @Override
            public void onSuccess(LiveResponse data) {
                LiveRTCManager.ins().getRTMClient().requestLiveRoomList(mRequestListRoomList);
            }

            @Override
            public void onError(int errorCode, String message) {
                LiveRTCManager.ins().getRTMClient().requestLiveRoomList(mRequestListRoomList);
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
}
