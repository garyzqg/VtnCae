package com.iflytek.ai.recorder;

import android.util.Log;

import com.iflytek.ai.cae.util.PcmFileUtil;
import com.iflytek.alsa.AlsaRecorder;
import com.iflytek.cae.BuildConfig;
import com.iflytek.ai.cae.util.RootShell;

public class BothlentRecorder extends AbstractRawAudioRecorder {
    private static final String TAG = "BothlentRecorder";

    // pcm录音设备号，根据实际情况设置
    //tinycap /sdcard/test.pcm -D 0 -d 0 -c 4 -r 48000 -b 32 -p 768 -n 10

    //tinycap /sdcard/test.pcm -D 0 -d 2 -c 2 -r 16000 -b 24 -p 1024
    /**
     * pcm 声卡设备号
     */
    private final static int mPcmDevice = 2;
    /**
     * 通道数量
     */
    private static int mPcmChannel = 2;
    /**
     * 采样率
     */
    private final static int mPcmSampleRate = 64000;
    /**
     *  一次中断的帧数 一般不同修改，某些不支持这么大数字时会报错，可以尝试减小改值，例如 1023
     */
    private final static int mPcmPeriodSize = 1536;
    /**
     * 周期数 一般不同修改
     */
    private final static int mPcmPeriodCount = 8;
    /**
     * pcm 位宽 0-PCM_FORMAT_S16_LE、<br>1-PCM_FORMAT_S32_LE、<br>2-PCM_FORMAT_S8、<br>3-PCM_FORMAT_S24_LE、<br>4-PCM_FORMAT_MAX
     */
    private final static int mPcmFormat = 0;
    private AlsaRecorder mAlsaRecorder;
    // 多通道原始音频保存路径
    private static final String mInitialAudioDir = "/sdcard/cae/CAEInitialAudio/";
    private PcmFileUtil mInitialFileUtil;

    public BothlentRecorder() {
        super();
    }

    @Override
    protected void innerInit() {
//        RootShell.execRootCmdSilent("setenforce 0");
//        RootShell.execRootCmdSilent("chmod 777 /dev/snd/pcmC0D2c");

        //开始创建录音声卡实例
        Log.d(TAG, "shengjie-BothlentRecorder AlsaRecorder.createInstance");
        mAlsaRecorder = AlsaRecorder.createInstance(0, mPcmDevice, mPcmChannel, mPcmSampleRate,
                mPcmPeriodSize, mPcmPeriodCount, mPcmFormat);
        mInitialFileUtil = new PcmFileUtil(mInitialAudioDir);
    }

    @Override
    protected void innerStartRecord() {
        if (mAlsaRecorder!=null){
            Log.d(TAG, "shengjie-BothlentRecorder innerStartRecord startRecording");
            int recRet = mAlsaRecorder.startRecording(new AlsaRecorder.PcmListener() {
                @Override
                public void onPcmData(byte[] data, int dataLen) {
                    mInitialFileUtil.write(data, 0, data.length);
                    switch (BuildConfig.FLAVOR_mic) {
                        case "mic6_circle":
                        case "mic6_line": {
                            Log.d(TAG, "shengjie-BothlentRecorder onPcmData mic6_line");
                            mPcmListener.onPcmData(addCnForMutiMic(data));
                        }
                        break;

                        case "mic4_line": {
                            Log.d(TAG, "shengjie-BothlentRecorder onPcmData mic4_line dataLen:" + dataLen);
                            mPcmListener.onPcmData(addCnFor2MicN4(data));
                        }
                        break;

                        case "mic2_line": {
                            Log.d(TAG, "shengjie-BothlentRecorder onPcmData mic2_line");
                            mPcmListener.onPcmData(addCnFor2MicN4(data));
                        }
                        break;
                    }
                }
            });
            if (0 == recRet) {
                mPcmListener.onRecordStart();
            }else {
                Log.i(TAG, "start recording fail...");
                mPcmListener.onError(recRet, "alsa record error " + recRet);
            }
        }else {
            Log.d(TAG,"AlsaRecorder is null ..  .");
            //TODO 错误回调
        }
    }

    @Override
    protected void innerStopRecord() {
        if(mAlsaRecorder != null) {
            mAlsaRecorder.stopRecording();
        }
        mInitialFileUtil.closeWriteFile();
    }

    @Override
    protected void innerDestroy() {
        if(mAlsaRecorder != null) {
            mAlsaRecorder.destroy();
        }
    }

    @Override
    protected void innerstartSaveAudio() {
        if(mInitialFileUtil != null) {
            mInitialFileUtil.createPcmFile();
        }
    }

    @Override
    protected void innerstopSaveAudio() {
        if(mInitialFileUtil != null) {
            mInitialFileUtil.closeWriteFile();
        }
    }

    //6mic 通道号添加 8ch 32bits
    private byte[] addCnForMutiMic(byte[] data) {
        int datasize=data.length;
        byte[] newdata=new byte[datasize*2];// 乘以2是数据从16bit变为32bit；
        int j=0;
        int k=0;
        int index= 0;
        int step = datasize/2;

        while(j<step) {// 除以2是两个字节作为一组数据，进行添加通道号处理；
            for (int i=1; i<9;i++) {
                k = 4*j;
                index= 2*j;
                newdata[k]=00;
                newdata[k+1]=00;
                newdata[k+2]=data[index];
                newdata[k+3]=data[index+1];
                j++;
            }

        }
        data = null;
        return newdata;
    }

