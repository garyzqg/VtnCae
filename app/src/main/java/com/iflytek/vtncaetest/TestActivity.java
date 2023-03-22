package com.iflytek.vtncaetest;

import android.os.Bundle;
import android.view.View;

import com.iflytek.vtncaetest.server.HttpServer;
import com.iflytek.vtncaetest.server.ServerConfig;
import com.iflytek.vtncaetest.util.InitUtil;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import fi.iki.elonen.NanoHTTPD;

public class TestActivity extends AppCompatActivity {

    private HttpServer mHttpServer;

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHttpServer.stop();
    }
}