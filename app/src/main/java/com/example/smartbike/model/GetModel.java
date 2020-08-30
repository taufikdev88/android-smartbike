package com.example.smartbike.model;

import com.google.gson.annotations.SerializedName;

public class GetModel {
    @SerializedName("status")
    private String status;
    @SerializedName("state")
    private String state;

    public GetModel(String status, String state) {
        this.status = status;
        this.state = state;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
