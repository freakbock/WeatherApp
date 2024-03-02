package com.example.weatherapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private NetworkChangeListener listener;

    public void setNetworkChangeListener(NetworkChangeListener listener){
        this.listener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (listener != null) {
            Log.d("NetworkListener", "Подключение обновилось");
            listener.onNetworkChanged(isConnected);
        }
    }
    public interface NetworkChangeListener{
        void onNetworkChanged(boolean isConnected);
    }
}
