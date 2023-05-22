package com.inspur.robotspeech.bean;

/**
 * @author : zhangqinggong
 * date    : 2023/5/22 14:27
 * desc    : EntityBean
 */
public class EntityBean {
//    "entities" : [
//    {
//        "entityEndIndex" : "0",
//            "entityLabel" : "place",
//            "entityNormValue" : "办公室",
//            "entityStartIndex" : "0",
//            "entityValue" : "办公室"
//    }
//   ]

    private String entityEndIndex;
    private String entityLabel;
    private String entityNormValue;
    private String entityStartIndex;
    private String entityValue;

    public String getEntityEndIndex() {
        return entityEndIndex;
    }

    public void setEntityEndIndex(String entityEndIndex) {
        this.entityEndIndex = entityEndIndex;
    }

    public String getEntityLabel() {
        return entityLabel;
    }

    public void setEntityLabel(String entityLabel) {
        this.entityLabel = entityLabel;
    }

    public String getEntityNormValue() {
        return entityNormValue;
    }

    public void setEntityNormValue(String entityNormValue) {
        this.entityNormValue = entityNormValue;
    }

    public String getEntityStartIndex() {
        return entityStartIndex;
    }

    public void setEntityStartIndex(String entityStartIndex) {
        this.entityStartIndex = entityStartIndex;
    }

    public String getEntityValue() {
        return entityValue;
    }

    public void setEntityValue(String entityValue) {
        this.entityValue = entityValue;
    }

    @Override
    public String toString() {
        return "EntityBean{" +
                "entityEndIndex='" + entityEndIndex + '\'' +
                ", entityLabel='" + entityLabel + '\'' +
                ", entityNormValue='" + entityNormValue + '\'' +
                ", entityStartIndex='" + entityStartIndex + '\'' +
                ", entityValue='" + entityValue + '\'' +
                '}';
    }
}
