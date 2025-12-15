package dev.scrythe.customlag;

import java.awt.*;

public class LatencyColors {
    // Inspiration from Better Ping Display


    private static final int PING_START = 0;
    private static final int PING_MID = 150;
    private static final int PING_END = 300;

    private static final Color COLOR_START = new Color(0x00E676);
    private static final Color COLOR_MID = new Color(0xD6CD30);
    private static final Color COLOR_END = new Color(0xE53935);

    private static double calculateLatencyRatio(int start, int end, int latency) {
        latency = Math.min(latency, end);
        return (double) (latency - start) / (end - start);
    }

    private static Color lerp(Color color_start, Color color_end, double latency_ratio) {
        int redDiff = color_end.getRed() - color_start.getRed();
        int greenDiff = color_end.getGreen() - color_start.getGreen();
        int blueDiff = color_end.getBlue() - color_start.getBlue();

        int newRed = (int) Math.round(color_start.getRed() + redDiff * latency_ratio);
        int newGreen = (int) Math.round(color_start.getGreen() + greenDiff * latency_ratio);
        int newBlue = (int) Math.round(color_start.getBlue() + blueDiff * latency_ratio);

        return new Color(newRed, newGreen, newBlue);
    }

    public static Color getColorOfLatency(int latency) {
        if (latency < PING_MID) {
            double latencyRatio = calculateLatencyRatio(PING_START, PING_MID, latency);
            return lerp(COLOR_START, COLOR_MID, latencyRatio);
        }
        double latencyRatio = calculateLatencyRatio(PING_MID, PING_END, latency);
        return lerp(COLOR_MID, COLOR_END, latencyRatio);
    }
}
