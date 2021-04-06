package com.example.workouttimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.workouttimer.services.TimerService;
import com.example.workouttimer.utilities.Config;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private SpotifyAppRemote mSpotifyAppRemote;
    private BroadcastReceiver timerReceiver;

    long initialTime, remainingTime;
    boolean isRunning, isReset, isFinished = false;

    TextView time_tv;
    CircularProgressBar circularProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting broadcast listener to timer updates and updating ui accordingly
        IntentFilter filter = new IntentFilter(Config.TIMER_BROADCAST_CHANNEL);
        timerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long timeLeft = intent.getLongExtra(Config.TIMER_BROADCAST_TIME_LEFT, 0);
                long progress = intent.getLongExtra(Config.TIMER_BROADCAST_TIME_PROGRESS, 0);
                remainingTime = timeLeft;
                // Updating ui
                updateTimeUi(timeLeft, progress);
                if (timeLeft == 0) {
                    isRunning = false;
                    isFinished = true;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(timerReceiver, filter);

        // Init timer UI
        time_tv = findViewById(R.id.tv_time);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        initTimerBtns();

        // Init Timer if first started
        if (!isFinished) {
            initialTime = remainingTime = Config.INITIAL_TIMER;
            isReset = true;
            updateTimeUi(Config.INITIAL_TIMER, 100);
            circularProgressBar.setProgressWithAnimation(100, (long) 2000);
        }
        else
            updateTimeUi(0,0);

        // Init spotify player UI
        initSpotifyPlayerBtns();

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Setting spotify connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(Config.CLIENT_ID)
                        .setRedirectUri(Config.REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        // Attempting to connect to spotify app remote
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.i("MainActivity", "Connected to spotify");

                        // Interacting with app remote
                        onConnectSpotify();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.i("MainActivity", throwable.getMessage(), throwable);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnecting from app remote
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Removing local broadcast listener
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerReceiver);
        stopTimer();
    }

    // Setting a spotify player listener with a callback that updates UI
    private void onConnectSpotify() {
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                        // TODO: UPDATE UI
                    }
                });
    }

    // Starts/resumes the timer
    private void startTimer() {
        Intent intent = new Intent(this, TimerService.class);
        intent.putExtra(Config.TIMER_COMMAND, Config.TIMER_START);
        startService(intent);
        isRunning = true;
        isFinished = true;
    }

    // Stops the timer
    private void stopTimer() {
        Intent intent = new Intent(this, TimerService.class);
        intent.putExtra(Config.TIMER_COMMAND, Config.TIMER_STOP);
        startService(intent);
        isRunning = false;
    }

    // Resets the timer
    private void resetTimer() {
        remainingTime = initialTime;
        Intent intent = new Intent(this, TimerService.class);
        intent.putExtra(Config.TIMER_COMMAND, Config.TIMER_RESET);
        startService(intent);
        isReset = true;
    }

    // Sets a new duration on the timer and resets it
    private void setTime(long time) {
        if (!isRunning) {
            Intent intent = new Intent(this, TimerService.class);
            intent.putExtra(Config.TIMER_COMMAND, Config.TIMER_SET_TIME);
            intent.putExtra(Config.TIMER_DURATION, time);
            startService(intent);
        }
    }

    // Updates the timer text view with time left and progressbar with progress %
    private void updateTimeUi(long timeLeft, float progress) {
        // TODO: IMPLEMENT
        String timeString = timeLongToString(timeLeft);
        time_tv.setText(timeString);
        circularProgressBar.setProgress(progress);
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

    // Increments the timer by TIMER_INCREMENT (default 10 sec)
    private void incrementTimer() {

    }

    // Decrements the timer by TIMER_INCREMENT (default 10 sec)
    private void decrementTimer() {

    }

    private void initSpotifyPlayerBtns (){
        Button btn_prev, btn_pause, btn_resume, btn_next;

        btn_pause = findViewById(R.id.player_pause);
        btn_resume = findViewById(R.id.player_play);
        btn_prev = findViewById(R.id.player_prev);
        btn_next = findViewById(R.id.player_next);

        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotifyAppRemote.getPlayerApi().pause();
            }
        });
        btn_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotifyAppRemote.getPlayerApi().resume();
            }
        });
        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotifyAppRemote.getPlayerApi().skipPrevious();
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpotifyAppRemote.getPlayerApi().skipNext();
            }
        });
    }

    private void initTimerBtns () {
        Button btn_start, btn_stop, btn_reset, btn_plus, btn_minus;

        btn_plus = findViewById(R.id.btn_plus);
        btn_minus = findViewById(R.id.btn_minus);
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        btn_reset = findViewById(R.id.btn_reset);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });
        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (initialTime < 3600000 && !isRunning && isReset){ // Less than 60:00, paused and reset
                    initialTime += Config.TIMER_INCREMENT;
                    remainingTime = initialTime;
                    updateTimeUi(initialTime, 100);
                    setTime(initialTime);
                }
            }
        });
        btn_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (initialTime >= Config.TIMER_INCREMENT && !isRunning && isReset){ // Less than 60:00, paused and reset
                    initialTime -= Config.TIMER_INCREMENT;
                    remainingTime = initialTime;
                    updateTimeUi(initialTime, 100);
                    setTime(initialTime);
                }
            }
        });
    }

}



