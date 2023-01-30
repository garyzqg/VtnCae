package com.iflytek.vtncaetest.websocket;

import com.iflytek.vtncaetest.util.LogUtil;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

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
   }

}
