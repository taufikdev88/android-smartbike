package com.example.smartbike;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartbike.model.SettingsModel;
import com.example.smartbike.services.ApiClient;
import com.example.smartbike.services.GetServices;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SettingActivity extends AppCompatActivity {
    TextInputEditText txtNormalRpm;
    TextInputEditText txtSpeedRpm;
    Button btnSave;
    Button btnCancel;

    GetServices services;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        txtNormalRpm = findViewById(R.id.settingTxtNormalMode);
        txtSpeedRpm = findViewById(R.id.settingTxtSpeedMode);
        btnSave = findViewById(R.id.settingBtnSave);
        btnCancel = findViewById(R.id.settingBtnCancel);

        services = ApiClient.getRetrofitInstance().create(GetServices.class);
        Call<SettingsModel> call = services.setRead();
        call.enqueue(new Callback<SettingsModel>() {
            @Override
            public void onResponse(Call<SettingsModel> call, Response<SettingsModel> response) {
                if(response.isSuccessful()){
                    SettingsModel settingsModel = response.body();
                    txtNormalRpm.setText(String.valueOf(settingsModel.getNormalRPM()));
                    txtSpeedRpm.setText(String.valueOf(settingsModel.getTurboRPM()));
                } else {
                    Toast.makeText(SettingActivity.this, "Cant communicate with device", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SettingsModel> call, Throwable t) {
                Log.d("RETROFIT DEBUG", t.getMessage());
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Call<SettingsModel> save = services.setWrite(Integer.parseInt(txtNormalRpm.getText().toString()), Integer.parseInt(txtSpeedRpm.getText().toString()));
                    save.enqueue(new Callback<SettingsModel>() {
                        @Override
                        public void onResponse(Call<SettingsModel> call, Response<SettingsModel> response) {
                            if(response.isSuccessful()){
                                Toast.makeText(SettingActivity.this, "Successfull saving rpm", Toast.LENGTH_SHORT).show();
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                },2000);
                            } else {
                                Toast.makeText(SettingActivity.this, "Failed setting rpm", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<SettingsModel> call, Throwable t) {
                            Log.d("RETROFIT DEBUG", t.getMessage());
                        }
                    });
                } catch (Exception e){
                    Toast.makeText(SettingActivity.this, "Invalid RPM Value, use Integer Value", Toast.LENGTH_SHORT).show();
                    Log.d("SETTING DEBUG", e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
