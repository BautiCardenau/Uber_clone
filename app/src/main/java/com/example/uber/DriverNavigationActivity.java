package com.example.uber;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.Provider;
import java.sql.Driver;

public class DriverNavigationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LatLng requestLocation;
    //LatLng driverLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_navigation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMyLocationEnabled(true);

        Intent intent = getIntent();
        requestLocation = new LatLng(intent.getDoubleExtra("requestLatitude", 0),
                intent.getDoubleExtra("requestLongitude", 0));
        //driverLocation = new LatLng(intent.getDoubleExtra("driverLatitude", 0),
                //intent.getDoubleExtra("driverLongitude", 0));

        LocationManager locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(DriverNavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Location lastKnownLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null){
            String latMy = String.valueOf(lastKnownLocation.getLatitude());
            String lngMy = String.valueOf(lastKnownLocation.getLongitude());

            String url = "http://maps.google.com/maps?saddr=" + latMy + ","
                    + lngMy + "&daddr=" + requestLocation.latitude +
                    "," + requestLocation.longitude;

            Intent navigation = new Intent(Intent.ACTION_VIEW);
            navigation.setData(Uri.parse(url));

            startActivity(navigation);
        }
        }
    }
}
