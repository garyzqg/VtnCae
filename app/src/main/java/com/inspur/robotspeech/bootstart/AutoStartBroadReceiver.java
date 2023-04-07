package com.inspur.robotspeech.bootstart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.inspur.robotspeech.MainActivity;

/**
 * @author : zhangqinggong
 * date    : 2023/4/6 19:00
 * desc    : 开机广播监听
 */
public class AutoStartBroadReceiver extends BroadcastReceiver {
    private static final String TAG = "AutoStartBroadReceiver";
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "接收广播 onReceive: " + intent.getAction());
        //开机启动
        if (ACTION.equals(intent.getAction())) {

//            第一种方式：根据包名
//            PackageManager packageManager = context.getPackageManager();
//            Intent mainIntent = packageManager.getLaunchIntentForPackage("com.harry.martin");
//            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(mainIntent);
//            context.startService(mainIntent);


//           第二种方式：指定class类，跳转到相应的Acitivity
            Intent mainIntent = new Intent(context, MainActivity.class);
            /**
             * Intent.FLAG_ACTIVITY_NEW_TASK
             * Intent.FLAG_ACTIVITY_CLEAR_TOP
             */
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainIntent);


//            context.startService(mainIntent);
        }
    }

}



