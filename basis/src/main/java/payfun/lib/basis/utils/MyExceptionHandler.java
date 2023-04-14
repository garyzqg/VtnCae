package payfun.lib.basis.utils;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
/**
 * Created by HY on 2020-02-24
 * Description:
 */
public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static String TAG = new Throwable().getStackTrace()[0].getClassName();
    private Application application;

    public MyExceptionHandler(Application application) {
        this.application = application;
        initErrorFile();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        LogUtil.eTag(TAG, "======应用程序异常======");
        LogUtil.eTag(TAG, "异常信息：\n" + Log.getStackTraceString(ex));
    }

    private void initErrorFile(){
    }


    /**
     * 判断是否有SD卡（内置&外置）
     *
     * @author ZhengWx
     * @date 2015年2月7日 上午10:58:27
     * @return
     * @since 1.0
     */
    public boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equalsIgnoreCase(status)) {
            return false;
        }

        return true;
    }
}
