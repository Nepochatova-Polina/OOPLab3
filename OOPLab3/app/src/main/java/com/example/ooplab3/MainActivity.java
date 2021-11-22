package com.example.ooplab3;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private HashMap<String, List<TimeRecord>> storedGestures;
    private final List<TimeRecord> recordedData = new ArrayList<>();
    private Boolean isRecordingGesture = false;

    private LocalDateTime startedRecordingTime = LocalDateTime.now();
    private Sensor sensor = null;

    private AlertDialog setGestureNameDialog;
    private AlertDialog showRecognizedGestureAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerator = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorManager.registerListener(new AccelerometerEventListener(), accelerator, 50000);

        EditText inputEditTextField = new EditText(this);

        showRecognizedGestureAlert = new AlertDialog.Builder(MainActivity.this)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .create();

        setGestureNameDialog = new AlertDialog.Builder(this)
                .setTitle("Gesture name")
                .setMessage("Enter gesture name")
                .setView(inputEditTextField)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    String gestureName = inputEditTextField.getText().toString();
                    storeGesture(gestureName);
                })
                .setNegativeButton("Cancel", null)
                .create();


        storedGestures = new HashMap<>();

        findViewById(R.id.recordGesture).setOnClickListener(this::onRecordGesturePress);
        findViewById(R.id.recognizeGesture).setOnClickListener(this::recognizeGesture);
        findViewById(R.id.storeGesture).setOnClickListener(this::onStoreGestureClick);
    }

    private void onStoreGestureClick(View v) {
        setGestureNameDialog.show();
    }

    private void storeGesture(String name) {
        Log.d("d", "Add new gesture");
        List<TimeRecord> newList = new ArrayList<>(recordedData);
        storedGestures.put(name, newList);
    }

    private void recognizeGesture(View v) {
        HashMap<String, Float> mostPossibleGestures = new HashMap<>();

        for (Map.Entry<String, List<TimeRecord>> entry : storedGestures.entrySet()) {
            List<Float> xStoredPoints = entry.getValue().stream()
                    .map(point -> point.x)
                    .collect(Collectors.toList());
            List<Float> xRecordedPoints = recordedData.stream()
                    .map(point -> point.x)
                    .collect(Collectors.toList());

            float xError = DTW.distance(xStoredPoints, xRecordedPoints);

            List<Float> yStoredPoints = entry.getValue().stream()
                    .map(point -> point.y)
                    .collect(Collectors.toList());
            List<Float> yRecordedPoints = recordedData.stream()
                    .map(point -> point.y)
                    .collect(Collectors.toList());

            float yError = DTW.distance(yStoredPoints, yRecordedPoints);

            List<Float> zStoredPoints = entry.getValue().stream()
                    .map(point -> point.z)
                    .collect(Collectors.toList());
            List<Float> zRecordedPoints = recordedData.stream()
                    .map(point -> point.z)
                    .collect(Collectors.toList());

            float zError = DTW.distance(zStoredPoints, zRecordedPoints);
            Log.d("d","Calculated errors: x=" + xError + "; y=" + yError + "; z=" + zError);
            //if errors pass threshold
            mostPossibleGestures.put(entry.getKey(), xError * yError * zError);
        }

        if(mostPossibleGestures.isEmpty()) {
            showRecognizedGestureAlert.setTitle("Gesture not recognized");
            showRecognizedGestureAlert.setMessage(":(");
            showRecognizedGestureAlert.show();
            return;
        }

        String gestureName = null;
        float minError = Float.MAX_VALUE;
        for (Map.Entry<String, Float> possibleGesture : mostPossibleGestures.entrySet()) {
            System.out.println(possibleGesture.getKey());
            if (minError > possibleGesture.getValue()) {
                minError = possibleGesture.getValue();
                gestureName = possibleGesture.getKey();
            }
        }

        showRecognizedGestureAlert.setTitle("Gesture recognized");
        showRecognizedGestureAlert.setMessage(gestureName);
        showRecognizedGestureAlert.show();
    }

    @SuppressLint("SetTextI18n")
    private void onRecordGesturePress(View view) {
        Button button = (Button) view;
        if (isRecordingGesture) {
            button.setText("Tap to start recording gesture");
            stopRecordingGesture();
        } else {
            button.setText("Tap to stop recording gesture");
            startRecordingGesture();
        }
    }

    private void startRecordingGesture() {
        recordedData.clear();
        startedRecordingTime = LocalDateTime.now();
        isRecordingGesture = true;
    }

    private void stopRecordingGesture() {
        isRecordingGesture = false;
        displayRecordedData();
    }

    private void displayRecordedData() {
        List<Entry> xEntries = new ArrayList<>();
        List<Entry> yEntries = new ArrayList<>();
        List<Entry> zEntries = new ArrayList<>();
        for (TimeRecord point: recordedData) {
            xEntries.add(new Entry(point.ms, point.x));
            yEntries.add(new Entry(point.ms, point.y));
            zEntries.add(new Entry(point.ms, point.z));
        }
        LineDataSet xSet = new LineDataSet(xEntries, "X");
        xSet.setColor(Color.RED);

        LineDataSet ySet = new LineDataSet(yEntries, "Y");
        ySet.setColor(Color.BLUE);

        LineDataSet zSet = new LineDataSet(zEntries, "Z");
        zSet.setColor(Color.CYAN);

        LineData lineData = new LineData();
        lineData.addDataSet(xSet);
        lineData.addDataSet(ySet);
        lineData.addDataSet(zSet);
        lineData.setDrawValues(false);

        LineChart lineChart = (LineChart) findViewById(R.id.graph);
        lineChart.setData(lineData);

        lineChart.invalidate();
    }

    private class AccelerometerEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {

            if (isRecordingGesture) {
                TimeRecord point = new TimeRecord();
                point.x = event.values[0];
                point.y = event.values[1];
                point.z = event.values[2];
                point.ms = Duration.between(startedRecordingTime, LocalDateTime.now()).toMillis();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }
}