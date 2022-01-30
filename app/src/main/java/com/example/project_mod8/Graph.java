package com.example.project_mod8;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Graph extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private static final String TAG = "Graph";
    GraphView graphViewAccel;
    GraphView graphViewGyro;
//    private static double STEP_THRESH_HOLD = 12.0;
    private static double STEP_THRESH_HOLD = 3.8;
    LineGraphSeries<DataPoint> seriesXAccel;
    LineGraphSeries<DataPoint> seriesYAccel;
    LineGraphSeries<DataPoint> seriesZAccel;
    LineGraphSeries<DataPoint> linearMagnitudeSeries;
    LineGraphSeries<DataPoint> seriesXGyro;
    LineGraphSeries<DataPoint> seriesYGyro;
    LineGraphSeries<DataPoint> seriesZGyro;
    double ax = 0.00, ay = 0.00, az = 0.00, gx = 0.00, gy = 0.00, gz = 0.00, magnitude1 = 0.00, magnitude2 = 0.00;
    final int dataPointsAmount = 80;
    double angle = 0.0;
    double meter = 0.0;
    int iteration = 0;
    TextView angleText;
    TextView targetText;
    TextView instructionText;
    Long lastTimeStamp;
    Long lastTimeStamp1;
    double resetDuration;
    double resetDuration1;
    boolean activity_active;
    List<Instruction> instructionList;

    private String roomID;
    private DatabaseReference RoomRef;
    private Room roomFinal;
    private ValueEventListener treeValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        activity_active = true;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        targetText = (TextView) findViewById(R.id.targetView);
        instructionText = (TextView) findViewById(R.id.instructionView);
        instructionList = new ArrayList<>();

        targetText.setText("Hello");
        instructionText.setText("starting task");

        final Button startButton = (Button) findViewById(R.id.startRecording);
        final Button resetButton = (Button) findViewById(R.id.stopRecording);
        startButton.setEnabled(false);
        resetButton.setEnabled(false);

        roomID = getIntent().getStringExtra("roomID");
        RoomRef = FirebaseDatabase.getInstance().getReference("Rooms/" + roomID +"/instructions");
        treeValue = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("Graph","Data loaded");
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    String value = snapshot.getValue(String.class);
                    if (value != null) {
                        String[] tmp = value.split("/");
                        if (tmp[0].equals("walk")) {
                           instructionList.add(new Instruction(Instruction.InsType.WALK, 0, Double.valueOf(tmp[1])));
                        } else if (tmp[0].equals("turn")) {
                          instructionList.add(new Instruction(Instruction.InsType.TURN, Double.valueOf(tmp[1]), 0));
                        } else {
                            instructionList.add(new Instruction(Instruction.InsType.FIN, 0, 0));
                        }
                    }

                }
                startButton.setEnabled(true);
//                RoomRef.removeEventListener(treeValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("Graph","remove tree listener");
            }
        };

        RoomRef.addListenerForSingleValueEvent(treeValue);

//        instructionList.add(new Instruction(Instruction.InsType.WALK, 0, 9.5));
//        instructionList.add(new Instruction(Instruction.InsType.TURN, 90, 0));
//        instructionList.add(new Instruction(Instruction.InsType.WALK, 0, 3));
//        instructionList.add(new Instruction(Instruction.InsType.FIN, 0, 0));
//        seriesXAccel = getInitializeGraphSeries();
//        seriesXAccel.setColor(Color.rgb(255, 0, 0));
//        graphViewAccel.addSeries(seriesXAccel);
//        seriesYAccel = getInitializeGraphSeries();
//        seriesYAccel.setColor(Color.rgb(0, 255, 0));
//        graphViewAccel.addSeries(seriesYAccel);
//        seriesZAccel = getInitializeGraphSeries();
//        seriesZAccel.setColor(Color.rgb(0, 0, 255));
//        graphViewAccel.addSeries(seriesZAccel);

//        linearMagnitudeSeries = getInitializeGraphSeries();
//        linearMagnitudeSeries.setColor(Color.rgb(255, 0, 0));
//        graphViewAccel.addSeries(linearMagnitudeSeries);


        //gyro
