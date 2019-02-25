package okhttp3.tools;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;

/**
 * 应用层拦截器
 *
 * GZip解压修复
 *
 * 防止 API 服务端下发的数据不符合 HTTP 协议规范
 * 调用 API 服务端下发的数据都是 gzip 压缩的,但是 response 的 header 里面并没有 Content-Encoding:gzip 的 header 头
 */
public final class GZipFixResponseInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Response originalResponse = chain.proceed(originalRequest);
        return originalResponse.newBuilder()
                .body(new GZipFixedResponseBody(originalResponse.body()))
                .build();
    }

    private static class GZipFixedResponseBody extends ResponseBody {

        private final ResponseBody wrapped;
        private boolean shouldFixGZipResp = false;
        private BufferedSource bufferedSource;

        GZipFixedResponseBody(ResponseBody wrapped) {
            super();
            this.wrapped = wrapped;
        }

        @Override
        public MediaType contentType() {
            return wrapped.contentType();
        }

        @Override
        public long contentLength() {
            checkGZipFixed();
            return shouldFixGZipResp ? -1L : wrapped.contentLength();
        }

        @Override
        public BufferedSource source() {
            checkGZipFixed();
            return bufferedSource;
        }

        private void checkGZipFixed() {
            if (bufferedSource == null) {
                try {
                    PushbackInputStream stream = new PushbackInputStream(wrapped.byteStream(), 2);
                    byte[] signature = new byte[2];
                    final int readStatus = stream.read(signature);
                    stream.unread(signature);
                    final int streamHeader = ((int) signature[0] & 0xff) | ((signature[1] << 8) & 0xff00);
                    shouldFixGZipResp = readStatus == 2 && GZIPInputStream.GZIP_MAGIC == streamHeader;
                    if (shouldFixGZipResp) {
                        bufferedSource = Okio.buffer(Okio.source(new GZIPInputStream(stream)));
                    } else {
                        bufferedSource = Okio.buffer(Okio.source(stream));
                    }
                } catch (IOException ignore) {
                    bufferedSource = wrapped.source();
                }
            }
        }
    }
}
