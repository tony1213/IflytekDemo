package com.abilix.robot.hrobot.voice.base;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.robot.et.common.lib.http.cookie.CookieManger;
import com.robot.et.common.lib.interceptor.MulityBaseUrlInterceptor;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BaseService extends Service {

    private Context context;
    private OkHttpClient okHttpClient;
    public Retrofit retrofit;

//    private static final String BASE_URL_TURING = "http://www.tuling123.com/";
    private static final String BASE_URL_TURING = "http://chart.abilixstore.com/";

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
                .baseUrl(BASE_URL_TURING)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
