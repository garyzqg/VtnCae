package com.iflytek.ai.recorder;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.iflytek.aiui.AIUIConstant;

public abstract class AbstractRawAudioRecorder implements RawAudioRecorder {
    private static final int MSG_INIT = 0;
    private static final int MSG_START_RECORD = 1;
    private static final int MSG_STOP_RECORD = 2;
    private static final int MSG_DESTROY = 3;
    private static final int MSG_START_SAVE_AUDIO = 4;
    private static final int MSG_STOP_SAVE_AUDIO = 5;


    private HandlerThread mWorkThread;
    private Handler mWorkHandler;
    protected RecordListener mPcmListener;

    public AbstractRawAudioRecorder() {
       mWorkThread = new HandlerThread("raw audio record thread");
       mWorkThread.start();

       mWorkHandler = new Handler(mWorkThread.getLooper()) {
           @Override
           public void handleMessage(Message msg) {
               switch (msg.what) {
                   case MSG_INIT: {
                       innerInit();
                   }
                   break;

                   case MSG_START_RECORD: {
                        innerStartRecord();
                   }
                   break;

                   case MSG_STOP_RECORD: {
                       innerStopRecord();
                   }
                   break;

                   case MSG_DESTROY: {
                       innerDestroy();
                       mWorkThread.quit();
                   }
                   break;

                   case MSG_START_SAVE_AUDIO: {
                        innerstartSaveAudio();
                   }
                   break;

                   case MSG_STOP_SAVE_AUDIO: {
                       innerstopSaveAudio();
                   }
                   break;

                   default: {
                       super.handleMessage(msg);
                   }
               }
           }
       };

       mWorkHandler.sendMessage(mWorkHandler.obtainMessage(MSG_INIT));
    }

    @Override
    public final int startRecord(RecordListener listener) {
        mPcmListener = listener;
        mWorkHandler.sendMessage(mWorkHandler.obtainMessage(MSG_START_RECORD));
        return AIUIConstant.SUCCESS;
    }

    @Override
    public final void stopRecord() {
        mWorkHandler.sendMessage(mWorkHandler.obtainMessage(MSG_STOP_RECORD));
    }

    @Override
    public final void destroy() {
        mWorkHandler.sendMessage(mWorkHandler.obtainMessage(MSG_DESTROY));
    }

    @Override
    public final void startSaveAudio() {
        mWorkHandler.sendMessage(mWorkHandler.obtainMessage(MSG_START_SAVE_AUDIO));
    }

    @Override
    public final void stopSaveAudio() {
        mWorkHandler.sendMessage(mWorkHandler.obtainMessage(MSG_STOP_SAVE_AUDIO));
    }

    protected abstract void innerInit();

    protected abstract void innerStartRecord();

    protected abstract void innerStopRecord();

    protected abstract void innerDestroy();

    protected abstract void innerstartSaveAudio();

    protected abstract void innerstopSaveAudio();
}
