package okhttp3.cookie;

import java.util.List;

import javax.annotation.Nullable;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

public abstract class CookiePersistor {
    public static final CookiePersistor MEMORY = new MemoryCookiePersistor();

    @Nullable
    public abstract List<Cookie> load(HttpUrl index);

    public abstract boolean update(HttpUrl index, @Nullable List<Cookie> cookies);

    public abstract boolean clear();
}
