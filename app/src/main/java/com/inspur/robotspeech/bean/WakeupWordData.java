package com.inspur.robotspeech.bean;

import java.util.List;

/**
 * @author : zhangqinggong
 * date    : 2023/3/21 15:44
 * desc    : NanoHTTPD服务器返回参数 唤醒词列表
 */
public class WakeupWordData {
    private List<WakeupWord> wakeupWordList;
    private String currWakeupWord;


    public List<WakeupWord> getWakeupWordList() {
        return wakeupWordList;
    }

    public void setWakeupWordList(List<WakeupWord> wakeupWordList) {
        this.wakeupWordList = wakeupWordList;
    }

    public String getCurrWakeupWord() {
        return currWakeupWord;
    }

    public void setCurrWakeupWord(String currWakeupWord) {
        this.currWakeupWord = currWakeupWord;
    }

    public static class WakeupWord{
        String wakeupWord;//nihaoxiaoxin
        String name;//你好小新

        public WakeupWord(String wakeupWord, String name) {
            this.wakeupWord = wakeupWord;
            this.name = name;
        }

        public String getWakeupWord() {
            return wakeupWord;
        }

        public void setWakeupWord(String wakeupWord) {
            this.wakeupWord = wakeupWord;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
