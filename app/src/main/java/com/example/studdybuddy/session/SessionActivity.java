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

import com.example.studdybuddy.BuildConfig;
import com.example.studdybuddy.MainActivity;
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

    public static final int TIMER_NOTIF_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.session_view);
        TextView timer = findViewById(R.id.timer);
        updater = new TimerManager(timer);
    }

    // todo: the timer notifies us when the back button is pressed to end a session
    //       only trigger when the app is left
    //       add'l: account for when the phone is turned off
    //       and add a broadcast manager which registers at this time
    //       and fires if the phone is turned back on
    //       and the app is not opened right away
    //          - cheat it with a slight delay (a couple seconds)
    //          - clear it if the app opens before then (onResume)
    // ticking in the background
    //    stop immediately for now when the app is left intentionally
    //    if in the background, save the current time into the bundle
    //    we can figure out how the app was closed via the power manager
    //       - back button: do nothing (end the session)
    //       - home button: notify
    //       - power button: keep tracking
    //          - save bundle
    //          - flip a flag which onRestore can pick up on
    //          - tell onRestore whether we're still in a session or not
    //          - the user doesn't have to follow our receiver
    //          - use the alarm + receiver to communicate a "stop time" to the application
    //   the timer manager will close shortly so we cant depend on it

    // dynamically register a broadcast receiver inside sessionactivity
    // it will trigger after a few seconds and flip some flag
    // we can then check if that flag is raised once we return to the app
    // if it is then we can handle the paused case accordingly

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updater.setTimer(intent.getIntExtra(OLD_TIMER_VAL, 0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notifMgr = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);

        assert notifMgr != null;

        Intent startContext = getIntent();
        TextView timer = findViewById(R.id.timer);
        timer.setText(updater.getTimerValueAsString());

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            notificationChannel = new NotificationChannel(NOTIF_CHANNEL_NAME, NOTIF_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
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

        // reopens the main activity
        Intent reopenSession = new Intent(this, SessionActivity.class);
        assert reopenSession != null;
        reopenSession.putExtra("the", "monkey");
        reopenSession.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        reopenSession.putExtra(OLD_TIMER_VAL, updater.getTimerValue());

        PendingIntent pendSession = PendingIntent.getActivity(this, TIMER_NOTIF_ID, reopenSession, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendSession);

        Log.e("heads up!", "stopped");

        assert notifMgr != null;
        notifMgr.notify(TIMER_NOTIF_ID, builder.build());




    }

    // create an onStop call which displays a notification containing the current timer value
}
