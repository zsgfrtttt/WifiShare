package share.wifi.csz.com.bean;

import android.net.wifi.p2p.WifiP2pDevice;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by csz on 2019/8/2.
 */

public class GroupChat implements MultiItemEntity {
    private WifiP2pDevice device;
    private byte type;
    private String mContent;
    private int GroupChatType ;  //other self

    public GroupChat(byte type, String content, int groupChatType) {
        this.type = type;
        mContent = content;
        GroupChatType = groupChatType;
    }

    public GroupChat(byte contentTypeString, int what) {
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    @Override
    public int getItemType() {
        return GroupChatType;
    }
}
