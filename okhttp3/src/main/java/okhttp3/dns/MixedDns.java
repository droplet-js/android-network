package okhttp3.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import okhttp3.Dns;
import okhttp3.proxy.ProxyDoctor;

public final class MixedDns implements Dns {

    private final DnsCache dnsCache;
    private final ProxyDoctor proxyDoctor;

    public MixedDns(@Nonnull DnsCache dnsCache, @Nonnull ProxyDoctor proxyDoctor) {
        this.dnsCache = dnsCache;
        this.proxyDoctor = proxyDoctor;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        List<InetAddress> results = new ArrayList<InetAddress>();
        if (!proxyDoctor.detect()) {
            DnsCache.Entry remoteEntry = dnsCache.resolveRemoteHost(hostname);
            if (remoteEntry != null && !remoteEntry.isEmpty()) {
                results.addAll(remoteEntry.convert());
            }
            DnsCache.Entry localEntry = dnsCache.resolveLocalHost(hostname);
            if (localEntry != null && !localEntry.isEmpty()) {
                results.addAll(localEntry.convert());
            }
        }
        results.addAll(Dns.SYSTEM.lookup(hostname));
        return results;
    }
}
