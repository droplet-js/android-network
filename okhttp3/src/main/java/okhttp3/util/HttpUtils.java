package okhttp3.util;

import java.util.Date;
import java.util.Locale;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.HttpHeaders;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.cache.CacheStrategy;

public final class HttpUtils {

    private static final CacheControl EMPTY = new CacheControl.Builder().build();

    private HttpUtils() {
    }

    public static <T> boolean isUpToDate(Response response) {
        if (response != null && response.isSuccessful()) {
            CacheControl cacheControl = response.request().cacheControl();
            if (!TextUtils.equals(cacheControl.toString(), CacheControl.FORCE_CACHE.toString()) || !isOverdue(response)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOverdue(Response response) {
        if (response != null) {
            Date servedDate = parseServedDate(response);
            if (servedDate != null) {
                Request request = response.request().newBuilder().cacheControl(EMPTY).build();
                CacheStrategy.Factory factory = new CacheStrategy.Factory(servedDate.getTime(), request, response);
                if (factory.get().networkRequest != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValid(Response response) {
        return response != null && response.isSuccessful();
    }

    public static Date parseServedDate(Response response) {
        Headers headers = response.headers();
        return headers != null ? headers.getDate(HttpHeaders.DATE) : null;
    }

    public static Date parseSentDate(Response response) {
        return new Date(response.sentRequestAtMillis());
    }

    public static Date parseReceivedDate(Response response) {
        return new Date(response.receivedResponseAtMillis());
    }

    public static String oauth(String tokenType, String accessToken) {
        return String.format(Locale.getDefault(), "%1$s %2$s", tokenType, accessToken);
    }
}
