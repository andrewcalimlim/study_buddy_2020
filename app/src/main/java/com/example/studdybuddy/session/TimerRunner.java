package com.example.studdybuddy.session;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class TimerRunner implements Runnable {

    private TextView counter;
    private AtomicInteger secondsPassed;
    private Handler parent;
    private Activity uiThread;

    public TimerRunner(Handler parent, AtomicInteger seconds, TextView view, Activity activity) {
        super();

        this.counter = view;
        this.secondsPassed = seconds;
        this.parent = parent;
        this.uiThread = activity;
    }

    public void changeTextView(TextView view) {
        this.counter = view;
    }

    @Override
    public void run() {
        final int time = secondsPassed.incrementAndGet();
        Log.e("current time", ((Integer)time).toString());
        Log.e("hc time", ((Integer)counter.hashCode()).toString());

        uiThread.runOnUiThread(new Runnable() {
            public void run() {
                counter.setText(TimerManager.getTimeString(time));
            }
        });

        counter.setText(TimerManager.getTimeString(time));
        parent.postDelayed(this, 1000);
    }
}