//        graphViewGyro = (GraphView) findViewById(R.id.graphGyro);
//        setDefaultSettingsForGraph(graphViewGyro);
//
//        seriesXGyro = getInitializeGraphSeries();
//        seriesXGyro.setColor(Color.rgb(220, 220, 0));
//        graphViewGyro.addSeries(seriesXGyro);
//        seriesYGyro = getInitializeGraphSeries();
//        seriesYGyro.setColor(Color.rgb(220, 0, 220));
//        graphViewGyro.addSeries(seriesYGyro);
//        seriesZGyro = getInitializeGraphSeries();
//        seriesZGyro.setColor(Color.rgb(0, 220, 220));
//        graphViewGyro.addSeries(seriesZGyro);
//
//        runPlotter();
//        runSomething();
    }

    private int currentIns = -1;
    private double goalDistance;
    private double walkedDistance;

    private double goalAngel;
    private boolean angleStarting;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void preRun() {
        instructionText.setText("Loading...");
        targetText.setText("getting next task");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms

                runNextInstuction();
            }
        }, 1000);
    }

    private void runNextInstuction() {
        currentIns++;
        if (currentIns >= instructionList.size()) {
            return;
        }
        Instruction instruction = instructionList.get(currentIns);
        if (instruction.getType() == Instruction.InsType.WALK) {
            starting = true;
            steps = 0;
            goalDistance =  instruction.getMeter();
            walkedDistance = 0;
            instructionText.setText("Walk straight for " + goalDistance);
            targetText.setText("Walked: " + 0 + "/" + goalDistance);
        } else if (instruction.getType() == Instruction.InsType.TURN) {
            angle = 0;
            targetText.setText("Turn: " + 0 + "/" + goalAngel);
            goalAngel = angle + instruction.getAngle();
            angleStarting = true;
            instructionText.setText("Turn " + goalAngel + " degree");

        } else if (instruction.getType() == Instruction.InsType.FIN) {
            instructionText.setText("Congrats");
            targetText.setText("You are at your room");
        }
    }

    protected double[] lowPassFilter( double[] input, double[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 1.0 * (input[i] - output[i]);
        }
        return output;
    }

    private static final double STEP_LENGTH = 0.7;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            ax = sensorEvent.values[0];
            ay = sensorEvent.values[1];
            az = sensorEvent.values[2];


            magnitude1 = Math.sqrt(ax*ax + ay*ay + az*az);
            gravity[0] = magnitude1;
            lowPassFilter(gravity, smoothed);

            currentSample = smoothed[0];



            long diff = System.currentTimeMillis() - startingTimeStamp;
