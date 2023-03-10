package com.iflytek.vtncaetest;

import android.Manifest;
import android.content.res.AssetManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.iflytek.iflyos.cae.CAE;
import com.iflytek.vtncaetest.audio.AudioTrackOperator;
import com.iflytek.vtncaetest.cae.CaeOperator;
import com.iflytek.vtncaetest.cae.OnCaeOperatorlistener;
import com.iflytek.vtncaetest.recorder.RecOperator;
import com.iflytek.vtncaetest.recorder.RecordListener;
import com.iflytek.vtncaetest.util.InitUtil;
import com.iflytek.vtncaetest.util.LogUtil;
import com.iflytek.vtncaetest.util.RecordAudioUtil;
import com.iflytek.vtncaetest.websocket.WebsocketOperator;

import org.json.JSONArray;
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
    private WebsocketOperator mWebsocketOperator;
    private String mIatMessage;//iat有效数据


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
        findViewById(R.id.audioplay).setOnClickListener(this);
        findViewById(R.id.status).setOnClickListener(this);
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
            case R.id.audioplay:
                audioPlayTest();
//                CAE.CAESetRealBeam(3);
                break;
            case R.id.status:
                LogUtil.iTag(TAG, "state: "+mAudioTrackOperator.getState() +" playState" +mAudioTrackOperator.getPlayState());
                break;
            default:
                break;
        }

    }

    private void audioPlayTest() {
        mAudioTrackOperator.play();
        mAudioTrackOperator.writeSource(MainActivity.this,"audio/xiaojuan_box_welcome.pcm");
    }

    private void initSDK() {
        // 初始化AIUI
        createAgent();
        // 初始化CAE
        initCaeEngine();
        // 初始化alsa录音
        initAlsa();
        //初始化AudioTrack
        initAudioTrack();
        //初始化websocket
        initWebsocket();
    }

    private void initWebsocket() {
        if (mWebsocketOperator == null){
            mWebsocketOperator = new WebsocketOperator();
            mWebsocketOperator.initWebSocket(new WebsocketOperator.IWebsocketListener() {
                @Override
                public void OnTtsData(byte[] audioData, boolean isFinish) {
                    // TODO: 2023/1/30 每次都调用play?
                    mAudioTrackOperator.play();
                    mAudioTrackOperator.write(audioData,isFinish);
                }

                @Override
                public void onOpen() {
                    mAudioTrackOperator.play();
                    mAudioTrackOperator.writeSource(MainActivity.this,"audio/xiaojuan_box_wakeUpReply.pcm");
                }

                @Override
                public void onError() {
                    mAudioTrackOperator.play();
                    mAudioTrackOperator.writeSource(MainActivity.this,"audio/xiaojuan_box_disconnect.pcm");
                }

            });
        }
    }

    private void initAudioTrack() {
        if (mAudioTrackOperator == null){
            mAudioTrackOperator = new AudioTrackOperator();
            mAudioTrackOperator.createStreamModeAudioTrack();
//            mAudioTrackOperator.play();
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
            LogUtil.iTag(TAG, "create aiui agent");

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                ret = mCaeOperator.initCAE(onCaeOperatorlistener);
                if(ret == 0){
                    strTip = "CAE初始化成功";

                    String ver =  mCaeOperator.getCAEVersion();
                    LogUtil.iTag(TAG,"vae ver is: "+ver);
//            initAlsa();
                }else{
                    strTip = "CAE初始化失败,错误信息为："+ ret;
                }
            }
        }).start();

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
//                    Log.e(TAG,"开始测试音频读写");
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
//                    Log.e(TAG,"测试音频读写完成");
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
                    LogUtil.iTag(TAG,"AIUI -- 已连接服务器");
                    String uid = event.data.getString("uid");
                    break;

                case AIUIConstant.EVENT_SERVER_DISCONNECTED:
                    LogUtil.iTag(TAG,"AIUI -- 与服务器断开连接");
                    break;

                case AIUIConstant.EVENT_WAKEUP:
                    LogUtil.iTag(TAG,"AIUI -- WAKEUP 进入识别状态");
                    break;

                case AIUIConstant.EVENT_RESULT:
