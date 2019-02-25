package okhttp3.cookie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.util.TextUtils;

final class PersistentCookieStore {

    private final CookiePersistor persistor;

    PersistentCookieStore(CookiePersistor persistor) {
        this.persistor = persistor;
    }

    public void put(HttpUrl url, List<Cookie> cookies) {
        if (cookies != null && !cookies.isEmpty()) {
            HttpUrl index = getEffectiveUrl(url);

            List<Cookie> persistCookies = persistor.load(index);
            List<Cookie> effectiveCookies = persistCookies != null ? new ArrayList<>(persistCookies) : new ArrayList<>();

            List<Cookie> shouldRemoveCookies = new ArrayList<>();

            for (Cookie cookie : cookies) {
                for (Cookie effectiveCookie : effectiveCookies) {
                    if (effectiveCookie.equals(cookie)) {
                        shouldRemoveCookies.add(effectiveCookie);
                    } else if (TextUtils.equals(effectiveCookie.name(), cookie.name())
                            && TextUtils.equals(effectiveCookie.domain(), cookie.domain())
                            && TextUtils.equals(effectiveCookie.path(), cookie.path())) {
                        shouldRemoveCookies.add(effectiveCookie);
                    }
                }
            }

            effectiveCookies.removeAll(shouldRemoveCookies);
            effectiveCookies.addAll(cookies);

            persistor.update(index, effectiveCookies);
        }
    }

    public List<Cookie> get(HttpUrl url) {
        HttpUrl index = getEffectiveUrl(url);

        List<Cookie> persistCookies = persistor.load(index);
        List<Cookie> effectiveCookies = persistCookies != null ? new ArrayList<>(persistCookies) : new ArrayList<>();

        return Collections.unmodifiableList(effectiveCookies);
    }

    private HttpUrl getEffectiveUrl(HttpUrl url) {
        return new HttpUrl.Builder()
                .scheme("http")
                .host(url.host())
                .build();
    }
}
