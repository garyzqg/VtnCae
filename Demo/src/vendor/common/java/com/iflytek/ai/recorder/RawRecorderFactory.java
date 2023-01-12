package com.iflytek.ai.recorder;

import android.content.Context;

public class RawRecorderFactory {
    public static RawAudioRecorder createRawAudioRecorder(Context context) {
       return new BothlentRecorder();
    }
}
