package okhttp3.tools;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 网络层拦截器
 *
 * 响应进度
 */
public final class ProgressResponseInterceptor implements Interceptor {

    private final HttpProgressListener listener;

    public ProgressResponseInterceptor(HttpProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Response originalResponse = chain.proceed(originalRequest);
        return originalResponse.newBuilder()
                .body(new ProgressResponseBody(originalResponse.body(), new CallbackAdapter(originalRequest.url().toString(), originalRequest.method(), listener)))
                .build();
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
        public void onRead(long progressBytes, long totalBytes) {
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

    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody wrapped;
        private final Callback callback;

        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody wrapped, Callback callback) {
            super();
            this.wrapped = wrapped;
            this.callback = callback;
        }

        @Override
        public MediaType contentType() {
            return wrapped.contentType();
        }

        @Override
        public long contentLength() {
            return wrapped.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(new ProgressForwardingSource(wrapped.source(), contentLength(), callback));
            }
            return bufferedSource;
        }
    }

    private static class ProgressForwardingSource extends ForwardingSource {

        private final long totalBytes;
        private final Callback callback;

        private long progressBytes = 0L;

        public ProgressForwardingSource(Source delegate, long totalBytes, Callback callback) {
            super(delegate);
            this.totalBytes = totalBytes;
            this.callback = callback;
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            long bytesRead = super.read(sink, byteCount);
            progressBytes += bytesRead != -1 ? bytesRead : 0;
//            boolean isDone = bytesRead == -1;
            if (callback != null) {
                callback.onRead(progressBytes, totalBytes);
            }
            return bytesRead;
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
        public void onRead(long progressBytes, long totalBytes);
        public void onClose(long progressBytes, long totalBytes);
    }
}
