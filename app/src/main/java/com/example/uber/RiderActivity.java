package com.example.uber;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Boolean isUberRequested = false;
    LatLng latLng;
    Button callUberButton;
    TextView infoTextView;
    Marker userMarker;
    Boolean isDriverAssigned = false;

    Handler handler = new Handler();

    public void logOut (View view){
        ParseUser.logOut();

        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }

    public void checkForUpdates(){

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.whereExists("driverUsername");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (objects.size() > 0){

                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo("username", objects.get(0).getString("driverUsername"));
                        query.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> objects, ParseException e) {

                                if (e == null && objects.size() > 0){

                                    isDriverAssigned = true;

                                    ParseGeoPoint driverLocation = objects.get(0).getParseGeoPoint("location");

                                    if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                        if (lastKnownLocation != null){

                                            ParseGeoPoint userLocation = new ParseGeoPoint(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                                            double distance = (double) Math.round(userLocation.distanceInKilometersTo(driverLocation) * 10)/10;
                                            infoTextView.setText("Your driver is " + distance + " km away");

                                            if (distance < 0.01){

                                                infoTextView.setText("Your driver is here");
                                                isDriverAssigned = false;

                                                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
                                                query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

                                                query.findInBackground(new FindCallback<ParseObject>() {
                                                    @Override
                                                    public void done(List<ParseObject> objects, ParseException e) {
                                                        if (e == null && objects.size() > 0){
                                                            for (ParseObject object : objects){
                                                                object.deleteInBackground();
                                                            }
                                                        }
                                                    }
                                                });

                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        infoTextView.setText("");
                                                        callUberButton.setVisibility(View.VISIBLE);
                                                        callUberButton.setText("Call uber");
                                                        isUberRequested = false;
                                                    }
                                                }, 5000);

                                            } else {

                                                mMap.clear();

                                                LatLng userLatLng = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                                                LatLng driverLatLng = new LatLng(driverLocation.getLatitude(),driverLocation.getLongitude());

                                                Marker driverMarker= mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver's Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                                userMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.stickman)));

                                                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                                                builder.include(userMarker.getPosition());
                                                builder.include(driverMarker.getPosition());

                                                LatLngBounds bounds = builder.build();

                                                int width = getResources().getDisplayMetrics().widthPixels;
                                                int height = getResources().getDisplayMetrics().heightPixels;
                                                int padding = (int) (width * 0.40); // offset from edges of the map 10% of screen

                                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                                                mMap.animateCamera(cu);

                                                callUberButton.setVisibility(View.INVISIBLE);

                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        checkForUpdates();
                                                    }
                                                }, 2000);
                                            }
                                        }
                                    }

                                }

                            }
                        });
                    }
                }

                if (!isDriverAssigned){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkForUpdates();
                        }
                    }, 2000);
                }
            }
        });

    }

    public void requestOrCancelUber (View view){

        if (isUberRequested) {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null){
                        if (objects.size() > 0) {
                            for (ParseObject object:objects){
                                object.deleteInBackground();
                            }
                            isUberRequested = false;
                            callUberButton.setText("Call uber");
                        }

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                checkForUpdates();
                            }
                        }, 2000);
                    }

                }
            });

        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 10, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastKnownLocation != null) {
                    ParseObject request = new ParseObject("Request");
                    request.put("username", ParseUser.getCurrentUser().getUsername());
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    request.put("location", parseGeoPoint);
                    request.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                callUberButton.setText("Cancel uber");
                                isUberRequested = true;

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        checkForUpdates();
                                    }
                                }, 2000);
                            } else {
                                Toast.makeText(RiderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }
    }

    public void centerMap (View view) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 10, locationListener);

                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        callUberButton = findViewById(R.id.acceptRideButton);
        infoTextView = findViewById(R.id.infoTextView);

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (e == null){
                    if (objects.size() > 0) {
                        isUberRequested = true;
                        callUberButton.setText("Cancel uber");
                        checkForUpdates();
                    }
                }

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mMap.clear();
                latLng = new LatLng(location.getLatitude(),location.getLongitude());
                userMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.stickman)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));
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


        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 10, locationListener);
        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 10, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                mMap.clear();
                LatLng latLng = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
                userMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.stickman)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));
            }
        }
            }
}
