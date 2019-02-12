package io.github.v7lin.network;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormatCache;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.android.WebkitCookieJar;
import okhttp3.android.util.NetworkUtils;
import okhttp3.connectivity.android.AndroidConnectivityDoctor;
import okhttp3.dns.DnsCache;
import okhttp3.dns.MixedDns;
import okhttp3.dns.UDPResolver;
import okhttp3.internal.platform.Platform;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.proxy.android.AndroidProxyDoctor;
import okhttp3.tools.HttpProgressListener;
import okhttp3.tools.OptimizedRequestInterceptor;
import okhttp3.tools.OptimizedResponseInterceptor;
import okhttp3.tools.ProgressRequestInterceptor;
import okhttp3.tools.ProgressResponseInterceptor;
import okhttp3.tools.UserAgentInterceptor;

public class MainActivity extends Activity {

    private OkHttpClient.Builder clientBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .cookieJar(new WebkitCookieJar(this, true))
                .dns(new MixedDns(
                        new DnsCache.Builder()
                                .registerUDPResolver(new UDPResolver(UDPResolver.UDP_ALIYUN_DNS_SERVER_IP_1))
                                .registerUDPResolver(new UDPResolver(UDPResolver.UDP_ALIYUN_DNS_SERVER_IP_2))
                                .build(),
                        new AndroidProxyDoctor(this)
                ))
                .addInterceptor(new UserAgentInterceptor(NetworkUtils.getUserAgent(this)))
                .addInterceptor(new OptimizedRequestInterceptor(new AndroidConnectivityDoctor(this)))
                .addNetworkInterceptor(new OptimizedResponseInterceptor())
                .addNetworkInterceptor(new HttpLoggingInterceptor(HttpLoggingInterceptor.LoggerLevel.BODY))
                .addNetworkInterceptor(new ProgressRequestInterceptor(new HttpProgressListener() {
                    @Override
                    public void onProgressChanged(String url, String method, long progressBytes, long totalBytes, boolean isDone) {
                        Platform.get().log(Platform.INFO, String.format("progress request - %1$s %2$s %3$d/%4$d done:%5$b", method, url, progressBytes, totalBytes, isDone), null);
                    }
                }))
                .addNetworkInterceptor(new ProgressResponseInterceptor(new HttpProgressListener() {
                    @Override
                    public void onProgressChanged(String url, String method, long progressBytes, long totalBytes, boolean isDone) {
                        Platform.get().log(Platform.INFO, String.format("progress response - %1$s %2$s %3$d/%4$d done:%5$b", method, url, progressBytes, totalBytes, isDone), null);
                    }
                }));
        FormatCache.setFormatCache(clientBuilder, new FormatCache(getExternalCacheDir(), 512 * 1024 * 1024, FormatCache.DEFAULT_KEY_FORMATTER));

        findViewById(R.id.http_get).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUrl url = HttpUrl.parse("https://www.baidu.com/");
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                clientBuilder.build()
                        .newCall(request)
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Platform.get().log(Platform.INFO, String.format("error: %1$s", e.toString()), null);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Platform.get().log(Platform.INFO, String.format("resp: %1$d - %2$s- %3$s", response.code(), response.message(), response.body().string()), null);
                            }
                        });
            }
        });

        findViewById(R.id.http_get_json).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUrl url = HttpUrl.parse("https://www.apiopen.top/satinApi?type=1&page=1");
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();
                clientBuilder.build()
                        .newCall(request)
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Platform.get().log(Platform.INFO, String.format("error: %1$s", e.toString()), null);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Platform.get().log(Platform.INFO, String.format("resp: %1$d - %2$s- %3$s", response.code(), response.message(), response.body().string()), null);
                            }
                        });
            }
        });
    }
}
