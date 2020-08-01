package com.example.uber;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class requestInMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LatLng requestLocation;
    LatLng driverLocation;
    Intent intent;

    public void acceptRide (View view){

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        query.whereEqualTo("username",intent.getStringExtra("username"));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (objects.size() > 0){
                        for (ParseObject object : objects){
                            object.put("driverUsername", ParseUser.getCurrentUser().getUsername());
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null){
                                        /*
                                        Intent intent1 = new Intent(getApplicationContext(), DriverNavigationActivity.class);
                                        intent.putExtra("requestLatitude", requestLocation.latitude);
                                        intent.putExtra("requestLongitude", requestLocation.longitude);
                                        //intent.putExtra("driverLatitude", driverLocation.latitude);
                                        //intent.putExtra("driverLongitude", driverLocation.longitude);
                                        startActivity(intent1);
                                         */
                                        String url = "http://maps.google.com/maps?saddr=" + driverLocation.latitude + ","
                                                + driverLocation.longitude + "&daddr=" + requestLocation.latitude +
                                                "," + requestLocation.longitude;

                                        Intent navigation = new Intent(Intent.ACTION_VIEW);
                                        navigation.setData(Uri.parse(url));

                                        startActivity(navigation);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
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

        intent = getIntent();
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
