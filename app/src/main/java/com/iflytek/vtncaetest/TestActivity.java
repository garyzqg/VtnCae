package com.iflytek.vtncaetest;

import android.os.Bundle;
import android.view.View;

import com.iflytek.vtncaetest.mqtt.MqttOperater;
import com.iflytek.vtncaetest.mqtt.MqttServiceConnction;
import com.iflytek.vtncaetest.server.HttpServer;
import com.iflytek.vtncaetest.server.ServerConfig;
import com.iflytek.vtncaetest.util.InitUtil;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import fi.iki.elonen.NanoHTTPD;

public class TestActivity extends AppCompatActivity {

    private HttpServer mHttpServer;
    private MqttServiceConnction mMqttServiceConnction;
    private MqttOperater mMqttOperater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        InitUtil.init(this);
        findViewById(R.id.init).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHttpServer = new HttpServer(ServerConfig.HTTP_IP, ServerConfig.HTTP_PORT);
                //三种启动方式都行
                //mHttpServer.start()
                //mHttpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT)
                try {
                    mHttpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mMqttOperater = new MqttOperater();
        mMqttOperater.bindService(this);

        findViewById(R.id.send_msg_topic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMqttOperater.pulishWakeup(60);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHttpServer.stop();

        mMqttOperater.unbindService(this);
    }
}