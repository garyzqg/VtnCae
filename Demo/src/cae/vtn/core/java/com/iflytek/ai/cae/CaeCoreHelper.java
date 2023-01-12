package com.iflytek.ai.cae;

import android.content.Context;
import android.util.Log;

import com.iflytek.ai.cae.OnCaeOperatorlistener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.iflyos.cae.CAE;
import com.iflytek.iflyos.cae.ICAEListener;


public class CaeCoreHelper {
    final static String TAG = "CaeCoreHelper";
    //添加资源文件自动copy 到对应目录下

    private static String mWorkDir = "/sdcard/AI/assets/";

    private OnCaeOperatorlistener caeOperatorlistener;

    public CaeCoreHelper(OnCaeOperatorlistener listener, boolean is2Mic) {
        this.caeOperatorlistener = listener;
        EngineInit();
    }


    public static void portingFile(Context context) {
        copyAssetFolder(context, "vtn", String.format("%s/vtn", mWorkDir));
    }


    public static boolean hasModeFile() {
        return false;
    }

    public boolean EngineInit() {
        Log.d(TAG, "shengjie-EngineInit in");
        CAE.loadLib();
        String configPath = String.format("%s/%s", mWorkDir, "vtn/config/vtn.ini");
        int isInit = CAE.CAENew("614184de-f61d-4a05-93b3-85d052fc4b10", configPath, mCAEListener);
        String ver = CAE.CAEGetVersion();
        Log.d(TAG, "shengjie-EngineInit  result: " + isInit + " version:" + ver + " config path " + configPath);
        CAE.CAESetShowLog(0);
        //CAE.CAESetRealBeam(0);
        return isInit == 0;
    }

    //送入原始音频到算法中
    public void writeAudio(byte[] audio) {
        CAE.CAEAudioWrite(audio, audio.length);
    }

    //重置引擎，需要初始化引擎
    public void ResetEngine() {

    }

    public void DestoryEngine() {
        CAE.CAEDestory();
    }

    private ICAEListener mCAEListener = new ICAEListener() {
        @Override
        public void onWakeup(String result) {
            try {
                JSONObject wakeupResult = new JSONObject(result).optJSONObject("ivw");
                int beam = wakeupResult.optInt("beam");
                caeOperatorlistener.onWakeup(wakeupResult.optInt("angle"), beam);
                CAE.CAESetRealBeam(beam);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAudioCallback(byte[] audioData, int dataLen) {
            if (caeOperatorlistener != null) {
                caeOperatorlistener.onAudio(audioData, dataLen);
            }
        }

        @Override
        public void onIvwAudioCallback(byte[] bytes, int i) {

        }
    };

    public static boolean copyAssetFolder(Context context, String srcName, String dstName) {
        try {
            boolean result = true;
            String fileList[] = context.getAssets().list(srcName);
            if (fileList == null) return false;

            if (fileList.length == 0) {
                result = copyAssetFile(context, srcName, dstName);
            } else {
                File file = new File(dstName);
                result = file.mkdirs();
                for (String filename : fileList) {
                    result &= copyAssetFolder(context, srcName + File.separator + filename, dstName + File.separator + filename);
                }
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyAssetFile(Context context, String srcName, String dstName) {
        try {
            InputStream in = context.getAssets().open(srcName);
            File outFile = new File(dstName);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


}
