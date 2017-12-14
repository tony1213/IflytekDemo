package com.abilix.robot.hrobot.voice.event;

public class SpeechStatus {

    private String status; //状态

    private int percent;
    private int beginPos;
    private int endPos;
    private String info;

    public SpeechStatus(String status) {
        this.status = status;
    }

    public SpeechStatus(int percent, int beginPos, int endPos) {
        this.percent = percent;
        this.beginPos = beginPos;
        this.endPos = endPos;
    }

    public SpeechStatus(int percent, int beginPos, int endPos, String info) {
        this.percent = percent;
        this.beginPos = beginPos;
        this.endPos = endPos;
        this.info = info;

    }

    public SpeechStatus(String status, int percent, int beginPos, int endPos, String info) {
        this.status = status;
        this.percent = percent;
        this.beginPos = beginPos;
        this.endPos = endPos;
        this.info = info;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getBeginPos() {
        return beginPos;
    }

    public void setBeginPos(int beginPos) {
        this.beginPos = beginPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
