package com.iflytek.vtncaetest.util;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import payfun.lib.basis.utils.LogUtil;

/**
 * Android运行linux命令
 */
public final class USBCardFinder {
    private static final String TAG = "USBCardFiner";
    private static boolean mHaveRoot = false;
    private static int cardNum = 0;

    public interface SoundCardNameCheck {
        boolean checkPrimaryName(String name);
        boolean checkSecondaryName(String name);
    }

    /**
     * 判断机器Android是否已经root，即是否获取root权限
     */
    public static boolean haveRoot() {
        if (!mHaveRoot) {
            int ret = execRootCmdSilent("echo test"); // 通过执行测试命令来检测
            if (ret != -1) {
                LogUtil.iTag(TAG, "have root!");
                mHaveRoot = true;
            } else {
                LogUtil.iTag(TAG, "not root!");
            }
        } else {
            LogUtil.iTag(TAG, "mHaveRoot = true, have root!");
        }
        return mHaveRoot;
    }

    public static int fetchCards(int card, SoundCardNameCheck check) {
        cardNum = card;
        if (cardNum == -1) {
            cardNum = execRootCmd("sh", "cat /proc/asound/cards", check);
        }
        if (cardNum == -1) {
            // 865
            cardNum = execRootCmd("ubiot", "cat /proc/asound/cards", check);
        }
//        execRootCmdSilent("setenforce 0");
//        execRootCmdSilent("chmod 777 /dev/snd/pcmC" + cardNum + "D0c");
        return cardNum;
    }

    public static void execShellCmd(String cmd) {
        //865需要使用ubiot
        String auth = "ubiot";
        try {
            Process process = Runtime.getRuntime().exec(auth);
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, "RootShell-execShellCmd-Error:" + e.getMessage());
        }
    }

    public static int getCardNum(String line) {
        int cardN = -1;

//        Log.d(TAG, "Find USB card:" + line);
        line = line.replace('[', ',');
        line = line.replace(']', ',');
//        Log.d(TAG, "Find USB card parse:" + line);
        String[] strs = line.split(",");
        if (strs.length > 0) {
            String numStr = strs[0].trim();
            cardN = Integer.parseInt(numStr);
        }
        LogUtil.iTag(TAG, "USB card Number=" + cardN);

        return cardN;
    }

    /**
     * 执行命令并且输出结果
     */
    public static int execRootCmd(String suCmd, String cmd, SoundCardNameCheck check) {
        int cardN = -1;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        try {
            Process p = Runtime.getRuntime().exec(suCmd);// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            Log.i(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            //while ((line = dis.readUTF()) != null) {
//            (line.contains(" Bothlent UAC Dongle") || line.contains("HSWL Microphone"))

            List<String> resultLines = new ArrayList<>();

            while ((line = dis.readLine()) != null) {
                resultLines.add(line);

                if (check.checkPrimaryName(line)) {
                    cardN = getCardNum(line);
                    break;
                }
            }

            if (cardN == -1) {
                for (String curLine : resultLines) {
                    if (check.checkSecondaryName(curLine)) {
                        cardN = getCardNum(curLine);
                        break;
                    }
                }
            }

            p.waitFor();
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
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cardN;
    }

    /**
     * 执行命令但不关注结果输出
     */
    public static int execRootCmdSilent(String cmd) {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());

            Log.i(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
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
