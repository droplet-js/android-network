package okhttp3.android.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.webkit.WebSettings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import okhttp3.internal.Version;

public final class NetworkUtils {
    private NetworkUtils() {
    }

    public static String getUserAgent(Context context) {
        String userAgent = "";
        try {
            userAgent = WebSettings.getDefaultUserAgent(context);
        } catch (Exception ignore) {
        }
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = System.getProperty("http.agent");
        }
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = String.format(Locale.getDefault(),
                    "Android/%s(SDK: %d); %s; %s", Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Version.userAgent(), Build.FINGERPRINT);
        }
        if (!TextUtils.isEmpty(userAgent)) {
            try {
                userAgent = URLEncoder.encode(userAgent, "UTF-8");
            } catch (UnsupportedEncodingException ignore) {
            }
        }
        return userAgent;
    }

    public static boolean networkConnected(Context context) {
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
