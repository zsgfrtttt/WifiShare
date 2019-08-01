package share.wifi.csz.com.wifishare.permission;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static share.wifi.csz.com.wifishare.permission.SmartPermission.PageType.ANDROID_SETTING_PAGE;
import static share.wifi.csz.com.wifishare.permission.SmartPermission.PageType.DEFAULT;
import static share.wifi.csz.com.wifishare.permission.SmartPermission.PageType.MANAGER_PAGE;


/**
 * Created by csz on 2018/5/18.
 * 注意 ：用此组件需保证不同的权限（权限组）请求code不相同
 */

public class SmartPermission {
    private Activity mActivity;
    private Fragment mFragment;
    private int mRequestCode;
    private String[] mPers;
    private ISuccess mSuccess;
    private IFailure mFailure;
    private IDenied mDenied;
    private int mPageType = DEFAULT;

    @IntDef({MANAGER_PAGE, ANDROID_SETTING_PAGE, DEFAULT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PageType {
        int DEFAULT = -1;
        int MANAGER_PAGE = 1;
        int ANDROID_SETTING_PAGE = 0;
    }

    private SmartPermission(Activity activity) {
        this.mActivity = activity;
    }

    private SmartPermission(Fragment fragment) {
        this.mFragment = fragment;
    }

    public static SmartPermission activity(Activity activity) {
        return new SmartPermission(activity);
    }

    public static SmartPermission fragment(Fragment fragment) {
        return new SmartPermission(fragment);
    }

    public SmartPermission premissions(String... pers) {
        this.mPers = pers;
        return this;
    }

    public SmartPermission code(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    public SmartPermission pageType(@PageType int pageType) {
        this.mPageType = pageType;
        return this;
    }

    public SmartPermission onSuccess(ISuccess success) {
        this.mSuccess = success;
        return this;
    }

    public SmartPermission onFailure(IFailure failure) {
        this.mFailure = failure;
        return this;
    }

    public SmartPermission onDenied(IDenied denied) {
        this.mDenied = denied;
        return this;
    }

    //避免内存泄漏
    private void resetField() {
        mActivity = null;
        mFragment = null;
        mRequestCode = 0;
        mPers = null;
        mPageType = DEFAULT;
    }

    public void request() {
        if (mActivity == null && mFragment == null) {
            throw new NullPointerException("field mActivity or mFragment must should be init.");
        }
        Body body = new Body(mActivity, mFragment, mRequestCode, mPers, mSuccess, mFailure, mDenied, mPageType);
        requestPermission(body);
    }

    private void requestPermission(Body body) {
        if (mActivity == null) {
            throw new IllegalArgumentException("the with(Activity) must not be null");
        }
        boolean pass;
        List<String> noGrantList = PermissionHelper.getNoGrantPermissions(body);
        pass = noGrantList.isEmpty();

        if (pass) {
            if (mSuccess != null) {
                mSuccess.onSuccess();
            }
            mSuccess = null;
            mFailure = null;
            mDenied = null;
        } else {
            if (mDenied != null){
                mDenied.onDenied();
            }
            //注意：只加入还未被用户允许的权限（权限组）,用于Activity或Fragment回调使用
            RequestManager.putSuccess(mRequestCode, mSuccess);
            RequestManager.putFailure(mRequestCode, mFailure);
            PermissionHelper.requestPermissionWithListener(body, noGrantList);
        }
        //请求完成重置参数
        resetField();
        body.reset();
    }

    public static class Body {
        private Activity mActivity;
        private Fragment mFragment;
        private int mRequestCode;
        private String[] mPers;
        private ISuccess mSuccess;
        private IFailure mFailure;
        private IDenied mDenied;
        private int mPageType = DEFAULT;

        public Body() {
        }

        public Body(Activity activity, Fragment fragment, int requestCode, String[] pers, ISuccess success, IFailure failure, IDenied denied, int pageType) {
            this.mActivity = activity;
            this.mFragment = fragment;
            this.mRequestCode = requestCode;
            this.mPers = pers;
            this.mSuccess = success;
            this.mFailure = failure;
            this.mDenied = denied;
            this.mPageType = pageType;
        }

        public Context getContext() {
            if (mActivity == null || mFragment == null) {
                throw new NullPointerException("mActivity or mFragment can not be null");
            }
            return mActivity == null ? mFragment.getContext() : mActivity;
        }

        public Activity getActivity() {
            return mActivity;
        }

        public void setActivity(Activity activity) {
            this.mActivity = activity;
        }

        public Fragment getFragment() {
            return mFragment;
        }

        public void setFragment(Fragment fragment) {
            this.mFragment = fragment;
        }

        public int getRequestCode() {
            return mRequestCode;
        }

        public void setRequestCode(int requestCode) {
            this.mRequestCode = requestCode;
        }

        public String[] getPers() {
            return mPers;
        }

        public void setPers(String[] pers) {
            this.mPers = pers;
        }

        public int getPageType() {
            return mPageType;
        }

        public void setPageType(int pageType) {
            this.mPageType = pageType;
        }

        public ISuccess getSuccess() {
            return mSuccess;
        }

        public void setSuccess(ISuccess success) {
            this.mSuccess = success;
        }

        public IFailure getFailure() {
            return mFailure;
        }

        public void setFailure(IFailure failure) {
            this.mFailure = failure;
        }

        public IDenied getDenied() {
            return mDenied;
        }

        public void setDenied(IDenied denied) {
            this.mDenied = denied;
        }

        public void reset() {
            this.mActivity = null;
            this.mFragment = null;
            this.mRequestCode = 0;
            this.mPers = null;
            this.mSuccess = null;
            this.mFailure = null;
            this.mDenied = null;
            this.mPageType = DEFAULT;
        }
    }
}
