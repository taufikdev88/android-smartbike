package com.example.smartbike.handler;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.smartbike.model.MapsModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    // nama database dan tabel
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "DB_SMARTBIKE.db";
    private static final String TABLE_NAME = "TBL_HISTORY";
    // nama kolom
    private static final String KEY_ID = "id";
    private static final String KEY_STEP = "step";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME = "time";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // saat proses pembuatan databse
        String create_sql = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY,%s INTEGER,%s TEXT,%s TEXT,%s TEXT,%s TEXT)", TABLE_NAME, KEY_ID, KEY_STEP, KEY_DATE, KEY_TIME, KEY_LATITUDE, KEY_LONGITUDE);
        db.execSQL(create_sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // ini adalah proses penyimpanan data ke database
    public void addRecord(MapsModel mapsModel){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_STEP, mapsModel.getStep());
        values.put(KEY_DATE, mapsModel.getDate());
        values.put(KEY_TIME, mapsModel.getTime());
        values.put(KEY_LATITUDE, mapsModel.getLatitude());
        values.put(KEY_LONGITUDE, mapsModel.getLongitude());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // ambil data berdasarkan stepnya
    public ArrayList<MapsModel> getStepList() {
        ArrayList<MapsModel> stepList = new ArrayList<>();
        String selectQuery = String.format("SELECT * FROM %s GROUP BY `%s`", TABLE_NAME, KEY_STEP);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do {
                MapsModel mapsModel = new MapsModel();
                if(!cursor.isNull(0)) mapsModel.setId(Integer.parseInt(cursor.getString(0)));
                if(!cursor.isNull(1)) mapsModel.setStep(Integer.parseInt(cursor.getString(1)));
                if(!cursor.isNull(2)) mapsModel.setDate(cursor.getString(2));
                if(!cursor.isNull(3)) mapsModel.setTime(cursor.getString(3));
                if(!cursor.isNull(4)) mapsModel.setLatitude(Float.parseFloat(cursor.getString(4)));
                if(!cursor.isNull(5)) mapsModel.setLongitude(Float.parseFloat(cursor.getString(5)));

                stepList.add(mapsModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return stepList;
    }

    // ambil semua history lat long berdasarkan single objek MapsModel yang berisi tanggal dan stepnya
    public ArrayList<MapsModel> getMapsHistory(MapsModel mapsModel) {
        ArrayList<MapsModel> historyList = new ArrayList<>();
        @SuppressLint("DefaultLocale") String selectQuery = String.format("SELECT * FROM %s WHERE %s = '%d' AND %s = '%s'", TABLE_NAME, KEY_STEP, mapsModel.getStep(), KEY_DATE, mapsModel.getDate());

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                mapsModel = new MapsModel();
                mapsModel.setId(Integer.parseInt(cursor.getString(0)));
                mapsModel.setStep(Integer.parseInt(cursor.getString(1)));
                mapsModel.setDate(cursor.getString(2));
                mapsModel.setTime(cursor.getString(3));
                mapsModel.setLatitude(Float.parseFloat(cursor.getString(4)));
                mapsModel.setLongitude(Float.parseFloat(cursor.getString(5)));

                historyList.add(mapsModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return historyList;
    }

    // perintah hapus
    public boolean deleteRecord(MapsModel mapsModel){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, String.format("%s = ? and %s = ?", KEY_STEP, KEY_DATE), new String[]{
                String.valueOf(mapsModel.getStep()),
                mapsModel.getDate()
        });
        db.close();
        return true;
    }

    // perintah untuk mencari tahu nilai login terakhir pada tanggal yang sama
    public int getLastStepByDate(String date){
        String getQuery = String.format("SELECT MAX(%s) FROM %s WHERE %s = '%s'", KEY_STEP, TABLE_NAME, KEY_DATE, date);
        Log.d("DB DEBUG", getQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(getQuery, null);

        int step = 0;
        cursor.moveToFirst();
        step = cursor.getInt(0);
        step += 1;
        Log.d("DB DEBUG", "step: " + step);
        cursor.close();
        db.close();
        return step;
    }
}
