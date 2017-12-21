package com.abilix.robot.hrobot.voice.apollo;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.abilix.robot.hrobot.voice.base.BaseService;
import com.abilix.robot.hrobot.voice.event.UserQuestionResult;
import com.abilix.robot.hrobot.voice.event.TuringCloudAnswer;
import com.abilix.robot.hrobot.voice.turing.entity.TuringResultInfo;
import com.abilix.robot.hrobot.voice.turing.entity.VoiceResultInfo;
import com.abilix.robot.hrobot.voice.turing.service.AbilixCloudService;
import com.abilix.robot.hrobot.voice.turing.service.TuringService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApolloService extends BaseService {

    private final ApolloBinder binder;

    private String host = "tcp://10.107.2.137:61613";
    private String userName = "admin";
    private String passWord = "password";
    private static final String clientid = "H_Robot_002";
    public static final String TOPIC = "toclient/125";
    private int i = 1;

    private Handler handler;

    private MqttClient client;

    private MqttConnectOptions options;

    private ScheduledExecutorService scheduler;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public ApolloService() {
        super();
        this.binder = new ApolloBinder(this);
    }

    @Override
    public void onCreate() {
        Log.e("Apollo", "onCreate()");
        super.onCreate();
        init();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 2) {
                    Toast.makeText(ApolloService.this, "连接成功", Toast.LENGTH_SHORT).show();
                    try {
                        client.subscribe(TOPIC, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (msg.what == 3) {
                    Toast.makeText(ApolloService.this, "连接失败，系统正在重连", Toast.LENGTH_SHORT).show();
                }
            }
        };
        startReconnect();
    }

    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (!client.isConnected()) {
                    connect();
                }
            }
        }, 0, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    private void init() {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, clientid, new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            //设置回调
            client.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    Log.e("Apollo", "connectionLost");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    Log.e("Apollo", "deliveryComplete" + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message) throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    Log.e("Apollo", "messageArrived");
                    EventBus.getDefault().post(new UserQuestionResult(message.toString()));

                    //Abilix的知识库
                    AbilixCloudService apiService = retrofit.create(AbilixCloudService.class);
                    Call<ResponseBody> call = apiService.query(message.toString());
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            try {
                                String result = new String(response.body().bytes());
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    String answer = jsonObject.getString("ans");
                                    EventBus.getDefault().post(new TuringCloudAnswer(answer));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });


                    //图灵的知识库
                    /*TuringService apiService = retrofit.create(TuringService.class);
                    Call<TuringResultInfo> call = apiService.search("0cdd9edd8c34a2825efb676e5c1f7192", message.toString(), "002");
                    call.enqueue(new Callback<TuringResultInfo>() {
                        @Override
                        public void onResponse(Call<TuringResultInfo> call, Response<TuringResultInfo> response) {
                            if (response.body()!=null){
                                Log.e("Apollo", "Apollo:" + response.body().getText());
                                EventBus.getDefault().post(new TuringCloudAnswer(response.body().getText()));
                            }
                        }

                        @Override
                        public void onFailure(Call<TuringResultInfo> call, Throwable t) {

                        }
                    });*/

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    client.connect(options);
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            scheduler.shutdown();
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
