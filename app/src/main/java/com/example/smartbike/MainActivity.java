package com.example.smartbike;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.example.smartbike.handler.DatabaseHandler;
import com.example.smartbike.model.GetModel;
import com.example.smartbike.model.MainModel;
import com.example.smartbike.model.MapsModel;
import com.example.smartbike.services.ApiClient;
import com.example.smartbike.services.GetServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CODE = 1;

    private enum State {
        OFF,
        ON,
        NORMAL,
        SPEED
    }

    private int step = 0;

    private State state = State.OFF;
    private TextView txtRpmMon;
    private SeekBar seekState;
    private CardView cardNormal;
    private CardView cardSpeed;
    private ImageView imageHeader;

    DatabaseHandler databaseHandler;
    Date now;
    SimpleDateFormat dateFormat;
    LocationManager locationManager;
    GetServices services;
    Disposable disposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting agar handphone layarnya tetep nyala saat aplikasi ini dibuka
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // set tampilan aktifity ini menggunakan layout main
        setContentView(R.layout.activity_main);
        // siapkan object LocationManager untuk nanti digunakan melihat posisi org tersebut
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        txtRpmMon = findViewById(R.id.mainTxtRpmMon);
        seekState = findViewById(R.id.mainSeekState);
        ImageButton btnMaps = findViewById(R.id.mainBtnMaps);
        ImageButton btnSetting = findViewById(R.id.mainBtnSetting);
        ImageButton btnNormal = findViewById(R.id.mainBtnNormal);
        ImageButton btnSpeed = findViewById(R.id.mainBtnSpeed);
        cardNormal = findViewById(R.id.mainCardNormal);
        cardSpeed = findViewById(R.id.mainCardSpeed);
        imageHeader = findViewById(R.id.mainImageHeader);
        // set text rpm sebagai text yg berubah ubah
        TextViewCompat.setAutoSizeTextTypeWithDefaults(txtRpmMon, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        // saat di klik ikon setting, maka nanti layar setting aplikasinya terbuka
        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
        });

        btnMaps.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
        });

        // menggunakan seekbar untuk ganti on off, agar sistem bener bener dinyalakan oleh user, karena jika model button, bisa saja kepencet
        seekState.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 90) state = State.ON;
                else state = State.OFF;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // setelah user berhenti menggeser, di cek dulu statusnya ON atau OFF
                switch (state) {
                    case ON:
                        // jika terakhir di geser belum ada 100 tp sudah diatas 90, langsung aja set ke 100
                        seekBar.setProgress(100);
                        // cek dulu permission untuk akses lokasi
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                            requestPermissionsIfNecessary(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION });
                        } else if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissionsIfNecessary(new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION});
                        } else {
                            // setelah semua permission lokasi dapat, mulai request update lokasi dengan interval 2000ms dengan minimum perpindahan jarak 1 meter
                            // saat 2 detik sekali dan ada perpindahan jarak minimal 1 meter, maka akan menjalankan fungsi locationListener yg ada di bawah
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, locationListener);
                        }
                        // dan aktifkan motor
                        motorOn();
                        break;
                    case OFF:
                        // kalau off set tampilan nya seperti awal semua dan matikan motor dan berhenti melihat pergantian lokasi
                        cardNormal.setCardBackgroundColor(Color.parseColor("#FFEB3B"));
                        cardNormal.setCardElevation(0);
                        cardSpeed.setCardBackgroundColor(Color.parseColor("#FFEB3B"));
                        cardSpeed.setCardElevation(0);
                        seekBar.setProgress(0);
                        locationManager.removeUpdates(locationListener);
                        motorOff();
                        break;
                }
            }
        });
        // jika button mode normal di pencet
        btnNormal.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                // jika motor belum on, harus di on kan dulu
                if (state.equals(State.OFF)) {
                    Toast.makeText(MainActivity.this, R.string.warning_main_start_mode, Toast.LENGTH_LONG).show();
                    seekState.findFocus();
                    return;
                }
                // menggunakan retrofit untuk koneksi ke arduino
                // dengan memberi nilai parameter pada retrofit motorOn true dan mode normal true
                Call<MainModel> normal = services.main(1,1);
                normal.enqueue(new Callback<MainModel>() {
                    @Override
                    public void onResponse(Call<MainModel> call, Response<MainModel> response) {
                        // kalo sukses di kirim ke arduino baru , set tampilan nya
                        if(response.isSuccessful()){
                            cardNormal.setCardElevation(24);
                            cardNormal.setCardBackgroundColor(Color.parseColor("#FFC107"));

                            cardSpeed.setCardElevation(0);
                            cardSpeed.setCardBackgroundColor(Color.parseColor("#FFEB3B"));

                            imageHeader.setImageResource(R.drawable.normal);
                            state = State.NORMAL;
                            Toast.makeText(getApplicationContext(), "Normal Mode", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Cant communicate with device", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MainModel> call, Throwable t) {
                        Log.d("RETROFIT DEBUG", t.getMessage());
                    }
                });
            }
        });

        btnSpeed.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                if (state.equals(State.OFF)) {
                    Toast.makeText(MainActivity.this, R.string.warning_main_start_mode, Toast.LENGTH_LONG).show();
                    seekState.findFocus();
                    return;
                }
                Call<MainModel> speed = services.main(1,0);
                speed.enqueue(new Callback<MainModel>() {
                    @Override
                    public void onResponse(Call<MainModel> call, Response<MainModel> response) {
                        if(response.isSuccessful()){
                            cardSpeed.setCardElevation(24);
                            cardSpeed.setCardBackgroundColor(Color.parseColor("#FFC107"));

                            cardNormal.setCardElevation(0);
                            cardNormal.setCardBackgroundColor(Color.parseColor("#FFEB3B"));

                            imageHeader.setImageResource(R.drawable.speed);
                            state = State.SPEED;
                            Toast.makeText(getApplicationContext(), "Speed Mode", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Cant Communicate with device", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MainModel> call, Throwable t) {
                        Log.d("RETROFIT DEBUG", t.getMessage());
                    }
                });
            }
        });

        // siapkan koneksi ke database
        databaseHandler = new DatabaseHandler(getApplicationContext());
        // siapkan variabel tanggal sekarang
        now = new Date(System.currentTimeMillis());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        Log.d(getString(R.string.app_name), "Date: " + dateFormat.format(now));
        // ambil nilai terakhir jumlah koneksi pada hari ini
        step = databaseHandler.getLastStepByDate(dateFormat.format(now));

        // request permission untuk akses lokasi yg didapat oleh handphone
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        });
        // siapkan variabel untuk retrofit / koneksi ke arduino
        services = ApiClient.getRetrofitInstance().create(GetServices.class);
    }

    private void motorOn() {
        Call<MainModel> on = services.main(0, 1);
        on.enqueue(new Callback<MainModel>() {
            @Override
            public void onResponse(Call<MainModel> call, Response<MainModel> response) {
                if(response.isSuccessful()){
                    MainModel mainModel = response.body();
                    Log.d("RETROFIT DEBUG", mainModel.getStatus() + ", " + mainModel.isState());
                    Toast.makeText(getApplicationContext(), "Motor Ready", Toast.LENGTH_SHORT).show();
                    txtRpmMon.setText("Motor On");
//                    if(disposable == null){
//                        startReadRpm();
//                    } else {
//                        if(disposable.isDisposed()){
//                            startReadRpm();
//                        }
//                    }
                } else {
                    Toast.makeText(MainActivity.this, "Cannot communicate with device", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MainModel> call, Throwable t) {
                Log.d("RETROFIT DEBUG", t.getMessage());
                Toast.makeText(MainActivity.this, "Cannot communicate with device", Toast.LENGTH_SHORT).show();
                seekState.setProgress(0);
            }
        });
    }

    // gak dipake lagi karena ga perlu cek rpm terus
    private void startReadRpm(){
        disposable = Observable.interval(1, 5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::callEndPoint, this::onError);
    }

    // gak dipake lagi
    private void callEndPoint(long along){
        Observable<GetModel> observable = services.get();
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(result -> result.getState())
                .subscribe(this::handleResult, this::handleError);
    }

    // gak dipake
    private void handleResult(String result) {
        txtRpmMon.setText(result);
    }

    // gak dipake
    private void handleError(Throwable t){
        t.printStackTrace();
    }

    private void onError(Throwable t){
        t.printStackTrace();
    }

    private void motorOff() {
        Call<MainModel> off = services.main(0, 1);
        off.enqueue(new Callback<MainModel>() {
            @Override
            public void onResponse(Call<MainModel> call, Response<MainModel> response) {
                if(response.isSuccessful()){
                    MainModel mainModel = response.body();
                    Log.d("RETROFIT DEBUG", mainModel.getStatus() + ", " + mainModel.isState());
                    Toast.makeText(getApplicationContext(), "Motor Off", Toast.LENGTH_SHORT).show();
//                    disposable.dispose();
                    txtRpmMon.setText("Motor Off");
                } else {
                    Toast.makeText(MainActivity.this, "Cannot communicate with device", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MainModel> call, Throwable t) {
                Log.d("RETROFIT DEBUG", t.getMessage());
            }
        });
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // ini adalah fungsi tadi , saat lokasinya berubah maka
            Log.d(getString(R.string.app_name), "Lat: " + location.getLatitude() + " ,Long: " + location.getLongitude());
            // ini objek yg digunakan untuk mempermudah menyimpan payload data yg nanti di simpan ke database
            MapsModel mapsModel = new MapsModel();
            // set step to last step today
            mapsModel.setStep(step);
            // set date to today
            now = new Date(System.currentTimeMillis());
            dateFormat.applyPattern("dd/MM/yyyy");
            mapsModel.setDate(dateFormat.format(now));
            dateFormat.applyPattern("HH:mm");
            mapsModel.setTime(dateFormat.format(now));
            // set location by phone location
            mapsModel.setLatitude(location.getLatitude());
            mapsModel.setLongitude(location.getLongitude());
            // save to database
            databaseHandler.addRecord(mapsModel);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    // saat aplikasi di pause (user berpindah ke aplikasi lain)
    @Override
    protected void onPause() {
        super.onPause();
        cardNormal.setCardBackgroundColor(Color.parseColor("#FFEB3B"));
        cardNormal.setCardElevation(0);
        cardSpeed.setCardBackgroundColor(Color.parseColor("#FFEB3B"));
        cardSpeed.setCardElevation(0);
        seekState.setProgress(0);
        Toast.makeText(getApplicationContext(), "Motor Off", Toast.LENGTH_SHORT).show();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ArrayList<String> permissionToRequest = new ArrayList<>(Arrays.asList(permissions).subList(0, grantResults.length));
        if(permissionToRequest.size() > 0){
            ActivityCompat.requestPermissions(this, permissionToRequest.toArray(new String[0]),REQUEST_PERMISSION_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this,permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSION_CODE);
        }
    }
}