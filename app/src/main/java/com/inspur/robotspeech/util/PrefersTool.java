package com.inspur.robotspeech.util;

import payfun.lib.basis.utils.SpUtil;

/**
 * @author : zhangqinggong
 * date    : 2023/2/27 9:41
 * desc    : sp管理类
 */
public class PrefersTool {
    private static final String VOICE_NAME = "voice_name";//音色
    private static final String ACCESS_TOKEN = "accesstoken";

    public static void setAccesstoken(String accesstoken) {
        SpUtil.getInstance().put(ACCESS_TOKEN, accesstoken);
    }
    public static String getAccesstoken() {
        return SpUtil.getInstance().getString(ACCESS_TOKEN);
    }

    public static void setVoiceName(String voiceName) {
        SpUtil.getInstance().put(VOICE_NAME, voiceName);
    }
    public static String getVoiceName() {
        return SpUtil.getInstance().getString(VOICE_NAME, "XiaoshuangNeural");
    }

}
