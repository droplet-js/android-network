package okhttp3.tools;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class ReplaceHostInterceptor implements Interceptor {

    private final Map<String, String> replaceHostMap = new HashMap<>();

    public ReplaceHostInterceptor() {
    }

    public void add(String host, String replaceHost) {
        replaceHostMap.put(host, replaceHost);
    }

    public void remove(String host) {
        replaceHostMap.remove(host);
    }

    public void clear() {
        replaceHostMap.clear();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        HttpUrl originalHttpUrl = originalRequest.url();
        if (replaceHostMap.containsKey(originalHttpUrl.host())) {
            HttpUrl replaceHttpUrl = originalHttpUrl.newBuilder()
                    .host(replaceHostMap.get(originalHttpUrl.host()))
                    .build();
            Response replaceResponse = chain.proceed(originalRequest.newBuilder()
                    .url(replaceHttpUrl)
                    .build());
            return replaceResponse.newBuilder()
                    .request(replaceResponse.request()
                            .newBuilder()
                            .url(originalHttpUrl)
                            .build())
                    .build();
        } else {
            return chain.proceed(originalRequest);
        }
    }
}
