package share.wifi.csz.com.wifishare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import share.wifi.csz.com.adapter.WifiListAdapter;
import share.wifi.csz.com.base.BaseActivity;
import share.wifi.csz.com.wifishare.permission.ISuccess;
import share.wifi.csz.com.wifishare.permission.SmartPermission;
import share.wifi.csz.com.wifishare.receive.WifiReceiver;

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
                mAdapter = new WifiListAdapter((List<WifiP2pDevice>) list.getDeviceList());
                mRv.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        };

        mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {

            }
        };
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
}
