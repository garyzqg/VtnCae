package com.inspur.robotspeech.net;

/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 网络常量
 */
public class NetConstants {
    public static final boolean lOG_SWITCH = true;


    public static final String USER_ACCOUNT = "demo_user";
    public static final String USER_PWD = "123456wp";

    //http 登录
    public static final String BASE_URL_TEST = "http://10.180.151.125:18088";
    public static final String BASE_URL_PROD = "http://apigateway.icloudbot.com";

    //http 调用机器人应用层接口
    public static final String BASE_URL_USER = "http://192.168.10.50:8010";


    //websocket
    public static final String BASE_WS_URL_TEST = "ws://10.180.151.125:18088";
    public static final String BASE_WS_URL_PROD = "ws://apigateway.icloudbot.com";

    //mqtt
    public static final String MQTT_HOST = "ws://192.168.10.50:8083/mqtt";

    //httpserver
    public static final String HTTP_SERVER_IP = "192.168.10.219";//手机ip地址
    public static final int HTTP_SERVER_PORT = 8004;//自定义

}
