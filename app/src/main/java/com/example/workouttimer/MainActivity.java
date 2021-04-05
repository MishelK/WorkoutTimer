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

    long initialTime = Config.INITIAL_TIMER;
    long remainingTime = Config.INITIAL_TIMER;
    TextView time_tv;
    CircularProgressBar circularProgressBar;
    EditText new_time;
    ImageButton btn_start, btn_stop, btn_reset;
    Button btn_set_time;

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

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(timerReceiver, filter);

        // Init timer UI
        time_tv = findViewById(R.id.tv_time);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        circularProgressBar.setProgressWithAnimation(100, (long)2000);

        new_time = findViewById(R.id.et_new_time);
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        btn_reset = findViewById(R.id.btn_reset);
        btn_set_time = findViewById(R.id.btn_set_time);
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
        btn_set_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long time = Long.parseLong(new_time.getText().toString());
                if (time > 0) {
                    setTime(time);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connecting to spotify app remote
        // Setting the connection parameters
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
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerReceiver);
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
    }

    // Stops the timer
    private void stopTimer() {
        Intent intent = new Intent(this, TimerService.class);
        intent.putExtra(Config.TIMER_COMMAND, Config.TIMER_STOP);
        startService(intent);
    }

    // Resets the timer
    private void resetTimer() {
        remainingTime = initialTime;
        Intent intent = new Intent(this, TimerService.class);
        intent.putExtra(Config.TIMER_COMMAND, Config.TIMER_RESET);
        startService(intent);
    }

    // Sets a new duration on the timer and resets it
    private void setTime(long time) {
        initialTime = time;
        remainingTime = time;
        Intent intent = new Intent(this, TimerService.class);
        intent.putExtra(Config.TIMER_COMMAND, Config.TIMER_SET_TIME);
        intent.putExtra(Config.TIMER_DURATION, time);
        startService(intent);
    }

    // Updates the timer text view with time left
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
}



