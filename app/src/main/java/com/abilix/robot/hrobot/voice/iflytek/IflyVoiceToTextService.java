package com.abilix.robot.hrobot.voice.iflytek;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.abilix.robot.hrobot.voice.common.BroadcastAction;
import com.abilix.robot.hrobot.voice.common.DataConfig;
import com.abilix.robot.hrobot.voice.event.CloudResult;
import com.abilix.robot.hrobot.voice.event.RecognizeResult;
import com.abilix.robot.hrobot.voice.turing.entity.TuringResultInfo;
import com.abilix.robot.hrobot.voice.turing.service.TuringService;
import com.abilix.robot.hrobot.voice.util.GsonParse;
import com.abilix.robot.hrobot.voice.iflytek.util.IflyUtils;
import com.abilix.robot.hrobot.voice.util.Utilities;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.robot.et.common.lib.http.cookie.CookieManger;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.LinkedHashMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IflyVoiceToTextService extends Service {
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private int ret = 0; // 函数调用返回值

    private Context context;
    private OkHttpClient okHttpClient;
    private Retrofit retrofit;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.e("voice", "onCreate");
        super.onCreate();
        init();
        // 初始化SpeechRecognizer对象
        mIat = SpeechRecognizer.createRecognizer(this, mTtsInitListener);
//        uploadUserThesaurus();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.ACTION_RESUME_MONITOR_CHAT);
        filter.addAction(BroadcastAction.ACTION_STOP_LISTENER);
        registerReceiver(receiver, filter);

    }

    private void init() {
        context = this;
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .cookieJar(new CookieManger(context))
                .build();
        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("http://www.tuling123.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_RESUME_MONITOR_CHAT)) {// 开始对话监听
                Log.e("voice", "开始监听");
                listenBegin();
            } else if ((intent.getAction().equals(BroadcastAction.ACTION_STOP_LISTENER))) {// 停止对话监听
                Log.e("voice", "停止监听");
                stopListen();
            }
        }
    };

    //开启听
    private void listenBegin() {
        listen(true, DataConfig.VOICER_TIPS_DEFAULT);
    }

    private void stopListen() {
        if (mIat.isListening()) {
            mIat.cancel();
        }
    }

    private void listen(boolean isTypeCloud, String language) {
        mIatResults.clear();
        // 设置参数
        IflyUtils.setVoiceToTextParam(mIat, isTypeCloud, language);
        // 不显示听写对话框
        ret = mIat.startListening(mRecognizerListener);

        if (ret != ErrorCode.SUCCESS) {
            Log.e("voice", "听写失败,错误码：" + ret);
        } else {
            Log.e("voice", "开始听写");
        }
    }

    //听写监听器
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.e("voice", "开始说话 ");
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
            Log.e("voice", "结束说话 ");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.e("voice", "onResult ");
            // 有人说话
            String result = GsonParse.printResult(results, mIatResults);
            Log.e("voice", "识别结果：" + result);
//            Toast.makeText(IflyVoiceToTextService.this, "识别结果：" + result, Toast.LENGTH_SHORT).show();
            if (isLast) {
                Log.e("voicet", "onResult  isLast:"+result);
                if (mIat.isListening()) {
                    mIat.cancel();
                }

                /*if (!TextUtils.isEmpty(result)) {
                    //如果只有一个字的话，直接继续听
                    char[] datas = result.toCharArray();
                    if (datas.length == 1) {
                        if (!DataConfig.isBluetoothBox) {
                            listenBegin();
                        }
                        return;
                    }
                    // 问科大讯飞
                    BroadcastShare.askIfly(result);
                } else {
                    if (DataConfig.isBluetoothBox) {
                        DataConfig.isBluetoothBox = false;
                        return;
                    }
                    listenBegin();
                }*/
                EventBus.getDefault().post(new RecognizeResult(result));

                TuringService apiService = retrofit.create(TuringService.class);
                Call<TuringResultInfo> call = apiService.search("0cdd9edd8c34a2825efb676e5c1f7192", result,"64592827");
                call.enqueue(new Callback<TuringResultInfo>() {
                    @Override
                    public void onResponse(Call<TuringResultInfo> call, Response<TuringResultInfo> response) {
                        Log.e("APP", "code:" + response.body().getCode() + ",text:" + response.body().getText() + ",url:" + response.body().getUrl());
                        if (response.body().getText()!=null){
                            EventBus.getDefault().post(new CloudResult(response.body().getText()));
                        }else {
                            EventBus.getDefault().post(new CloudResult("Error Result"));
                        }
                    }

                    @Override
                    public void onFailure(Call<TuringResultInfo> call, Throwable t) {
//                        result.setText("返回数据失败");
                        t.printStackTrace();
                    }
                });


            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.e("voice", "当前正在说话，音量大小： volume==" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            Log.e("voice", "会话id ");
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            // if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            // String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            // Log.d(TAG, "session id =" + sid);
            // }
        }
    };

    //初始化监听
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                //初始化失败,错误码
                Log.e("voiceresult", "初始化失败");
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    //上传词表监听器
    private LexiconListener mLexiconListener = new LexiconListener() {

        @Override
        public void onLexiconUpdated(String lexiconId, SpeechError error) {
            if (error != null) {
                Log.e("voiceresult", "上传联系人词表error===" + error.toString());
//                uploadUserThesaurus();
            } else {
                Log.e("voiceresult", "上传联系人词表成功");
            }
        }
    };

    //上传词表
    private void uploadUserThesaurus() {
        String contents = Utilities.readFile("userwords", "utf-8");
        // 指定引擎类型
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 置编码类型
        mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        ret = mIat.updateLexicon("userword", contents, mLexiconListener);
        if (ret != ErrorCode.SUCCESS) {
            Log.e("voiceresult", "上传热词失败,错误码==" + ret);
//            uploadUserThesaurus();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIat.isListening()) {
            mIat.cancel();
        }
        mIat.destroy();
        unregisterReceiver(receiver);
    }
}
