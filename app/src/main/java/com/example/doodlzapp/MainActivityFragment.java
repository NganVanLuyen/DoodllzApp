package com.example.doodlzapp;

import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainActivityFragment extends Fragment {
    private DoodleView doodleView; // handles touch events and draws
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private boolean dialogOnScreen = false;

    private static final int ACCELERATION_THRESHOLD = 100000;
    private static final int SAVE_IMAGE_PERMISSION_REQUEST_CODE = 1;

    // called when Fragment's view needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =
                inflater.inflate(R.layout.main_fragment, container, false);

        setHasOptionsMenu(true);

        // get reference to the DoodleView
        doodleView = (DoodleView) view.findViewById(R.id.doodleView);

        // initialize acceleration values
        acceleration = 0.00f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
        return view;
    }

    // sensor
    @Override
    public void onResume() {
        super.onResume();
        enableAccelerometerListening();
    }

    // enable listening for accelerometer events
    private void enableAccelerometerListening() {
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(
                        Context.SENSOR_SERVICE);

        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    // stop listening for accelerometer events
    @Override
    public void onPause() {
        super.onPause();
        disableAccelerometerListening();
    }

    // disable listening for accelerometer events
    private void disableAccelerometerListening() {
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(
                        Context.SENSOR_SERVICE);

        sensorManager.unregisterListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    // event handler for accelerometer events
    private final SensorEventListener sensorEventListener =
            new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (!dialogOnScreen) {
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];

                        // save previous acceleration value
                        lastAcceleration = currentAcceleration;

                        // calculate the current acceleration
                        currentAcceleration = x * x + y * y + z * z;

                        // calculate the change in acceleration
                        acceleration = currentAcceleration *
                                (currentAcceleration - lastAcceleration);

                        // if the acceleration is above a certain threshold
                        if (acceleration > ACCELERATION_THRESHOLD)
                            confirmErase();
                    }
                }

                // required method of interface SensorEventListener
                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };

    // confirm whether image should be erase
    private void confirmErase() {
        EraseImageDialogFragment fragment = new EraseImageDialogFragment();
        fragment.show(getFragmentManager(), "erase dialog");
    }

    // displays the fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.doodle_fragment_menu, menu);
    }

    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.color:
                ColorDialogFragment colorDialog = new ColorDialogFragment();
                colorDialog.show(getFragmentManager(), "color dialog");
                return true;
            case R.id.line_width:
                LineWidthDialogFragment widthDialog = new LineWidthDialogFragment();
                widthDialog.show(getFragmentManager(), "line width dialog");
                return true;
            case R.id.delete_drawing:
                confirmErase();
                return true;
            case R.id.save:
                saveImage();
                return true;
            case R.id.print:
                doodleView.printImage();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // requests the permission
    private void saveImage() {
        if (getContext().checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {

            // explanation for permission
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(getActivity());

                builder.setMessage(R.string.permission_explanation);

                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        SAVE_IMAGE_PERMISSION_REQUEST_CODE);
                            }
                        }
                );

                builder.create().show();
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SAVE_IMAGE_PERMISSION_REQUEST_CODE);
            }
        } else {
            doodleView.saveImage();
        }
    }

    // called by the system when the user either grants or denies the permission for saving an image
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // requested permission
        switch (requestCode) {
            case SAVE_IMAGE_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    doodleView.saveImage();
                return;
        }
    }

    // returns the DoodleView
    public DoodleView getDoodleView() {
        return doodleView;
    }

    // indicates whether a dialog is displayed
    public void setDialogOnScreen(boolean visible) {
        dialogOnScreen = visible;
    }
}