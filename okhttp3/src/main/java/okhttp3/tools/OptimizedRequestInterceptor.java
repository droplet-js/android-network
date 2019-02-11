package okhttp3.tools;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import okhttp3.CacheControl;
import okhttp3.HttpHeaders;
import okhttp3.HttpStatus;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.HttpMethod;
import okhttp3.util.TextUtils;

/**
 * 应用层拦截器
 */
public class OptimizedRequestInterceptor implements Interceptor {

    @Override
    public final Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        if (!HttpMethod.invalidatesCache(originalRequest.method())) {
            // 强刷
            if (TextUtils.equals(originalRequest.cacheControl().toString(), CacheControl.FORCE_NETWORK.toString())) {
                Request originalFixedRequest = originalRequest.newBuilder()
                        .removeHeader(HttpHeaders.CACHE_CONTROL)
                        .removeHeader(HttpHeaders.PRAGMA)
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build();
                return chain.proceed(originalFixedRequest);
            }
            Response response = null;
            // 非强刷
            try {
                response = chain.proceed(originalRequest);
                // 用户手动调时间，让当前时间小于缓存创建时间，这时候缓存不会过期
                if (response.receivedResponseAtMillis() > System.currentTimeMillis()) {
                    originalRequest = originalRequest.newBuilder()
                            .removeHeader(HttpHeaders.CACHE_CONTROL)
                            .removeHeader(HttpHeaders.PRAGMA)
                            .cacheControl(CacheControl.FORCE_NETWORK)
                            .build();
                    response = chain.proceed(originalRequest);
                }
            } catch (ConnectException | SocketTimeoutException e) {
                if (shouldUseCacheIfServerError(originalRequest)) {
                    Request forceCacheRequest = originalRequest.newBuilder()
                            .removeHeader(HttpHeaders.CACHE_CONTROL)
                            .removeHeader(HttpHeaders.PRAGMA)
                            .removeHeader(HttpHeaders.IF_NONE_MATCH)
                            .removeHeader(HttpHeaders.IF_MODIFIED_SINCE)
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                    return chain.proceed(forceCacheRequest);
                } else {
                    throw e;
                }
            } catch (IOException e) {
                // 判断是否需要强制调用缓存
                if (shouldUseCacheIfThrowError(originalRequest, e)) {
                    Request forceCacheRequest = originalRequest.newBuilder()
                            .removeHeader(HttpHeaders.CACHE_CONTROL)
                            .removeHeader(HttpHeaders.PRAGMA)
                            .removeHeader(HttpHeaders.IF_NONE_MATCH)
                            .removeHeader(HttpHeaders.IF_MODIFIED_SINCE)
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                    return chain.proceed(forceCacheRequest);
                } else {
                    throw e;
                }
            }
            // 服务器错误
            if (response.code() >= HttpStatus.INTERNAL_SERVER_ERROR.code()/*500*/) {
                // 判断是否需要强制调用缓存
                if (shouldUseCacheIfServerError(originalRequest)) {
                    Request forceCacheRequest = originalRequest.newBuilder()
                            .removeHeader(HttpHeaders.CACHE_CONTROL)
                            .removeHeader(HttpHeaders.PRAGMA)
                            .removeHeader(HttpHeaders.IF_NONE_MATCH)
                            .removeHeader(HttpHeaders.IF_MODIFIED_SINCE)
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                    response = chain.proceed(forceCacheRequest);
                    return response;
                }
            }
            return response;
        }
        return chain.proceed(originalRequest);
    }

    // Override
    protected boolean shouldUseCacheIfServerError(Request originalRequest) {
        return true;
    }

    // Override check network connectivity
    protected boolean shouldUseCacheIfThrowError(Request originalRequest, Throwable throwable) {
        return true;
    }
}
