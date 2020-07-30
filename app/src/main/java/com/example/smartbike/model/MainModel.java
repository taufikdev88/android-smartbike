package com.example.smartbike.model;

import com.google.gson.annotations.SerializedName;

public class MainModel {
    @SerializedName("status")
    private String status;
    @SerializedName("state")
    private boolean state;

    public MainModel(String status, boolean state) {
        this.status = status;
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
