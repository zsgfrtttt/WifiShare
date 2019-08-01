package share.wifi.csz.com.wifishare.permission;

import java.util.HashMap;

/**
 * Created by csz on 2018/5/18.
 * 记录请求的权限信息，
 * 用于在{@link android.app.Activity#onRequestPermissionsResult(int, String[], int[])}回调
 */

public class RequestManager {

    private static final HashMap<Integer,ISuccess> SUCCESS_REQUEST_ONCALL = new HashMap<>();

    private static final HashMap<Integer,IFailure> FAILURE_REQUEST_ONCALL = new HashMap<>();

    private RequestManager(){}

    public static ISuccess getSuccess(int code){
        return SUCCESS_REQUEST_ONCALL.get(code);
    }

    public static IFailure getFailure(int code){
        return FAILURE_REQUEST_ONCALL.get(code);
    }

    public static void putSuccess(int code,ISuccess success){
        if (success == null){
            return;
        }
        SUCCESS_REQUEST_ONCALL.put(code,success);
    }

    public static void putFailure(int code,IFailure failure){
        if (failure == null){
            return;
        }
        FAILURE_REQUEST_ONCALL.put(code,failure);
    }

    public static void removeSuccess(int code){
        SUCCESS_REQUEST_ONCALL.remove(code);
    }

    public static void removeFailure(int code){
        FAILURE_REQUEST_ONCALL.remove(code);
    }
}
