package okhttp3.cookie;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

public interface CookiePersistor {
    List<Cookie> load(HttpUrl index);

    boolean update(HttpUrl index, List<Cookie> persistentCookies);

    boolean clear();
}
