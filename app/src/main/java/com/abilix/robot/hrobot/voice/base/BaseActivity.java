package com.abilix.robot.hrobot.voice.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.robot.et.common.lib.http.cookie.CookieManger;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class BaseActivity extends Activity {

    private Context context;
    private OkHttpClient okHttpClient;
    public Retrofit retrofit;

    private static final String BASE_URL_SERVER = "http://10.107.2.137:8080/ApolloWebService/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .cookieJar(new CookieManger(context))
                .build();
        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
