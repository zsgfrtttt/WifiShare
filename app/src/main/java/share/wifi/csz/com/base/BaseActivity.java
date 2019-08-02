package share.wifi.csz.com.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import share.wifi.csz.com.wifishare.permission.PermissionHelper;

/**
 * Created by csz on 2019/8/1.
 */

public abstract class BaseActivity extends AppCompatActivity{

    private Unbinder bind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        bind= ButterKnife.bind(this);
    }

    protected abstract int getLayoutId();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bind != null){
            bind.unbind();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        PermissionHelper.onReqPermissionsResult(requestCode,permissions,grantResults);
    }
}
