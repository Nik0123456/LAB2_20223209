package com.example.l2_20223209.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

public class ConnectivityChecker {
    
    private Context context;
    
    public ConnectivityChecker(Context context) {
        this.context = context;
    }
    
    public boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) {
                return false;
            }
            
            NetworkCapabilities networkCapabilities = 
                connectivityManager.getNetworkCapabilities(activeNetwork);
            
            return networkCapabilities != null && (
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }
}