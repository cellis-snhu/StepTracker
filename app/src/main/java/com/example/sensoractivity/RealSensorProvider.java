package com.example.sensoractivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


// Wraps the Android SensorManager and forwards accelerometer data to the Listener
public class RealSensorProvider implements AccelerometerDataProvider, SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private AccelerometerDataProvider.Listener listener;

    public RealSensorProvider(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void setListener(AccelerometerDataProvider.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (listener != null && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            listener.onData(event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }
}

