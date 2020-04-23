package com.example.studdybuddy.session;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
    private NotificationChannel notificationChannel;

    public static final String OLD_TIMER_VAL = "oldTimerVal";
    public static final String NOTIF_CHANNEL_NAME = "StudyBuddySessions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.session_view);
        TextView timer = findViewById(R.id.timer);
        updater = new TimerManager(timer);

        Intent startContext = getIntent();
        updater.setTimer(startContext.getIntExtra(OLD_TIMER_VAL, 0));
        timer.setText(updater.getTimerValueAsString());

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            notificationChannel = new NotificationChannel(NOTIF_CHANNEL_NAME, NOTIF_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notifMgr = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
            assert notifMgr != null;
            notifMgr.createNotificationChannel(notificationChannel);
        }

        updater.startTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        String display = TimerManager.getTimeString(updater.getTimerValue());
        NotificationManager notifMgr = (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        updater.stopTimer();

        // note: this is depreciated since API26 -- since our minver is API24 it still works but :/
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT < 26) {
            builder = new Notification.Builder(getApplicationContext());
        } else {
            builder = new Notification.Builder(getApplicationContext(), NOTIF_CHANNEL_NAME);
        }

        builder.setContentTitle("world of grapes")
                .setContentText("PAUSED AT " + TimerManager.getTimeString(updater.getTimerValue()))
                .setSmallIcon(R.drawable.cattron_superscale)
                .setAutoCancel(true);

        Intent reopenSession = new Intent(this, SessionActivity.class);
        reopenSession.putExtra(OLD_TIMER_VAL, updater.getTimerValue());

        PendingIntent pendSession = PendingIntent.getActivity(this, 12, reopenSession, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendSession);

        Log.e("heads up!", "stopped");

        assert notifMgr != null;
        notifMgr.notify(12, builder.build());




    }

    // create an onStop call which displays a notification containing the current timer value
}
