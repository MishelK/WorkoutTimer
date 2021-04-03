package com.example.workouttimer.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class TimerService extends Service {



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

                    break;
                case "stop":

                    break;
                case "reset":
                    
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
