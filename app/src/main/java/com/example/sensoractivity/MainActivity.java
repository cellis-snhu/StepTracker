package com.example.sensoractivity;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // setup sensor manager and accelerometer sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView accelerometerTextView;
    private AccelerometerDataProvider provider;

    // toggle useCsvData to run a CSV of format: time_step, float_x, float_y, float_z
    // that is accelerometer data
    private boolean useCsvData = true;

    private Pedometer pedometer;
    private TextView pointCountTextView;

    private TextView peakCountTextView;

    private int threshold = 11;
    // minimum distance between detected peaks (removes noisy peaks)
    // 5 seems like a good minimum based on viewing the SVG output in a graph
    private int distance = 5;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        accelerometerTextView = findViewById(R.id.accelerometerDataText);
        pointCountTextView = findViewById(R.id.pointCountText);
        peakCountTextView = findViewById(R.id.peakCountText);

        pedometer = new Pedometer();

        // Choose data source
        if (useCsvData) {
            InputStream csvStream = getResources().openRawResource(R.raw.pedometer_sample); // put CSV in res/raw
            provider = new CsvSensorProvider(csvStream);
        } else {
            provider = new RealSensorProvider(this);
        }

        // Set listener for incoming accelerometer data
        provider.setListener((x, y, z) -> {
            // process the data (updates counts)
            pedometer.processData(x, y, z, threshold, distance);

            // snapshot counts
            final int points = pedometer.getPointCount();
            final int peaks = pedometer.getPeakCount();
            final String accelData = String.format("X: %.2f\nY: %.2f\nZ: %.2f", x, y, z);

            // update UI on the main thread
            runOnUiThread(() -> {
                accelerometerTextView.setText(accelData);
                pointCountTextView.setText("Data Points: " + points);
                peakCountTextView.setText("Steps (peaks): " + peaks);
            });
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        // register listener onResume to start capturing data when activity resumes
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        provider.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister listener onPause to save power
        //        sensorManager.unregisterListener(this);
        provider.stop();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Read accelerometer values for each axis
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // store accelerometer data in string
            String accelerometerData = String.format("X: %.2f\nY: %.2f\nZ: %.2f", x, y, z);

            // Update the TextView to display accelerometer data
            TextView accelerometerTextView = findViewById(R.id.accelerometerDataText);
            accelerometerTextView.setText(accelerometerData);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}