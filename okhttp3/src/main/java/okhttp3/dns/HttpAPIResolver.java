package okhttp3.dns;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.cookie.PersistentCookieJar;
import okhttp3.util.TextUtils;

public abstract class HttpAPIResolver extends HttpResolver {

    protected OkHttpClient.Builder clientBuilder;

    public HttpAPIResolver() {
        this.clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .cookieJar(PersistentCookieJar.memory());
    }

    public HttpAPIResolver interceptor(Interceptor interceptor) {
        clientBuilder.addInterceptor(interceptor);
        return this;
    }

    public HttpAPIResolver networkInterceptor(Interceptor networkInterceptor) {
        clientBuilder.addNetworkInterceptor(networkInterceptor);
        return this;
    }

    public HttpAPIResolver cookieJar(CookieJar cookieJar) {
        clientBuilder.cookieJar(cookieJar);
        return this;
    }

    public HttpAPIResolver cache(Cache cache) {
        clientBuilder.cache(cache);
        return this;
    }

    public final String generateMchIP() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (nifs.hasMoreElements()) {
                NetworkInterface nif = nifs.nextElement();// 得到每一个网络接口绑定的所有ip
                if (nif != null) {
                    // usbnet 须排除
                    if (nif.getDisplayName().startsWith("eth") // 以太网
                            || nif.getDisplayName().startsWith("wlan") // WiFi
                            || nif.getDisplayName().startsWith("ccmni")
                            || nif.getDisplayName().contains("rmnet")) {
                        // 获取有效的链接
                        Enumeration<InetAddress> ips = nif.getInetAddresses();
                        // 遍历每一个接口绑定的所有ip
                        while (ips.hasMoreElements()) {
                            InetAddress ip = ips.nextElement();
                            if (ip != null && !ip.isLoopbackAddress() && ip instanceof Inet4Address) {
                                ipaddress = ip.getHostAddress();
                                break;
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(ipaddress)) {
            ipaddress = "127.0.0.1";
        }
        return ipaddress;
    }
}
