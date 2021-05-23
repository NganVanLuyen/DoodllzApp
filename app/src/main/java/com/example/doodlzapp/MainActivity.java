package com.example.doodlzapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

//        // determine screen size
//        int screenSize =
//                getResources().getConfiguration().screenLayout &
//                        Configuration.SCREENLAYOUT_SIZE_MASK;
//
//        // use landscape for extra large tablets; otherwise, use portrait
//        if (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
//            setRequestedOrientation(
//                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        else
//            setRequestedOrientation(
//                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}