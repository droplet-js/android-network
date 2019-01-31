package okhttp3.dns;

import java.net.InetAddress;

import okhttp3.util.TextUtils;

public final class LocalResolver extends DnsCache.Resolver {

    private String host;
    private String ip;

    public LocalResolver(String host, String ip) {
        super();
        this.host = host;
        this.ip = ip;
    }

    @Override
    public DnsCache.Entry getDns(String host) {
        try {
            if (TextUtils.equals(host, this.host)) {
                DnsCache.Entry entry = new DnsCache.Entry(this.host).noCache();
                entry.addIP(InetAddress.getByName(ip), DEFAULT_DNS_TTL);
                return entry;
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public DnsCache.Type getType() {
        return DnsCache.Type.LOCAL;
    }
}
