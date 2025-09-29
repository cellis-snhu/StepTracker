package com.example.sensoractivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Replays a csv file of accelerometer data
 */
public class CsvSensorProvider implements AccelerometerDataProvider {
    private final List<float[]> data = new ArrayList<>();
    private volatile boolean running = false;
    private Thread workerThread;
    private AccelerometerDataProvider.Listener listener;
    private long intervalMs = 5; // replay speed interval set by setReplaySpeed

    public CsvSensorProvider(InputStream inputStream) {
        loadCsv(inputStream);
    }

    private void loadCsv(InputStream inputStream) {
        if (inputStream == null) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { // skip header
                    firstLine = false;
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] tokens = line.split(",");
                if (tokens.length < 4) continue;
                try {
                    float x = Float.parseFloat(tokens[1].trim());
                    float y = Float.parseFloat(tokens[2].trim());
                    float z = Float.parseFloat(tokens[3].trim());
                    data.add(new float[]{x, y, z});
                } catch (NumberFormatException ignored) {
                    // error on any broken line
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setListener(AccelerometerDataProvider.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        if (running || data.isEmpty()) return;
        running = true;
        workerThread = new Thread(() -> {
            int idx = 0;
            while (running && idx < data.size()) {
                float[] v = data.get(idx++);
                if (listener != null) {
                    listener.onData(v[0], v[1], v[2]);
                }
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            running = false;
        }, "CsvSensorProvider-Thread");
        workerThread.start();
    }

    @Override
    public void stop() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
            workerThread = null;
        }
    }

    // optionally set the replay rate (speed)
    public void setReplaySpeed(long ms) { this.intervalMs = Math.max(1, ms); }
}
