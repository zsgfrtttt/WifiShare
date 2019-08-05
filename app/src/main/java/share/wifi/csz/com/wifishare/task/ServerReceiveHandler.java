package share.wifi.csz.com.wifishare.task;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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

public class ServerReceiveHandler extends Thread {

    private ServerSocket mServerSocket;
    private boolean mDone;
    private ExecutorService mAccpetClientPool = Executors.newCachedThreadPool();
    private ExecutorService mWriteThread = Executors.newCachedThreadPool();
    private final List<AcceptRunnable> mAcceptRunnableList;

    private Handler mHandler;

    public ServerReceiveHandler(Handler handler) {
        mHandler = handler;
        mAcceptRunnableList = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            while (!mDone) {
                mServerSocket = new ServerSocket(Config.PORT);
                mServerSocket.setReuseAddress(true);
                Socket client = mServerSocket.accept();
                LogUtil.info("接收到一个客户端连接");
                handleClientSocket(client);
            }
            LogUtil.info("服务器运行结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket(Socket client) {
        try {
            AcceptRunnable acceptRunnable = new AcceptRunnable(client);
            synchronized (ServerReceiveHandler.class) {
                mAcceptRunnableList.add(acceptRunnable);
            }
            mAccpetClientPool.execute(acceptRunnable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(final String str) {
        //全量发送
        mWriteThread.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (ServerReceiveHandler.class) {
                    for (AcceptRunnable acceptRunnable : mAcceptRunnableList) {
                        acceptRunnable.sendMessage(str);
                    }
                }
            }
        });

    }

    public void close() {
        if (!mDone) {
            mDone = true;
            CloseUtil.close(mServerSocket);
            if (!mWriteThread.isShutdown()) {
                mWriteThread.shutdownNow();
            }
            if (!mAccpetClientPool.isShutdown()) {
                mAccpetClientPool.shutdownNow();
            }
            synchronized (ServerReceiveHandler.class) {
                mAcceptRunnableList.clear();
            }
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
            LogUtil.info("ServerReceiveHandler close.");
        }
    }

    public boolean isDone() {
        return mDone;
    }

    class AcceptRunnable implements Runnable {
        private final Socket mSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private byte mInType;
        private String mIp;
        private final CountDownLatch mPrepareLatch;

        AcceptRunnable(Socket socket) throws IOException {
            this.mSocket = socket;
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
            mPrepareLatch = new CountDownLatch(1);
        }

        @Override
        public void run() {
            int BUFFER_SIZE = 1024;
            byte[] data = new byte[BUFFER_SIZE];
            int count;
            try {
                boolean started = false;
                //4位校验 + 8位ip
                while (!mDone && (count = inputStream.read(data, 0, started ? BUFFER_SIZE : 4 + 8)) != -1) {
                    for (int i = 0; i < count; i++) {
                        LogUtil.info("server Rece : " + data[i]);
                    }
                    if (mDone) break;
                    if (!started) {
                        if (verifyProtocal(data)) break;
                        answerToClient();
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
                CloseUtil.close(inputStream, outputStream,mSocket);
                synchronized (ServerReceiveHandler.class) {
                    mAcceptRunnableList.remove(AcceptRunnable.this);
                }
            }
        }

        private void answerToClient() {
            try {
                byte[] bytes = ByteUtil.byteMergerAll(Config.HEADER, new byte[]{Config.CONTENT_TYPE_STRING});
                outputStream.write(bytes);
                outputStream.flush();
                mPrepareLatch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendMessage(String str) {
            try {
                mPrepareLatch.await();
                byte[] bytes = ByteUtil.byteMergerAll(Config.HEADER, new byte[]{Config.CONTENT_TYPE_STRING}, str.getBytes());
                outputStream.write(bytes);
                outputStream.flush();
                Message.obtain(mHandler, GroupChatActivity.MSG_OWNER_SEND, str).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 处理元数据
         *
         * @param content
         */
        private void handleMetadata(byte[] content) {
            if (mInType == Config.CONTENT_TYPE_STRING) {
                Message.obtain(mHandler, GroupChatActivity.MSG_OWNER_RECEIVED, new String(content)).sendToTarget();
            } else if (mInType == Config.CONTENT_TYPE_FILE) {
                //TODO
            }
        }

        /**
         * 验证协议，获取客户端ip，接收信息类型
         *
         * @param data
         * @return
         */
        private boolean verifyProtocal(byte[] data) {
            byte[] header = Arrays.copyOf(data, Config.HEADER.length);
            //报头是否符合
            if (!Arrays.equals(header, Config.HEADER)) return true;
            byte[] ip = new byte[8];
            System.arraycopy(data, 4, ip, 0, 8);
            mIp = ByteUtil.byteToIp(ip);
            LogUtil.info("服务器接收到客户端的消息   ：" + mIp + "   type : " + mInType);
            return false;
        }
    }

}
