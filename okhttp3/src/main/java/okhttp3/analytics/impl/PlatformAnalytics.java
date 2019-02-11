package okhttp3.analytics.impl;

import java.util.IdentityHashMap;
import java.util.Map;

import okhttp3.analytics.AnalyticsInterceptor;
import okhttp3.internal.platform.Platform;
import okhttp3.util.TextUtils;

final class PlatformAnalytics implements AnalyticsInterceptor.Analytics {

    @Override
    public void start(String method, String url, String message) {
        Platform.get().log(Platform.INFO, "--> " + method + " " + url + (!TextUtils.isEmpty(message) ? " (" + message + ")" : ""), null);
    }

    @Override
    public void connection(String protocol) {
        Platform.get().log(Platform.INFO, "Protocol " + protocol, null);
    }

    @Override
    public void requestHeaders(IdentityHashMap<String, String> requestHeaders) {
        for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
            Platform.get().log(Platform.INFO, requestHeader.getKey() + ": " + requestHeader.getValue(), null);
        }
    }

    @Override
    public void requestPlaintextBody(String plaintext) {
        Platform.get().log(Platform.INFO, plaintext, null);
    }

    @Override
    public void requestOmitted(String method, String message) {
        Platform.get().log(Platform.INFO, "--> END " + method + (!TextUtils.isEmpty(message) ? " (" + message + ")" : ""), null);
    }

    @Override
    public void response() {

    }

    @Override
    public void error(String url, Exception e) {
        Platform.get().log(Platform.INFO, "<-- HTTP FAILED: " + url + " " + e, null);
    }

    @Override
    public void status(int statusCode, String reasonPhrase, String url, long tookMs, String message) {
        Platform.get().log(Platform.INFO, "<-- " + statusCode + " " + reasonPhrase + " " + url + " (" + tookMs + "ms, " + message + ")", null);
    }

    @Override
    public void responseHeaders(IdentityHashMap<String, String> responseHeaders) {
        for (Map.Entry<String, String> responseHeader : responseHeaders.entrySet()) {
            Platform.get().log(Platform.INFO, responseHeader.getKey() + ": " + responseHeader.getValue(), null);
        }
    }

    @Override
    public void responsePlaintextBody(String plaintext) {
        Platform.get().log(Platform.INFO, plaintext, null);
    }

    @Override
    public void responseOmitted(String method, String message) {
        Platform.get().log(Platform.INFO, "<-- END " + method + (!TextUtils.isEmpty(message) ? " (" + message + ")" : ""), null);
    }

    @Override
    public void finish(long contentLength) {

    }

    @Override
    public void end() {

    }
}