//                    LogUtil.iTag(TAG, "AIUI EVENT_RESULT --- INFO -- " + event.info);
//                    LogUtil.iTag(TAG, "AIUI EVENT_RESULT --- DATA -- " + event.data);
                    //听写结果(iat)
                    //语义结果(nlp)
                    //后处理服务结果(tpp)
                    //云端tts结果(tts)
                    //翻译结果(itrans)

                    /**
                     * event.info
                     * {
                     *     "data": [{
                     *         "params": {
                     *             "sub": "iat",
                     *         },
                     *         "content": [{
                     *             "dte": "utf8",
                     *             "dtf": "json",
                     *             "cnt_id": "0"
                     *         }]
                     *     }]
                     * }
                     */
                    try {
                        JSONObject info = new JSONObject(event.info);
                        JSONObject infoData = info.optJSONArray("data").optJSONObject(0);
                        String sub = infoData.optJSONObject("params").optString("sub");
                        JSONObject content = infoData.optJSONArray("content").optJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.optString("cnt_id");
                            String resultString = new String(event.data.getByteArray(cnt_id), "utf-8");
                            if ("iat".equals(sub) && resultString.length() > 2) {
                                JSONObject result = new JSONObject(resultString);
                                JSONObject text = result.optJSONObject("text");
                                boolean ls = text.optBoolean("ls");//是否结束
                                JSONArray ws = text.optJSONArray("ws");
                                StringBuilder currentIatMessage = new StringBuilder();
                                for (int j = 0; j < ws.length(); j++) {
                                    JSONArray cw = ws.optJSONObject(j).optJSONArray("cw");
                                    String w = cw.optJSONObject(0).optString("w");
                                    if (!TextUtils.isEmpty(w)){
                                        currentIatMessage.append(w);
                                    }
                                }

                                LogUtil.iTag(TAG, "AIUI EVENT_RESULT --- iat -- current -- " + currentIatMessage);

                                if (currentIatMessage!= null && currentIatMessage.length()>0){
                                    mIatMessage = currentIatMessage.toString();
                                }

                                if (ls){
                                    LogUtil.iTag(TAG, "AIUI EVENT_RESULT --- iat -- final -- " + mIatMessage);
                                    mWebsocketOperator.sendMessage(mIatMessage);
                                }

                            }
                        }


                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;

                case AIUIConstant.EVENT_ERROR:
                    setText("错误: " + event.arg1 + "\n" + event.info);
                    setText("---------error_aiui---------");
                    break;

                case AIUIConstant.EVENT_VAD:
//                    if (AIUIConstant.VAD_BOS == event.arg1) {
//                        LogUtil.iTag(TAG,"AIUI -- VAD 找到vad_bos");
//                    } else if (AIUIConstant.VAD_BOS_TIMEOUT == event.arg1) {
//                        LogUtil.iTag(TAG,"AIUI -- VAD 前端点超时");
//                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
//                        LogUtil.iTag(TAG,"AIUI -- VAD 找到vad_eos");
//                    } else {
//                        LogUtil.iTag(TAG, "AIUI -- VAD " + event.arg2);
//                    }
                    break;
                case AIUIConstant.EVENT_SLEEP:
                    LogUtil.iTag(TAG, "AIUI -- 设备进入休眠");
                    break;

                case AIUIConstant.EVENT_START_RECORD:
                    LogUtil.iTag(TAG, "AIUI -- 已开始录音");
                    break;

                case AIUIConstant.EVENT_STOP_RECORD:
                    LogUtil.iTag(TAG, "AIUI -- 已停止录音");
                    break;

                case AIUIConstant.EVENT_STATE:    // 状态事件
                    mAIUIState = event.arg1;
                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                        LogUtil.iTag(TAG, "AIUI -- STATE_IDLE");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                        LogUtil.iTag(TAG, "AIUI -- STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                        LogUtil.iTag(TAG, "AIUI -- STATE_WORKING");
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
            if(mAIUIState == AIUIConstant.STATE_WORKING && mAudioTrackOperator.getPlayState() != AudioTrack.PLAYSTATE_PLAYING){
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
            LogUtil.iTag(TAG,"CAE -- wakeup success ,angle:" + a + " beam:" + b +"，唤醒次数"+i);
            i++;
            setText("唤醒成功,angle:" + a + " beam:" + b );
            setText("---------WAKEUP_CAE---------");
            // CAE SDK触发唤醒后给AIUI SDK发送手动唤醒事件：让AIUI SDK置于工作状态
            AIUIMessage resetWakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(resetWakeupMsg);

            //websocket建联 若已连接状态需要先断开
            mWebsocketOperator.connectWebSocket();

            //播放本地音频文件 欢迎 需要先停止当前播放且释放队列内数据
            mAudioTrackOperator.shutdownExecutor();
            mAudioTrackOperator.stop();
            mAudioTrackOperator.flush();

            // TODO: 2023/2/1 唤醒后默认切换到音源位置的beam, 此时如果环形麦跟随机器转动,需要手动调用方法设置beam 目前设置为5(M1)
            CAE.CAESetRealBeam(5);


//            mAudioTrackOperator.play();
//            mAudioTrackOperator.writeSource(MainActivity.this,"audio/xiaojuan_box_welcome.pcm");

        }
    };


    /**
     * Alsa录音回调消息处理
     */
    private RecordListener onRecordListener = new RecordListener() {
        @Override
        public void onPcmData(byte[] bytes) {
//            LogUtil.iTag(TAG,"ALSA录音消息回调");
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
            LogUtil.dTag(TAG, "distoryCaeEngine is Done!");
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