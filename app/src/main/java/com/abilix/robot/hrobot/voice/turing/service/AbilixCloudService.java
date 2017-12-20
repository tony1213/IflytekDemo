package com.abilix.robot.hrobot.voice.turing.service;


import com.abilix.robot.hrobot.voice.turing.entity.VoiceResultInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

//http://chart.abilixstore.com/voice.ashx?req=
public interface AbilixCloudService {
    @GET("voice.ashx?req={info}")
    Call<VoiceResultInfo> send(@Path("info") String info);

    @GET("group/{id}/users")
    Call<List<VoiceResultInfo>> groupList(@Path("id") int groupId);
}
