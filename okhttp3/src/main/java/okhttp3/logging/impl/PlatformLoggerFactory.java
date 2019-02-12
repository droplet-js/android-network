package okhttp3.logging.impl;

import okhttp3.logging.HttpLoggingInterceptor;

public final class PlatformLoggerFactory implements HttpLoggingInterceptor.LoggerFactory {

    @Override
    public HttpLoggingInterceptor.Logger logger(String method, String url) {
        return new PlatformLogger(method, url);
    }
}
