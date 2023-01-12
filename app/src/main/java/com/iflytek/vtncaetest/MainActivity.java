package com.iflytek.vtncaetest;

import android.Manifest;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.aiui.AIUISetting;
import com.iflytek.vtncaetest.audio.AudioTrackOperator;
import com.iflytek.vtncaetest.cae.CaeOperator;
import com.iflytek.vtncaetest.cae.OnCaeOperatorlistener;
import com.iflytek.vtncaetest.recorder.RecOperator;
import com.iflytek.vtncaetest.recorder.RecordListener;
import com.iflytek.vtncaetest.util.InitUtil;
import com.iflytek.vtncaetest.util.LogUtil;
import com.iflytek.vtncaetest.util.LogUtils;
import com.iflytek.vtncaetest.util.RecordAudioUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static String TAG = MainActivity.class.getSimpleName();
    private static int ret = 0;
    private static String strTip = "";
    private static boolean isWakeup = false;

    private TextView mResText;
    private ScrollView mScrollView;
    private Button btnSave;
    // 多麦克算法库
    private CaeOperator mCaeOperator;
    private RecOperator mRecOperator;
    // AIUI
    private AIUIAgent mAIUIAgent = null;
    // AIUI工作状态
    private int mAIUIState = AIUIConstant.STATE_IDLE;



    Handler handler = new Handler();

    // 录音机工作状态
    private static boolean isRecording = false;
    // 写音频线程工作中
    private static boolean isWriting = false;
    private AudioTrackOperator mAudioTrackOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLayout();
        requestPermissions();
        // 资源拷贝
        CaeOperator.portingFile(this);


        InitUtil.init(this);
    }



    private void initLayout() {
        findViewById(R.id.init_sdk).setOnClickListener(this);
        findViewById(R.id.btnRec).setOnClickListener(this);
        findViewById(R.id.btnStop).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.writeTest).setOnClickListener(this);
        mScrollView = findViewById(R.id.scrollView);
        mResText = findViewById(R.id.res_text);
        btnSave = findViewById(R.id.btnSave);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.init_sdk:
                initSDK();
                break;
            case R.id.btnRec:
                startReord();
                break;
            case R.id.btnStop:
                stopRecord();
                break;
            case R.id.btnSave:
                saveAudio();
                break;
            case R.id.writeTest:
                writeAudioTest();
                break;
            default:
                break;
        }

    }

