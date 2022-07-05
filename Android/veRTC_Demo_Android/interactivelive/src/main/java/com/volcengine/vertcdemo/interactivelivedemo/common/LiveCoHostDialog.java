package com.volcengine.vertcdemo.interactivelivedemo.common;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.volcengine.vertcdemo.common.BaseDialog;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.interactivelive.R;
import com.volcengine.vertcdemo.interactivelivedemo.bean.GetActiveHostListResponse;
import com.volcengine.vertcdemo.interactivelivedemo.bean.LiveUserInfo;
import com.volcengine.vertcdemo.interactivelivedemo.core.LiveDataManager;
import com.volcengine.vertcdemo.interactivelivedemo.core.LiveRTCManager;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AnchorLinkInviteEvent;
import com.volcengine.vertcdemo.interactivelivedemo.core.event.AnchorLinkReplyEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;

public class LiveCoHostDialog extends BaseDialog {

    private final int mHighLightColor = Color.parseColor("#ffffff");
    private final int mNormalColor = Color.parseColor("#86909C");

    private final List<LiveUserInfo> mCoHostData = new LinkedList<>();
    private RecyclerView mHostRv;
    private TextView mNoContentTipTv;
    private CoHostAdapter mCoHostAdapter;
    private final CoHostCallback mCoHostCallback;

    private final IRequestCallback<GetActiveHostListResponse> mGetActiveHostCallback = new IRequestCallback<GetActiveHostListResponse>() {
        @Override
        public void onSuccess(GetActiveHostListResponse data) {
            setCoHostList(data.anchorList);
        }

        @Override
        public void onError(int errorCode, String message) {

        }
    };

    public LiveCoHostDialog(Context context, CoHostCallback coHostCallback) {
        super(context, R.style.CommonDialog);
        this.mCoHostCallback = coHostCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_live_co_host_list);
        initWindow();
        initUI(mCoHostCallback);
        setCanceledOnTouchOutside(true);
    }

    public void setCoHostList(List<LiveUserInfo> infoList) {
        if (infoList == null || infoList.size() == 0) {
            mNoContentTipTv.setVisibility(View.VISIBLE);
        } else {
            mNoContentTipTv.setVisibility(View.GONE);
            mCoHostAdapter.setData(infoList);
        }
    }

    private void initUI(CoHostCallback coHostCallback) {
        mHostRv = findViewById(R.id.dialog_live_host_list_rv);
        mHostRv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        mCoHostAdapter = new CoHostAdapter(mCoHostData, coHostCallback);
        mHostRv.setAdapter(mCoHostAdapter);

        mNoContentTipTv = findViewById(R.id.no_content_tip);
        TextView coHostTv = findViewById(R.id.dialog_live_host_list);
        TextView hostPKTv = findViewById(R.id.dialog_live_host_pk_list);
        View raiseIndicator = findViewById(R.id.dialog_voice_users_raise_indicator);
        View listenerIndicator = findViewById(R.id.dialog_voice_users_listener_indicator);
        mHostRv.setVisibility(View.VISIBLE);
        coHostTv.setTextColor(mHighLightColor);
        hostPKTv.setTextColor(mNormalColor);
        raiseIndicator.setVisibility(View.VISIBLE);
        listenerIndicator.setVisibility(View.GONE);
        coHostTv.setOnClickListener(v -> {
            mHostRv.setVisibility(View.VISIBLE);
            coHostTv.setTextColor(mHighLightColor);
            hostPKTv.setTextColor(mNormalColor);
            raiseIndicator.setVisibility(View.VISIBLE);
            listenerIndicator.setVisibility(View.GONE);
            mNoContentTipTv.setVisibility(mCoHostAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            mNoContentTipTv.setText("暂无其他主播在线");
        });
        hostPKTv.setOnClickListener(v -> {
            mHostRv.setVisibility(View.GONE);
            coHostTv.setTextColor(mNormalColor);
            hostPKTv.setTextColor(mHighLightColor);
            raiseIndicator.setVisibility(View.GONE);
            listenerIndicator.setVisibility(View.VISIBLE);
            mNoContentTipTv.setVisibility(View.VISIBLE);
            mNoContentTipTv.setText("主播正在赶来的路上");
        });
    }

    private void initWindow() {
        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        window.setDimAmount(0);
        initData();
    }

    @Override
    public void show() {
        super.show();
        SolutionDemoEventManager.register(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        SolutionDemoEventManager.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnchorLinkInviteEvent(AnchorLinkInviteEvent event) {
        dismiss();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnchorLinkReplyEvent(AnchorLinkReplyEvent event) {
        dismiss();
    }

    private void initData() {
        LiveRTCManager.ins().getRTMClient().requestActiveHostList(mGetActiveHostCallback);
    }

    private static class CoHostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<LiveUserInfo> mData = new LinkedList<>();
        private final CoHostCallback mCoHostCallback;

        public CoHostAdapter(List<LiveUserInfo> data, CoHostCallback coHostCallback) {
            mData.addAll(data);
            mCoHostCallback = coHostCallback;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_host_list, parent, false);
            return new CoHostViewHolder(view, mCoHostCallback);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof CoHostViewHolder) {
                ((CoHostViewHolder) holder).bind(mData.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void setData(List<LiveUserInfo> userInfo) {
            mData.clear();
            if (userInfo != null) {
                mData.addAll(userInfo);
            }
            notifyDataSetChanged();
        }
    }

    private static class CoHostViewHolder extends RecyclerView.ViewHolder {

        private final TextView mUserPrefixTv;
        private final TextView mUserNameTv;
        private final TextView mOptionTv;
        private final CoHostCallback mCoHostCallback;
        private LiveUserInfo mInfo;

        public CoHostViewHolder(@NonNull View itemView, CoHostCallback coHostCallback) {
            super(itemView);
            mUserPrefixTv = itemView.findViewById(R.id.item_voice_user_prefix);
            mUserNameTv = itemView.findViewById(R.id.item_voice_user_name);
            mOptionTv = itemView.findViewById(R.id.item_voice_user_option);
            mCoHostCallback = coHostCallback;
            mOptionTv.setOnClickListener((v) -> {
                if (mInfo != null && mCoHostCallback != null) {
                    LiveUserInfo info = mInfo;
                    mCoHostCallback.onClick(info);
                }
            });
        }

        public void bind(LiveUserInfo info) {
            mInfo = info;
            mUserPrefixTv.setText(info.userName.substring(0, 1));
            mUserNameTv.setText(info.userName);
            if (info.status == LiveDataManager.USER_STATUS_HOST_INVITING) {
                mOptionTv.setText("已邀请");
                mOptionTv.setAlpha(0.5F);
            } else if (info.status == LiveDataManager.USER_STATUS_CO_HOSTING) {
                mOptionTv.setText("正在连线");
                mOptionTv.setAlpha(0.5F);
            } else {
                mOptionTv.setText("邀请连线");
                mOptionTv.setAlpha(1F);
            }
        }
    }

    public interface CoHostCallback {
        void onClick(LiveUserInfo info);
    }
}