package okhttp3.dns.impl;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.dns.DnsCache;
import okhttp3.dns.HttpAPIResolver;
import okhttp3.util.TextUtils;

// 阿里云 HttpDNS - 免费但有次数限制
// http://203.107.1.1/{account_id}/d?host=www.taobao.com&ip=42.120.74.196
public final class AliyunResolver extends HttpAPIResolver {

    public static final String ACCOUNT_ID_1 = "100000";
    public static final String ACCOUNT_ID_2 = "139450";

    private final String accountId;

    private AliyunResolver(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public DnsCache.Entry getDns(String host) {
        try {
            if (!TextUtils.isEmpty(accountId)) {
                return getDnsActual(accountId, host);
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    private DnsCache.Entry getDnsActual(String accountId, String host) {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("http")
                .host("203.107.1.1")
                .addPathSegment(accountId)// account_id 阿里云控制台注册
                .addPathSegment("d")
                .addQueryParameter("host", host)
//                .addQueryParameter("ip", generateMchIP())
                .build();
        OkHttpClient client = clientBuilder.build();//new OkHttpClient();
        Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                String body = response.body().string();
                JSONObject jsonObject = new JSONObject(body);

                long ttl = DEFAULT_DNS_TTL;
                String ttlStr = !jsonObject.isNull("ttl") ? jsonObject.optString("ttl") : null;
                if (!TextUtils.isEmpty(ttlStr) && TextUtils.isDigitsOnly(ttlStr)) {
                    ttl = Integer.parseInt(ttlStr);
                }

                JSONArray ips = !jsonObject.isNull("ips") ? jsonObject.optJSONArray("ips") : null;
                if (ips != null && ips.length() > 0) {
                    DnsCache.Entry entry = new DnsCache.Entry(host);
                    for (int i = 0; i < ips.length(); i ++) {
                        String ip = ips.optString(i);
                        entry.addIP(InetAddress.getByName(ip), ttl);
                    }
                    return !entry.isEmpty() ? entry : null;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static final class Builder {

        private String accountId;

        public Builder() {
        }

        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        public AliyunResolver build() {
            return new AliyunResolver(accountId);
        }
    }
}
