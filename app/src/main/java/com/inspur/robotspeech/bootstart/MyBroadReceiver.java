package com.inspur.robotspeech.bootstart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.inspur.robotspeech.MainActivity;

/**
 * @author : zhangqinggong
 * date    : 2023/4/6 19:00
 * desc    : 开机广播监听
 */
public class MyBroadReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBroadReceiver";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";//开机
    private static final String ACTION_USB_ATTACHED = UsbManager.ACTION_USB_DEVICE_ATTACHED;//USB连接
    private static final String ACTION_USB_DETACHED = UsbManager.ACTION_USB_DEVICE_DETACHED;//USB断开

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "接收广播 onReceive: " + action);
        if (ACTION_BOOT.equals(action)) {//开机启动

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
//            context.startActivity(mainIntent);


//            context.startService(mainIntent);
        }else if (ACTION_USB_ATTACHED.equals(action) || ACTION_USB_DETACHED.equals(action)){//usb插拔监听
            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            int productId = usbDevice.getProductId();//pid
            int vendorId = usbDevice.getVendorId();//vid
            if (productId == 42150 && vendorId == 7531){//环形麦usb插拔提示
                Intent i = new Intent();
                i.setAction("usbChange");
                i.putExtra("usbStatus",ACTION_USB_ATTACHED.equals(action)?0:1);
                context.sendBroadcast(i);
            }
        }
    }

}



