package com.papbl.runningapps.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.papbl.runningapps.R;
import com.papbl.runningapps.model.Track;
import com.papbl.runningapps.service.LocationReq;
import com.papbl.runningapps.service.Network;
import com.papbl.runningapps.service.Stopwatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private ArrayList<LatLng> points = new ArrayList<>();
    private Polyline lines;
    private FusedLocationProviderClient mFusedClient;
    private boolean trackingLocation = false;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationCallback locationCallback;
    private Button btTracking;

    private TextView stopwatch;
    private final int MSG_START_TIMER = 0;
    private final int MSG_STOP_TIMER = 1;
    private final int MSG_UPDATE_TIMER = 2;

    private Stopwatch timer = new Stopwatch();
    private final int REFRESH_RATE = 100;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_TIMER:
                    timer.start(); //start timer
                    mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                    break;

                case MSG_UPDATE_TIMER:
                    stopwatch.setText(timerText(timer.getElapsedTime()));
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,REFRESH_RATE); //text view is updated every second,
                    break;                                  //though the timer is still running
                case MSG_STOP_TIMER:
                    mHandler.removeMessages(MSG_UPDATE_TIMER); // no more updates.
                    timer.stop();//stop timer
                    stopwatch.setText(timerText(timer.getElapsedTime()));
                    break;

                default:
                    break;
            }
        }
    };

    private LatLng point;
    private double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        lat = intent.getDoubleExtra("lat", -7.944551);
        lng = intent.getDoubleExtra("lng", 112.610901);

        btTracking = findViewById(R.id.btTracking);
        btTracking.setText(R.string.trackingOff);
        btTracking.setOnClickListener(this);
        stopwatch = findViewById(R.id.tvTimerRun);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("User").child(firebaseUser.getUid());

        mFusedClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(RunActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(RunActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }else {
            mMap = googleMap;
            mMap.setMyLocationEnabled(true);
            LatLng point;

            point = new LatLng(lat, lng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 20));
        }
    }
    private String timerText(long time){

        int seconds = (int) (time / 1000);

        int minute = seconds / 60;

        seconds = seconds % 60;

        return "" + minute + ":"
                + String.format("%02d", seconds) ;
    }
    private void startTracking(){
        if (Network.networkInfo(this) != null){
            if (Network.gpsEnabled(this)){
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
                }else {
                    points.clear();
                    trackingLocation = true;
                    mFusedClient.requestLocationUpdates(LocationReq.getLocationRequest(), locationCallback, null);
                    mHandler.sendEmptyMessage(MSG_START_TIMER);
                    btTracking.setText(R.string.trackingOn);
                }
            }else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.GpsOff);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        }else {
            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.noConnection, Snackbar.LENGTH_LONG).show();
        }
    }

    private void stopTracking(){
        if (trackingLocation){
            trackingLocation = false;
            mFusedClient.removeLocationUpdates(locationCallback);
            if (points.size() > 0){
                addMarker(points.get(0));
                addMarker(points.get(points.size() - 1));
            }
            btTracking.setText(R.string.trackingOff);

            mHandler.sendEmptyMessage(MSG_STOP_TIMER);


            finishSnackbar(getJarak()+" m",stopwatch.getText().toString(),getKalori()).show();
        }
    }
    private float getJarak(){
        float jarak = 0;
        for (int i = 0; i < points.size()-1; i++) {
            Location l1 = new Location(LocationManager.GPS_PROVIDER);
            l1.setLatitude(points.get(i).latitude);
            l1.setLongitude(points.get(i).longitude);
            Location l2 = new Location(LocationManager.GPS_PROVIDER);
            l2.setLatitude(points.get(i+1).latitude);
            l2.setLongitude(points.get(i+1).longitude);
            jarak = jarak + l1.distanceTo(l2);
        }
        return jarak;
    }
    private String getKalori(){
        return 0.06*getJarak()+" Kalori Terbakar";
    }

    public void onLocationChanged(Location location){
        Toast.makeText(this, location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();

        point = new LatLng(location.getLatitude(), location.getLongitude());
        points.add(point);
        drawing();
    }

    public void drawing(){
        PolylineOptions line = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i<points.size(); i++){
            point = points.get(i);
            line.add(point);
        }

        lines = mMap.addPolyline(line);
    }

    public void addMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private Snackbar finishSnackbar(String sJarak, String sDurasi, String sKalori){
        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);
        LayoutInflater objLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View snackView = objLayoutInflater.inflate(R.layout.view_snackbar_detail_run_, null);
        ImageView imageView = snackView.findViewById(R.id.btnCloseDetailRun);
        Button save = snackView.findViewById(R.id.btnSaveDetailRun);
        TextView jarak = snackView.findViewById(R.id.tvJarakDetailRun),
                durasi = snackView.findViewById(R.id.tvDurasiDetailRun),
                kalori = snackView.findViewById(R.id.tvKaloriDetailRun);

        jarak.setText(sJarak);
        durasi.setText(sDurasi);
        kalori.setText(sKalori);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                String pattern = "yyyy-MM-dd";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

                final String date = simpleDateFormat.format(new Date());
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            Track track = new Track(String.valueOf(dataSnapshot.child("track").getChildrenCount()+1),
                                    String.valueOf(getJarak()),
                                    date,stopwatch.getText().toString(),
                                    getKalori(),
                                    points.get(0).latitude,
                                    points.get(0).longitude,
                                    points.get(points.size()-1).latitude,
                                    points.get(points.size()-1).longitude);
                            databaseReference.child("track").child(String.valueOf(dataSnapshot.child("track").getChildrenCount()+1)).setValue(track);
                            progressDialog.dismiss();
                            startActivity(new Intent(RunActivity.this,MainActivity.class));
                            finish();
                        }catch (Exception ex){
                            Track track = new Track("1",
                                    String.valueOf(getJarak()),
                                    date,stopwatch.getText().toString(),
                                    getKalori(),
                                    points.get(0).latitude,
                                    points.get(0).longitude,
                                    points.get(points.size()-1).latitude,
                                    points.get(points.size()-1).longitude);
                            databaseReference.child("track").child("1").setValue(track);
                            progressDialog.dismiss();
                            startActivity(new Intent(RunActivity.this,MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        layout.setPadding(0,0,0,0);

        layout.addView(snackView, 0);
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite));
        return snackbar;
    }

    @Override
    public void onClick(View v) {
        if (!trackingLocation){
            startTracking();
        }else{
            stopTracking();
        }
    }

    @Override
    public void onBackPressed() {
        if (!trackingLocation){
            Toast.makeText(this,"Running",Toast.LENGTH_LONG).show();
        }else{
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Exit?");
            builder.setMessage("Exit will not save your track");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
    }
}
