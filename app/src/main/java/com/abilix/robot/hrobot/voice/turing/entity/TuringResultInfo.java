package com.abilix.robot.hrobot.voice.turing.entity;

/**
 * @author tony
 */

public class TuringResultInfo {
/*    private String key;     //Turing APIkey
    private String info;    //文本内容
    private String userid;  //用户唯一标志*/

    private String code;    //链接类标识码
    private String text;    //提示语
    private String url;     //链接地址

/*    public TuringResultInfo(String key, String info, String userid) {
        this.key = key;
        this.info = info;
        this.userid = userid;
    }*/

    public TuringResultInfo(String key, String info, String userid, String code, String text, String url) {
        /*this.key = key;
        this.info = info;
        this.userid = userid;*/
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

    /*public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }*/
}
