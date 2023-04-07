package com.inspur.robotspeech.cae;

public interface OnCaeOperatorlistener {
    void onAudio(byte[] audioData, int dataLen);
    void onWakeup(int angle ,int beam);

}
