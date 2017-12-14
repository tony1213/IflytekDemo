package com.abilix.robot.hrobot.voice.iflytek;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.abilix.robot.hrobot.voice.common.DataConfig;
import com.abilix.robot.hrobot.voice.event.SpeechStatus;
import com.abilix.robot.hrobot.voice.iflytek.util.IflyUtils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import org.greenrobot.eventbus.EventBus;

public class TTSService extends Service {
    private final TTSBinder binder;
    // 语音合成对象
    private SpeechSynthesizer mTts;

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    public TTSService() {
        super();
        this.binder = new TTSBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void speak(String content, boolean isTypeCloud) {
        String speakMen = "";
        if (isTypeCloud) {
            if (DataConfig.isLanguageSwitch) {
                speakMen = DataConfig.VOICER_TIPS_DEFAULT_ENGLISH;
            } else {
                speakMen = DataConfig.VOICER_TIPS_DEFAULT;
            }
        } else {
            speakMen = DataConfig.VOICER_TIPS_LOCAL;
        }
        IflyUtils.setTextToVoiceParam(mTts, isTypeCloud, speakMen, "60", "50", "50");
        int code = mTts.startSpeaking(content, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                // 未安装则跳转到提示安装页面
            } else {
                Log.e("Speak", "语音合成失败,错误码: " + code);
            }
        }
    }

    // 初始化监听
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Log.e("Speak", "初始化失败,错误码:" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    // 合成回调监听
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        // 开始播放
        @Override
        public void onSpeakBegin() {
            Log.i("Speak", "语音合成 开始播放");
        }

        // 暂停播放
        @Override
        public void onSpeakPaused() {
            Log.i("Speak", "语音合成 暂停播放");
        }

        // 继续播放
        @Override
        public void onSpeakResumed() {
            Log.i("Speak", "语音合成 继续播放");
        }

        // 合成进度
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            Log.i("Speak", "语音合成 合成进度");
        }

        // 播放进度
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            Log.i("Speak", "语音合成 播放进度");
        }

        @Override
        public void onCompleted(SpeechError error) {
            //说话结束
            if (error == null) {
                Log.i("Speak", "播放完成 ");
                EventBus.getDefault().post(new SpeechStatus("Completed"));
            } else if (error != null) {
                Log.e("Speak", "onCompleted  error=" + error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Log.d("Speak", "session id =" + sid);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTts.isSpeaking()) {
            mTts.stopSpeaking();
        }
        // 退出时释放连接
        mTts.destroy();
    }

}
