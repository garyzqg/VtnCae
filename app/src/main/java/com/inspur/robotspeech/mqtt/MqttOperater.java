package com.inspur.robotspeech.mqtt;

import android.content.Context;
import android.content.Intent;

/**
 * @author : zhangqinggong
 * date    : 2023/3/22 14:28
 * desc    : MQTT消息发布操作类
 */
public class MqttOperater {
    private MqttServiceConnction mMqttServiceConnction;

    /**
     * 绑定服务 mqtt建联
     * @param context
     */
    public void bindService(Context context){
        mMqttServiceConnction = new MqttServiceConnction();
        Intent intent = new Intent(context, MqttService.class);
        context.bindService(intent, mMqttServiceConnction, context.BIND_AUTO_CREATE);
    }

    /**
     * 发布消息 唤醒 TOPIC:WAKEUP_TOPIC
     * @param angle 角度
     */
    public void pulishWakeup(int angle){
        if (mMqttServiceConnction != null){
            mMqttServiceConnction.getMqttService().sendMessage(MqttService.WAKEUP_TOPIC,"{\"angle\":\""+angle+"\"}");
        }
    }

    /**
     * 发布消息 超时后休眠 需要重新唤醒 TOPIC:VOICE_END_TOPIC
     */
    public void pulishEnd(){
        if (mMqttServiceConnction != null){
            mMqttServiceConnction.getMqttService().sendMessage(MqttService.VOICE_END_TOPIC,"{\"sleep\":\"1\"}");
        }
    }

    /**
     * 发布消息 nlp数据 TOPIC:CUSTOM_QA_TOPIC
     * 在云平台上面配置以qa_开头的意图相关的用户自定义的科普问答相关问题，语音说这个问题，例如仙冲有什么好玩的，
     * 语义理解之后往/bot/service/voice/order/question主题发送数据
     */
    public void pulishQaTopic(String nlpData){
        if (mMqttServiceConnction != null){
            mMqttServiceConnction.getMqttService().sendMessage(MqttService.CUSTOM_QA_TOPIC,nlpData);
        }
    }

    /**
     * 发布消息 nlp数据 TOPIC:COMMAND_TOPIC
     * intent command_开头
     */
    public void pulishCommandTopic(String nlpData){
        if (mMqttServiceConnction != null){
            mMqttServiceConnction.getMqttService().sendMessage(MqttService.COMMAND_TOPIC,nlpData);
        }
    }

    /**
     * 发布消息 nlp数据 TOPIC:GENARAL_TOPIC
     * intent 除了qa_和command_开头
     */
    public void pulishGenaralTopic(String nlpData){
        if (mMqttServiceConnction != null){
            mMqttServiceConnction.getMqttService().sendMessage(MqttService.GENARAL_TOPIC,nlpData);
        }
    }

    /**
     * 发布消息 流式语音识别 TOPIC:VOICE_RECO_TOPIC
     *
     * {"text":["今天","天气"，"怎么样"]}
     */
    public void pulishVoiceRecTopic(String rec){
        if (mMqttServiceConnction != null){
            mMqttServiceConnction.getMqttService().sendMessage(MqttService.VOICE_RECO_TOPIC,rec);
        }
    }

    /**
     * 解绑服务
     * @param context
     */
    public void unbindService(Context context){
        if (mMqttServiceConnction != null){
            context.unbindService(mMqttServiceConnction);
        }
    }
}
