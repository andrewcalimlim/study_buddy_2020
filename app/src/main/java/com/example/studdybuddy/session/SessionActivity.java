package com.example.studdybuddy.session;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private enum NotificationType {
        PAUSE,
        REMIND,
        NONE
    }

    private TimerManager updater;
    private NotificationChannel notificationChannel;

    private NotificationType stopSignal;

    public static final String OLD_TIMER_VAL = "oldTimerVal";
    public static final String IS_TIMER_RUNNING = "isTimerRunning";
    public static final String TIMER_STOP_TIME = "timerStopTime";
    public static final String NOTIF_CHANNEL_NAME = "StudyBuddySessions";

    public static final int TIMER_NOTIF_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.session_view);
        TextView timer = findViewById(R.id.timer);
        updater = new TimerManager(timer, this);

        NotificationManager notifMgr = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);

        assert notifMgr != null;

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            notificationChannel = new NotificationChannel(NOTIF_CHANNEL_NAME, NOTIF_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notifMgr.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean bgRun = savedInstanceState.getBoolean(IS_TIMER_RUNNING);
        int delta = savedInstanceState.getInt(OLD_TIMER_VAL, 0);
        if (bgRun) {
            // restore value from previous state
            delta += (int)((System.currentTimeMillis() - savedInstanceState.getLong(TIMER_STOP_TIME, System.currentTimeMillis())) / 1000);
        }

        updater.setTimer(delta);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if we restored, the timer is set up
        // if we did not, the timer is sleeping
        // either way, calling startTimer will be correct.

        // something weird happens after the emulated device turns off/on twice
        // all of the components are working
        // it appears to work otherwise but this alone is a problem
        updater.startTimer();
        Log.e("state", ((Integer)updater.getTimerValue()).toString());
        ((TextView)findViewById(R.id.timer)).setText(updater.getTimerValueAsString());
    }

    @Override
    public void onBackPressed() {
        updater.stopTimer();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        PowerManager powerMgr = (PowerManager)getSystemService(POWER_SERVICE);
        assert powerMgr != null;

        // no guarantee whether this will be called before or after onStop due to current target
        // so I'm just accounting for both cases by doing the work twice
        outState.putInt(OLD_TIMER_VAL, updater.getTimerValue());
        outState.putLong(TIMER_STOP_TIME, System.currentTimeMillis());
        // good stop case
        if (!powerMgr.isInteractive()) {
            outState.putBoolean(IS_TIMER_RUNNING, true);
        } else {
            outState.putBoolean(IS_TIMER_RUNNING, false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        PowerManager powerMgr = (PowerManager)getSystemService(POWER_SERVICE);
        assert powerMgr != null;
        if (!powerMgr.isInteractive()) {
            Log.e("test", "ouch!");
            stopSignal = NotificationType.REMIND;
            updater.sleepTimer();
        } else if (updater.isTimerRunning()) {
            stopSignal = NotificationType.PAUSE;
        } else {
            stopSignal = NotificationType.NONE;
        }

        updater.stopTimer();
        // create pause notification if relevant
        NotificationManager notifMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        assert notifMgr != null;
        Notification result;
        switch (stopSignal) {
            case PAUSE:
                result = createPauseNotification();
                notifMgr.notify(TIMER_NOTIF_ID, result);
                break;
            case REMIND:
                result = createRemindNotification();
                break;
            default:
                result = null;
                break;
        }


    }

    private Notification createPauseNotification() {
        Notification.Builder builder = getBuilder();

        Intent reopenSession = new Intent(this, this.getClass());
        PendingIntent reopenPending = PendingIntent.getActivity(this, TIMER_NOTIF_ID, reopenSession, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentTitle("SESSION PAUSED")
                .setContentText(updater.getTimerValueAsString())
                .setSmallIcon(R.drawable.cattron_superscale)
                .setContentIntent(reopenPending)
                .setAutoCancel(true);

        return builder.build();

    }

    private Notification createRemindNotification() {
        Notification.Builder builder = getBuilder();

        Intent reopenSession = new Intent(this, this.getClass());
        PendingIntent reopenPending = PendingIntent.getActivity(this, TIMER_NOTIF_ID, reopenSession, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentTitle("RETURN TO SESSION")
                .setContentText(updater.getTimerValueAsString())
                .setSmallIcon(R.drawable.cattron_superscale)
                .setContentIntent(reopenPending)
                .setAutoCancel(true);

        return builder.build();
    }

    private Notification.Builder getBuilder() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new Notification.Builder(this, NOTIF_CHANNEL_NAME);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder;
    }
}
