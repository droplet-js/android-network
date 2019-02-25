package okhttp3.tools;

import java.io.IOException;

import okhttp3.HttpHeaders;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * 网络层拦截器
 *
 * Spring Cloud 默认不支持 GZip 压缩
 */
public final class GZipRequestInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        if (originalRequest.body() == null || originalRequest.header(HttpHeaders.CONTENT_ENCODING) != null) {
            return chain.proceed(originalRequest);
        }
        Request compressedRequest = originalRequest.newBuilder()
                .header(HttpHeaders.CONTENT_ENCODING, "gzip")
                .method(originalRequest.method(), new GZipRequestBody(originalRequest.body()))
                .build();
        return chain.proceed(compressedRequest);
    }

    private static class GZipRequestBody extends RequestBody {

        private RequestBody wrapped;

        GZipRequestBody(RequestBody wrapped) {
            super();
            this.wrapped = wrapped;
        }

        @Override
        public MediaType contentType() {
            return wrapped.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            return -1; // We don't know the compressed length in advance!
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
            wrapped.writeTo(gzipSink);
            gzipSink.close();
        }
    }
}
