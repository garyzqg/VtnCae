package com.inspur.robotspeech.recorder;

public interface RecordListener {
    void onPcmData(byte[] data);
}
