package com.example.smartbike.model;

import com.google.gson.annotations.SerializedName;

public class GetModel {
    @SerializedName("status")
    private String status;
    @SerializedName("rpm")
    private int rpm;

    public GetModel(String status, int rpm) {
        this.status = status;
        this.rpm = rpm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRpm() {
        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }
}
