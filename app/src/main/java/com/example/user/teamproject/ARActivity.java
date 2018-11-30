package com.example.user.teamproject;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * Created by Jason on 10/26/18.
 */

public class ARActivity extends AppCompatActivity implements SensorEventListener {
    SurfaceView cameraView;
    TextView direction;
    ImageView arrow;
    ImageView compass;
    ImageView pin;
    TextView distance;
    TextView targetDirection;
    CameraSource cameraSource;
    final int REQUEST_CAMERA_PERMISSION_ID = 1001;

    private static SensorManager sensorManager;
    private Sensor sensor;
    LocationManager locationManager;
    Context context;

    private float curDegree = 0f;
    private float curDegreeForN = 0f;
    private double degree;
    private double myLat;
    private double myLon;

    Double doubleTargetLat;
    Double doubleTargetLon;

    private Location myLocation = new Location("me");
    private Location target = new Location("target");

    // Ask for permissions for camera and GPS
    public void OnRequestPermissionsResultCallback(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION_ID:
                if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        context = this;
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);

        // Binding the views
        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        direction = (TextView) findViewById(R.id.direction);
        arrow = findViewById(R.id.arrow);
        pin = findViewById(R.id.pinImage);
        compass = findViewById(R.id.compass);
        pin.setVisibility(View.GONE);
        distance = findViewById(R.id.distance);
        targetDirection = findViewById(R.id.targetDirection);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Setup Camera on Surface View
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.w("AR", "We are in trouble");
        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1920, 1080)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true).
                            build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ARActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_ID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {

                }
            });
        }

        // Get the target device's location
        Intent intent = getIntent();
        String targetLat = intent.getStringExtra("targetLat");
        String targetLon = intent.getStringExtra("targetLon");
        String username = intent.getStringExtra("username");
        getSupportActionBar().setTitle(username + "'s location");

        doubleTargetLat = Double.valueOf(targetLat);
        doubleTargetLon = Double.valueOf(targetLon);

        target.setLatitude(doubleTargetLat);
        target.setLongitude(doubleTargetLon);

        // Setup the location manager to get the device location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                myLat = location.getLatitude();
                myLon = location.getLongitude();
                myLocation = location;
                distance.setText(calculateDistance(myLat, myLon, doubleTargetLat, doubleTargetLon));
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000,
                1, locationListenerGPS);
        isLocationEnabled();

        // Setup orientation sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    private void isLocationEnabled() {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Keep the sensor listening
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            Toast.makeText(this, "Not supported", Toast.LENGTH_SHORT).show();
        }

        isLocationEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Stop the sensor
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Get the angle between my device and north
        float degree = Math.round(event.values[0]);
        float degreeForN = degree;

        // Calculate the angle between my device to the target device
        if (myLocation != null) {
            float bearing = myLocation.bearingTo(target);
            GeomagneticField geomagneticField =
                    new GeomagneticField(Double.valueOf(myLat).floatValue(),
                            Double.valueOf(myLon).floatValue(),
                            Double.valueOf(myLocation.getAltitude()).floatValue(),
                            System.currentTimeMillis());
            degree -= geomagneticField.getDeclination();
            if (bearing < 0) {
                bearing += 360;
            }
            degree = bearing - degree;
            if (degree < 0) {
                degree += 360;
            }
        }

        // If my device is pointing towards the target device, we have a pin on camera
        // indicate the target device location
        if (degree <= 20 || degree >= 340) {
            pin.setVisibility(View.VISIBLE);
            pin.setY(400);
            if (degree > 0 && degree < 20) {
                pin.setX((float) (103 * degree + 1940) / 4);
            }
            if (degree >= 340 && degree <= 360) {
                pin.setX((float) (117 * degree - 40180) / 4);
            }
        } else {
            // If my device is not pointing towards the target device
            // we don't show the pin
            pin.setVisibility(View.GONE);
        }

        // Perform the arrow animation
        targetDirection.setText("Heading " + Float.toString(degree));
        RotateAnimation rotateAnimation = new RotateAnimation(curDegree, degree,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setFillAfter(true);
        arrow.startAnimation(rotateAnimation);

        RotateAnimation rotateAnimationCompass = new RotateAnimation(curDegreeForN, (float) -degreeForN,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimationCompass.setDuration(1000);
        rotateAnimationCompass.setFillAfter(true);
        compass.startAnimation(rotateAnimationCompass);

        // Update the angle in degree
        curDegree = degree;
        curDegreeForN = -degreeForN;
    }

    // Calculates the distance using the Haversine function
    private String calculateDistance(double lat, double lon, double myLat, double myLon) {
        double dLat = (myLat - lat) * (Math.PI / 180);
        double dLon = (myLon - lon) * (Math.PI / 180);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(lat * (Math.PI / 180)) * Math.cos(myLat * (Math.PI / 180))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = 6371 * c * 1000;
        return String.format("%.2f", d) + "m";

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    // Enable back button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
