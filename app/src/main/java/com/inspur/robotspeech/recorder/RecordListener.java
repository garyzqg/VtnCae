package com.inspur.robotspeech.recorder;

public interface RecordListener {
    void onPcmData(byte[] data);
    void startRecordStatus(boolean success,String msg);
}