//    static {
//        System.loadLibrary("tinyalsa");
//        System.loadLibrary("balsa-jni");
//    }

    private void initSDK() {
        // 初始化AIUI
        createAgent();
        // 初始化CAE
        initCaeEngine();
        // 初始化alsa录音
        initAlsa();
        //初始化AudioTrack
        initAudioTrack();

    }

    private void initAudioTrack() {
        if (mAudioTrackOperator == null){
            mAudioTrackOperator = new AudioTrackOperator();
            mAudioTrackOperator.createStreamModeAudioTrack();
        }
    }


    /**
     * 读取AIUI配置
     */
    private String getAIUIParams() {
        String params = "";
        AssetManager assetManager = getResources().getAssets();
        try {
            InputStream ins = assetManager.open("cfg/aiui_phone.cfg");
            byte[] buffer = new byte[ins.available()];
            ins.read(buffer);
            ins.close();
            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     * 初始化AIUI
     */
    private void createAgent() {
        if (null == mAIUIAgent) {
            Log.i(TAG, "create aiui agent");

            AIUISetting.setSystemInfo(AIUIConstant.KEY_SERIAL_NUM, CaeOperator.AUTH_SN);

            mAIUIAgent = AIUIAgent.createAgent(this, getAIUIParams(), mAIUIListener);
        }

        if (null == mAIUIAgent) {
            strTip = "AIUI初始化失败!";
        } else {
            strTip = "AIUI初始化成功!";
        }
        setText(strTip);
        setText("---------create_AIUI---------");

    }

    /**
     * 初始化CAE
     */
    private void initCaeEngine() {
        mCaeOperator = new CaeOperator();
        ret = mCaeOperator.initCAE(onCaeOperatorlistener);
        if(ret == 0){
            strTip = "CAE初始化成功";

           String ver =  mCaeOperator.getCAEVersion();
           Log.e(TAG,"vae ver is: "+ver);
//            initAlsa();
        }else{
            strTip = "CAE初始化失败,错误信息为："+ ret;
        }
        setText(strTip);
        setText("---------init_CAE---------");
    }

    /**
     * 初始化ALSA
     */
    private void initAlsa() {
        mRecOperator = new RecOperator();
        mRecOperator.initRec(this,onRecordListener);
    }


    private void startReord() {
        if(!isRecording && mRecOperator != null){
            if(isWriting){
                setText("正在写音频测试中，等结束后再开启录音测试");
                setText("---------start_alsa_record---------");
                return;
            }
            ret = mRecOperator.startrecord();
            if(0 == ret){
                strTip = "开启录音成功！";
                isRecording = true;
            }else if(111111 == ret){
                strTip = "AlsaRecorder is null ...";
            }else {
                strTip = "开启录音失败，请查看/dev/snd/下的设备节点是否有777权限！\nAndroid 8.0 以上需要暂时使用setenforce 0 命令关闭Selinux权限！";
                distoryRecord();
            }
            setText(strTip);
            setText("---------start_alsa_record---------");
        }
    }

    private void stopRecord() {
        if(isRecording && mRecOperator != null){
            mRecOperator.stopRecord();
            btnSave.setText("开始保存");
            mCaeOperator.stopSaveAudio();
            isRecording = false;
            setText("停止录音");
            setText("---------stop_alsa_record---------");
        }
    }

    private void saveAudio(){
        if(mCaeOperator!=null) {
            if(!mCaeOperator.isAudioSaving()){//默认为false
                mCaeOperator.startSaveAudio();
                btnSave.setText("停止保存");
            }else{
                mCaeOperator.stopSaveAudio();
                btnSave.setText("开始保存");
            }
        }
    }


    /**
     * 读取外部音频写入 CAE SDK
     */
    private void writeAudioTest(){
        if(isRecording || isWriting || mCaeOperator==null){
            return;
        }
        isWriting = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 写入音频
                InputStream in = null;
                try {
                    // 主动开启CAE工作：1波束方面 阵列正向进行降噪
//                    mCaeOperator.setRealBeam(1);
                    in = getResources().getAssets().open("audio/caoza2.pcm");
                    // 2mic 2通道 96k 16bit
//                    byte[] audio = new byte[512*12*4];

                    byte[] audio = new byte[512*2*8];
                    int byteread = 0;
                    Log.e(TAG,"开始测试音频读写");
                    // 流式读取文件写入aiui
                    while ((byteread = in.read(audio)) != -1) {
                        if(!isWriting){
                            break;
                        }
                        byte[] data = RecordAudioUtil.addCnFor2MicN4(audio);
//                        byte[] data = RecordAudioUtil.adapeter4Mic(audio);
//                        byte[] data = RecordAudioUtil.adapeter6Mic(audio);
//                        mCaeOperator.saveAduio(data,CaeOperator.mAlsaRecFileUtil);


                        mCaeOperator.writeAudioTest(data);
                        Thread.sleep(40);
                    }
                    in.close();
                    Log.e(TAG,"测试音频读写完成");
                    setText("-------测试音频读写完成-------");

                    isWriting = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    isWriting = false;
                }
            }
        }).start();
    }



    /**
     * AIUI 回调信息处理
     */
    private AIUIListener mAIUIListener = new AIUIListener() {
        @Override
        public void onEvent(AIUIEvent event) {
            switch (event.eventType) {
                case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
                    LogUtils.i(TAG,"已连接服务器");
                    String uid = event.data.getString("uid");
                    break;

                case AIUIConstant.EVENT_SERVER_DISCONNECTED:
                    LogUtils.i(TAG,"与服务器断开连接");
                    break;

                case AIUIConstant.EVENT_WAKEUP:
                    LogUtils.i(TAG,"进入识别状态");
                    break;

                case AIUIConstant.EVENT_RESULT:
                    LogUtil.iTag(TAG, "AIUI EVENT_RESULT --- INFO -- " + event.info);
                    LogUtil.iTag(TAG, "AIUI EVENT_RESULT --- DATA -- " + event.data);
                    //听写结果(iat)
                    //语义结果(nlp)
                    //后处理服务结果(tpp)
                    //云端tts结果(tts)
                    //翻译结果(itrans)
                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));

                            String sid = event.data.getString("sid");

                            String sub = params.optString("sub");
                            JSONObject result = cntJson.optJSONObject("intent");
                            if ("nlp".equals(sub) && result.length() > 2) {
                                LogUtils.i(TAG, "nlp result :" + result.toString());
                                // 解析得到语义结果
                                String str = "";
                                //在线语义结果
                                String iatRes = result.optString("text");
                                if (result.optInt("rc") == 0) {
                                    str = "语义rc=0，识别内容：" + iatRes;
                                } else {
                                    str = "语义rc≠0，识别内容：" + iatRes;
                                }
                                setText(str);
                                setText("---------nlp_reslut---------");

                            }
                        }


