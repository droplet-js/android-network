package okhttp3.ua.android;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebSettings;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import okhttp3.internal.Version;
import okhttp3.ua.UserAgentDoctor;

public class AndroidUserAgentDoctor implements UserAgentDoctor {

    private final Context context;

    public AndroidUserAgentDoctor(Context context) {
        this.context = context.getApplicationContext() != null ? context.getApplicationContext() : context;
    }

    @Override
    public String detect() {
        String userAgent = null;
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
}
