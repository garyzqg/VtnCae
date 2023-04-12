package com.inspur.robotspeech.server;

import android.text.TextUtils;

import com.inspur.robotspeech.bean.ServerResponse;
import com.inspur.robotspeech.bean.WakeupWordData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import payfun.lib.basis.utils.LogUtil;
import payfun.lib.net.helper.GsonHelper;

/**
 * @author : zhangqinggong
 * date    : 2023/3/20 9:56
 * desc    : 服务器
 */
public class HttpServer extends NanoHTTPD {
    private static final String TAG = "HttpServer";
    private HttpListener httpListener;
    public HttpServer(String hostname, int port) {
        super(hostname, port);
    }

    public void setListener(HttpListener httpListener){
        this.httpListener = httpListener;
    }
    @Override
    public Response serve(IHTTPSession session) {
        LogUtil.iTag(TAG, "serve uri: " + session.getUri());

        Map<String, List<String>> parameters = session.getParameters();
        if (session.getMethod() == Method.POST){
            Map<String, String> postBodys = new HashMap<String, String>();
            JSONObject json = null;
            try {
                //post请求需要先调用parseBody 将参数映射到postBodys中(只针对json格式body,如果是form表单还需要到parameters取)
                session.parseBody(postBodys);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
                e.printStackTrace();
            }

            LogUtil.iTag(TAG, "serve POST body: " + postBodys);

            String body= postBodys.get("postData");

            try {
                json = new JSONObject(body);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (TextUtils.equals(session.getUri(), ServerConfig.HTTP_CHANGE_TIMBRE)) {//切换音色
                String voiceName = json.optString("voiceName");//音色
//                String scene = json.optString("scene");//情景 暂时不用
                if (httpListener != null){
                    httpListener.onChangeTimbre(voiceName);
                }

                Response response = responseJsonString(0, "success",null);
                return response;
            } else if (TextUtils.equals(session.getUri(), ServerConfig.HTTP_CHANGE_WAKEUP_WORD)) {//切换唤醒词
                String wakeupWord = parameters.get("WakeupWord").get(0);//唤醒词
                // TODO: 2023/3/21 当前未使用
                Response response = responseJsonString(0, "success",null);
                return response;
            }else if (TextUtils.equals(session.getUri(), ServerConfig.HTTP_SET_TOKEN)) {//设置token
                String token = json.optString("botID");//token
                if (httpListener != null){
                    httpListener.onSetToken(token);
                }
                //这个接口code 200 表示成功 且参数结构与其它接口不一致
                Response response = newFixedLengthResponse("{\"code\":200, \"data\" : \"{}\"}");
                return response;
            }
        }else {
            LogUtil.iTag(TAG, "serve GET parameter: " + session.getQueryParameterString());
            if (TextUtils.equals(session.getUri(), ServerConfig.HTTP_WAKEUP)) {//手动唤醒
                //没有参数
                if (httpListener != null){
                    httpListener.onWakeUp();
                }
                Response response = responseJsonString(0, "success",null);
                return response;
            } else if (TextUtils.equals(session.getUri(), ServerConfig.HTTP_SET_SLEEP)){//手动休眠
                if (httpListener != null){
                    httpListener.onSleep();
                }
                Response response = responseJsonString(0, "success",null);
                return response;
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

    public interface HttpListener{
        void onChangeTimbre(String timbre);
        void onSetToken(String token);
        void onWakeUp();
        void onSleep();
    }
}
