package com.example.workouttimer.utilities;

public class Config {
    // Spotify App Remote API
    public static final String CLIENT_ID = "008364546c024db1818a96724881f3e7";
    public static final String REDIRECT_URI = "workouttimer://callback";

    // Timer Service
    public static final long TIMER_INTERVAL = 100;
    public static final long INITIAL_TIMER = 10000;
    public static final String TIMER_COMMAND = "command";
    public static final String TIMER_START = "start";
    public static final String TIMER_STOP = "stop";
    public static final String TIMER_RESET = "reset";
    public static final String TIMER_SET_TIME = "set_time";
    public static final String TIMER_DURATION = "duration";

    // Broadcast channel
    public static final String TIMER_BROADCAST_CHANNEL = "timer_channel";
    public static final String TIMER_BROADCAST_TIME_LEFT = "time_left";
    public static final String TIMER_BROADCAST_TIME_PROGRESS = "time_progress";
}