//                        String sub = params.optString("sub");
//                        if ("tts".equals(sub)) {
//                            if (content.has("cnt_id")) {
//                                String sid = event.data.getString("sid");
//                                String cnt_id = content.getString("cnt_id");
//                                byte[] audio = event.data.getByteArray(cnt_id); //合成音频数据
//
//                                mAudioTrackOperator.write(audio);
//                                /**
//                                 *
//                                 * 音频块位置状态信息，取值：
//                                 * - 0（合成音频开始块）
//                                 * - 1（合成音频中间块，可出现多次）
//                                 * - 2（合成音频结束块)
//                                 * - 3（合成音频独立块,在短合成文本时出现）
//                                 *
//                                 * 举例说明：
//                                 * 一个正常语音合成可能对应的块顺序如下：
//                                 *   0 1 1 1 ... 2
//                                 * 一个短的语音合成可能对应的块顺序如下:
//                                 *   3
//                                 **/
//                                int dts = content.getInt("dts");
//                                int frameId = content.getInt("frame_id");// 音频段id，取值：1,2,3,...
//
//                                int percent = event.data.getInt("percent"); //合成进度
//
//                                boolean isCancel = "1".equals(content.getString("cancel"));  //合成过程中是否被取消
//                            }
//                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;

                case AIUIConstant.EVENT_ERROR:
                    setText("错误: " + event.arg1 + "\n" + event.info);
                    setText("---------error_aiui---------");
                    break;

                case AIUIConstant.EVENT_VAD:
                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        LogUtils.i("找到vad_bos");
                    } else if (AIUIConstant.VAD_BOS_TIMEOUT == event.arg1) {
                        LogUtils.i("前端点超时");
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        LogUtils.i("找到vad_eos");
                    } else {
                        LogUtils.i(TAG, "event_vad" + event.arg2);
                    }
                    break;
                case AIUIConstant.EVENT_SLEEP:
                    LogUtils.i(TAG, "设备进入休眠");
                    break;

                case AIUIConstant.EVENT_START_RECORD:
                    LogUtils.i(TAG, "已开始录音");
                    break;

                case AIUIConstant.EVENT_STOP_RECORD:
                    LogUtils.i(TAG, "已停止录音");
                    break;

                case AIUIConstant.EVENT_STATE:    // 状态事件
                    mAIUIState = event.arg1;
                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                        LogUtils.i(TAG, "event state is STATE_IDLE");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                        LogUtils.i(TAG, "event state is STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                        LogUtils.i(TAG, "event state is STATE_WORKING");
                    }
                    break;

                default:
                    break;
            }
        }
    };


    int i = 1;
    /**
     * CAE 回调消息处理
     */
    private OnCaeOperatorlistener onCaeOperatorlistener = new OnCaeOperatorlistener() {
        @Override
        public void onAudio(byte[] audioData, int dataLen) {
            // CAE降噪后音频写入AIUI SDK进行语音交互
            if(mAIUIState == AIUIConstant.STATE_WORKING){
                String params = "data_type=audio,sample_rate=16000";
                AIUIMessage msg = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, audioData);
                mAIUIAgent.sendMessage(msg);
            }


        }

        @Override
        public void onWakeup(int angle ,int beam) {
            // 唤醒响应时间分析标记
//            byte[] data = new byte[16];
//            Arrays.fill(data, (byte) (0xff/2));
//            mCaeOperator.saveAduio(data,CaeOperator.mAlsaRawFileUtil);

            // 取消AIUI的上一轮播报
//            AIUIMessage cancelTts = new AIUIMessage(AIUIConstant.CMD_TTS,AIUIConstant.CANCEL, 0, "", null);
//            mAIUIAgent.sendMessage(cancelTts);

            final int a = angle;
            final int b = beam;
            LogUtils.d(TAG,"wakeup success ,angle:" + a + " beam:" + b +"，唤醒次数"+i);
            i++;
            setText("唤醒成功,angle:" + a + " beam:" + b );
            setText("---------WAKEUP_CAE---------");
            // CAE SDK触发唤醒后给AIUI SDK发送手动唤醒事件：让AIUI SDK置于工作状态
            AIUIMessage resetWakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(resetWakeupMsg);

        }
    };


    /**
     * Alsa录音回调消息处理
     */
    private RecordListener onRecordListener = new RecordListener() {
        @Override
        public void onPcmData(byte[] bytes) {

            // 保存原始录音数据
            mCaeOperator.saveAduio(bytes,CaeOperator.mAlsaRawFileUtil);

            /**
             * CAE引擎写入的录音数据由 vtn.ini-input_audio_unit配置，2表示接收16bit数据，4表示接收32bit音频
             * 线性/环形6mic 录音数据是16bit（RecOperator.mPcmFormat） 所以此处不需要转换，可直接写入CAE
             */

            // 录音数据转换：usb声卡 线性2mic
//            byte[] data = RecordAudioUtil.adapeter2Mic(bytes);
            // 录音数据转换：usb声卡 线性4mic
//            byte[] data = RecordAudioUtil.adapeter4Mic(bytes);
            // 录音数据转换：usb声卡 线性/环形6mic
//            byte[] data = RecordAudioUtil.adapeter6Mic(bytes);


            // 保存转换后录音数据
//            mCaeOperator.saveAduio(bytes,CaeOperator.mAlsaRecFileUtil);
            // 写入CAE引擎
            mCaeOperator.writeAudioTest(bytes);

        }
    };

    private void distoryRecord(){
        if (null != mRecOperator && null != mCaeOperator) {
            mRecOperator.stopRecord();
            mCaeOperator.stopSaveAudio();
        }else{
            Log.d(TAG, "distoryCaeEngine is Done!");
        }
    }


    /**
     * 申请权限
     */
    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS,
                        Manifest.permission.INTERNET}, 0x0010);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据scrolview 和子view去测量滑动的位置
     *
     * @param scrollView
     * @param view
     */
    private void scrollToBottom(final ScrollView scrollView, final View view) {

        handler.post(new Runnable() {

            @Override
            public void run() {
                if (scrollView == null || view == null) {
                    return;
                }
                // offset偏移量。是指当textview中内容超出 scrollview的高度，那么超出部分就是偏移量
                int offset = view.getMeasuredHeight()
                        - scrollView.getMeasuredHeight();
                if (offset < 0) {
                    offset = 0;
                }
                //scrollview开始滚动
                scrollView.scrollTo(0, offset);
            }
        });
    }

    private void setText(final String str) {
        if(Looper.getMainLooper() == Looper.myLooper()){
            mResText.append(str + " \n");
            scrollToBottom(mScrollView, mResText);
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mResText.append(str + " \n");
                    scrollToBottom(mScrollView, mResText);
                }
            });
        }

    }


}