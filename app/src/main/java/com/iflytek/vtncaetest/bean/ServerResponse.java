package com.iflytek.vtncaetest.bean;

/**
 * @author : zhangqinggong
 * date    : 2023/3/21 15:44
 * desc    : NanoHTTPD服务器返回参数 base
 */
public class ServerResponse<T> {
    private int code;
    private String msg;
    private T data;

    public ServerResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}
