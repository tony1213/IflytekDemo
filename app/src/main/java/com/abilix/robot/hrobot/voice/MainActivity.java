package com.abilix.robot.hrobot.voice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.abilix.robot.hrobot.voice.common.BroadcastAction;
import com.abilix.robot.hrobot.voice.event.CloudResult;
import com.abilix.robot.hrobot.voice.event.RecognizeResult;
import com.abilix.robot.hrobot.voice.iflytek.IflySpeakService;
import com.abilix.robot.hrobot.voice.iflytek.IflyVoiceToTextService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @BindView(R.id.recognize_result)
    public TextView mRecognizeResult;
    @BindView(R.id.cloud_result)
    public TextView mCloudResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        //科大讯飞语音合成（嘴巴<在线或者离线语音合成>）
        startService(new Intent(this, IflySpeakService.class));
        //语音听写（耳朵<声音转化成文字>）
        startService(new Intent(this, IflyVoiceToTextService.class));
    }

    @OnClick({R.id.start, R.id.stop})
    public void execClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.start:
                intent.setAction(BroadcastAction.ACTION_RESUME_MONITOR_CHAT);
                sendBroadcast(intent);
                break;
            case R.id.stop:
                intent.setAction(BroadcastAction.ACTION_STOP_LISTENER);
                sendBroadcast(intent);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCloudResultReceivedEvent(CloudResult event) {
        Log.e("voice", "event:" + event.getData());
        mCloudResult.setText(event.getData());
        Intent intent = new Intent();
        intent.setAction(BroadcastAction.ACTION_VOICE_TO_TEXT_SPEAK);
        intent.putExtra("result",event.getData());
        sendBroadcast(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecognizeResultReceivedEvent(RecognizeResult event) {
        Log.e("voice", "event2:" + event.getData());
        mRecognizeResult.setText(event.getData());
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, IflySpeakService.class));
        stopService(new Intent(this, IflyVoiceToTextService.class));
    }
}
