package okhttp3.tools;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okhttp3.util.TextUtils;
import okio.Buffer;

/**
 * 应用层拦截器
 */
public abstract class CipherCryptInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder secureRequestBuilder = originalRequest.newBuilder();
        if (TextUtils.equals(originalRequest.method(), "POST")) {
            RequestBody requestBody = originalRequest.body();
            if (requestBody != null) {
                secureRequestBuilder.post(encryptRequest(originalRequest, requestBody));
            }
        } else if (TextUtils.equals(originalRequest.method(), "DELETE")) {
            RequestBody requestBody = originalRequest.body();
            if (requestBody != null && !requestBody.equals(Util.EMPTY_REQUEST)) {
                secureRequestBuilder.delete(encryptRequest(originalRequest, requestBody));
            }
        } else if (TextUtils.equals(originalRequest.method(), "PUT")) {
            RequestBody requestBody = originalRequest.body();
            if (requestBody != null) {
                secureRequestBuilder.put(encryptRequest(originalRequest, requestBody));
            }
        } else if (TextUtils.equals(originalRequest.method(), "PATCH")) {
            RequestBody requestBody = originalRequest.body();
            if (requestBody != null) {
                secureRequestBuilder.patch(encryptRequest(originalRequest, requestBody));
            }
        }
        Response secureResponse = chain.proceed(secureRequestBuilder.build());
        Response.Builder originalResponseBuilder = secureResponse.newBuilder()
                .request(originalRequest);
        ResponseBody responseBody = secureResponse.body();
        if (responseBody != null) {
            originalResponseBuilder.body(decryptResponse(secureResponse, responseBody));
        }
        return originalResponseBuilder.build();
    }

    private RequestBody encryptRequest(Request originalRequest, RequestBody requestBody) throws IOException {
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        return RequestBody.create(requestBody.contentType(), encrypt(originalRequest, buffer.readByteArray()));
    }

    private ResponseBody decryptResponse(Response secureResponse, ResponseBody responseBody) throws IOException {
        return ResponseBody.create(responseBody.contentType(), decrypt(secureResponse, responseBody.bytes()));
    }

    public abstract byte[] encrypt(Request originalRequest, byte[] buffer);

    public abstract byte[] decrypt(Response secureResponse, byte[] buffer);
}
