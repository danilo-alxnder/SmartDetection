package com.example.project_mod8;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class IndoorNavigationActivity extends AppCompatActivity  implements SensorEventListener {
    private static final String TAG = "IndoorNavigation";
    private double STEP_LENGTH = 0.78;
    private SensorManager sensorManager;
    GraphView graphViewAccel;
    GraphView graphViewGyro;
    LineGraphSeries<DataPoint> seriesXAccel;
    LineGraphSeries<DataPoint> seriesYAccel;
    LineGraphSeries<DataPoint> seriesZAccel;
    LineGraphSeries<DataPoint> seriesXGyro;
    LineGraphSeries<DataPoint> seriesYGyro;
    LineGraphSeries<DataPoint> seriesZGyro;
    double ax = 0.00, ay = 0.00, az = 0.00, gx = 0.00, gy = 0.00, gz = 0.00;
    final int dataPointsAmount = 80;
    double angle = 0.0;
    double meter = 0.0;
    int iteration = 0;
    TextView angleText;
    TextView meterText;
    Long lastTimeStamp;
    Long lastTimeStamp1;
    double resetDuration;
    double resetDuration1;
    boolean pauseAcc;
    private boolean activityPermissionGranted;
    private int PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 11252;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_navigation);

        angleText = (TextView) findViewById(R.id.indoorAngleTextView);
        meterText = (TextView) findViewById(R.id.indoorDistanceTextVIew);

        Button backButton = (Button) findViewById(R.id.backIndoorButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), MainActivity.class));
            }
        });
        pauseAcc = true;
        Button resetButton = (Button) findViewById(R.id.indoorNavigationResetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDuration1 = 0.0;
                meter = 0.0;
                meterText.setText(String.format("%.4f",meter));
                resetDuration = 0.0;
                angle = 0.0;
                angleText.setText(String.format("%.4f",angle));
                pauseAcc = true;
            }
        });

        activityPermissionGranted = false;
        getActivityPermission();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) == null) {
            Toast.makeText(IndoorNavigationActivity.this, "no step detector sensor", Toast.LENGTH_LONG).show();
            Log.i(TAG, "no step detector sensor");
        }
//        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected double[] lowPassFilter( double[] input, double[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 1.0 * (input[i] - output[i]);
        }
        return output;
    }

    private double[] gravity = new double[1];
    private double[] smoothed = new double[1];
    private boolean triggered = false;
    private double currentSample;
    private double previousSample =  DEFAULT_DOUBLE;
    private static final double DEFAULT_DOUBLE = -6969.6969;
    private double bottom = DEFAULT_DOUBLE*(-1), peak = DEFAULT_DOUBLE, bottom1 = DEFAULT_DOUBLE*(-1), pre_bot = DEFAULT_DOUBLE*(-1);
    private double bottomts = 0, peakts = 0, bottom1ts, pre_botts;
    private int steps = 0;
    private boolean found_peak = false;


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR));

    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void getActivityPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED) {
            activityPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
        }
    }
}