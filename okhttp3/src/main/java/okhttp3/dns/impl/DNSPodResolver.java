package okhttp3.dns.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.dns.DnsCache;
import okhttp3.dns.HttpAPIResolver;
import okhttp3.util.TextUtils;

// 腾讯 DNSPod - 免费但有次数限制
// http://119.29.29.29/d?dn=www.dnspod.cn&ip=1.1.1.1&ttl=1
public final class DNSPodResolver extends HttpAPIResolver {

    private String appId;
    private String appKey;

    public DNSPodResolver() {
    }

    public DNSPodResolver appId(String appId) {
        this.appId = appId;
        return this;
    }

    public DNSPodResolver appKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    @Override
    public DnsCache.Entry getDns(String host) {
        try {
            if (!TextUtils.isEmpty(appId) && !TextUtils.isEmpty(appKey)) {
                return getDnsPaid(host, appId, appKey);
            } else {
                return getDnsFree(host);
            }
        } catch (Exception e) {
        }
        return null;
    }

    private DnsCache.Entry getDnsFree(String host) throws IOException {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("http")
                .host("119.29.29.29")
                .addPathSegment("d")
                .addQueryParameter("dn", host)
//                .addQueryParameter("ip", generateMchIP())
                .addQueryParameter("ttl", "1")
                .build();
        OkHttpClient client = clientBuilder.build();//new OkHttpClient();
        Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            String body = response.body().string();
            return parse(host, body);
        }
        return null;
    }

    private DnsCache.Entry getDnsPaid(String host, String appId, String appKey) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("http")
                .host("119.29.29.29")
                .addPathSegment("d")
                .addQueryParameter("dn", encrypt(host, appKey))
                .addQueryParameter("id", appId)
//                .addQueryParameter("ip", generateMchIP())
                .addQueryParameter("ttl", "1")
                .build();
        OkHttpClient client = clientBuilder.build();//new OkHttpClient();
        Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            String body = response.body().string();
            return parse(host, decrypt(body, appKey));
        }
        return null;
    }

    private String encrypt(String host, String appKey) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        // 初始化密钥
        SecretKeySpec keySpec = new SecretKeySpec(appKey.getBytes("utf-8"), "DES");
        // 选择使用 DES 算法，ECB 方式，填充方式为 PKCS5Padding
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        // 初始化
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        // 获取加密后的字符串
        byte[] encryptedStr = cipher.doFinal(host.getBytes("utf-8"));
        return bytesToHexStr(encryptedStr);
    }

    private String decrypt(String body, String appKey) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        // 初始化密钥
        SecretKeySpec keySpec = new SecretKeySpec(appKey.getBytes("utf-8"), "DES");
        // 选择使用 DES 算法，ECB 方式，填充方式为 PKCS5Padding
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        // 初始化
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        // 获取解密后的字符串
        byte[] decryptedStr = cipher.doFinal(hexStrToBytes(body));
        return new String(decryptedStr);
    }

    private String bytesToHexStr(byte[] src) {
        StringBuilder builder = new StringBuilder("");
        if (src != null && src.length > 0) {
            for (int i = 0; i < src.length; i++) {
                int v = src[i] & 0xFF;
                String hstr = Integer.toHexString(v);
                if (hstr.length() < 2) {
                    builder.append(0);
                }
                builder.append(hstr);
            }
        }
        return builder.toString();
    }

    private byte[] hexStrToBytes(String hexStr) {
        if (!TextUtils.isEmpty(hexStr)) {
            hexStr = hexStr.toUpperCase();
            int length = hexStr.length() / 2;
            char[] hexChars = hexStr.toCharArray();
            byte[] d = new byte[length];
            for (int i = 0; i < length; i++) {
                int pos = i * 2;
                d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }
            return d;
        }
        return null;
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private DnsCache.Entry parse(String host, String body) throws UnknownHostException {
        if (!TextUtils.isEmpty(body)) {
            String[] values = TextUtils.split(body, ",");
            if (values != null && values.length > 0 && !TextUtils.isEmpty(values[0])) {
                long ttl = DEFAULT_DNS_TTL;
                if (values.length >= 2) {
                    if (!TextUtils.isEmpty(values[1]) &&TextUtils.isDigitsOnly(values[1])) {
                        ttl = Integer.parseInt(values[1]);
                    }
                }
                String[] ips = TextUtils.split(values[0], ";");
                if (ips != null && ips.length > 0) {
                    DnsCache.Entry entry = new DnsCache.Entry(host);
                    for (String ip : ips) {
                        entry.addIP(InetAddress.getByName(ip), ttl);
                    }
                    return !entry.isEmpty() ? entry : null;
                }
            }
        }
        return null;
    }
}
