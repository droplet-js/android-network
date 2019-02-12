package okhttp3.proxy;

/**
 * 检测系统是否已经设置代理，请参考HttpDNS API文档。
 */
public interface ProxyDoctor {
    public boolean detect();
}
