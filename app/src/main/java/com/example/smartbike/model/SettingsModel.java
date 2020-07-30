package com.example.smartbike.model;

import com.google.gson.annotations.SerializedName;

public class SettingsModel {
    @SerializedName("speedRpm")
    private int TurboRPM;
    @SerializedName("normalRpm")
    private int NormalRPM;

    public SettingsModel(int turboRPM, int normalRPM){
        TurboRPM = turboRPM;
        NormalRPM = normalRPM;
    }

    public int getTurboRPM() {
        return TurboRPM;
    }

    public void setTurboRPM(int turboRPM) {
        TurboRPM = turboRPM;
    }

    public int getNormalRPM() {
        return NormalRPM;
    }

    public void setNormalRPM(int normalRPM) {
        NormalRPM = normalRPM;
    }
}
