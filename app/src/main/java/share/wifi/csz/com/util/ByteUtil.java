package share.wifi.csz.com.util;

import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by csz on 2019/8/2.
 */

public class ByteUtil {

    public static String byteToIp(byte[] ip){
        int index = 0;
        short a = (short) (ip[index++] << 8 | ip[index++]);
        short b = (short) (ip[index++] << 8 | ip[index++]);
        short c = (short) (ip[index++] << 8 | ip[index++]);
        short d = (short) (ip[index++] << 8 | ip[index++]);
        StringBuilder builder = new StringBuilder();
        builder.append(a).append(".").append(b).append(".").append(c).append(".").append(d);
        return builder.toString();
    }

    public static byte[] ipToByte(String ip){
        byte[] bytes = new byte[8];
        int index = 0;
        String[] ips = ip.split("\\.");
        bytes[index++] = (byte) (Short.parseShort(ips[0]) >> 8);
        bytes[index++] = (byte) (Short.parseShort(ips[0]));
        bytes[index++] = (byte) (Short.parseShort(ips[1]) >> 8);
        bytes[index++] = (byte) (Short.parseShort(ips[1]));
        bytes[index++] = (byte) (Short.parseShort(ips[2]) >> 8);
        bytes[index++] = (byte) (Short.parseShort(ips[2]));
        bytes[index++] = (byte) (Short.parseShort(ips[3]) >> 8);
        bytes[index++] = (byte) (Short.parseShort(ips[3]));
        return bytes;
    }

    public static byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++) {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    static public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address)
                        if (!inetAddress.isLoopbackAddress()) {
                            return inetAddress.getHostAddress();
                        }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
