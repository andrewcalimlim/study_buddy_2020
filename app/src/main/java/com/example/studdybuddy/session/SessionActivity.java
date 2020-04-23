package com.example.studdybuddy.session;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
