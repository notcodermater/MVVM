package com.aspire.baselibrary.serialport;

import android.os.SystemClock;
import android.util.Log;

import com.aspire.baselibrary.serialport.message.LogManager;
import com.aspire.baselibrary.serialport.message.RecvMessage;
import com.aspire.baselibrary.serialport.message.RecvMessageWenshidu;
import com.licheedev.hwutils.ByteUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 读串口线程
 */
public class SerialReadThreadWenshidu extends Thread {

    private static final String TAG = "SerialReadThread";

    private BufferedInputStream mInputStream;

    public SerialReadThreadWenshidu(InputStream is) {
        mInputStream = new BufferedInputStream(is);
    }

    @Override
    public void run() {
        byte[] received = new byte[1024];
        int size;

        Log.e(TAG,"开始读线程");

        while (true) {

            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            try {

                int available = mInputStream.available();

                if (available > 0) {
                    size = mInputStream.read(received);
                    if (size > 0) {
                        onDataReceive(received, size);
                    }
                } else {
                    // 暂停一点时间，免得一直循环造成CPU占用率过高
                    SystemClock.sleep(1);
                }
            } catch (IOException e) {
                Log.e(TAG,"读取数据失败"+e.getMessage());
            }
            //Thread.yield();
        }
        Log.e(TAG,"结束读进程");
    }

    /**
     * 处理获取到的数据
     *
     * @param received
     * @param size
     */
    private void onDataReceive(byte[] received, int size) {
        // TODO: 2018/3/22 解决粘包、分包等
        String hexStr = ByteUtil.bytes2HexStr(received, 0, size);
        LogManager.instance().post(new RecvMessageWenshidu(hexStr));
    }

    /**
     * 停止读线程
     */
    public void close() {

        try {
            mInputStream.close();
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        } finally {
            super.interrupt();
        }
    }
}