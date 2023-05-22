package com.inspur.robotspeech.bean;

import java.util.List;

/**
 * @author : zhangqinggong
 * date    : 2023/1/13 15:27
 * desc    : NlpBean
 */
public class NlpBean {
    //{"data":{"question":"你好","answer":"你也好","entities":[],"finish":true,"intent":"qa_general_intent"},"type":"nlp"}
    private String question;
    private String answer;
    private List<EntityBean> entities;
    private boolean finish;
    private String intent;
    public List<EntityBean> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityBean> entities) {
        this.entities = entities;
    }


    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }


    @Override
    public String toString() {
        return "NlpBean{" +
                "question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", entities=" + entities +
                ", finish=" + finish +
                ", intent='" + intent + '\'' +
                '}';
    }
}
