package okhttp3.tools;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class ReplaceUrlInterceptor implements Interceptor {

    private final Map<String, String> replaceMap = new HashMap<>();

    public ReplaceUrlInterceptor() {
    }

    public void add(String target, String replacement) {
        replaceMap.put(target, replacement);
    }

    public void remove(String target) {
        replaceMap.remove(target);
    }

    public void clear() {
        replaceMap.clear();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        HttpUrl originalHttpUrl = originalRequest.url();
        String originalUrl = originalHttpUrl.toString();
        for (Map.Entry<String, String> replaceEntry : replaceMap.entrySet()) {
            if (originalUrl.startsWith(replaceEntry.getKey())) {
                HttpUrl replaceHttpUrl = HttpUrl.parse(originalUrl.replace(replaceEntry.getKey(), replaceEntry.getValue()));
                Response replaceResponse = chain.proceed(originalRequest.newBuilder()
                        .url(replaceHttpUrl)
                        .build());
                return replaceResponse.newBuilder()
                        .request(replaceResponse.request()
                                .newBuilder()
                                .url(originalHttpUrl)
                                .build())
                        .build();
            }
        }
        return chain.proceed(originalRequest);
    }
}