    //4mic通道适配,输入8通道数据，适配成6通道数据
    private byte[] adapeter4Mic(byte[] data) {
        //  int size = ((data.length/8)*2)*6;
        int size = (data.length/8)*6;
        byte[] cpy=new byte[size];
        int j=0;

        while(j<data.length/16) {

            cpy[12 * j + 0] = data[16 * j +0];
            cpy[12* j + 1] = data[16 * j +1];

            cpy[12 * j + 2] = data[16 * j +2];
            cpy[12* j + 3] = data[16 * j +3];


            cpy[12 * j + 4] = data[16 * j +4];
            cpy[12* j + 5] = data[16 * j +5];


            cpy[12 * j + 6] = data[16 * j +6];
            cpy[12* j + 7] = data[16 * j +7];

            //通道7--》ref1
            cpy[12 * j + 8] = data[16 * j +12];
            cpy[12* j + 9] = data[16 * j +13];

            //通道8 --》 ref2
            cpy[12 * j + 10] = data[16 * j +14];
            cpy[12* j + 11] = data[16 * j +15];

            j++;
        }
        return cpy;
    }

    //4mic通道适配,输入8通道数据，适配成6通道数据
    private byte[] adapeter4Mic32bit(byte[] data) {
        //  int size = ((data.length/8)*2)*6;
        int size = (data.length/8)*6*2;

        byte[] cpy=new byte[size];
        int j=0;

        while(j<data.length/16) {

            cpy[24 * j + 0] = 0x00;
            cpy[24* j + 1] = 0x01;
            cpy[24 * j + 2] = data[16 * j +0];
            cpy[24* j + 3] = data[16 * j +1];



            cpy[24 * j + 4] = 0x00;
            cpy[24* j + 5] = 0x02;
            cpy[24 * j + 6] = data[16 * j +2];
            cpy[24* j + 7] = data[16 * j +3];


            cpy[24 * j + 8] = 0x00;
            cpy[24* j + 9] = 0x03;
            cpy[24 * j + 10] = data[16 * j +4];
            cpy[24* j + 11] = data[16 * j +5];


            cpy[24 * j + 12] = 0x00;
            cpy[24* j + 13] = 0x04;
            cpy[24 * j + 14] = data[16 * j +6];
            cpy[24* j + 15] = data[16 * j +7];

            //通道7--》ref1
            cpy[24 * j + 16] = 0x00;
            cpy[24* j + 17] = 0x05;
            cpy[24 * j + 18] = data[16 * j +12];
            cpy[24* j + 19] = data[16 * j +13];

            //通道8 --》 ref2
            cpy[24 * j + 20] = 0x00;
            cpy[24* j + 21] = 0x06;
            cpy[24 * j + 22] = data[16 * j +14];
            cpy[24* j + 23] = data[16 * j +15];

            j++;
        }
        return cpy;
    }

    //6mic 16bit-> 2mic 32bit通道适配
    private byte[] addCnFor2Mic(byte[] data) {
        byte[] cpy=new byte[data.length];
        int j=0;

        //通道： mic1 mic2 ref ref
        while(j<data.length/16) {
            cpy[16 * j] = 00;
            cpy[16 * j + 1] = (byte) 1;
            cpy[16 * j + 2] = data[16 * j + 0];
            cpy[16 * j + 3] = data[16 * j + 1];

            cpy[16 * j + 4] = 00;
            cpy[16 * j + 5] = (byte) 2;
            cpy[16 * j + 6] = data[16 * j + 2];
            cpy[16 * j + 7] = data[16 * j + 3];

            cpy[16 * j + 8] = 00;
            cpy[16 * j + 9] = (byte) 3;
            cpy[16 * j + 10] = data[16 * j + 12];
            cpy[16 * j + 11] = data[16 * j + 13];

            cpy[16 * j + 12] = 00;
            cpy[16 * j + 13] = (byte) 4;
            cpy[16 * j + 14] = data[16 * j + 14];
            cpy[16 * j + 15] = data[16 * j + 15];

            j++;
        }
        return cpy;
    }

    //2mic通道适配 4c 16k 16bit 转 4c 16k 32bit（复用第三通道回采信号）
    public static byte[] addCnFor2MicN4(byte[] data) {
        byte[] cpy=new byte[data.length*2];
        int j=0;

        //通道： mic1 mic2 ref ref
        while(j<data.length/8) {
            cpy[16*j]=00;
            cpy[16*j+1]=  00;
            cpy[16 * j + 2] = data[8 * j +0];
            cpy[16* j + 3] = data[8 * j +1];

            cpy[16*j+4]=00;
            cpy[16*j+5]=  00;
            cpy[16 * j + 6] = data[8 * j +2];
            cpy[16* j + 7] = data[8 * j +3];

            cpy[16*j+8]=00;
            cpy[16*j+9]=  00;
            cpy[16 * j + 10] = data[8 * j +4];
            cpy[16* j + 11] = data[8 * j +5];
            // 复用第三通道回采数据
            cpy[16*j+12]=00;
            cpy[16*j+13]=  00;
            cpy[16 * j + 14] = data[8 * j +4];
            cpy[16* j + 15] = data[8 * j +5];

            j++;
        }
        return cpy;
    }

}
