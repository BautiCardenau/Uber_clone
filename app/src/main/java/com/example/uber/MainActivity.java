package com.example.uber;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;

import com.parse.ParseAnalytics;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity {

    public void redirectUser () {
        Intent intent;
        String userType = ParseUser.getCurrentUser().get("Type").toString();

        if (userType.equals("rider")){
            intent = new Intent(getApplicationContext(),RiderActivity.class);
            startActivity(intent);
        } else if (userType.equals("driver")){
            intent = new Intent(getApplicationContext(),DriverActivity.class);
            startActivity(intent);
        }


    }


    public void getStarted (View view){
        Switch userSwitch = (Switch) findViewById(R.id.userSwitch);

        String userType = "rider";
        if (userSwitch.isChecked()){
            userType = "driver";
        }
        ParseUser.getCurrentUser().put("Type", userType);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                //Redirect
                redirectUser();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ParseUser.getCurrentUser() == null){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null){
                        Log.i("Info", "Success");
                    } else {
                        Log.i("Info", e.getMessage());
                    }
                }
            });
        } else {
            if(ParseUser.getCurrentUser().get("Type") != null){
                redirectUser();
            }
        }

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }
}
