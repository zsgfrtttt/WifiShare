package share.wifi.csz.com.wifishare.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import share.wifi.csz.com.wifishare.permission.support.ManufacturerSupportUtil;
import share.wifi.csz.com.wifishare.permission.support.PermissionsPageManager;
import share.wifi.csz.com.wifishare.permission.support.apply.PermissionsChecker;

/**
 * Created by csz on 2018/5/18.
 */

public class PermissionHelper {

    /**
     * 对国产orm的定制化权限适配
     * @return 返回未授权的权限集合
     */
    public static boolean checkPermissions(SmartPermission.Body requestBody) {
        return getNoGrantPermissions(requestBody).isEmpty();
    }


    /**
     * 对国产orm的定制化权限适配
     * @return 返回未授权的权限集合
     */
    public static List<String> getNoGrantPermissions(SmartPermission.Body requestBody) {
        List<String> noGrantPermission = new ArrayList<>();
        //匹配5.0-6.0的国产定制orm
        if (ManufacturerSupportUtil.isUnderMNeedChecked(true)) {
            for (String permission : requestBody.getPers()) {
                if (PermissionsChecker.isPermissionGranted(requestBody.getActivity(), permission)) {
                    //允许
                } else {
                    //拒绝
                    noGrantPermission.add(permission);
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : requestBody.getPers()) {
                if (ContextCompat.checkSelfPermission(requestBody.getActivity(), permission) == PackageManager.PERMISSION_GRANTED) {
                    //允许
                    if (!PermissionsChecker.isPermissionGranted(requestBody.getActivity(), permission)) {
                        noGrantPermission.add(permission);
                    }
                } else {
                    noGrantPermission.add(permission);
                }
            }
        } else {
            //其他情况都为允许
        }
        return noGrantPermission;
    }


    /**
     * 对国产orm的定制化权限适配
     */
    public static void requestPermissionWithListener(SmartPermission.Body requestBody,List<String> noGrantList) {
        List<String> noGrantPermission = noGrantList;

        //被用户拒绝权限后做额外的处理
        if (!noGrantPermission.isEmpty()) {
            int pageType = requestBody.getPageType();
            Intent intent = null;
            if (pageType == SmartPermission.PageType.MANAGER_PAGE) {
                intent = PermissionsPageManager.getIntent(requestBody.getActivity());
            } else if (pageType == SmartPermission.PageType.ANDROID_SETTING_PAGE) {
                intent = PermissionsPageManager.getSettingIntent(requestBody.getActivity());
            }

            //国产5.0-6.0
            if (ManufacturerSupportUtil.isUnderMNeedChecked(true)) {

            } else {
                //国产6.0及以上版本 及 被系统拒绝
                requestPermission(requestBody.getActivity(), noGrantPermission, requestBody.getRequestCode());
            }
        }
    }

    /**
     * 检查运行时权限并且请求权限 (Activity)
     *
     * @param activity
     * @param permissions
     * @param requestCode
     * @return
     */
    public static boolean requestAftercheckPermission(Activity activity, String[] permissions, int requestCode) {
        List<String> list = new ArrayList<>();
        for (String per : permissions) {
            int code = ContextCompat.checkSelfPermission(activity, per);
            if (code != PackageManager.PERMISSION_GRANTED) {
                list.add(per);
            }
        }
        if (list.isEmpty()) {
            return true;
        }
        String[] ungrabted = new String[list.size()];
        ActivityCompat.requestPermissions(activity, list.toArray(ungrabted), requestCode);
        return false;
    }

    /**
     * 检查运行时权限并且请求权限 (Fragment)
     *
     * @param fragment
     * @param permissions
     * @param requestCode
     * @return
     */
    public static boolean requestAftercheckPermission(Fragment fragment, String[] permissions, int requestCode) {
        List<String> list = new ArrayList<>();
        for (String per : permissions) {
            int code = ContextCompat.checkSelfPermission(fragment.getActivity(), per);
            if (code != PackageManager.PERMISSION_GRANTED) {
                list.add(per);
            }
        }
        if (list.isEmpty()) {
            return true;
        }
        String[] ungrabted = new String[list.size()];
        fragment.requestPermissions(list.toArray(ungrabted), requestCode);
        return false;
    }

    /**
     * 请求运行时权限 (Activity)
     *
     * @param activity
     * @param permissions
     * @param requestCode
     * @return
     */
    public static boolean requestPermission(Activity activity, List<String> permissions, int requestCode) {
        String[] ungrabted = new String[permissions.size()];
        ActivityCompat.requestPermissions(activity, permissions.toArray(ungrabted), requestCode);
        return false;
    }


    /**
     * 检查权限列表
     *
     * @param op 这个值被hide了，去AppOpsManager类源码找，如位置权限  AppOpsManager.OP_GPS==2
     *           0是网络定位权限，1是gps定位权限，2是所有定位权限
     *           返回值：0代表有权限，1代表拒绝权限 ，3代表询问是否有 ，-1代表出错
     */
    public static int checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            Class c = object.getClass();
            try {
                Class[] cArg = new Class[3];
                cArg[0] = int.class;
                cArg[1] = int.class;
                cArg[2] = String.class;
                Method lMethod = c.getDeclaredMethod("checkOp", cArg);
                return (Integer) lMethod.invoke(object, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * Activity调用这个方法监听
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public static void onReqPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //DialogHelper.dismiss();
        if (grantResults.length <= 0) {
            return;
        }
        ISuccess success = RequestManager.getSuccess(requestCode);
        IFailure failure = RequestManager.getFailure(requestCode);
        boolean allowed = checkGranted(grantResults);

        if (allowed) {
            if (success != null) {
                success.onSuccess();
            }
        } else {
            if (failure != null) {
                failure.onFailure();
            }
        }
        RequestManager.removeSuccess(requestCode);
        RequestManager.removeFailure(requestCode);
    }

    /**
     * 用户的授权结果
     */
    private static boolean  checkGranted(int[] grantResults) {
        boolean allowed = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allowed = false;
                break;
            }
        }
        return allowed;
    }
}
