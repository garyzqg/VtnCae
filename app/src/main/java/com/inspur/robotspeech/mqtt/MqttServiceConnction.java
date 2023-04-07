package com.inspur.robotspeech.mqtt;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * @author : zhangqinggong
 * date    : 2023/3/22 10:31
 * desc    :
 */
public class MqttServiceConnction implements ServiceConnection {
    private MqttService mqttService;
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MqttService.CustomBinder binder = (MqttService.CustomBinder) service;
        mqttService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    public MqttService getMqttService(){
        if(mqttService != null){
            return mqttService;
        }
        return null;
    }
}
