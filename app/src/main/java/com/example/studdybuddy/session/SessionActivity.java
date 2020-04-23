package com.example.studdybuddy.session;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studdybuddy.R;

/**
 * The session activity represents the appearance and behavior of a productivity session in progress.
 * This class will deal with tracking the user's session duration, as well as spawning relevant notifications
 * over the course of the session.
 *
 * (only writing the stuff below because i've never heard it)
 *
 * Android activities generally end up representing a single screen. There is an "Activity Stack"
 * or "back stack" which contains all of these. Hitting the back button on an android device, then,
 * ends the current activity and displays the next one on a stack. Starting a new activity
 * will add that activity to the back stack.
 *
 * In this way, a stack can be seen as a representation of "depth". For instance, we may want to have
 * a "home screen" for our app, then nest the calendar, pet, and session views down one level. We
 * can split this up further using transitions to abstract swaps between activities.
 */
public class SessionActivity extends AppCompatActivity {

    private TimerManager updater;
    private NotificationChannelGroup notificationChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_view);
        final TextView timer = findViewById(R.id.timer);

        updater = new TimerManager(timer);

        updater.startTimer();

        notificationChannel = new NotificationChannelGroup();

    }

    @Override
    protected void onStop() {
        super.onStop();
        String display = TimerManager.getTimeString(updater.getTimerValue());
        NotificationManager notifMgr = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);


    }

    // create an onStop call which displays a notification containing the current timer value
}
