package okhttp3.connectivity.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Process;

import okhttp3.connectivity.ConnectivityDoctor;

public final class AndroidConnectivityDoctor implements ConnectivityDoctor {

    private final Context context;

    public AndroidConnectivityDoctor(Context context) {
        this.context = context.getApplicationContext() != null ? context.getApplicationContext() : context;
    }

    @Override
    public boolean detect() {
        if (context.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED) {
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
