package com.iflytek.vtncaetest.net;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 接口管理类 - 上层用户
 */
public interface UserServer {
    @POST("/egdeUser/v1/botId/info/register")
    Observable<ResponseBody> register(@Body RequestBody body);
}
