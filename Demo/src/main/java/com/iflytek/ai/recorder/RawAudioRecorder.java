package com.iflytek.ai.recorder;

public interface RawAudioRecorder {
    int startRecord(RecordListener listener);
    void stopRecord();
    void destroy();
    void startSaveAudio();
    void stopSaveAudio();

    class RecordListener {
        public void onRecordStart() {}
        public void onPcmData(byte[] data) {}
        public void onError(int error, String errorMessage) {}
    }
}
