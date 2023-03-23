package com.iflytek.vtncaetest.server;

import android.text.TextUtils;

import com.iflytek.vtncaetest.bean.ServerResponse;
import com.iflytek.vtncaetest.bean.WakeupWordData;
import com.iflytek.vtncaetest.util.GsonHelper;
import com.iflytek.vtncaetest.util.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * @author : zhangqinggong
 * date    : 2023/3/20 9:56
 * desc    : 服务器
 */
public class HttpServer extends NanoHTTPD {
    private static final String TAG = "HttpServer";

    public HttpServer(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        //打印请求数据
//        LogUtil.iTag(TAG, "serve getRemoteHostName: " + session.getRemoteHostName());
        LogUtil.iTag(TAG, "serve uri: " + session.getUri());
        try {
            //post请求需要先调用parseBody 否则getParameters获取不到参数
            session.parseBody(new HashMap<String, String>());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }

        Map<String, List<String>> parameters = session.getParameters();
        if (session.getMethod() == Method.POST){
            LogUtil.iTag(TAG, "serve POST body: " + session.getQueryParameterString());
            if (TextUtils.equals(session.getUri(), ServerConfig.HTTP_CHANGE_TIMBRE)) {//切换音色
                String scene = parameters.get("scene").get(0);//情景
                String voiceName = parameters.get("voiceName").get(0);//音色名称
                // TODO: 2023/3/21
                Response response = responseJsonString(0, "success",null);
                return response;
            } else if (TextUtils.equals(session.getUri(), ServerConfig.HTTP_CHANGE_WAKEUP_WORD)) {//切换唤醒词
                String wakeupWord = parameters.get("WakeupWord").get(0);//唤醒词
                // TODO: 2023/3/21 当前未使用
                Response response = responseJsonString(0, "success",null);
                return response;
            }else if (TextUtils.equals(session.getUri(), ServerConfig.HTTP_SET_TOKEN)) {//设置token
                String token = parameters.get("botID").get(0);//token
                // TODO: 2023/3/21
                //这个接口code 200 表示成功 且参数结构与其它接口不一致
                Response response = newFixedLengthResponse("{\"code\":200, \"data\" : \"{}\"}");
                return response;
            }
        }else {
            LogUtil.iTag(TAG, "serve GET parameter: " + session.getQueryParameterString());
            if (TextUtils.equals(session.getUri(), ServerConfig.HTTP_WAKEUP)) {//手动唤醒
                //没有参数
                // TODO: 2023/3/21 唤醒
                Response response = responseJsonString(0, "success",null);
                return response;
            } else if (TextUtils.equals(session.getUri(), ServerConfig.HTTP_SET_SLEEP)){//手动休眠
                // TODO: 2023/3/23 休眠 文档没有 待实现
            } else if (TextUtils.equals(session.getUri(), ServerConfig.HTTP_GET_WAKEUP_WORD_LIST)){//获取唤醒词列表
                // TODO: 2023/3/21 当前未使用
                WakeupWordData wakeupWordData = new WakeupWordData();
                wakeupWordData.setCurrWakeupWord("xiaoxinxiaoxin");
                WakeupWordData.WakeupWord wakeupWord1 = new WakeupWordData.WakeupWord("nihaoxiaoxin","你好小新");
                WakeupWordData.WakeupWord wakeupWord2 = new WakeupWordData.WakeupWord("xiaoxinxiaoxin","小新小新");
                ArrayList<WakeupWordData.WakeupWord> wakeupWords = new ArrayList<>();
                wakeupWords.add(wakeupWord1);
                wakeupWords.add(wakeupWord2);
                wakeupWordData.setWakeupWordList(wakeupWords);

                Response response = responseJsonString(0, "success",wakeupWordData);
                return response;
            }
        }
        return super.serve(session);
    }

    private <T> Response responseJsonString(int code, String msg, T data) {
        ServerResponse<T> serverResponse = new ServerResponse<>(code, msg);
        if (data != null){
            serverResponse.setData(data);
        }
        return newFixedLengthResponse(GsonHelper.GSON.toJson(serverResponse));
    }
}
