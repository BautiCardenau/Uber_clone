package com.example.uber;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class requestInMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LatLng requestLocation;
    LatLng driverLocation;

    public void acceptRide (View view){
        Intent intent = new Intent(getApplicationContext(), DriverNavigationActivity.class);
        intent.putExtra("requestLatitude", requestLocation.latitude);
        intent.putExtra("requestLongitude", requestLocation.longitude);
        //intent.putExtra("driverLatitude", driverLocation.latitude);
        //intent.putExtra("driverLongitude", driverLocation.longitude);
        //should erase Request from server
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_in_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        requestLocation = new LatLng(intent.getDoubleExtra("requestLatitude", 0),
                intent.getDoubleExtra("requestLongitude", 0));
        driverLocation = new LatLng(intent.getDoubleExtra("driverLatitude", 0),
                intent.getDoubleExtra("driverLongitude", 0));

        Marker requestMarker= mMap.addMarker(new MarkerOptions().position(requestLocation).title("Request Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title("Your Location"));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(requestMarker.getPosition());
        builder.include(driverMarker.getPosition());

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.40); // offset from edges of the map 10% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cu);
    }
}
