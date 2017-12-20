package com.abilix.robot.hrobot.voice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.abilix.robot.hrobot.voice.base.BaseActivity;
import com.abilix.robot.hrobot.voice.event.CustomerAnswerResult;
import com.abilix.robot.hrobot.voice.event.UserQuestionResult;
import com.abilix.robot.hrobot.voice.event.TuringCloudAnswer;
import com.abilix.robot.hrobot.voice.iflytek.TTSBinder;
import com.abilix.robot.hrobot.voice.iflytek.TTSService;
import com.abilix.robot.hrobot.voice.iflytek.VTTBinder;
import com.abilix.robot.hrobot.voice.iflytek.VTTService;
import com.abilix.robot.hrobot.voice.apollo.ApolloBinder;
import com.abilix.robot.hrobot.voice.apollo.ApolloService;
import com.abilix.robot.hrobot.voice.turing.entity.VoiceResultInfo;
import com.abilix.robot.hrobot.voice.turing.service.TuringService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    @BindView(R.id.et_customer_question)
    public EditText mRecognizeResult;
    @BindView(R.id.et_cloud_answer)
    public EditText mTuringResult;
    @BindView(R.id.et_customer_answer)
    public EditText mCustomerServiceAnswer;

    private ServiceConnection serviceConnection_vtt;
    private ServiceConnection serviceConnection_tts;
    private ServiceConnection serviceConnection_mqtt;
    private VTTBinder vttBinder;
    private TTSBinder ttsBinder;
    private ApolloBinder apolloBinder;

    private int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_customer_service);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceConnection_vtt = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                vttBinder = (VTTBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                vttBinder = null;
            }
        };
        Intent intent = new Intent(this, VTTService.class);
        startService(intent);
        bindService(intent, serviceConnection_vtt, 0);
        serviceConnection_tts = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ttsBinder = (TTSBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                ttsBinder = null;
            }
        };
        Intent intent2 = new Intent(this, TTSService.class);
        startService(intent2);
        bindService(intent2, serviceConnection_tts, 0);
        serviceConnection_mqtt = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                apolloBinder = (ApolloBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                apolloBinder = null;
            }
        };
        Intent intent3 = new Intent(this, ApolloService.class);
        startService(intent3);
        bindService(intent3, serviceConnection_mqtt, 0);
    }

    @OnClick({R.id.btn_customer_voice, R.id.btn_select_cloud, R.id.btn_select_customer})
    public void execClick(View view) {

        switch (view.getId()) {
            case R.id.btn_customer_voice:
                vttBinder.listenStart();
                break;
            case R.id.btn_select_cloud:
                String result = mTuringResult.getText().toString().trim();
                TuringService apiService = retrofit.create(TuringService.class);
                Call<VoiceResultInfo> call = apiService.send(result, "002");
                call.enqueue(new Callback<VoiceResultInfo>() {
                    @Override
                    public void onResponse(Call<VoiceResultInfo> call, Response<VoiceResultInfo> response) {
                    }

                    @Override
                    public void onFailure(Call<VoiceResultInfo> call, Throwable t) {

                    }
                });
                break;
            case R.id.btn_select_customer:
                String result2 = mCustomerServiceAnswer.getText().toString().trim();
                TuringService apiService2 = retrofit.create(TuringService.class);
                Call<VoiceResultInfo> call2 = apiService2.send(result2, "002");
                call2.enqueue(new Callback<VoiceResultInfo>() {
                    @Override
                    public void onResponse(Call<VoiceResultInfo> call, Response<VoiceResultInfo> response) {
                    }

                    @Override
                    public void onFailure(Call<VoiceResultInfo> call, Throwable t) {

                    }
                });
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceivedQuestionEvent(UserQuestionResult event) {
        Log.e("voice", "Question:" + event.getData());
        mRecognizeResult.setText(event.getData());
        ttsBinder.speak(event.getData(), true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceivedTuringAnswerEvent(TuringCloudAnswer event) {
        Log.e("voice", "TuringAnswer:" + event.getData());
        mTuringResult.setText(event.getData());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecognizeCustomerAnswerEvent(CustomerAnswerResult event) {
        Log.e("voice", "CustomerAnswer:" + event.getData());
        mCustomerServiceAnswer.setText(event.getData());
    }


    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        unbindService(serviceConnection_vtt);
        unbindService(serviceConnection_tts);
        unbindService(serviceConnection_mqtt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
