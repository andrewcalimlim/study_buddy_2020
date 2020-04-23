package com.example.studdybuddy.session;

import android.os.Handler;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class TimerRunner implements Runnable {

    private TextView counter;
    private AtomicInteger secondsPassed;
    private Handler parent;

    public TimerRunner(Handler parent, AtomicInteger seconds, TextView view) {
        super();

        this.counter = view;
        this.secondsPassed = seconds;
        this.parent = parent;
    }

    @Override
    public void run() {
        int time = secondsPassed.incrementAndGet();
        counter.setText(TimerManager.getTimeString(time));
        parent.postDelayed(this, 1000);
    }
}
