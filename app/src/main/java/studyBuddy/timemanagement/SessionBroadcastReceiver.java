package studyBuddy.timemanagement;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.studdybuddy.R;

import studyBuddy.MainActivity;
import studyBuddy.SessionActivity;

public class SessionBroadcastReceiver extends BroadcastReceiver {

    private static long EPS_INTERVAL = 1000;
    public static String DELETE_INTENT = "deleteIntent";
    public static String NOTIFICATION_CHANNEL = "studySession";
    static public String NOTIFICATION_ID = "notificationID";
    static public String SESSION_END = "sessionEnd";
    static public String SESSION_START = "sessionStart";

    // handle reopening
    static public String REOPEN_SESSION = "reopenSession";

    /**
     * Create a notification builder based on build version.
     * @param ctx - The application context.
     * @return - A new notification builder.
     */
    private Notification.Builder getBuilder(Context ctx) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new Notification.Builder(ctx, NOTIFICATION_CHANNEL);
        } else {
            builder = new Notification.Builder(ctx);
        }

        return builder;
    }

    /**
     * On receiving the desired intent, creates a new notification representing the new number of
     * seconds remaining, and sets up an alarm to ping itself in a minute.
     * @param ctx - The current context.
     * @param intent - The intent received.
     */
    @Override
    public void onReceive(Context ctx, Intent intent) {
        // if intent dismissed: clear
        // clear alarm in
        intent.getAction();

        // get alarm manager
        AlarmManager alarmMGR = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        assert alarmMGR != null;

        // get information from intent
        int notificationID = intent.getIntExtra(NOTIFICATION_ID, 0);
        long sessionEndTime = intent.getLongExtra(SESSION_END, System.currentTimeMillis());
        long sessionStartTime = intent.getLongExtra(SESSION_START, System.currentTimeMillis());
        long duration = sessionEndTime - System.currentTimeMillis();

        // set up the intent which will trigger the broadcastreceiver to update in a minute
        Intent nextBroadcastIntent = new Intent(ctx, SessionBroadcastReceiver.class);
        nextBroadcastIntent.putExtra(NOTIFICATION_ID, notificationID);
        nextBroadcastIntent.putExtra(SESSION_END, sessionEndTime);
        nextBroadcastIntent.putExtra(SESSION_START, sessionStartTime);
        PendingIntent broadIntent = PendingIntent.getBroadcast(ctx, SessionActivity.INTENT_ID, nextBroadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // set up the intent which triggers our session activity again
        Intent activityIntent = new Intent(ctx, MainActivity.class);
        activityIntent.putExtra(DELETE_INTENT, broadIntent);
        activityIntent.putExtra(REOPEN_SESSION, true);
        activityIntent.putExtra(SESSION_END, sessionEndTime);
        activityIntent.putExtra(SESSION_START, sessionStartTime);
        NotificationManager mgr = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        assert mgr != null;

        // build a notification based on our incoming intent values
        Notification.Builder builder = getBuilder(ctx);
        builder.setContentIntent(PendingIntent.getActivity(ctx, SessionActivity.INTENT_ID, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setSmallIcon(R.drawable.cattronsuperscale);
        builder.setContentTitle(ctx.getResources().getString(R.string.app_name));
        builder.setAutoCancel(true);

        long nextNotificationDuration = (long)(60000 * Math.ceil(duration / 60000.0) - 60000);
        // difference between current "time remaining" and when we send the notif
        long timeToNextUpdate = (duration - nextNotificationDuration);

        if (duration <= 0) {
            String timeBuilder = "You are " + (int) Math.ceil(duration / 60000.0) + " minute(s) into your session!";
            builder.setContentText(timeBuilder);
            builder.setOngoing(true);
            alarmMGR.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeToNextUpdate, broadIntent);
        } else if (duration > EPS_INTERVAL) {
            // session is incomplete -- remind the user that it is in progress
            // duration rounded down -- time at which we will send the next notification

            String timeBuilder = (int) Math.ceil(duration / 60000.0) +
                    " minute(s) remaining!";
            builder.setContentText(timeBuilder);
            builder.setOngoing(true);
            alarmMGR.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeToNextUpdate, broadIntent);
        } else {
            // session is over -- let the user know
            builder.setOngoing(false);
            builder.setContentText("Session complete!");
            // let them know that we're done
        }

        mgr.notify(notificationID, builder.build());
    }
}
