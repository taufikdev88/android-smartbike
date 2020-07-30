package com.example.smartbike;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartbike.adaptor.MapsAdaptor;
import com.example.smartbike.handler.DatabaseHandler;
import com.example.smartbike.model.MapsModel;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.shape.ShapeConverter;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class MapsActivity extends AppCompatActivity {
    private final int REQUEST_PERMISSION_CODE = 1;
    public MapView mapView;
    private RecyclerView listMapsHistory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapView = findViewById(R.id.mapsMainMaps);
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        Context context = getApplicationContext();
        Configuration.getInstance().load(context, context.getSharedPreferences("mapscache", MODE_PRIVATE));

//        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        final IMapController mapController = mapView.getController();
        mapController.setZoom(18.0);

        GeoPoint startPoint = new GeoPoint(-7.291691, 112.797641);
        mapController.setCenter(startPoint);

        listMapsHistory = findViewById(R.id.mapsListHistory);
        final DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
        final ArrayList<MapsModel> mapsModels = databaseHandler.getStepList();
        for (int i=0; i<mapsModels.size(); i++){
            Log.d("DB DEBUG", String.valueOf(mapsModels.get(i).getStep()));
        }
        final MapsAdaptor adaptor = new MapsAdaptor(mapsModels);
        listMapsHistory.setAdapter(adaptor);
        listMapsHistory.setLayoutManager(new LinearLayoutManager(this));
        listMapsHistory.setHasFixedSize(true);
        listMapsHistory.setItemAnimator(new SlideInUpAnimator());
        listMapsHistory.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull final View view) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if(mapView.getOverlayManager().overlays().size() > 0){
                                mapView.getOverlayManager().remove(0);
                                Log.d("OVERLAY DEBUG","Hapus overlay yg sudah ada");
                            }
                            RecyclerView.ViewHolder viewHolder = listMapsHistory.getChildViewHolder(v);
                            int position = viewHolder.getAdapterPosition();
                            ArrayList<MapsModel> mapsHistory = databaseHandler.getMapsHistory(mapsModels.get(position));
                            Polyline polyline = new Polyline(mapView, true);
                            List<GeoPoint> pts = new ArrayList<>();
//                            Random r = new Random();
                            for (MapsModel m : mapsHistory){
                                Log.d("HISTORY DEBUG", "lat: " + m.getLatitude() + ", long: " + m.getLongitude());
//                                GeoPoint pt = new GeoPoint(m.getLatitude()*r.nextDouble(), m.getLongitude()*r.nextDouble());
                                GeoPoint pt = new GeoPoint(m.getLatitude(), m.getLongitude());
                                pts.add(pt);
                                mapController.setCenter(pt);
                            }
                            polyline.getOutlinePaint().setColor(Color.RED);
                            polyline.getOutlinePaint().setStrokeWidth(4.0f);
                            polyline.setPoints(pts);
                            polyline.setGeodesic(true);
                            mapView.getOverlayManager().add(polyline);
                            mapView.invalidate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        RecyclerView.ViewHolder viewHolder = listMapsHistory.getChildViewHolder(v);
                        final int position = viewHolder.getAdapterPosition();

                        final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setMessage("Do yout want to delete this record ?")
                                .setTitle(String.format("Delete Confirmation %s %s", mapsModels.get(position).getDate(), mapsModels.get(position).getTime()));
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    databaseHandler.deleteRecord(mapsModels.get(position));
                                    mapsModels.remove(position);
                                    listMapsHistory.removeViewAt(position);
                                    adaptor.notifyItemRemoved(position);
                                    adaptor.notifyItemRangeChanged(position, adaptor.dataSet.size());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return false;
                    }
                });
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

        List<Overlay> folder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                folder = ShapeConverter.convert(mapView, new File(String.format("%s/shape.shp", getApplicationContext().getDataDir().toString())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                folder = ShapeConverter.convert(mapView, new File(getApplicationInfo().dataDir + "/shape.shp"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        assert folder != null;
        mapView.getOverlayManager().addAll(folder);
        mapView.invalidate();
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
