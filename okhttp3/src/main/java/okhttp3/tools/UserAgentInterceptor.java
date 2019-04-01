package okhttp3.tools;

import java.io.IOException;

import okhttp3.HttpHeaders;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ua.UserAgentDoctor;

/**
 * 应用层拦截器
 */
public final class UserAgentInterceptor implements Interceptor {

    private final UserAgentDoctor userAgentDoctor;

    public UserAgentInterceptor(UserAgentDoctor userAgentDoctor) {
        this.userAgentDoctor = userAgentDoctor;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        if (originalRequest.header(HttpHeaders.USER_AGENT) != null) {
            return chain.proceed(originalRequest);
        }
        Request userAgentRequest = originalRequest.newBuilder()
                .header(HttpHeaders.USER_AGENT, userAgentDoctor.detect())
                .build();
        return chain.proceed(userAgentRequest);
    }
}
