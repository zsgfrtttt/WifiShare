package share.wifi.csz.com.wifishare.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import share.wifi.csz.com.adapter.GroupChatAdapter;
import share.wifi.csz.com.base.BaseActivity;
import share.wifi.csz.com.bean.GroupChat;
import share.wifi.csz.com.wifishare.R;
import share.wifi.csz.com.wifishare.constants.Config;
import share.wifi.csz.com.wifishare.task.ClientHandler;
import share.wifi.csz.com.wifishare.task.ServerReceiveHandler;

/**
 * Created by csz on 2019/8/2.
 */

public class GroupChatActivity extends BaseActivity {

    public static final int MSG_OWNER_RECEIVED = 0;
    public static final int MSG_CLIENT_RECEIVED = 1;
    public static final int MSG_CLIENT_SEND = 2;
    public static final int MSG_OWNER_SEND = 3;

    @BindView(R.id.rv)
    RecyclerView mRv;
    @BindView(R.id.et_content)
    EditText mEtContent;
    @BindView(R.id.btn_send)
    Button mBtnSend;
    private ServerReceiveHandler mServerReceiveHandler;
    private ClientHandler mClientHandler;
    private GroupChatAdapter mAdapter;
    private List<GroupChat> mChats;
    private boolean mIsOwner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        initView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_group_chat;
    }

    private void initView() {
        mChats = new ArrayList<>();
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroupChatAdapter(mChats);
        mRv.setAdapter(mAdapter);
    }

    private void init() {
        mIsOwner = getIntent().getBooleanExtra("owner", false);
        String address = getIntent().getStringExtra("address");
        if (mIsOwner) {
            if (mServerReceiveHandler == null || mServerReceiveHandler.isDone()) {
                mServerReceiveHandler = new ServerReceiveHandler(mHandler);
                mServerReceiveHandler.start();
            }
        } else {
            mClientHandler = new ClientHandler(mHandler, address);
            mClientHandler.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServerReceiveHandler != null) {
            mServerReceiveHandler.close();
        }
        if (mClientHandler != null){
            mClientHandler.close();
        }
    }

    @OnClick(R.id.btn_send)
    public void onViewClicked() {
        String trim = mEtContent.getText().toString().trim();
        if (TextUtils.isEmpty(trim)) return;
        if (mIsOwner) {
            mServerReceiveHandler.sendMessage(trim);
        } else {
            mClientHandler.sendMessage(trim);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CLIENT_RECEIVED:
                case MSG_OWNER_RECEIVED: {
                    GroupChat item = new GroupChat(Config.CONTENT_TYPE_STRING, (String) msg.obj, GroupChatAdapter.OTHER);
                    mAdapter.addData(item);
                    break;
                }
                case MSG_CLIENT_SEND:
                case MSG_OWNER_SEND: {
                    GroupChat item = new GroupChat(Config.CONTENT_TYPE_STRING, (String) msg.obj, GroupChatAdapter.SELF);
                    mAdapter.addData(item);
                    break;
                }
            }
        }
    };


}
