package com.example.workouttimer.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.workouttimer.MainActivity;
import com.example.workouttimer.R;
import com.example.workouttimer.utilities.Config;

public class TimerService extends Service {

    private static final String NOTIF_CHANNEL_NAME = "WorkoutTimer Channel";
    private static final String NOTIF_CHANNEL_ID = "MK_NOTIF_CHANNEL";
    private static final int NOTIF_ID = 1;

    private CountDownTimer countDownTimer;
    private long timerDuration = Config.INITIAL_TIMER; //Used upon initial start of timer
    private long timerTimeLeft = Config.INITIAL_TIMER; // Used for pausing and resuming a timer
    private boolean isReset = false; // Used for determining if startTimer is initial or a resume
    private boolean isRunning = false; // Used for determining if timer is running

    NotificationManager manager;
    NotificationCompat.Builder builder;
    RemoteViews remoteViews;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { // Called from startService()

        if (intent != null) {
            String command = intent.getStringExtra(Config.TIMER_COMMAND);
            if (command != null) {
                switch (command) {
                    case Config.TIMER_START:
                        startTimer();
                        Log.i("workouttimer", "Start Timer");
                        break;
                    case Config.TIMER_STOP:
                        stopTimer();
                        removeNotification();
                        Log.i("workouttimer", "Stop Timer");
                        break;
                    case Config.TIMER_RESET:
                        resetTimer();
                        Log.i("workouttimer", "Reset Timer");
                        break;
                    case Config.TIMER_SET_TIME:
                        setTime(intent.getLongExtra(Config.TIMER_DURATION, Config.INITIAL_TIMER));
                        Log.i("workouttimer", "Set time Timer");
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // If isReset then starts timer with initial timerDuration, else, resumes timer with timerTimeLeft
    private void startTimer(){

        if(isReset) // Case initial start, start the timer with timerDuration
            countDownTimer = new CountDownTimer(timerDuration, Config.TIMER_INTERVAL) { // onTick will be called every TIMER_INTERVAL (1 second) for the duration
                @Override
                public void onTick(long millisUntilFinished) {
                    timerTimeLeft = millisUntilFinished;
                    // TODO: UPDATE UI
                    sendTimeLeftBroadcast(millisUntilFinished);
                    startNotification(millisUntilFinished);
                    updateNotification(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    sendTimeLeftBroadcast(0);
                    stopTimer();
                    removeNotification();
                }
            }.start();

        else // Case resume timer
            countDownTimer = new CountDownTimer(timerTimeLeft, Config.TIMER_INTERVAL) { // onTick will be called every TIMER_INTERVAL (1 second) for the duration
                @Override
                public void onTick(long millisUntilFinished) {
                    timerTimeLeft = millisUntilFinished;
                    // TODO: UPDATE UI
                    sendTimeLeftBroadcast(millisUntilFinished);
                    startNotification(millisUntilFinished);
                    updateNotification(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    sendTimeLeftBroadcast(0);
                    stopTimer();
                    removeNotification();
                }
            }.start();

        isReset = false;
        isRunning = true;
    }

    // Cancels the timer
    private void stopTimer(){

        if(countDownTimer != null)
            countDownTimer.cancel();
        isRunning = false;
    }

    // Resets the time left on the timer and updates isReset
    private void resetTimer(){
        if (!isRunning) {
            timerTimeLeft = timerDuration;
            isReset = true;
            // TODO: UPDATE UI
            sendTimeLeftBroadcast(timerTimeLeft);
        }
    }

    // Setting a new duration for the timer and resetting the timer
    private void setTime(long duration) {

        if(isRunning)
            stopTimer();
        timerDuration = duration;
        resetTimer();
    }

    // Sends out a local broadcast with the time left on the timer and its current progress
    private void sendTimeLeftBroadcast(long timerTimeLeft) {
        Long progress = (long)((float)timerTimeLeft/timerDuration*100); // Calculating progress

        Intent intent = new Intent(Config.TIMER_BROADCAST_CHANNEL);
        intent.putExtra(Config.TIMER_BROADCAST_TIME_LEFT, timerTimeLeft);
        intent.putExtra(Config.TIMER_BROADCAST_TIME_PROGRESS, progress);
        LocalBroadcastManager.getInstance(TimerService.this).sendBroadcast(intent);
    }

    private void startNotification (long time) {
        // Building Notification
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID, NOTIF_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }
        builder = new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID);
        builder.setNotificationSilent();
        remoteViews = new RemoteViews(getPackageName(), R.layout.notif_layout);

        // Building pending intent
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("running", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Starting Notification
        builder.setCustomContentView(remoteViews);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.ic_play_button);
        builder.setOngoing(true);
        //startForeground(NOTIF_ID, builder.build());
        manager.notify(NOTIF_ID, builder.build());
    }

    private void updateNotification (long time) {
        remoteViews.setTextViewText(R.id.notif_time, timeLongToString(time));
        //startForeground(NOTIF_ID, builder.build());
        manager.notify(NOTIF_ID, builder.build());
    }

    private void removeNotification() {
        if (manager != null)
            manager.cancel(NOTIF_ID);
    }

    // Gets time in long and converts it to a string representing time in MM:SS format
    private String timeLongToString(long time) {

        String timeString = "";
        int minutes = (int) time / 60000;
        int seconds = (int) time % 60000 / 1000; // after performing % and removing time left in minutes we divide by 1000 to we have the number of seconds left
        if (minutes < 10) timeString += "0";
        timeString += "" + minutes;
        timeString += ":";
        if (seconds < 10) timeString += "0";
        timeString += seconds;
        return  timeString;
    }

}
