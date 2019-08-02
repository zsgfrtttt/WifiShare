package share.wifi.csz.com.wifishare.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import share.wifi.csz.com.adapter.WifiListAdapter;
import share.wifi.csz.com.base.BaseActivity;
import share.wifi.csz.com.wifishare.R;
import share.wifi.csz.com.wifishare.permission.ISuccess;
import share.wifi.csz.com.wifishare.permission.SmartPermission;
import share.wifi.csz.com.wifishare.receive.WifiReceiver;
import share.wifi.csz.com.wifishare.task.ClientHandler;
import share.wifi.csz.com.wifishare.task.ServerReceiveHandler;

public class MainActivity extends BaseActivity implements WifiReceiver.Callback {
    private static final String TAG = "ccsszz";

    @BindView(R.id.rv)
    RecyclerView mRv;

    private WifiReceiver mWifiReceiver;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pManager.ConnectionInfoListener mConnectionInfoListener;
    private WifiListAdapter mAdapter;
    private List<WifiP2pDevice> mWifiP2pDevices;
    private WifiP2pDevice mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initConfig();
        initReceiver();
    }

    private void initConfig() {
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(),null);

        mPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList list) {
                mWifiP2pDevices.clear();
                mWifiP2pDevices.addAll(list.getDeviceList());
                mAdapter.notifyDataSetChanged();
            }
        };

        mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if (info == null || !info.groupFormed) return;
                //判断自身是否为组长
                Intent i = new Intent(MainActivity.this,GroupChatActivity.class);
                i.putExtra("owner",info.isGroupOwner);
                i.putExtra("address",info.groupOwnerAddress.getHostAddress());
                startActivity(i);
                finish();
            }
        };

        mRv.setLayoutManager(new LinearLayoutManager(this));
        if (mWifiP2pDevices == null) mWifiP2pDevices = new ArrayList<>();
        mAdapter = new WifiListAdapter(mWifiP2pDevices);
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                handleAdapterClick(position);
            }
        });
        mRv.setAdapter(mAdapter);
    }

    private void handleAdapterClick(int position) {
        mDevice = mWifiP2pDevices.get(position);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = mDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
       // progressDialog = ProgressDialog.show(MainActivity.this, "提示", "连接中");

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //广播：WIFI_P2P_CONNECTION_CHANGED_ACTION
                Log.i(TAG,"connect success.");
            }

            @Override
            public void onFailure(int reason) {
                Log.i(TAG,"connect failure.");
            }
        });
    }

    private void initReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(mWifiReceiver = new WifiReceiver(this),intentFilter);
    }

    /**
     * 搜索
     * @param view
     */
    public void search(View view){
        SmartPermission.activity(this)
                .code(0)
                .premissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onSuccess(new ISuccess() {
                    @Override
                    public void onSuccess() {
                        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                //广播  WIFI_P2P_PEERS_CHANGED_ACTION
                                Log.d(TAG, "discoverPeers onSuccess");
                            }

                            @Override
                            public void onFailure(int reasonCode) {
                                Log.d(TAG, "discoverPeers onFailure");
                            }
                        });
                    }
                })
                .request();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWifiReceiver != null){
            unregisterReceiver(mWifiReceiver);
        }
    }

    @Override
    public void onPeersChange() {
        if (mManager != null) {
            mManager.requestPeers(mChannel, mPeerListListener);
        }
    }

    @Override
    public void onConnectChanged(NetworkInfo networkInfo) {
        // 连接成功,获取连接信息
        if (networkInfo.isConnected()) {
            Log.i(TAG, "onConnectChanged is connected . ");
            mManager.requestConnectionInfo(mChannel, mConnectionInfoListener);
        } else {
            Log.i(TAG, "onConnectChanged connect failure . ");
        }
    }


}
