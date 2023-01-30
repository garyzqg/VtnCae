package com.iflytek.vtncaetest.recorder;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.iflytek.alsa.AlsaRecorder;
import com.iflytek.vtncaetest.util.LogUtil;
import com.iflytek.vtncaetest.util.RootShell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class RecOperator {

    private static final String TAG = RecOperator.class.getSimpleName();

    // pcm录音设备号，根据实际情况设置createInstance AlsaRecorder
    /**
     * tinycap /sdcard/test.pcm -D 0 -d 0 -c 4 -r 48000 -b 32 -p 768 -n 10
     *
     * tinycap /sdcard/test.pcm -D 2 -d 0 -c 4 -r 16000 -b 16
     *
     * tinycap /sdcard/test.pcm -D 0 -d 0 -c 8 -r 16000 -b 16 -p 1024 -n 4   信步
     * tinycap /sdcard/test.pcm -D 1 -d 0 -c 8 -r 16000 -b 16 -p 1024 -n 4   通豪
     * tinycap /sdcard/test.pcm -D 3 -d 0 -c 8 -r 16000 -b 16 -p 1024 -n 4   卡奥斯
     * -D card          声卡
     * -d device        设备
     * -c channels      通道
     * -r rate          采样率
     * -b bits          pcm 位宽
     * -p period_size   一次中断的帧数
     * -n n_periods     周期数
     */

    /**
     * pcm 声卡号
     */
//    private final static int mPcmCard = 2;//信步
//    private final static int mPcmCard = 1;//通豪
    private final static int mPcmCard = 3;//卡奥斯
    /**
     * pcm 声卡设备号
     */
    private final static int mPcmDevice = 0;
    /**
     * 通道数量
     */
    private final static int mPcmChannel = 8;
    /**
     * 采样率
     */
    private final static int mPcmSampleRate = 16000;
    /**
     *  一次中断的帧数 一般不同修改，某些不支持这么大数字时会报错，可以尝试减小改值，例如 1023
     */
    private final static int mPcmPeriodSize = 1024;
    /**
     * 周期数 一般不同修改
     */
    private final static int mPcmPeriodCount = 4;
    /**
     * pcm 位宽 0-PCM_FORMAT_S16_LE、<br>1-PCM_FORMAT_S32_LE、<br>2-PCM_FORMAT_S8、<br>3-PCM_FORMAT_S24_LE、<br>4-PCM_FORMAT_MAX
     */
    private final static int mPcmFormat = 0;
    /**
     * 封装的ALS录音库，回调的音频帧大小。如果录音采样率不是16k,在转换音频的时候需要适配，例如48K音频录音可以改成 6144
     */
    private final static int mPcmBufferSize = 6144;

    protected RecordListener mPcmListener;

    //  录音数据信息透传回调监听
    private AlsaRecorder mAlsaRecorder;

    private static final int CMD_START = 0;
    private static final int CMD_STOP = 1;
    long preFrameTime;
    private Handler mHandler;
    public void initRec(Context context,RecordListener mRecordListener){
        HandlerThread handlerThread = new HandlerThread("alsa-handler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CMD_START:
                        if (mAlsaRecorder != null){
                            int recRet = mAlsaRecorder.startRecording(mAlsaPcmListener);
                            if (0 == recRet) {
                                LogUtil.iTag(TAG, "ALSA -- start recording sucess...");
                            }else {
                                LogUtil.iTag(TAG, "ALSA -- start recording fail...");
                            }
                        }else {
                            LogUtil.iTag(TAG, "ALSA -- mAlsaRecorder is null...");
                        }
                        break;
                    case CMD_STOP:
                        mAlsaRecorder.stopRecording();
                        LogUtil.iTag(TAG, "ALSA -- stopRecd ok...");
                        break;
                }
            }
        };

        RootShell.execRootCmdSilent("setenforce 0");
        RootShell.execRootCmdSilent("chmod 777 /dev/snd/pcmC"+mPcmCard+"D"+mPcmDevice+"c");
        mPcmListener = mRecordListener;
        mAlsaRecorder = AlsaRecorder.createInstance(mPcmCard, mPcmDevice, mPcmChannel, mPcmSampleRate,
                mPcmPeriodSize, mPcmPeriodCount, mPcmFormat);
//        mAlsaRecorder = AlsaRecorder.createInstance(mPcmCard, mPcmDevice, mPcmChannel, mPcmSampleRate,
//                mPcmPeriodSize, mPcmPeriodCount, mPcamFormat,mPcmBufferSize);
        mAlsaRecorder.setLogShow(false);                // Alsa-Jni日志控制 true-开启  false-关闭
    }



    // 开始录音
    public int startrecord(){
        Message.obtain(mHandler, CMD_START).sendToTarget();
        return 0;
    }


    // 停止录音
    public void stopRecord(){
        Message.obtain(mHandler, CMD_STOP).sendToTarget();

    }


    // tinyalsa录音音频监听器
    AlsaRecorder.PcmListener mAlsaPcmListener = new AlsaRecorder.PcmListener() {
        RandomAccessFile file;
        @Override
        public void onPcmData(byte[] bytes, int length) {
            if (file == null) {
                File tmp = new File("/sdcard/test.pcm");
                if (tmp.exists()) {
                    tmp.delete();
                }
                try {
                    file = new RandomAccessFile(tmp, "rw");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            try {
                file.write(bytes, 0, bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPcmListener.onPcmData(bytes);
        }

        @Override
        public void onError(int errorCode, String errorStr) {

        }


    };


}
