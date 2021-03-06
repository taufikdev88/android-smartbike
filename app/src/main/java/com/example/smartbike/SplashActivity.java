package com.example.smartbike;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {
    private int i = 0;
    private ProgressBar progressBar;
    WifiManager wifiManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressBar = findViewById(R.id.splash_progressbar);

        final Timer timer = new Timer();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        if (!wifiManager.isWifiEnabled()) {
//            Toast.makeText(SplashActivity.this, "Wifi must be enabled and please connect to SmartBike Wifi", Toast.LENGTH_LONG).show();
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                    startActivity(intent);
//                }
//            }, 1000);
//        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(++i >= 100){
                    timer.cancel();
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                progressBar.setProgress(i);
            }
        }, 0, 10);
    }
}
