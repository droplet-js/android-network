package okhttp3.tools;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.HttpHeaders;
import okhttp3.HttpStatus;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 应用层拦截器
 * 要比 {@link OptimizedRequestInterceptor} 晚注册
 */
public final class NotModifiedFixedInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Response response = chain.proceed(originalRequest);
        if (response.code() == HttpStatus.NOT_MODIFIED.code()) {
            Request forceCacheRequest = originalRequest.newBuilder()
                    .removeHeader(HttpHeaders.CACHE_CONTROL)
                    .removeHeader(HttpHeaders.PRAGMA)
                    .removeHeader(HttpHeaders.IF_NONE_MATCH)
                    .removeHeader(HttpHeaders.IF_MODIFIED_SINCE)
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
            response = chain.proceed(forceCacheRequest);
        }
        return response;
    }
}
