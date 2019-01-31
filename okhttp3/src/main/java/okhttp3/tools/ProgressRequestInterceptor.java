package okhttp3.tools;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 网络层拦截器
 *
 * 请求进度
 */
public final class ProgressRequestInterceptor implements Interceptor {

    private final HttpProgressListener listener;

    public ProgressRequestInterceptor(HttpProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        if (originalRequest.body() == null) {
            return chain.proceed(originalRequest);
        }
        Request progressRequest = originalRequest.newBuilder()
                .method(originalRequest.method(), new ProgressRequestBody(originalRequest.body(), new CallbackAdapter(originalRequest.url().toString(), originalRequest.method(), listener)))
                .build();
        return chain.proceed(progressRequest);
    }

    private static class CallbackAdapter implements Callback {

        private final String url;
        private final String method;
        private final HttpProgressListener listener;

        public CallbackAdapter(String url, String method, HttpProgressListener listener) {
            this.url = url;
            this.method = method;
            this.listener = listener;
        }

        @Override
        public void onWrite(long progressBytes, long totalBytes) {
            if (listener != null) {
                listener.onProgressChanged(url, method, progressBytes, totalBytes, false);
            }
        }

        @Override
        public void onClose(long progressBytes, long totalBytes) {
            if (listener != null) {
                listener.onProgressChanged(url, method, progressBytes, totalBytes, true);
            }
        }
    }

    private static class ProgressRequestBody extends RequestBody {

        private final RequestBody wrapped;
        private final Callback callback;

        public ProgressRequestBody(RequestBody wrapped, Callback callback) {
            super();
            this.wrapped = wrapped;
            this.callback = callback;
        }

        @Override
        public MediaType contentType() {
            return wrapped.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            return wrapped.contentLength();//super.contentLength();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            BufferedSink bufferedSink = Okio.buffer(new ProgressForwardingSink(sink, contentLength(), callback));
            wrapped.writeTo(bufferedSink);
            bufferedSink.close();
        }
    }

    private static class ProgressForwardingSink extends ForwardingSink {

        private final long totalBytes;
        private final Callback callback;

        private long progressBytes = 0L;

        public ProgressForwardingSink(Sink delegate, long totalBytes, Callback callback) {
            super(delegate);
            this.totalBytes = totalBytes;
            this.callback = callback;
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            progressBytes += byteCount;
//            boolean isDone = progressBytes == totalBytes;
            if (callback != null) {
                callback.onWrite(progressBytes, totalBytes);
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (callback != null) {
                callback.onClose(progressBytes, totalBytes);
            }
        }
    }

    interface Callback {
        public void onWrite(long progressBytes, long totalBytes);
        public void onClose(long progressBytes, long totalBytes);
    }
}
