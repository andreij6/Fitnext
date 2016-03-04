package com.creativejones.andre.fitnext.band;

import android.content.Intent;

public interface IFitwearable {

    void processIntent(Intent intent);

    void onDestroy();

    void connectAsync();

    void disconnectAsync();

    interface Listener {
        void userMessage(String message);

        String nextExercise();

        void onConnected(Boolean isConnected);
    }
}
