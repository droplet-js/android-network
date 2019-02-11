package okhttp3.android.util;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

@SuppressWarnings("deprecation")
public final class CookieUtils {

    public static final String SEPARATOR_SEMICOLON = "; ";
    public static final String SEPARATOR_EQUAL_SIGN = "=";

    /**
     * 获取cookie
     * @param url
     * @return
     */
    public static String get(Context context, String url) {
        String cookie = "";
        if (context != null && !TextUtils.isEmpty(url)) {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            cookieSyncManager.sync();
            cookie = CookieManager.getInstance().getCookie(url);
        }
        return cookie;
    }

    /**
     * 移除过期cookie
     */
    public static void removeExpiredCookie(Context context) {
        if (context != null) {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            cookieSyncManager.sync();

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);

            cookieManager.removeExpiredCookie();

            cookieSyncManager.sync();
        }
    }

    /**
     * 移除Session cookie
     */
    public static void removeSessionCookie(Context context) {
        if (context != null) {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            cookieSyncManager.sync();

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);

            cookieManager.removeSessionCookie();

            cookieSyncManager.sync();
        }
    }

    /**
     * 移除全部cookie
     */
    public static void removeAllCookie(Context context) {
        if (context != null) {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            cookieSyncManager.sync();

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);

            cookieManager.removeSessionCookie();
            cookieManager.removeAllCookie();

            cookieSyncManager.sync();
        }
    }

    private static <V> void appendCookieMeta(StringBuilder strBuilder, String key, V v) {
        String value = v != null ? v.toString() : "";
        if (!TextUtils.isEmpty(value)) {
            appendSeparatorSemicolon(strBuilder);
            if (!TextUtils.isEmpty(key)) {
                strBuilder.append(key).append(SEPARATOR_EQUAL_SIGN);
            }
            strBuilder.append(value);
            appendSeparatorSemicolon(strBuilder);
        }
    }

    private static void appendSeparatorSemicolon(StringBuilder strBuilder) {
        if (strBuilder.length() > 0 && !endsWith(strBuilder, SEPARATOR_SEMICOLON)) {
            strBuilder.append(SEPARATOR_SEMICOLON);
        }
    }

    /**
     * 判断是否以指定字符结尾
     *
     * @param strBuilder
     * @param suffix
     * @return
     */
    public static boolean endsWith(StringBuilder strBuilder, String suffix) {
        boolean result = false;
        if (strBuilder != null && !TextUtils.isEmpty(suffix)) {
            int endIndex = strBuilder.length() - suffix.length();
            result = endIndex >= 0 && strBuilder.lastIndexOf(suffix) == endIndex;
        }
        return result;
    }
}
