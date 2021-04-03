package com.example.workouttimer.services;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class TimerService extends Service {

    private static final long TIMER_INTERVAL = 1000;
    private static final long INITIAL_TIMER = 60000;

    private CountDownTimer countDownTimer;
    private long timerDuration = INITIAL_TIMER; //Used upon initial start of timer
    private long timerTimerLeft = INITIAL_TIMER; // Used for pausing and resuming a timer
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
        String command = intent.getStringExtra("command");
        if (command != null) {
            switch (command) {
                case "start":
                    startTimer();
                    break;
                case "stop":
                    stopTimer();
                    break;
                case "reset":
                    resetTimer();
                    break;
                case "set_time":
                    setTime(intent.getLongExtra("duration",INITIAL_TIMER));
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
            countDownTimer = new CountDownTimer(timerDuration, TIMER_INTERVAL) { // onTick will be called every TIMER_INTERVAL (1 second) for the duration
                @Override
                public void onTick(long millisUntilFinished) {
                    timerTimerLeft = millisUntilFinished;
                    // TODO: UPDATE UI
                }

                @Override
                public void onFinish() {
                }
            }.start();

        else // Case resume timer
            countDownTimer = new CountDownTimer(timerTimerLeft, TIMER_INTERVAL) { // onTick will be called every TIMER_INTERVAL (1 second) for the duration
                @Override
                public void onTick(long millisUntilFinished) {
                    timerTimerLeft = millisUntilFinished;
                    // TODO: UPDATE UI
                }

                @Override
                public void onFinish() {
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

        timerTimerLeft = timerDuration;
        isReset = true;
        // TODO: UPDATE UI
    }

    // Setting a new duration for the timer and reseting the timer
    private void setTime(long duration) {

        if(isRunning)
            stopTimer();
        timerDuration = duration;
        resetTimer();
    }

}
