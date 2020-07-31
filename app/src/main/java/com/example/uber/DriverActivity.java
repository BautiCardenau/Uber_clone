package com.example.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverActivity extends AppCompatActivity {

    LocationListener locationListener;
    LocationManager locationManager;
    LatLng latLng;
    ArrayList clientsArrayList;
    ArrayAdapter adapter;
    ArrayList<Double> requestLatitudes = new ArrayList<Double>();
    ArrayList<Double> requestLongitudes = new ArrayList<Double>();


    public void getLocation(){
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 10, locationListener);
        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 10, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
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
        setContentView(R.layout.activity_driver);

        final ListView clientsListView = findViewById(R.id.clientsListView);
        clientsArrayList = new ArrayList();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, clientsArrayList);
        clientsListView.setAdapter(adapter);

        clientsArrayList.clear();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latLng = new LatLng(location.getLatitude(),location.getLongitude());
                final ParseGeoPoint driversLocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());

                //Hasta aca deberia tener la location del driver, necesito la de los clientes.

                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
                query.whereNear("location", driversLocation);
                query.setLimit(5);

                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null){

                            clientsArrayList.clear();
                            requestLongitudes.clear();
                            requestLatitudes.clear();

                            if (objects.size() > 0){
                                for (ParseObject object : objects){
                                    ParseGeoPoint requestLocation = object.getParseGeoPoint("location");
                                    double distance = (double) Math.round(requestLocation.distanceInKilometersTo(driversLocation) * 10)/10;
                                    clientsArrayList.add(distance + " km");
                                    requestLatitudes.add(requestLocation.getLatitude());
                                    requestLongitudes.add(requestLocation.getLongitude());
                                }
                            } else {
                                clientsArrayList.add("No nearby requests");
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

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

        getLocation();

        clientsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                //start navigating towards this route

                if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnownLocation != null && requestLatitudes.size() > i && requestLongitudes.size() > i){

                        //Redirect driver to map showing both locations
                        Intent intent = new Intent(getApplicationContext(), requestInMapsActivity.class);
                        intent.putExtra("requestLatitude", requestLatitudes.get(i));
                        intent.putExtra("requestLongitude", requestLongitudes.get(i));
                        startActivity(intent);

                    }
                }
            }
        });
    }
}
