package com.iflytek.vtncaetest.util;


import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Android运行linux命令
 */
public final class RootShell {
    private static final String TAG = "RootShell";

    /**
     * 执行命令但不关注结果输出
     */
    public static int execRootCmdSilent(String cmd) {
        LogUtil.iTag(TAG, "run " + cmd);
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            LogUtil.iTag(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
            LogUtil.iTag(TAG, "run " + cmd + " result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
