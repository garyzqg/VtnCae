package com.iflytek.vtncaetest.websocket;

import android.text.TextUtils;

import com.iflytek.vtncaetest.bean.NlpBean;
import com.iflytek.vtncaetest.bean.TtsBean;
import com.iflytek.vtncaetest.net.NetConstants;
import com.iflytek.vtncaetest.util.Base64Utils;
import com.iflytek.vtncaetest.util.PrefersTool;

import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

import payfun.lib.basis.utils.LogUtil;
import payfun.lib.net.helper.GsonHelper;

/**
 * @author : zhangqinggong
 * date    : 2023/1/15 0:18
 * desc    : Websocket操作类
 */
public class WebsocketOperator {
   private static final String TAG = "WebsocketOperator";
   private JWebSocketClient mClient;
   private static String sessionId;
   private static WebsocketOperator instance;

   // TODO: 2023/4/4 暂时写死讲解机器人情景id
   private final String SCENE_ID = "1619893838471073794";
   //tts渠道 - 科大讯飞
   private final String TTS_TYPE_XF = "xfyun";
   //tts渠道 - 微软
   private final String TTS_TYPE_WR = "azure";
   //科大讯飞TTS只支持这一个音色
   private final String VOICE_NAME = "x2_xiaojuan";

   private WebsocketOperator() {
   }
   public static WebsocketOperator getInstance(){
      if (instance == null){
         instance = new WebsocketOperator();
      }
      return instance;
   }

   private static void getSessionId() {
      //启动时生成UUID作为sessionId 如果有登出操作需要重置
      sessionId = UUID.randomUUID().toString();
   }

