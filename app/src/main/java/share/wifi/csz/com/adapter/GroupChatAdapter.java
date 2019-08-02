package share.wifi.csz.com.adapter;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import share.wifi.csz.com.bean.GroupChat;
import share.wifi.csz.com.wifishare.R;
import share.wifi.csz.com.wifishare.constants.Config;

/**
 * Created by csz on 2019/8/2.
 */

public class GroupChatAdapter extends BaseMultiItemQuickAdapter<GroupChat,BaseViewHolder>{
    public static final int OTHER = 0;
    public static final int SELF = 1;

    public GroupChatAdapter(List<GroupChat> data) {
        super(data);
        addItemType(OTHER, R.layout.item_group_chat_other);
        addItemType(SELF, R.layout.item_group_chat_self);
    }

    @Override
    protected void convert(BaseViewHolder helper, GroupChat item) {
        int itemViewType = helper.getItemViewType();
        if (itemViewType == OTHER){
             if (item.getType() == Config.CONTENT_TYPE_STRING){
                 helper.setText(R.id.tv,item.getContent());
             }
        } else {
            if (item.getType() == Config.CONTENT_TYPE_STRING){
                helper.setText(R.id.tv,item.getContent());
            }
        }
    }
}
