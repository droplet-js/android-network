package okhttp3.proxy.android;

import android.content.Context;
import android.os.Build;

import okhttp3.proxy.ProxyDoctor;

@SuppressWarnings("deprecation")
public final class AndroidProxyDoctor implements ProxyDoctor {

    private final Context context;

    public AndroidProxyDoctor(Context context) {
        this.context = context.getApplicationContext() != null ? context.getApplicationContext() : context;
    }

    @Override
    public boolean detect() {
        String proxyHost;
        int proxyPort;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            proxyHost = System.getProperty("http.proxyHost");
            String port = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt(port != null ? port : "-1");
        } else {
            proxyHost = android.net.Proxy.getHost(context);
            proxyPort = android.net.Proxy.getPort(context);
        }
        return proxyHost != null && proxyPort != -1;
    }
}
