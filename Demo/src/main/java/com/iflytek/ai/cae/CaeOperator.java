package com.iflytek.ai.cae;

import android.content.Context;
import android.util.Log;

import com.iflytek.ai.recorder.RawAudioRecorder;
import com.iflytek.ai.cae.util.PcmFileUtil;
import com.iflytek.ai.recorder.RawRecorderFactory;


public class CaeOperator {
    private static final String TAG = CaeOperator.class.getSimpleName();
    // 唤醒成功后抛出的音频保存路径
    private static final String mCaeAudioDir = "/sdcard/cae/CAECaeAudio/";
    // 唤醒成功后抛出的音频保存路径
    private static final String mAsrAudioDir = "/sdcard/cae/CAEAsrAudio/";
    // 多通道原始音频保存路径
    private static final String mRawAudioDir = "/sdcard/cae/CAERawAudio/";


    private RawAudioRecorder mRawRecorder;
    private boolean mIsAudioSaving = false;
    private PcmFileUtil mAsrFileUtil;  // 保存唤醒降噪后录音
    private PcmFileUtil mRawFileUtil;   // 保存多通道原始录音数据
    private PcmFileUtil mCaeFileUtil;   // 保存多通道原始录音数据

    private CaeCoreHelper mCAECoreHelper;
    private OnCaeOperatorlistener mCAEListener;

    RawAudioRecorder.RecordListener mRecordListener = new RawAudioRecorder.RecordListener() {
        @Override
        public void onPcmData(byte[] data) {
            mRawFileUtil.write(data, 0, data.length);
            if (null != mCAECoreHelper) {
                mCAECoreHelper.writeAudio(data);
            }
            mCaeFileUtil.write(data, 0, data.length);
        }

        @Override
        public void onError(int error, String errorMessage) {
            Log.d(TAG, "recorder error" + error + " msg "  + errorMessage);
        }

        @Override
        public void onRecordStart() {
            Log.d(TAG, "CaeOperator CaeOperator_mRecordListener recorder success");
        }
    };

    OnCaeOperatorlistener mCAEProxyListener = new OnCaeOperatorlistener() {
        @Override
        public void onAudio(byte[] audioData, int dataLen) {
            Log.d(TAG, "shengjie-CaeOperator onAudio dataLen:" + dataLen);
            mAsrFileUtil.write(audioData, 0, audioData.length);
            if (null != mCAEListener) {
                mCAEListener.onAudio(audioData, dataLen);
            }
        }

        @Override
        public void onWakeup(int angle, int beam) {
            if (null != mCAEListener) {
                mCAEListener.onWakeup(angle, beam);
            }
        }
    };


    public void initCAEInstance(Context context, OnCaeOperatorlistener onCaeOperatorlistener){
        mCAEListener = onCaeOperatorlistener;
        mCAECoreHelper = new CaeCoreHelper(mCAEProxyListener, false);
        mRawRecorder = RawRecorderFactory.createRawAudioRecorder(context); //new BothlentRecorder

        mAsrFileUtil = new PcmFileUtil(mAsrAudioDir);
        mRawFileUtil = new PcmFileUtil(mRawAudioDir);
        mCaeFileUtil = new PcmFileUtil(mCaeAudioDir);
    }

    public boolean startRecord(final RawAudioRecorder.RecordListener listener){
        if (mRawRecorder!=null) {
            //这个动做干的事是在AbstractRawAudioRecorder里面做的，发送消息，具体实现又在BothlentRecorder里面实现
            mRawRecorder.startRecord(new RawAudioRecorder.RecordListener() {
                @Override
                public void onRecordStart() {
                    listener.onRecordStart(); //这个调用是demo.java用来更新UI的
                    mRecordListener.onRecordStart(); //仅仅用来打印log:recorder success
                }

                @Override
                public void onPcmData(byte[] data) {
                    listener.onPcmData(data);
                    mRecordListener.onPcmData(data);
                }

                @Override
                public void onError(int error, String errorMessage) {
                    listener.onError(error, errorMessage);
                    mRecordListener.onError(error, errorMessage);
                }
            });
        }
        return  true;
    }


    public void stopRecord() {
        if (mRawRecorder != null) {
            mRawRecorder.stopRecord();
        }

        stopSaveAudio();
    }

    public void restCaeEngine(){
        if (null != mCAECoreHelper) {
            mCAECoreHelper.ResetEngine();
        }
    }

    public void releaseCae(){
        stopSaveAudio();

        if(mRawRecorder!=null){
            mRawRecorder.destroy();
        }
        if(mCAECoreHelper !=null) {
            mCAECoreHelper.DestoryEngine();
            mCAECoreHelper = null;
        }
    }

    public void startSaveAudio() {
        mIsAudioSaving = true;
        mAsrFileUtil.createPcmFile();
        mRawFileUtil.createPcmFile();
        mCaeFileUtil.createPcmFile();
        mRawRecorder.startSaveAudio();
    }

    public void stopSaveAudio() {
        mIsAudioSaving = false;
        mAsrFileUtil.closeWriteFile();
        mRawFileUtil.closeWriteFile();
        mCaeFileUtil.closeWriteFile();
        mRawRecorder.stopSaveAudio();
    }

    public boolean isAudioSaving() {
        return mIsAudioSaving;
    }
}
