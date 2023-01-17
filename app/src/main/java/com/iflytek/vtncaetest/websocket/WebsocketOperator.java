package com.iflytek.vtncaetest.websocket;

import android.text.TextUtils;
import com.iflytek.vtncaetest.bean.NlpBean;
import com.iflytek.vtncaetest.bean.TtsBean;
import com.iflytek.vtncaetest.util.Base64Utils;
import com.iflytek.vtncaetest.util.GsonHelper;
import com.iflytek.vtncaetest.util.LogUtil;

import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * @author : zhangqinggong
 * date    : 2023/1/15 0:18
 * desc    : Websocket操作类
 */
public class WebsocketOperator {
   private static final String TAG = "WebsocketOperator";
   private JWebSocketClient mClient;
   /**
    * websocket初始化
    */
   public void initWebSocket() {
      if (mClient == null){
         //ws://101.43.161.46:58091/ws？token=fengweisen&scene=xiaoguo_box&voiceName=xiaozhong&speed=50&ttsType=crcloud
         URI uri = URI.create("ws://101.43.161.46:58091/ws?token=fengweisen&scene=main_box&voiceName=xiaozhong&speed=50&ttsType=crcloud");
         //为了方便对接收到的消息进行处理，可以在这重写onMessage()方法
         LogUtil.iTag(TAG,"WebSocket init");
         mClient = new JWebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
               LogUtil.iTag(TAG,"onOpen");
            }

            @Override
            public void onMessage(String message) {
               LogUtil.iTag(TAG,"onMessage:" + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
               LogUtil.iTag(TAG,"onClose: code:" + code+ " reason:" + reason+ " remote:" + remote);
               // TODO: 2023/1/13 断开连接后 是否控制不往aiui写数据 如何保证websocket的超时和AIUI的超时保持一致?
            }

            @Override
            public void onError(Exception ex) {
               LogUtil.iTag(TAG,"onError:" + ex.toString());
               // TODO: 2023/1/13 播放离线语音 网络似乎开小差了呢，请稍微找我聊天吧
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
               super.onMessage(bytes);
               //{"data":{"question":"你好","answer":"你也好","entities":[],"finish":true,"intent":"qa_general_intent"},"type":"nlp"}
               //{"type": "tts","data": {"is_finish": true,"audio": ""}}
               Charset charset = Charset.forName("utf-8");
               CharBuffer decode = charset.decode(bytes);
               try {
                  JSONObject jsonObject = new JSONObject(decode.toString());
                  String type = jsonObject.optString("type");
                  LogUtil.iTag(TAG,"onMessage: type: " + type);
                  String data = jsonObject.optString("data");
                  if (TextUtils.equals("nlp",type)){
                     NlpBean nlpBean = GsonHelper.GSON.fromJson(data, NlpBean.class);
                     String question = nlpBean.getQuestion();
                  }else if (TextUtils.equals("tts",type)){
                     TtsBean ttsBean = GsonHelper.GSON.fromJson(data, TtsBean.class);
                     boolean is_finish = ttsBean.isIs_finish();
                     String audio = ttsBean.getAudio();

                     byte[] audioByte = Base64Utils.base64EncodeToByte(audio);


                  }

               } catch (JSONException e) {
                  e.printStackTrace();
               }
            }
            };

         mClient.setConnectionLostTimeout(10*1000);
      }
   }

   /**
    * websocket连接
    */
   public void connectWebSocket() {
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
    * @param message
    */
   public void sendMessage(String message) {
      if(mClient != null && mClient.isOpen()){
         LogUtil.iTag("JWebSocketClient","sendMessage:" + message);
         mClient.send(message);
      }else {
         // TODO: 2023/1/13 此时如果是唤醒后超时没有交互,是否不做任何播报?
      }
   }
}
