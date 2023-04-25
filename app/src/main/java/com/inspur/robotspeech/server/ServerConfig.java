package com.inspur.robotspeech.server;

/**
 * @author : zhangqinggong
 * date    : 2023/3/20 10:33
 * desc    : HTTP接口URI,外部调用
 */
public class ServerConfig {

    /**
     * 切换音色 POST
     * @param scene (String 需要切换到的场景）
     * @param voiceName (String 音色名字）
     */
    public static final String HTTP_CHANGE_TIMBRE = "/bot/service/voice/v1/change/timbre";

    /**
     * 点击唤醒 GET
     */
    public static final String HTTP_WAKEUP = "/bot/service/voice/v1/awakeup";

    /**
     * 切换唤醒词 POST
     * @param WakeupWord (String 需要切换的唤醒词，如nihaoxiaoxin）
     * 切换前，要保证在assetts下，存在切换词+“.bin”文件
     */
    public static final String HTTP_CHANGE_WAKEUP_WORD = "/bot/service/voice/v1/change/wakeupword";

    /**
     * 获取唤醒词 GET
     * 返回assetts下所有唤醒词文件
     */
    public static final String HTTP_GET_WAKEUP_WORD_LIST = "/bot/service/voice/v1/wakeupwordlist";

    /**
     * 传递TOKEN POST
     * @param botID (String 最新的token信息）
     */
    public static final String HTTP_SET_TOKEN = "/bot/service/voice/v1/token";


    /**
     * 休眠
     */
    public static final String HTTP_SET_SLEEP = "/bot/service/voice/v1/sleep";

    /**
     * 设置音量
     */
    public static final String HTTP_SET_VOLUME = "/bot/service/voice/v1/setvolume";



}
