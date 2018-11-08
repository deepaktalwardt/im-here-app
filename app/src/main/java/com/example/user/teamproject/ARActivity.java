package com.example.user.teamproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;


/**
 * Created by Jason on 10/26/18.
 */

public class ARActivity extends AppCompatActivity implements SensorEventListener {
    SurfaceView cameraView;
    TextView textView;
    ImageView arrow;
    CameraSource cameraSource;
    final int REQUEST_CAMERA_PERMISSION_ID = 1001;

    private static SensorManager sensorManager;
    private  Sensor sensor;

    private  float curDegree = 0f;
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

        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        textView = (TextView) findViewById(R.id.text_view);
        arrow = findViewById(R.id.arrow);
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.w("AR", "We fucked up");
        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
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
                    //textView.setText("I saw it!!!!");
//                    final SparseArray<TextBlock> item1 = detections.getDetectedItems();
//                    if (item1.size() != 0) {
//                        boolean post = textView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                StringBuilder sb = new StringBuilder();
//                                for (int i = 0; i < item1.size(); i++) {
//                                    TextBlock item = item1.ValueAt(i);
//                                    sb.append(item.getValue());
//                                    sb.append("\n");
//                                }
//                                textView.setText(sb.toString());
//                            }
//                        });
//                    }

                }
            });
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Toast.makeText(this, "Not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int degree = Math.round(event.values[0]);
        String direciton = "";
        if (degree <= 20 || degree > 335) {
            direciton = "N";
        } else if (degree > 20 && degree <= 65) {
            direciton = "NE";
        } else if (degree > 65 && degree <= 110) {
            direciton = "E";
        } else if (degree > 110 && degree <= 155) {
            direciton = "SE";
        } else if (degree > 155 && degree <= 200) {
            direciton = "S";
        } else if (degree > 200 && degree <= 245) {
            direciton = "SW";
        } else if (degree > 245 && degree <= 290) {
            direciton = "W";
        } else if (degree > 290 && degree <= 335) {
            direciton = "NW";
        } else {
            direciton = "";
        }
        textView.setText(Integer.toString(degree) + (char) 0x00B0 + "  " + direciton);

        RotateAnimation rotateAnimation = new RotateAnimation(curDegree, -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setFillAfter(true);
        arrow.startAnimation(rotateAnimation);
        curDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
