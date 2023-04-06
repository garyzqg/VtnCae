package com.iflytek.vtncaetest.net;

/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 网络常量
 */
public class NetConstants {
    public static final String USER_ACCOUNT = "lixingyu";
    public static final String USER_PWD = "Aa111111";

    //http 登录
    public static final String BASE_URL_TEST = "http://10.180.151.125:18088";
    public static final String BASE_URL_PROD = "http://101.43.161.46:60306";

    //http 调用机器人应用层接口
    public static final String BASE_URL_USER = "http://192.168.10.50:8010";


    //websocket
    public static final String BASE_WS_URL_TEST = "ws://10.180.151.125:18088";
    public static final String BASE_WS_URL_PROD = "ws://101.43.161.46:60306";

    //mqtt
    public static final String MQTT_HOST = "ws://10.180.149.110:8083/mqtt";

    //httpserver
    public static final String HTTP_SERVER_IP = "10.180.149.241";//手机ip地址
    public static final int HTTP_SERVER_PORT = 8080;//自定义

}
