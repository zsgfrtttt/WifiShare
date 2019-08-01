package share.wifi.csz.com.wifishare.permission.support;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import share.wifi.csz.com.wifishare.permission.support.manufacturer.HUAWEI;
import share.wifi.csz.com.wifishare.permission.support.manufacturer.MEIZU;
import share.wifi.csz.com.wifishare.permission.support.manufacturer.OPPO;
import share.wifi.csz.com.wifishare.permission.support.manufacturer.PermissionsPage;
import share.wifi.csz.com.wifishare.permission.support.manufacturer.Protogenesis;
import share.wifi.csz.com.wifishare.permission.support.manufacturer.VIVO;
import share.wifi.csz.com.wifishare.permission.support.manufacturer.XIAOMI;


/**
 * Created by joker on 2017/8/4.
 */

public class PermissionsPageManager {
    /**
     * Build.MANUFACTURER
     */
    static final String MANUFACTURER_HUAWEI = "HUAWEI".toLowerCase();
    static final String MANUFACTURER_XIAOMI = "XIAOMI".toLowerCase();
    static final String MANUFACTURER_OPPO = "OPPO".toLowerCase();
    static final String MANUFACTURER_VIVO = "vivo".toLowerCase();
    static final String MANUFACTURER_MEIZU = "meizu".toLowerCase();
    static final String manufacturer = Build.MANUFACTURER.toLowerCase();

    public static String getManufacturer() {
        return manufacturer;
    }

    public static Intent getIntent(Activity activity) {
        PermissionsPage permissionsPage = new Protogenesis(activity);
        try {
            if (MANUFACTURER_HUAWEI.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new HUAWEI(activity);
            } else if (MANUFACTURER_OPPO.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new OPPO(activity);
            } else if (MANUFACTURER_VIVO.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new VIVO(activity);
            } else if (MANUFACTURER_XIAOMI.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new XIAOMI(activity);
            } else if (MANUFACTURER_MEIZU.equalsIgnoreCase(manufacturer)) {
                permissionsPage = new MEIZU(activity);
            }

            return permissionsPage.settingIntent();
        } catch (Exception e) {
            Log.e("Permissions4M", "手机品牌为：" + manufacturer + "异常抛出，：" + e.getMessage());
            permissionsPage = new Protogenesis(activity);
            return ((Protogenesis) permissionsPage).settingIntent();
        }
    }

    public static Intent getSettingIntent(Activity activity) {
        return new Protogenesis(activity).settingIntent();
    }

    public static boolean isXIAOMI() {
        return getManufacturer().equalsIgnoreCase(MANUFACTURER_XIAOMI);
    }

    public static boolean isOPPO() {
        return getManufacturer().equalsIgnoreCase(MANUFACTURER_OPPO);
    }

    public static boolean isMEIZU() {
        return getManufacturer().equalsIgnoreCase(MANUFACTURER_MEIZU);
    }
}
