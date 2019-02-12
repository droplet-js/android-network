package okhttp3.cookie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public final class PersistentCookieJar implements CookieJar {

    private final PersistentCookieStore cookieStore;

    private PersistentCookieJar(PersistentCookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookieStore != null) {
            if (cookies != null && !cookies.isEmpty()) {
                cookieStore.put(url, Collections.unmodifiableList(cookies));
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = new ArrayList<>();
        if (cookieStore != null) {
            List<Cookie> effectiveCookies = cookieStore.get(url);
            if (effectiveCookies != null && !effectiveCookies.isEmpty()) {
                for (Cookie effectiveCookie : effectiveCookies) {
                    if (System.currentTimeMillis() < effectiveCookie.expiresAt()) {
                        cookies.add(effectiveCookie);
                    }
                }
            }
        }
        return Collections.unmodifiableList(cookies);
    }

    public static PersistentCookieJar memory() {
        return persistent(CookiePersistor.MEMORY);
    }

    public static PersistentCookieJar persistent(CookiePersistor persistor) {
        return new PersistentCookieJar(new PersistentCookieStore(persistor));
    }
}
