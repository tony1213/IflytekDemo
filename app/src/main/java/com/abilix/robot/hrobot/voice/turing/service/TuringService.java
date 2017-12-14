package com.abilix.robot.hrobot.voice.turing.service;

import com.abilix.robot.hrobot.voice.turing.entity.TuringResultInfo;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author Tony 2016-08-10
 */

public interface TuringService {
    @POST("openapi/api")
    Call<TuringResultInfo> search(@Query("key") String type, @Query("info") String info, @Query("userid") String userid);
}
