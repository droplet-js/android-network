package okhttp3.dns;

public abstract class HttpResolver extends DnsCache.Resolver {

    @Override
    public DnsCache.Type getType() {
        return DnsCache.Type.HTTP;
    }
}
