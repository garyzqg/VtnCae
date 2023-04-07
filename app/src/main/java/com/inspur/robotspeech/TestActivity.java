package com.inspur.robotspeech;

import android.os.Bundle;
import android.view.View;

import com.inspur.robotspeech.mqtt.MqttOperater;
import com.inspur.robotspeech.mqtt.MqttServiceConnction;
import com.inspur.robotspeech.net.NetConstants;
import com.inspur.robotspeech.server.HttpServer;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import fi.iki.elonen.NanoHTTPD;
import payfun.lib.basis.utils.InitUtil;

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
                mHttpServer = new HttpServer(NetConstants.HTTP_SERVER_IP, NetConstants.HTTP_SERVER_PORT);
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