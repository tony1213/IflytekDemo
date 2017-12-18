package com.abilix.robot.hrobot.voice;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.abilix.robot.hrobot.voice.event.CloudResult;
import com.abilix.robot.hrobot.voice.event.RecognizeResult;
import com.abilix.robot.hrobot.voice.event.SpeechStatus;
import com.abilix.robot.hrobot.voice.iflytek.TTSBinder;
import com.abilix.robot.hrobot.voice.iflytek.TTSService;
import com.abilix.robot.hrobot.voice.iflytek.VTTBinder;
import com.abilix.robot.hrobot.voice.iflytek.VTTService;
import com.abilix.robot.hrobot.voice.apollo.ApolloBinder;
import com.abilix.robot.hrobot.voice.apollo.ApolloService;

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
        setContentView(R.layout.activity_main);
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

    @OnClick({R.id.start, R.id.stop})
    public void execClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                vttBinder.listenStart();
                break;
            case R.id.stop:
                vttBinder.listenStop();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCloudResultReceivedEvent(CloudResult event) {
        Log.e("voice", "event:" + event.getData());
        mCloudResult.setText(event.getData());
        ttsBinder.speak(event.getData(), true);
        /*NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.mipmap.ic_launcher, "Mqtt即时推送", System.currentTimeMillis());
        notification.contentView = new RemoteViews("com.abilix.robot.hrobot.voice", R.layout.activity_notification);
        notification.contentView.setTextViewText(R.id.tv_desc, event.getData());
        notification.defaults = Notification.DEFAULT_SOUND;
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        manager.notify(i++, notification);*/

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecognizeResultReceivedEvent(RecognizeResult event) {
        Log.e("voice", "event2:" + event.getData());
        mRecognizeResult.setText(event.getData());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSpeechCompletedEvent(SpeechStatus event) {
        Log.e("voice", "event3:" + event.getStatus());
        if (TextUtils.equals("Completed", event.getStatus())) {
            vttBinder.listenStart();
        }
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
