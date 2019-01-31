package okhttp3.tools;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.HttpHeaders;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.HttpMethod;
import okhttp3.util.TextUtils;

/**
 * 网络层拦截器
 * 优化缓存
 *
 * https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Caching_FAQ
 */
public final class OptimizedResponseInterceptor implements Interceptor {

    private final int maxAgeSeconds;

    public OptimizedResponseInterceptor() {
        this(3);
    }

    public OptimizedResponseInterceptor(int maxAge) {
        this.maxAgeSeconds = maxAge;
    }

    @Override
    public final Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Response originalResponse = chain.proceed(originalRequest);
        if (!HttpMethod.invalidatesCache(originalRequest.method())) {
            if (originalResponse.isSuccessful()) {
                if (TextUtils.isEmpty(originalResponse.header(HttpHeaders.LAST_MODIFIED))
                        && TextUtils.isEmpty(originalResponse.header(HttpHeaders.ETAG))
                        && TextUtils.isEmpty(originalResponse.header(HttpHeaders.EXPIRES))
                        && TextUtils.isEmpty(originalResponse.header(HttpHeaders.AGE))) {
                    // 智能添加缓存信息
                    boolean shouldOptimizedCache = false;
                    if (TextUtils.isEmpty(originalResponse.header(HttpHeaders.CACHE_CONTROL))
                            && TextUtils.isEmpty(originalResponse.header(HttpHeaders.PRAGMA))) {
                        shouldOptimizedCache = true;
                    } else {
                        CacheControl cacheControl = originalResponse.cacheControl();
                        shouldOptimizedCache = cacheControl.noCache() || cacheControl.noStore();
                    }
                    if (shouldOptimizedCache) {
                        return originalResponse.newBuilder()
                                .removeHeader(HttpHeaders.PRAGMA)//Pragma:no-cache。在HTTP/1.1协议中，它的含义和Cache-Control:no-cache相同。
                                .header(HttpHeaders.CACHE_CONTROL, new CacheControl.Builder().maxAge(maxAgeSeconds, TimeUnit.SECONDS).build().toString())//添加缓存请求头
                                .build();
                    }
                }
            }
        }
        return originalResponse;
    }
}
