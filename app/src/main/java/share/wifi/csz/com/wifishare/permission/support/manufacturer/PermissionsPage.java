package share.wifi.csz.com.wifishare.permission.support.manufacturer;

import android.content.Intent;

/**
 * Created by csz on 2017/8/4.
 */

public interface PermissionsPage {
    String PACK_TAG = "package";

    // normally, ActivityNotFoundException
    Intent settingIntent() throws Exception;
}
