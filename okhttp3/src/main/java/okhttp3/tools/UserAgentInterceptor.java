package okhttp3.tools;

import java.io.IOException;

import okhttp3.HttpHeaders;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 应用层拦截器
 */
public final class UserAgentInterceptor implements Interceptor {

    private final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        if (originalRequest.header(HttpHeaders.USER_AGENT) != null) {
            return chain.proceed(originalRequest);
        }
        Request userAgentRequest = originalRequest.newBuilder()
                .header(HttpHeaders.USER_AGENT, userAgent)
                .build();
        return chain.proceed(userAgentRequest);
    }
}
