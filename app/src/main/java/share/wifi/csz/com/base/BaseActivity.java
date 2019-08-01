package share.wifi.csz.com.base;

import android.support.v7.app.AppCompatActivity;

import share.wifi.csz.com.wifishare.permission.PermissionHelper;

/**
 * Created by csz on 2019/8/1.
 */

public class BaseActivity extends AppCompatActivity{

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        PermissionHelper.onReqPermissionsResult(requestCode,permissions,grantResults);
    }
}
