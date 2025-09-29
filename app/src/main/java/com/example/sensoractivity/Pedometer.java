package com.example.sensoractivity;

public class Pedometer {
    private int pointCount = 0;
    private int peakCount = 0;

    // last 3 magnitudes
    private Float prev = null;
    private Float curr = null;
    private Float next = null;

    // index of last detected peak
    private int lastPeakIndex = -1;

    public void processData(float x, float y, float z, int threshold, int distance) {
        // every incoming point increments point count
        pointCount++;

        // compute magnitude of acceleration vector
        float magnitude = (float) Math.sqrt(x*x + y*y + z*z);

        // shift the window
        prev = curr;
        curr = next;
        next = magnitude;

        // only check when all three values exist
        if (prev != null && curr != null && next != null) {
            // find peak, discard when below threshold
            if (curr > prev && curr > next && curr > threshold) {
                // check minimum distance
                if (lastPeakIndex == -1 || (pointCount - 2 - lastPeakIndex) >= distance) {
                    peakCount++;
                    lastPeakIndex = pointCount - 2; // -2 because curr is middle value
                }
            }
        }
    }

    public int getPeakCount() {
        return peakCount;
    }

    public int getPointCount() {
        return pointCount;
    }

    public void reset() {
        pointCount = 0;
        peakCount = 0;
        prev = curr = next = null;
    }
}