   /**
    * websocket初始化
    */
   public void initWebSocket(boolean reInit,IWebsocketListener iWebsocketListener) {
      if (reInit || mClient == null) {
         getSessionId();
         //ws://101.43.161.46:58091/ws？token=fengweisen&scene=xiaoguo_box&voiceName=xiaozhong&speed=50&ttsType=crcloud
//         URI uri = URI.create("ws://101.43.161.46:58091/ws?token=fengweisen&scene=main_box&voiceName=xiaozhong&speed=50&ttsType=crcloud");
//         URI uri = URI.create(NetConstants.BASE_WS_URL_TEST+"/expressing/ws?sceneId="+SCENE_ID+"&voiceName="+ PrefersTool.getVoiceName()+"&ttsType="+TTS_TYPE_WR+"&sessionId="+sessionId);
         URI uri = URI.create(NetConstants.BASE_WS_URL_TEST+"/expressing/ws?sceneId="+SCENE_ID+"&voiceName="+ VOICE_NAME+"&ttsType="+TTS_TYPE_XF+"&sessionId="+sessionId);
         //为了方便对接收到的消息进行处理，可以在这重写onMessage()方法
         LogUtil.iTag(TAG, "WebSocket init");
         mClient = new JWebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
               LogUtil.iTag(TAG, "WebSocket onOpen");
               if (iWebsocketListener != null){
                  iWebsocketListener.onOpen();
               }
            }

            @Override
            public void onMessage(String message) {
//               LogUtil.iTag(TAG, "onMessage:" + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
               LogUtil.iTag(TAG, "WebSocket onClose: code:" + code + " reason:" + reason + " remote:" + remote);
               // TODO: 2023/1/13 断开连接后 是否控制不往aiui写数据 如何保证websocket的超时和AIUI的超时保持一致?
                if (iWebsocketListener != null){
                    iWebsocketListener.onClose(reason.contains("401"));
                }
            }

            @Override
            public void onError(Exception ex) {
               ex.printStackTrace();
               LogUtil.iTag(TAG, "WebSocket onError:" + ex.toString());
               if (iWebsocketListener != null){
                  iWebsocketListener.onError();
               }

            }

            @Override
            public void onMessage(ByteBuffer bytes) {
//               super.onMessage(bytes);
               //{"data":{"question":"你好","answer":"你也好","entities":[],"finish":true,"intent":"qa_general_intent"},"type":"nlp"}
               //{"type": "tts","data": {"is_finish": true,"audio": ""}}
               Charset charset = Charset.forName("utf-8");
               CharBuffer decode = charset.decode(bytes);
               String message = decode.toString();
               if (TextUtils.isEmpty(message)){
                  return;
               }

               try {
                  JSONObject jsonObject = new JSONObject(message);
                  String type = jsonObject.optString("type");
                  String data = jsonObject.optString("data");
                  LogUtil.iTag(TAG, "WebSocket onMessage -- type: " + type);
                  if (TextUtils.equals("nlp", type)) {
                     NlpBean nlpBean = GsonHelper.GSON.fromJson(data, NlpBean.class);
                     if (iWebsocketListener != null){
                        iWebsocketListener.OnNlpData(nlpBean);
                     }

                  } else if (TextUtils.equals("tts", type)) {
                     TtsBean ttsBean = GsonHelper.GSON.fromJson(data, TtsBean.class);
                     boolean is_finish = ttsBean.isIs_finish();
                     String audio = ttsBean.getAudio();
                     if (TextUtils.isEmpty(audio)){
                        if (iWebsocketListener != null){
                           iWebsocketListener.OnTtsData(null,is_finish);
                        }
                     }else {
                        byte[] audioByte = Base64Utils.base64DecodeToByte(audio);
                        if (iWebsocketListener != null){
                           iWebsocketListener.OnTtsData(audioByte,is_finish);
                        }
                     }

                  }

               } catch (JSONException e) {
                  e.printStackTrace();
               }
            }
         };
         setToken();
         mClient.setConnectionLostTimeout(10 * 1000);
      }
   }

   private void setToken(){
      String accesstoken = PrefersTool.getAccesstoken();
      if (!TextUtils.isEmpty(accesstoken)){
         mClient.addHeader("Authorization", "Bearer " + accesstoken);
      }
   }

   /**
    * websocket连接
    */
   public void connectWebSocket() {
      //需要先断开已有连接

      if (mClient != null && mClient.isOpen()){
         mClient.close();
      }

      new Thread(new Runnable() {
         @Override
         public void run() {
            //连接时可以使用connect()方法或connectBlocking()方法，建议使用connectBlocking()方法，
            // connectBlocking多出一个等待操作，会先连接再发送。
            if (mClient != null) {
               if (!mClient.isOpen()) {
                  try {
                     if (mClient.getReadyState().equals(ReadyState.NOT_YET_CONNECTED)) {
                        mClient.connectBlocking();
                        LogUtil.iTag("JWebSocketClient", "WebSocket connect");
                     } else if (mClient.getReadyState().equals(ReadyState.CLOSING) || mClient.getReadyState().equals(ReadyState.CLOSED)) {
                        mClient.reconnectBlocking();
                        LogUtil.iTag("JWebSocketClient", "WebSocket reconnect");
                     }
                  } catch (InterruptedException e) {
                     e.printStackTrace();
                  }
               }
            }
         }
      }).start();

   }

   /**
    * 发消息
    *
    * @param message
    */
   public void sendMessage(String message) {
      if (mClient != null && mClient.isOpen()) {
         LogUtil.iTag(TAG, "WebSocket sendMessage:" + message);
         mClient.send(message);
      } else {
         // TODO: 2023/1/13 此时如果是唤醒后超时没有交互,是否不做任何播报?
      }
   }

   public void close(){
      if (mClient != null && mClient.isOpen()){
         mClient.close();
      }
   }
   public interface IWebsocketListener{
      void OnTtsData(byte[] audioData,boolean isFinish);
      void OnNlpData(NlpBean nlpBean);
      void onOpen();
      void onError();
      void onClose(boolean isLogin);
   }
}
