package com.abilix.robot.hrobot.voice.base;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.robot.et.common.lib.http.cookie.CookieManger;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BaseService extends Service {

    private Context context;
    private OkHttpClient okHttpClient;
    public Retrofit retrofit;

    private static final String BASE_URL = "http://www.tuling123.com/";
    private static final String BASE_URL2 = "http://10.107.2.137:8080/ApolloWebService/";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .cookieJar(new CookieManger(context))
                .build();
        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL2)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
