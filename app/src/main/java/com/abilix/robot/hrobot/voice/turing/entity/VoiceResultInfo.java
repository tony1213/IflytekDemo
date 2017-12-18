package com.abilix.robot.hrobot.voice.turing.entity;

public class VoiceResultInfo {
    private String text;    //提示语

    public VoiceResultInfo(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
