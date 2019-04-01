package okhttp3.android;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.util.TextUtils;

@SuppressWarnings("deprecation")
public final class WebkitCookieJar implements CookieJar {

    private static final String TAG = "WebkitCookieJar";

    private Context context;
    private boolean saveAlways;

    public WebkitCookieJar(Context context, boolean saveAlways) {
        super();
        this.context = context.getApplicationContext() != null ? context.getApplicationContext() : context;
        this.saveAlways = saveAlways;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        // 携带过期时间等信息写入
        if (cookies != null && !cookies.isEmpty()) {
            try {
                CookieSyncManager.createInstance(context);
                for (Cookie cookie : cookies) {
                    if (cookie.persistent() || saveAlways) {
                        String cookieString = cookie.toString();
                        CookieManager.getInstance().setCookie(url.toString(), cookieString);
                    }
                }
            } finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().flush();
                } else {
                    CookieSyncManager.getInstance().sync();
                }
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        // 读取却不会携带过期时间等信息，过期的Cookie会被过滤掉
        try {
            CookieSyncManager.createInstance(context);
            String cookieStrAll = CookieManager.getInstance().getCookie(url.toString());
            if (!TextUtils.isEmpty(cookieStrAll)) {
                String[] cookieStrArray = cookieStrAll.split("; ");
                if (cookieStrArray != null && cookieStrArray.length > 0) {
                    List<Cookie> cookies = new ArrayList<Cookie>();
                    for (String cookieStr : cookieStrArray) {
                        cookies.add(Cookie.parse(url, cookieStr));
                    }
                    return Collections.unmodifiableList(cookies);
                } else {
                    return Collections.<Cookie>emptyList();
                }
            } else {
                return Collections.<Cookie>emptyList();
            }
        } finally {
        }
    }
}
