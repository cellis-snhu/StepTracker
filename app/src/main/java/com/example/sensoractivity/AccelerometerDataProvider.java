package com.example.sensoractivity;

// interface for accelerometer data sources (SensorManager or CSV Data)
public interface AccelerometerDataProvider {
    void start();
    void stop();
    void setListener(Listener listener);

    /* Functional listener used by MainActivity */
    interface Listener {
        void onData(float x, float y, float z);
    }
}

