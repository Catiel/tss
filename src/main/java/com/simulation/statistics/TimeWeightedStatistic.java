package com.simulation.statistics;

public class TimeWeightedStatistic {
    private double sum;
    private double lastValue;
    private double lastTime;
    private double totalTime;

    public TimeWeightedStatistic() {
        this.sum = 0;
        this.lastValue = 0;
        this.lastTime = 0;
        this.totalTime = 0;
    }

    public void update(double value, double time) {
        if (time > lastTime) {
            sum += lastValue * (time - lastTime);
            totalTime = time - 0; // desde el inicio
            lastValue = value;
            lastTime = time;
        }
    }

    public double getAverage() {
        return totalTime > 0 ? sum / totalTime : 0;
    }

    public void reset() {
        sum = 0;
        lastValue = 0;
        lastTime = 0;
        totalTime = 0;
    }
}
