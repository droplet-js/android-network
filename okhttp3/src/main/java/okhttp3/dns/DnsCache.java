package okhttp3.dns;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DnsCache {

    private final List<HttpResolver> httpDnsProviders;
    private final List<UDPResolver> udpDnsProviders;
    private final List<LocalResolver> localDnsProviders;

    private final Map<String, Entry> dnsMapByHost = new ConcurrentHashMap<>();

    private DnsCache(List<HttpResolver> httpDnsProviders, List<UDPResolver> udpDnsProviders, List<LocalResolver> localDnsProviders) {
        super();
        this.httpDnsProviders = httpDnsProviders;
        this.udpDnsProviders = udpDnsProviders;
        this.localDnsProviders = localDnsProviders;

        registerBuiltInResolver();
    }

    private void registerBuiltInResolver() {
    }

    public final Entry resolveRemoteHost(String host) {
        Entry entry = get(host);
        if (entry == null || entry.isEmpty()) {
            entry = getHttpServer(host);
            if (entry == null || entry.isEmpty()) {
                entry = getUDPServer(host);
            }
            if (entry != null && entry.shouldCache() && !entry.isEmpty()) {
                put(host, entry);
            }
        }
        return entry != null && !entry.isEmpty() ? entry : null;
    }

    public final Entry resolveLocalHost(String host) {
        return getLocalServer(host);
    }

    private Entry getHttpServer(String host) {
        for (Resolver resolver : httpDnsProviders) {
            Entry entry = resolver.getDns(host);
            if (entry != null && !entry.isEmpty()) {
                return entry;
            }
        }
        return null;
    }

    private Entry getUDPServer(String host) {
        for (Resolver resolver : udpDnsProviders) {
            Entry entry = resolver.getDns(host);
            if (entry != null && !entry.isEmpty()) {
                return entry;
            }
        }
        return null;
    }

    private Entry getLocalServer(String host) {
        for (Resolver resolver : localDnsProviders) {
            Entry entry = resolver.getDns(host);
            if (entry != null && !entry.isEmpty()) {
                return entry;
            }
        }
        return null;
    }

    private Entry get(String host) {
        Entry entry = dnsMapByHost.get(host);
        if (entry != null) {
            entry.makeUpToDate();
        }
        return entry;
    }

    private void put(String host, Entry entry) {
        dnsMapByHost.put(host, entry);
    }

    public final void clear() {
        dnsMapByHost.clear();
    }

    public enum Type {
        LOCAL,
        HTTP,
        UDP
    }

    public static abstract class Resolver {

        public static final long DEFAULT_DNS_TTL = 60;

        public abstract Entry getDns(String host);

        public abstract Type getType();
    }

    private static final class IP {

        private InetAddress ip;
        private long ttl;

        public IP(InetAddress ip, long ttl) {
            super();
            this.ip = ip;
            this.ttl = ttl;
        }

        public InetAddress getIP() {
            return ip;
        }

        public boolean isOverdue(long lookupTime, long reqTime) {
            return lookupTime > reqTime + ttl * 1000;
        }
    }

    public static final class Entry {

        private String host;
        private final List<IP> ips;
        private long reqTime;
        private boolean shouldCache = true;

        public Entry(String host) {
            super();
            this.host = host;
            ips = new ArrayList<>();
            reqTime = System.currentTimeMillis();
        }

        public Entry addIP(InetAddress ip, long ttl) {
            ips.add(new IP(ip, ttl));
            return this;
        }

        public Entry noCache() {
            shouldCache = false;
            return this;
        }

        public Entry makeUpToDate() {
            List<IP> overdueIPs = new ArrayList<>();
            final long lookupTime = System.currentTimeMillis();
            for (IP ip : ips) {
                if (ip.isOverdue(lookupTime, reqTime)) {
                    overdueIPs.add(ip);
                }
            }
            ips.removeAll(overdueIPs);
            return this;
        }

        public List<IP> getIPs() {
            return ips;
        }

        public boolean isEmpty() {
            return ips == null || ips.isEmpty();
        }

        public boolean shouldCache() {
            return shouldCache;
        }

        public List<InetAddress> convert() {
            List<InetAddress> inetAddresses = new ArrayList<>(ips.size());
            for (IP ip : ips) {
                inetAddresses.add(ip.getIP());
            }
            return inetAddresses;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("host:").append(host);
            builder.append(",ips:{");
            for (IP ip : ips) {
                builder.append(ip.getIP()).append(",");
            }
            builder.append("}");
            return builder.toString();
        }
    }

    public static final class Builder {
        private final List<HttpResolver> httpDnsProviders = new ArrayList<>();
        private final List<UDPResolver> udpDnsProviders = new ArrayList<>();
        private final List<LocalResolver> localDnsProviders = new ArrayList<>();

        public Builder() {
        }

        public Builder registerHttpResolver(HttpResolver httpResolver) {
            httpDnsProviders.add(httpResolver);
            return this;
        }

        public Builder registerUDPResolver(UDPResolver udpResolver) {
            udpDnsProviders.add(udpResolver);
            return this;
        }

        public Builder registerLocalResolver(LocalResolver localResolver) {
            localDnsProviders.add(localResolver);
            return this;
        }

        public DnsCache build() {
            return new DnsCache(Collections.unmodifiableList(httpDnsProviders), Collections.unmodifiableList(udpDnsProviders), Collections.unmodifiableList(localDnsProviders));
        }
    }
}
