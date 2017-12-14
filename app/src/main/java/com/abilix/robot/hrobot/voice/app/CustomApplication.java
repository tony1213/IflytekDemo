package com.abilix.robot.hrobot.voice.app;

import android.app.Application;

import com.abilix.robot.hrobot.voice.R;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

public class CustomApplication extends Application {
	private static CustomApplication instance;

	public static CustomApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		initVoice();
	}
	// 初始化科大讯飞
	private void initVoice() {
		StringBuffer param = new StringBuffer();
		param.append("appid=" + getString(R.string.app_id));
		param.append(",");
		// 设置使用v5+
		param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
		SpeechUtility.createUtility(this, param.toString());
	}
	
}
