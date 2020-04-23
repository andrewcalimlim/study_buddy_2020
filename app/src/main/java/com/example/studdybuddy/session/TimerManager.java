package com.example.studdybuddy.session;

import android.os.Handler;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class TimerManager {
    private final AtomicInteger secondsPassed;
    private final Handler updateManager;

    private boolean isTimerRunning;

    private final TextView timer;

    private final Runnable runner;

    public static final int MILLIS_DELAY = 1000;

    public TimerManager(TextView timeview) {
        this.secondsPassed = new AtomicInteger(0);
        this.updateManager = new Handler();
        this.timer = timeview;

        this.isTimerRunning = false;

        // todo: make new class for this
        // todo: also give it a public function for parsing time :)
        this.runner = new Runnable() {
            public void run() {
                StringBuilder builder = new StringBuilder();
                int time = secondsPassed.getAndIncrement();
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

                timer.setText(builder.toString());

                updateManager.postDelayed(runner, 1000);
            }
        };
    }

    public void startTimer() {
        updateManager.postDelayed(runner,1000);
        isTimerRunning = true;
    }

    public void stopTimer() {
        updateManager.removeCallbacks(runner);
        isTimerRunning = false;
    }

    public int getTimerValue() {
        return secondsPassed.get();
    }
}
