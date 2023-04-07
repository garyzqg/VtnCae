package com.inspur.robotspeech.net;

import android.text.TextUtils;

import com.inspur.robotspeech.bean.BaseResponse;
import com.inspur.robotspeech.bean.LoginBean;
import com.inspur.robotspeech.server.ServerConfig;
import com.inspur.robotspeech.util.Base64Utils;
import com.inspur.robotspeech.util.PrefersTool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import payfun.lib.net.NetManager;
import payfun.lib.net.helper.GsonHelper;
import payfun.lib.net.rx.BaseObserver;
import payfun.lib.net.rx.RxClient;
import payfun.lib.net.rx.RxScheduler;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author : zhangqinggong
 * date    : 2023/2/24 17:24
 * desc    : 音色获取网络操作类
 */
public class SpeechNet {
    public static void init(){
        NetManager.getInstance().initApi(SpeechServer.class, () -> new RxClient.Builder()
                .baseUrl(NetConstants.BASE_URL_TEST)
                .connectTimeout(10)
                .readTimeout(15)
                .writeTimeout(15)
                .addInterceptor(new HeaderInterceptor())
                .addConvertFactory(GsonConverterFactory.create())
                .addAdapterFactory(RxJava2CallAdapterFactory.create())
                .isUseLog(true)
        );

        NetManager.getInstance().initApi(UserServer.class, () -> new RxClient.Builder()
                .baseUrl(NetConstants.BASE_URL_USER)
                .connectTimeout(10)
                .readTimeout(15)
                .writeTimeout(15)
                .addConvertFactory(GsonConverterFactory.create())
                .addAdapterFactory(RxJava2CallAdapterFactory.create())
                .isUseLog(true)
        );
    }

    public static class HeaderInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            String token = PrefersTool.getAccesstoken();
            Request originalRequest = chain.request();
            if (TextUtils.isEmpty(token)) {
                return chain.proceed(originalRequest);
            } else {
                Request updateRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(updateRequest);
            }
        }
    }


    /**
     * 登录
     * @param observer
     */
    public static void login(String userName,String pwd,BaseObserver<BaseResponse<LoginBean>> observer){
        Map<String, Object> para = new HashMap<>();
        para.put("password", Base64Utils.base64EncodeToString(pwd));
        para.put("userName", userName);
        String s = GsonHelper.GSON.toJson(para);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),s);
        NetManager.getInstance().getApi(SpeechServer.class)
                .login(body)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }



    /**
     * 调用应用层接口绑定
     * @param observer
     */
    public static void register(BaseObserver<ResponseBody> observer){
        Map<String, Object> para = new HashMap<>();
        para.put("url", "http://"+NetConstants.HTTP_SERVER_IP+":"+NetConstants.HTTP_SERVER_PORT+ ServerConfig.HTTP_SET_TOKEN);
        para.put("param", "{\"is_required\":\"1\"}");
        String s = GsonHelper.GSON.toJson(para);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),s);
        NetManager.getInstance().getApi(UserServer.class)
                .register(body)
                .compose(RxScheduler.obsIo2Main())
                .subscribe(observer);
    }

}
