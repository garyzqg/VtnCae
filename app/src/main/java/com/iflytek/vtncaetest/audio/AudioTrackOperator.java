package com.iflytek.vtncaetest.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * @author : 张庆功
 * date   : 2023/1/6 10:26
 * desc   : 音频流播放操作类
 */
public class AudioTrackOperator {
    /**
     * 采样率
     */
    private final static int mPcmSampleRate = 16000;
    /**
     * 声道layout
     */
    private final static int mChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
    /**
     * 音频格式
     */
    private final static int mPcmFormat = AudioFormat.ENCODING_PCM_16BIT;

    private AudioTrack mAudioTrack;

    /**
     * 构建 AudioTrack 实例对象
     */
    public void createStreamModeAudioTrack() {
        if (mAudioTrack == null) {
            // 最好使用此函数计算缓冲区大小，而非自己手动计算
            //传入采样率、声道layout、音频格式
            int minBufferSize = AudioTrack.getMinBufferSize(mPcmSampleRate, mChannelConfig, mPcmFormat);
            /**
             * int streamType:表示了不同的音频播放策略，按下手机的音量键，可以看到有多个音量管理，比如可以单独禁止警告音但是可以开启
             * 乐播放声音，这就是不同的音频播放管理策略；以常量形式定义在AudioManager中，如下：
             *      STREAM_MUSIC:播放音频用这个就好
             *      STREAM_VOICE_CALL:电话声音
             *      STREAM_ALARM:警告音
             *      ......
             * int sampleRateInHz:音频采样率
             * int channelConfig:声道类型;CHANNEL_IN_XXX适用于录制音频，CHANNEL_OUT_XXX用于播放音频
             * int audioFormat:采样格式
             * int bufferSizeInBytes:音频会话的缓冲区大小。音频播放时，app将音频原始数据不停的输送给这个缓冲区，然后AudioTrack不停从这个缓冲区拿数据送给音频播放系统
             * 从而实现声音的播放
             * int mode:缓冲区数据的流动方式;如下：
             * MODE_STREAM:流式流动，只缓存部分
             * MODE_STATIC:一次性缓冲全部数据，适用于音频比较小的播放
             * 备注：对于录制音频，为了性能考虑，最好用CHANNEL_IN_MoNo单声道，而转变立体声的过程在声音的特效处理阶段来完成
             * */
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mPcmSampleRate,
                    mChannelConfig, mPcmFormat, minBufferSize,
                    AudioTrack.MODE_STREAM);


        }
    }

    public void write(byte[] buffer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int writeResult = mAudioTrack.write(buffer, 0, buffer.length);
            }
        }).start();
    }


    public void play() {
        mAudioTrack.play();
    }
}
