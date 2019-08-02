package share.wifi.csz.com.adapter;

import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.File;
import java.util.List;

/**
 * Created by csz  on 2019/8/1.
 */

public class WifiListAdapter extends BaseQuickAdapter<WifiP2pDevice, BaseViewHolder> {

    public WifiListAdapter(@Nullable List<WifiP2pDevice> data) {
        super(android.R.layout.simple_list_item_1, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, WifiP2pDevice item) {
        helper.setText(android.R.id.text1,item.deviceName + File.separator + item.deviceAddress);
        helper.addOnClickListener(android.R.id.text1);
    }

}