//            if (starting) {
//                writeToFile(diff + "/" + currentSample + "/" + magnitude1);
//            }

            if (starting && previousSample != DEFAULT_DOUBLE) {

                if (currentSample > STEP_THRESH_HOLD) {
                    if (!triggered) {
                        triggered = true;
//                        Log.i(TAG, "found pre-step");
                    }
                    if (!found_peak && currentSample > previousSample) {
                        peak = currentSample;
                        peakts = diff;
                    } else if (!found_peak && currentSample <previousSample){
//                        Log.i(TAG, "found peak");
                        found_peak = true;
                    }

                }
                if (found_peak && triggered && (currentSample < previousSample)) {
                    bottom1 = Math.min(bottom1, currentSample);
                    if (bottom1 == currentSample)
                        bottom1ts = diff;
                } else if (found_peak && triggered && (currentSample > previousSample)) {
                    if (peak != DEFAULT_DOUBLE) {
                        Log.i(TAG, "found step " + ",duration: " + (bottom1ts - pre_botts) + ", peak: " + peak);
//                        writeToFile("Log: " + pre_bot + "/" + pre_botts + "/" + peak + "/" + peakts + "/" +bottom1 + "/" +bottom1ts);
                        steps++;
//                        if (((bottom1ts - pre_botts) > 100)) {
//                            walkedDistance += STEP_LENGTH;
//                        }
                        walkedDistance += STEP_LENGTH;
                        if (walkedDistance > goalDistance ) {
                            targetText.setText("Congrats you finished this instruction");
                            preRun();
                            starting = false;
                            return;
                        }
                        BigDecimal bd = new BigDecimal(Double.toString(walkedDistance));
                        bd = bd.setScale(2, RoundingMode.HALF_UP);

                        String tmp = "Walked: " + bd.doubleValue() + "/" + goalDistance;
                        targetText.setText(tmp);
                    }
                    triggered = false;
                    found_peak = false;
                    bottom = DEFAULT_DOUBLE*(-1);
                    peak = DEFAULT_DOUBLE;
                    bottom1 = DEFAULT_DOUBLE*(-1);

                } else if (!found_peak && !triggered && currentSample < previousSample) {
                    bottom = Math.min(bottom, currentSample);
                    if (bottom == currentSample)
                        bottomts = diff;
                } else if (!found_peak && !triggered && currentSample > previousSample) {
                    pre_botts = bottomts;
                    pre_bot = bottom;
                    bottom = DEFAULT_DOUBLE*(-1);
                }
                previousSample = currentSample;
            } else {
                previousSample = currentSample;
            }


        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (angleStarting) {
                if (lastTimeStamp == null) {
                    lastTimeStamp = System.currentTimeMillis();
                    return;
                }
                Long thisTimeStamp = System.currentTimeMillis();
                Double duration = (thisTimeStamp - lastTimeStamp)/1000.0;
                lastTimeStamp = thisTimeStamp;
//            System.out.println(gz);
                if (Math.abs(gz) > 2)
                    angle -= gz * duration;
                    BigDecimal bd = new BigDecimal(Double.toString(angle));
                    bd = bd.setScale(2, RoundingMode.HALF_UP);
                    targetText.setText("Turn: " + bd + "/" + goalAngel);
                    if (Math.abs(angle) > Math.abs(goalAngel)) {
                        targetText.setText("Congrats you finished this instruction");
                        preRun();
                        angleStarting = false;
                        return;
                    }

//            System.out.println(resetDuration);



                gx = sensorEvent.values[0]*180/3.14;
                gy = sensorEvent.values[1]*180/3.14;
                gz = sensorEvent.values[2]*180/3.14;
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void setDefaultSettingsForGraph(GraphView graphView) {
        graphView.setBackgroundColor(getResources().getColor(android.R.color.white));
//        graphView.getViewport().setScalableY(true);
//        graphView.getViewport().setXAxisBoundsManual(true);
//        graphView.getViewport().setMaxX(dataPointsAmount);
        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity_active = false;
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));

    }

    private LineGraphSeries<DataPoint> getInitializeGraphSeries()
    {
        return new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 0),
        });
    }

    private String file_name;
    private File gpxfile;
    private FileWriter fileWriter;
    private long startingTimeStamp;
    private boolean starting = false;
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onClickStart(View view) {
        Button startButton = (Button) findViewById(R.id.startRecording);
        startButton.setEnabled(false);
        Button resetButton = (Button) findViewById(R.id.stopRecording);
        resetButton.setEnabled(true);
        preRun();
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
//        LocalDateTime now = LocalDateTime.now();
//        file_name = dtf.format(now);
//        System.out.println("file name: " + file_name);
//        starting = true;
//        File file = new File(Graph.this.getFilesDir(), "text");
//
//        if (!file.exists()) {
//            file.mkdir();
//        }
//        try {
//            gpxfile = new File(file, file_name + ".txt");
//            fileWriter = new FileWriter(gpxfile);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void writeToFile(String text) {
        try {
            fileWriter.append(text).append("\n");
            fileWriter.flush();
            Log.i(TAG,"Write: " + text);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onClickStop(View view) throws IOException {
//        fileWriter.close();

//        Log.i(TAG,"close file: " + gpxfile);
        Button startButton = (Button) findViewById(R.id.startRecording);
        startButton.setEnabled(true);
        Button resetButton = (Button) findViewById(R.id.stopRecording);
        resetButton.setEnabled(false);
        currentIns = -1;
        starting = false;
        angleStarting = false;

        bottom = DEFAULT_DOUBLE*(-1);
        peak = DEFAULT_DOUBLE;
        bottom1 = DEFAULT_DOUBLE*(-1);
        triggered = false;
        found_peak = false;

    }
}
