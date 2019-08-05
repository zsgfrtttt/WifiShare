package share.wifi.csz.com.wifishare.task;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import share.wifi.csz.com.util.ByteUtil;
import share.wifi.csz.com.util.CloseUtil;
import share.wifi.csz.com.util.LogUtil;
import share.wifi.csz.com.wifishare.activity.GroupChatActivity;
import share.wifi.csz.com.wifishare.constants.Config;

/**
 * Created by csz on 2019/8/2.
 */

public class ClientHandler extends Thread {

    private Handler mHandler;
    private Socket socket;
    private String host;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ClientReceiveHandler mClientReceiveHandler;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    public ClientHandler(Handler handler, String host) throws IOException {
        LogUtil.info("host :"+ host);
        this.mHandler = handler;
        this.host = host;
    }

    @Override
    public void run() {
        try {
            socket = new Socket();
            socket.bind(new InetSocketAddress(Config.PORT_CLIENT + new Random().nextInt(1000)));
            connect();
            mClientReceiveHandler = new ClientReceiveHandler();
            mClientReceiveHandler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect(){
        try {
            if (socket.isConnected()) {
                LogUtil.info("socket is connet");
                socket.close();
            }
            socket.connect((new InetSocketAddress(host, Config.PORT)), 2000);

            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            byte[] bytePre = ByteUtil.byteMergerAll(Config.HEADER,ByteUtil.ipToByte(ByteUtil.getLocalIpAddress()));
            outputStream.write(bytePre);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(final String str) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] bytes = ByteUtil.byteMergerAll(Config.HEADER,new byte[]{Config.CONTENT_TYPE_STRING},str.getBytes());
                    if (outputStream == null){
                        connect();
                    }
                    outputStream.write(bytes);
                    outputStream.flush();
                    Message.obtain(mHandler, GroupChatActivity.MSG_CLIENT_SEND,str).sendToTarget();
                    LogUtil.info("客户端已发送");
                } catch (Exception e) {
                    e.printStackTrace();
                    Message.obtain(mHandler,GroupChatActivity.MSG_ERROR_CLIENT_LINK).sendToTarget();
                }
            }
        });
    }

    public void close(){
        if (mClientReceiveHandler != null) {
            mClientReceiveHandler.close();
        }
    }


    private class ClientReceiveHandler extends Thread {

        private boolean mDone;
        private byte mInType;

        @Override
        public void run() {

            int BUFFER_SIZE = 1024;
            byte[] data = new byte[BUFFER_SIZE];
            int count;
            try {
                boolean started = false;
                //4位校验 + 1位内容类型
                while (!mDone && (count = inputStream.read(data, 0, started ? BUFFER_SIZE : 4 + 1)) != -1) {
                    LogUtil.info("count : " + count);
                    for (int i = 0; i < count; i++) {
                        LogUtil.info("client : "+data[i]);
                    }
                    if (mDone) break;
                    if (!started) {
                        if (verifyProtocal(data)) break;
                        started = true;
                    } else {
                        byte[] header = Arrays.copyOf(data, Config.HEADER.length);
                        //新的发送   4位校验 + 1位内容类型
                        if (Arrays.equals(header, Config.HEADER)) {
                            mInType = Array.getByte(data, 4);
                            byte[] content = new byte[count - 5];
                            System.arraycopy(data, 5, content, 0, content.length);
                            handleMetadata(content);
                        } else {
                            //接上次发送
                            handleMetadata(data);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                CloseUtil.close(inputStream, outputStream,socket);
            }
        }

        /**
         * 验证协议，获取客户端ip，接收信息类型
         * @param data
         * @return
         */
        private boolean verifyProtocal(byte[] data) {
            byte[] header = Arrays.copyOf(data, Config.HEADER.length);
            //报头是否符合
            if (!Arrays.equals(header, Config.HEADER)) return true;
            mInType = Array.getByte(data, data.length - 1);
            return false;
        }

        /**
         * 处理元数据
         * @param content
         */
        private void handleMetadata(byte[] content) {
            if (mInType == Config.CONTENT_TYPE_STRING){
                Message.obtain(mHandler, GroupChatActivity.MSG_CLIENT_RECEIVED,new String(content)).sendToTarget();
            } else if (mInType == Config.CONTENT_TYPE_FILE){
                //TODO
            }
        }

        private void close(){
            mDone = true;
            CloseUtil.close(inputStream, outputStream,socket);
            if (mHandler != null){
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
        }
    }

}
