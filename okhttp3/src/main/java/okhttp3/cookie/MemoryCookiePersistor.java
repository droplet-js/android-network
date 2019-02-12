package okhttp3.cookie;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

final class MemoryCookiePersistor extends CookiePersistor {

    private final Map<HttpUrl, List<Cookie>> _urlIndex = new HashMap<>();

    @Nullable
    @Override
    public List<Cookie> load(HttpUrl index) {
        List<Cookie> cookies = _urlIndex.get(index);
        return cookies != null ? Collections.unmodifiableList(cookies) : null;
    }

    @Override
    public boolean update(HttpUrl index, @Nullable List<Cookie> cookies) {
        if (cookies != null) {
            _urlIndex.put(index, cookies);
        } else {
            _urlIndex.remove(index);
        }
        return true;
    }

    @Override
    public boolean clear() {
        _urlIndex.clear();
        return true;
    }
}
