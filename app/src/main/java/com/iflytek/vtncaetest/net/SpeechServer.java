package com.iflytek.vtncaetest.net;

import com.iflytek.vtncaetest.bean.BaseResponse;
import com.iflytek.vtncaetest.bean.LoginBean;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 接口管理类
 */
public interface SpeechServer {
    //登录
    @POST("/bot/service/mmip/auth/v2/login")
    Observable<BaseResponse<LoginBean>> login(@Body RequestBody body);
}
