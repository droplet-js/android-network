package okhttp3;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.connectivity.ConnectivityDoctor;
import okhttp3.cookie.PersistentCookieJar;
import okhttp3.internal.platform.Platform;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.tools.HttpProgressListener;
import okhttp3.tools.OptimizedRequestInterceptor;
import okhttp3.tools.OptimizedResponseInterceptor;
import okhttp3.tools.ProgressRequestInterceptor;
import okhttp3.tools.ProgressResponseInterceptor;
import okhttp3.tools.UserAgentInterceptor;

/**
 * 单元测试结果见 {project}/build/reports/tests/test/index.html
 */
@RunWith(JUnit4.class)
public class ExampleUnitTest {

    private OkHttpClient.Builder clientBuilder;

    @Before
    public void setUp() throws Exception {
        clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .cookieJar(PersistentCookieJar.memory())
                .addInterceptor(new UserAgentInterceptor("xxx"))
                .addInterceptor(new OptimizedRequestInterceptor(new ConnectivityDoctor() {
                    @Override
                    public boolean detect() {
                        return true;
                    }
                }))
                .addInterceptor(new OptimizedResponseInterceptor())
                .addInterceptor(new HttpLoggingInterceptor(HttpLoggingInterceptor.LoggerLevel.BODY))
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
//        FormatCache.setFormatCache(clientBuilder, new FormatCache(new File("/xxx/cache"), 512 * 1024 * 1024, FormatCache.DEFAULT_KEY_FORMATTER));
    }

    @Test
    public void httpGet() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
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
                        countDownLatch.countDown();
                        Platform.get().log(Platform.INFO, String.format("error: %1$s", e.toString()), null);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        countDownLatch.countDown();
                        Platform.get().log(Platform.INFO, String.format("resp: %1$d - %2$s- %3$s", response.code(), response.message(), response.body().string()), null);
                    }
                });
        countDownLatch.await();
    }

    @Test
    public void httpGetJson() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
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
                        countDownLatch.countDown();
                        Platform.get().log(Platform.INFO, String.format("error: %1$s", e.toString()), null);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        countDownLatch.countDown();
                        Platform.get().log(Platform.INFO, String.format("resp: %1$d - %2$s- %3$s", response.code(), response.message(), response.body().string()), null);
                    }
                });
        countDownLatch.await();
    }

//    @Test
//    public void unitFail() {
////        Assert.assertEquals("单元测试失败打断命令", "单元测试失败");
//        throw new RuntimeException("单元测试失败打断命令");
//    }

    @After
    public void tearDown() throws Exception {

    }
}