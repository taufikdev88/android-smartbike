package com.example.smartbike.services;

import com.example.smartbike.model.GetModel;
import com.example.smartbike.model.MainModel;
import com.example.smartbike.model.SettingsModel;


import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetServices {
    @GET("/main")
    Call<MainModel> main(
            @Query("motorOn") int motorOn,
            @Query("normal") int normal
    );
    @GET("/get")
    Observable<GetModel> get();
    @GET("/set")
    Call<SettingsModel> setRead();
    @GET("/set")
    Call<SettingsModel> setWrite(
            @Query("normalRpm") int normalRpm,
            @Query("speedRpm") int speedRpm
    );
}
