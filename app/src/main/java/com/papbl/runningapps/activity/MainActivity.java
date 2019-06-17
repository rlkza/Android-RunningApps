package com.papbl.runningapps.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.papbl.runningapps.R;
import com.papbl.runningapps.adapter.TrackAdapter;
import com.papbl.runningapps.model.Track;
import com.papbl.runningapps.service.LocationReq;
import com.papbl.runningapps.service.Network;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener {

    private TextView nama, email;
    private RecyclerView rvRunTarck;
    private FloatingActionButton fabAdd;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;

    private FusedLocationProviderClient mFusedClient;
    private LocationCallback locationCallback;

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private int reqGPS = 2;

    private TrackAdapter trackAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nama = findViewById(R.id.tvNamaMain);
        email = findViewById(R.id.tvEmailMain);
        rvRunTarck = findViewById(R.id.rvRunTrackMain);
        rvRunTarck.setLayoutManager(new LinearLayoutManager(this));
        fabAdd = findViewById(R.id.fabAddMain);
        progressBar = findViewById(R.id.progressBarMain);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("User").child(firebaseUser.getUid());

        databaseReference.addListenerForSingleValueEvent(this);


        mFusedClient = LocationServices.getFusedLocationProviderClient(this);


        fabAdd.setOnClickListener(this);
    }

    private void getLocation(Location location){
        mFusedClient.removeLocationUpdates(locationCallback);
        Intent intent = new Intent(this, RunActivity.class);
        intent.putExtra("lat", location.getLatitude());
        intent.putExtra("lng", location.getLongitude());
        startActivity(intent);
        finish();
    }
    private void setUpGPS(){
        if (Network.networkInfo(this) != null){
            if (Network.gpsEnabled(this)){
                reqLocation();
            }else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.GpsOff);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, reqGPS);
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


    private void reqLocation(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }else {
            mFusedClient.requestLocationUpdates(LocationReq.getLocationRequest(), locationCallback, null);
        }
    }

    @Override
    public void onClick(View v) {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                getLocation(locationResult.getLastLocation());
            }
        };
        if (Network.networkInfo(this) !=null){
            setUpGPS();
        }else {
            Toast.makeText(this,"No Connection",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        nama.setText(dataSnapshot.child("nama").getValue().toString());
        email.setText(dataSnapshot.child("email").getValue().toString());
        ArrayList<Track> tracks = new ArrayList<>();
        for(DataSnapshot d:dataSnapshot.child("track").getChildren()){
            Track track = new Track(d.getKey(),
                    d.child("jarak").getValue().toString(),
                    d.child("tanggal").getValue().toString(),
                    d.child("durasi").getValue().toString(),
                    d.child("kalori").getValue().toString(),
                    (double)d.child("latFrom").getValue(),
                    (double)d.child("lngFrom").getValue(),
                    (double)d.child("latDest").getValue(),
                    (double)d.child("lngDest").getValue());
            tracks.add(track);
        }
        trackAdapter = new TrackAdapter(tracks,this);
        rvRunTarck.setAdapter(trackAdapter);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuLogout) {
            logOut();
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == reqGPS){
            if (Network.gpsEnabled(this)){
                reqLocation();
            }
        }
    }
    private void logOut(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
