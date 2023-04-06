package com.iflytek.vtncaetest.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;

import com.iflytek.vtncaetest.net.NetConstants;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import payfun.lib.basis.utils.LogUtil;

/**
 * @author : zhangqinggong
 * date    : 2023/3/22 9:48
 * desc    : mqtt服务
 */
public class MqttService extends Service implements MqttCallback {
    private static final String TAG = "MqttService";
    private static MqttAndroidClient androidClient;

    private static String CLIENTID = "";
    private static final int QOS = 0;	//传输质量
    private MqttConnectOptions connectOptions;

    public static final String WAKEUP_TOPIC = "/bot/service/voice/awakeup/start";//语音唤醒
    public static final String COMMAND_TOPIC = "/bot/service/voice/order/command";//指令 意图 command_开头
    public static final String CUSTOM_QA_TOPIC = "/bot/service/voice/order/question";//qa_开头
    public static final String GENARAL_TOPIC = "/bot/service/voice/order/general";//除了qa_和command_开头的指令都往这个主题发送
    public static final String VOICE_RECO_TOPIC = "/bot/service/voice/speechrecognition";//流式语音识别内容  {"text":["今天","天气"，"怎么样"]}  暂时没用
    public static final String VOICE_END_TOPIC = "/bot/service/voice/awakeup/end";//休眠
    @Deprecated
    public static final String SLEEP_TOPIC = "/bot/server/voice/sleep";//已废弃

    @StringDef({WAKEUP_TOPIC, COMMAND_TOPIC, CUSTOM_QA_TOPIC, GENARAL_TOPIC, VOICE_RECO_TOPIC, VOICE_END_TOPIC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Topic {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {

        CLIENTID =  Settings.System.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        androidClient = new MqttAndroidClient(this, NetConstants.MQTT_HOST,CLIENTID);
        androidClient.setCallback(this);
        connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);//是否保存离线消息 false保存离线消息，下次上线可以接收离线时接收到的消息；true不保存离线消息，下次上线不接收离线时接收到的消息；
        connectOptions.setConnectionTimeout(10);
        connectOptions.setKeepAliveInterval(20);//设置会话心跳时间
        connectOptions.setAutomaticReconnect(true);//是否自动重连
//        connectOptions.setUserName(USERNAME);
//        connectOptions.setPassword(PSD.toCharArray());

        doClientConnection();
    }

    private void doClientConnection() {
        if(!androidClient.isConnected()){
            try {
                androidClient.connect(connectOptions,null,iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            //连接成功
            LogUtil.iTag(TAG,"MQTT connect success");
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            //连接失败
            LogUtil.iTag(TAG,"MQTT connect fail " + exception.toString());
            //已设置自动重连
//            doClientConnection();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new CustomBinder();
    }

    public class CustomBinder extends Binder {
        public MqttService getService(){
            return MqttService.this;
        }
    }

    //发送消息
    public boolean sendMessage(@Topic String topic, String message) {
        LogUtil.iTag(TAG,"MQTT sendMessage "+ message);
        if (androidClient != null && androidClient.isConnected()) {
            try {
                androidClient.publish(topic, message.getBytes(), QOS, true);
                return true;
            } catch (MqttException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            doClientConnection();
            return false;
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        LogUtil.iTag(TAG, "MQTT connect lost " + cause.toString());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        LogUtil.iTag(TAG, "MQTT messageArrived " + new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        LogUtil.iTag(TAG, "MQTT deliveryComplete ");

    }

    @Override
    public void onDestroy() {
        if(androidClient != null){
            try {
                androidClient.disconnect();
                androidClient.unregisterResources();
//                androidClient.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
