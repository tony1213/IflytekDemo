package com.abilix.robot.hrobot.voice.util;

import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.RecognizerResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class GsonParse {
    private final static String TAG = "jsonparse";

    // 科大讯飞语音听写的结果json解析
    public static String printResult(RecognizerResult results,
                                     HashMap<String, String> mIatResults) {
        String text = parseVoiceToTextResult(results.getResultString());
        String sn = "";
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String result = resultBuffer.toString();
        return result;
    }

    // 科大讯飞语音听写json解析
    public static String parseVoiceToTextResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
                // 如果需要多候选结果，解析数组其他字段
                // for(int j = 0; j < items.length(); j++)
                // {
                // JSONObject obj = items.getJSONObject(j);
                // ret.append(obj.getString("w"));
                // }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

	/*
     * 科大讯飞语义理解的json解析
	 * service + question + answer
	 */


    //百科,计算器,日期,社区问答,褒贬&问候&情绪,闲聊
    private static String getAnswerData(JSONObject jObject) {
        String json = "";
        try {
            JSONObject jsonObject = jObject.getJSONObject("answer");
            json = jsonObject.getString("text");
        } catch (JSONException e) {
            Log.i(TAG, "getAnswerData  JSONException");
        }
        return json;
    }

    //获取菜谱
    private static String getCookBookData(JSONObject jObject) {
        String json = "";
        try {
            JSONObject jsonObject = jObject.getJSONObject("data");
            JSONArray cookArray = jsonObject.getJSONArray("result");
            List<String> cooks = new ArrayList<String>();
            for (int i = 0; i < cookArray.length(); i++) {
                JSONObject object = cookArray.getJSONObject(i);
                String ingredient = object.getString("ingredient");// 主要材料
                String accessory = object.getString("accessory");// 辅助材料
                String content = "";
                if (!TextUtils.isEmpty(ingredient) && TextUtils.isEmpty(accessory)) {
                    content = "主料：" + ingredient;
                } else if (TextUtils.isEmpty(ingredient) && !TextUtils.isEmpty(accessory)) {
                    content = "辅料：" + accessory;
                } else if (!TextUtils.isEmpty(ingredient) && !TextUtils.isEmpty(accessory)) {
                    content = "主料：" + ingredient + "辅料：" + accessory;
                }
                cooks.add(content);
            }

            int size = cooks.size();
            if (cooks != null && size > 0) {
                Random random = new Random();
                int randNum = random.nextInt(size);
                json = cooks.get(randNum);
            }
        } catch (JSONException e) {
            Log.i(TAG, "getCookBookData  JSONException");
        }
        return json;
    }


    //获取天气

    //获取打电话
    private static String getPhoneData(JSONObject jObject) {
        String json = "";
        try {
            JSONObject jsonObject = jObject.getJSONObject("semantic");
            JSONObject object = jsonObject.getJSONObject("slots");
            String name = "";
            if (object.has("name")) {
                name = object.getString("name");// 拨打电话的人名
            } else if (object.has("code")) {
                name = object.getString("code");// 拨打电话的号码
            }
            json = name;
        } catch (JSONException e) {
            Log.i(TAG, "getPhoneData  JSONException");
        }
        return json;
    }
}
