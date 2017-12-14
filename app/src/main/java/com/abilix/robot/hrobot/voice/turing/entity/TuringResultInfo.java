package com.abilix.robot.hrobot.voice.turing.entity;

/**
 * @author tony
 */

public class TuringResultInfo {

    private String code;    //链接类标识码
    private String text;    //提示语
    private String url;     //链接地址

    public TuringResultInfo(String code, String text, String url) {
        this.code = code;
        this.text = text;
        this.url = url;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
