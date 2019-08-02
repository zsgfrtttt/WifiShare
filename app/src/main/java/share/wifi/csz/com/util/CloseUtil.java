package share.wifi.csz.com.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by com.csz on 2019/6/4.
 */

public class CloseUtil {

    public static final void close(Closeable... closeArray){
        if (closeArray==null || closeArray.length == 0) return;
        for (Closeable closeable : closeArray) {
            if (closeable == null) continue;
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

}
