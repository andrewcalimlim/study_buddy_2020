package com.example.studdybuddy.session;

import android.app.Activity;
import android.os.Handler;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class TimerManager {
    private final AtomicInteger secondsPassed;
    private final Handler updateManager;

    private boolean isTimerRunning;

    private TextView timer;

    private final TimerRunner runner;

    private long lastTimerValue;
    private boolean isSleeping;

    public static final int MILLIS_DELAY = 1000;

    public TimerManager(TextView timeview, Activity activity) {
        this.secondsPassed = new AtomicInteger(0);
        this.updateManager = new Handler();
        this.timer = timeview;
        this.isSleeping = false;
        this.lastTimerValue = System.currentTimeMillis();

        runner = new TimerRunner(this.updateManager, this.secondsPassed, this.timer, activity);

        this.isTimerRunning = false;

        // todo: make new class for this
        // todo: also give it a public function for parsing time :)
    }

    public void changeTextView(TextView view) {
        runner.changeTextView(view);
    }

    public void startTimer() {
        if (!isTimerRunning) {
            if (isSleeping) {
                // restore value
                int delta = (int)((System.currentTimeMillis() - lastTimerValue) / 1000);
                secondsPassed.set(secondsPassed.get() + delta);
                isSleeping = false;
            }
            updateManager.postDelayed(runner,1000);
            isTimerRunning = true;
        }
    }

    public void stopTimer() {
        updateManager.removeCallbacks(runner);
        isTimerRunning = false;
    }

    public void sleepTimer() {
        if (isTimerRunning) {
            lastTimerValue = System.currentTimeMillis();
            isSleeping = true;
            stopTimer();
        }
    }

    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    public boolean isTimerSleeping() {
        return isSleeping;
    }

    public void setTimer(int newValue) {
        secondsPassed.set(newValue);
    }

    public int getTimerValue() {
        return secondsPassed.get();
    }

    public String getTimerValueAsString() {
        return getTimeString(secondsPassed.get());
    }

    public static String getTimeString(int time) {

        StringBuilder builder = new StringBuilder();
        if (time < 36000) {
            builder.append(0);
        }

        builder.append(time / 3600);
        builder.append(":");

        time = time % 3600;

        if (time < 600) {
            builder.append(0);
        }

        builder.append(time / 60);
        builder.append(":");

        time = time % 60;

        if (time < 10) {
            builder.append(0);
        }

        builder.append(time);

        return builder.toString();
    }
}
