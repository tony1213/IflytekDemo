package com.abilix.robot.hrobot.voice.turing.service;


import com.abilix.robot.hrobot.voice.turing.entity.VoiceResultInfo;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AbilixCloudService {
    @GET("voice.ashx?req={info}")
    Call<VoiceResultInfo> send(@Path("info") String info);

    @GET("group/{id}/users")
    Call<List<VoiceResultInfo>> groupList(@Path("id") int groupId);

    @GET("voice.ashx?req=")
    Call<ResponseBody> query(@Query("req") String req);
}
