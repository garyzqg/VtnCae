package com.iflytek.vtncaetest.websocket;

import android.text.TextUtils;

import com.iflytek.vtncaetest.bean.NlpBean;
import com.iflytek.vtncaetest.bean.TtsBean;
import com.iflytek.vtncaetest.util.Base64Utils;
import com.iflytek.vtncaetest.util.GsonHelper;
import com.iflytek.vtncaetest.util.LogUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * @author : zhangqinggong
 * date    : 2023/1/13 10:22
 * desc    : WebSocketClient
 */
public class JWebSocketClient extends WebSocketClient {

   private static final String TAG = "JWebSocketClient";
   public JWebSocketClient(URI serverUri) {
      super(serverUri);
   }

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

}
