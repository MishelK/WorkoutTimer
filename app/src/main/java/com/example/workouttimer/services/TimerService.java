package com.example.workouttimer.services;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.workouttimer.utilities.Config;

public class TimerService extends Service {

    private CountDownTimer countDownTimer;
    private long timerDuration = Config.INITIAL_TIMER; //Used upon initial start of timer
    private long timerTimeLeft = Config.INITIAL_TIMER; // Used for pausing and resuming a timer
    private boolean isReset = false; // Used for determining if startTimer is initial or a resume
    private boolean isRunning = false; // Used for determining if timer is running

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
        String command = intent.getStringExtra(Config.TIMER_COMMAND);
        if (command != null) {
            switch (command) {
                case Config.TIMER_START:
                    startTimer();
                    Log.i("workouttimer", "Start Timer");
                    break;
                case Config.TIMER_STOP:
                    stopTimer();
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
                }

                @Override
                public void onFinish() {
                    sendTimeLeftBroadcast(0);
                }
            }.start();

        else // Case resume timer
            countDownTimer = new CountDownTimer(timerTimeLeft, Config.TIMER_INTERVAL) { // onTick will be called every TIMER_INTERVAL (1 second) for the duration
                @Override
                public void onTick(long millisUntilFinished) {
                    timerTimeLeft = millisUntilFinished;
                    // TODO: UPDATE UI
                    sendTimeLeftBroadcast(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    sendTimeLeftBroadcast(0);
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
}
