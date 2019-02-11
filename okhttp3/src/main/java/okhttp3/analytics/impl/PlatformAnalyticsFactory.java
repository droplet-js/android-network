package okhttp3.analytics.impl;

import okhttp3.analytics.AnalyticsInterceptor;

public final class PlatformAnalyticsFactory implements AnalyticsInterceptor.AnalyticsFactory {

    @Override
    public AnalyticsInterceptor.Analytics analytics(String method, String url) {
        return new PlatformAnalytics(method, url);
    }
}
