package okhttp3.logging;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.IdentityHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.HttpHeaders;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.impl.PlatformLoggerFactory;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

/**
 * 不要和 okhttp-logging-interceptor 中的 HttpLoggingInterceptor 共用
 */
public final class HttpLoggingInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final LoggerFactory factory;
    private final LoggerLevel level;

    public HttpLoggingInterceptor() {
        this(LoggerFactory.LOGCAT, LoggerLevel.BASIC);
    }

    public HttpLoggingInterceptor(LoggerLevel level) {
        this(LoggerFactory.LOGCAT, level);
    }

    public HttpLoggingInterceptor(LoggerFactory factory, LoggerLevel level) {
        this.factory = factory != null ? factory : LoggerFactory.LOGCAT;
        this.level = level != null ? level : LoggerLevel.BASIC;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (level == LoggerLevel.NONE) {
            return chain.proceed(request);
        }

        Logger logger = factory.logger(request.method(), request.url().toString());

        boolean logBody = level == LoggerLevel.BODY;
        boolean logHeaders = logBody || level == LoggerLevel.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        if (!logHeaders && hasRequestBody) {
            logger.start(requestBody.contentLength() + "-byte body");
        } else {
            logger.start(null);
        }

        Connection connection = chain.connection();
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        logger.connection(protocol.toString());

        if (logHeaders) {
            IdentityHashMap<String, String> requestHeaders = new IdentityHashMap<>();
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    requestHeaders.put(HttpHeaders.CONTENT_TYPE, requestBody.contentType().toString());
                }
                if (requestBody.contentLength() != -1) {
                    requestHeaders.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(requestBody.contentLength()));
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name) && !HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
                    requestHeaders.put(name, headers.value(i));
                }
            }

            logger.requestHeaders(requestHeaders);

            if (!logBody || !hasRequestBody) {
                logger.requestOmitted(null);
            } else if (bodyEncoded(request.headers())) {
                logger.requestOmitted("encoded body omitted");
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (isPlaintext(contentType, buffer)) {
                    logger.requestPlaintextBody(buffer.readString(charset));
                    logger.requestOmitted("plaintext " + requestBody.contentLength() + "-byte body");
                } else {
                    logger.requestOmitted("binary " + requestBody.contentLength() + "-byte body");
                }
            }
        }

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
            logger.response();
        } catch (Exception e) {
            logger.error(e);
            logger.end();
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        final long contentLength = responseBody.contentLength();
        String message = contentLength != -1 ? contentLength + "-byte body" : "unknown-length body";
        logger.status(response.code(), response.message(), contentLength, tookMs, message);

        IdentityHashMap<String, String> responseHeaders = new IdentityHashMap<>();
        if (logHeaders) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                responseHeaders.put(headers.name(i), headers.value(i));
            }
            logger.responseHeaders(responseHeaders);

            if (!logBody || !okhttp3.internal.http.HttpHeaders.hasBody(response)) {
                logger.responseOmitted(null);
            } else if (bodyEncoded(response.headers())) {
                logger.responseOmitted("encoded body omitted");
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                Long gzippedLength = null;
                if ("gzip".equalsIgnoreCase(headers.get(HttpHeaders.CONTENT_ENCODING))) {
                    gzippedLength = buffer.size();
                    try (GzipSource gzippedResponseBody = new GzipSource(buffer.clone())) {
                        buffer = new Buffer();
                        buffer.writeAll(gzippedResponseBody);
                    }
                }

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (!isPlaintext(contentType, buffer)) {
                    logger.responseOmitted("binary " + buffer.size() + "-byte body omitted");
                } else {
                    if (contentLength != 0) {
                        logger.responsePlaintextBody(buffer.clone().readString(charset));
                    }

                    if (gzippedLength != null) {
                        logger.responseOmitted(buffer.size() + "-byte, " + gzippedLength + "-gzipped-byte body");
                    } else {
                        logger.responseOmitted(buffer.size() + "-byte body");
                    }
                }
            }
        }
        logger.end();

        return response;
    }

    private boolean isPlaintext(MediaType contentType, Buffer buffer) {
        if (contentType != null && ("text".equalsIgnoreCase(contentType.type()) || "json".equalsIgnoreCase(contentType.subtype()))) {
            try {
                Buffer prefix = new Buffer();
                long byteCount = buffer.size() < 64 ? buffer.size() : 64;
                buffer.copyTo(prefix, 0, byteCount);
                for (int i = 0; i < 16; i++) {
                    if (prefix.exhausted()) {
                        break;
                    }
                    int codePoint = prefix.readUtf8CodePoint();
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false;
                    }
                }
                return true;
            } catch (EOFException e) {
                return false; // Truncated UTF-8 sequence.
            }
        }
        return false;
    }

    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get(HttpHeaders.CONTENT_ENCODING);
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }

    public enum LoggerLevel {
        NONE,
        BASIC,
        HEADERS,
        BODY
    }

    public interface LoggerFactory {
        public Logger logger(String method, String url);

        public static LoggerFactory LOGCAT = new PlatformLoggerFactory();
    }

    public static abstract class Logger {

        protected final String method;
        protected final String url;

        public Logger(String method, String url) {
            this.method = method;
            this.url = url;
        }

        public abstract void start(String message);
        public abstract void connection(String protocol);
        public abstract void requestHeaders(IdentityHashMap<String, String> requestHeaders);
        public abstract void requestPlaintextBody(String plaintext);
        public abstract void requestOmitted(String message);
        public abstract void response();
        public abstract void error(Exception e);
        public abstract void status(int statusCode, String reasonPhrase, long contentLength, long tookMs, String message);
        public abstract void responseHeaders(IdentityHashMap<String, String> responseHeaders);
        public abstract void responsePlaintextBody(String plaintext);
        public abstract void responseOmitted(String message);
        public abstract void end();
    }
}
