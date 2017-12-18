package com.abilix.robot.hrobot.voice.iflytek;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.abilix.robot.hrobot.voice.base.BaseService;
import com.abilix.robot.hrobot.voice.common.DataConfig;
import com.abilix.robot.hrobot.voice.event.CloudResult;
import com.abilix.robot.hrobot.voice.event.RecognizeResult;
import com.abilix.robot.hrobot.voice.turing.entity.TuringResultInfo;
import com.abilix.robot.hrobot.voice.turing.entity.VoiceResultInfo;
import com.abilix.robot.hrobot.voice.turing.service.TuringService;
import com.abilix.robot.hrobot.voice.util.GsonParse;
import com.abilix.robot.hrobot.voice.iflytek.util.IflyUtils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.LinkedHashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VTTService extends BaseService {

    private final VTTBinder binder;
    private SpeechRecognizer mIat; // 语音听写对象
    private HashMap<String, String> mIatResults;// 用HashMap存储听写结果

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public VTTService() {
        super();
        this.binder = new VTTBinder(this);
        this.mIatResults = new LinkedHashMap<String, String>();
    }

    @Override
    public void onCreate() {
        Log.e("voice", "onCreate");
        super.onCreate();
        // 初始化SpeechRecognizer对象
        mIat = SpeechRecognizer.createRecognizer(this, mTtsInitListener);
    }

    //开启听
    public void listenBegin() {
        listen(true, DataConfig.VOICER_TIPS_DEFAULT);
    }
    //停止听
    public void stopListen() {
        if (mIat.isListening()) {
            mIat.cancel();
        }
    }

    private void listen(boolean isTypeCloud, String language) {
        mIatResults.clear();
        // 设置参数
        IflyUtils.setVoiceToTextParam(mIat, isTypeCloud, language);
        // 不显示听写对话框
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            Log.e("voice", "听写失败,错误码：" + ret);
        } else {
            Log.i("voice", "开始听写");
        }
    }

    //听写监听器
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.i("voice", "开始说话 ");
        }

        @Override
        public void onError(SpeechError error) {
            Log.e("voice", "onError ");
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            if (mIat.isListening()) {
                mIat.cancel();
            }
            listenBegin();
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.i("voice", "结束说话 ");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String result = GsonParse.printResult(results, mIatResults);
            Log.i("voice", "识别结果：" + result);
            if (isLast) {
                if (mIat.isListening()) {
                    mIat.cancel();
                }
                EventBus.getDefault().post(new RecognizeResult(result));
                /*TuringService apiService = retrofit.create(TuringService.class);
                Call<TuringResultInfo> call = apiService.search("0cdd9edd8c34a2825efb676e5c1f7192", result, "64592827");
                call.enqueue(new Callback<TuringResultInfo>() {
                    @Override
                    public void onResponse(Call<TuringResultInfo> call, Response<TuringResultInfo> response) {
                        Log.e("APP", "code:" + response.body().getCode() + ",text:" + response.body().getText() + ",url:" + response.body().getUrl());
                        if (response.body().getText() != null) {
                            EventBus.getDefault().post(new CloudResult(response.body().getText()));
                        } else {
                            EventBus.getDefault().post(new CloudResult("Error Result"));
                        }
                    }

                    @Override
                    public void onFailure(Call<TuringResultInfo> call, Throwable t) {
                        t.printStackTrace();
                    }
                });*/
                TuringService apiService = retrofit.create(TuringService.class);
                Call<VoiceResultInfo> call = apiService.send(result,"64592827");
                call.enqueue(new Callback<VoiceResultInfo>() {
                    @Override
                    public void onResponse(Call<VoiceResultInfo> call, Response<VoiceResultInfo> response) {
                        Log.e("App","Data:"+response.body().getText());
                    }

                    @Override
                    public void onFailure(Call<VoiceResultInfo> call, Throwable t) {

                    }
                });
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.i("voice", "当前正在说话，音量大小： volume==" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因,若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Log.i("voice", "session id =" + sid);
            }
        }
    };

    //初始化监听
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                //初始化失败,错误码
                Log.e("voice", "初始化失败:"+code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIat.isListening()) {
            mIat.cancel();
        }
        mIat.destroy();
    }
}
