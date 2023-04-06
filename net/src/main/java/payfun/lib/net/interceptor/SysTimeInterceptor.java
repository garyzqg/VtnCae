package payfun.lib.net.interceptor;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import androidx.annotation.RequiresPermission;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import payfun.lib.basis.time.SysTimeUtil;

/**
 * @author : zhangqg
 * date   : 2021/5/28 18:01
 * desc   : <p>同步服务器时间到本地拦截器
 */
public class SysTimeInterceptor implements Interceptor {

    @RequiresPermission(android.Manifest.permission.SET_TIME)
    public SysTimeInterceptor() {
    }

    @NotNull
    @Override
    @RequiresPermission(android.Manifest.permission.SET_TIME)
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Response response = chain.proceed(originalRequest);
        try {
            SysTimeUtil.setSysTime(SysTimeUtil.getRemoteTime(response.header("date")), 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
