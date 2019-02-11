package okhttp3.dns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import okio.Buffer;
import okio.Okio;
import okio.Utf8;

// UDP dns
// 114.114.114.114
public final class UDPResolver extends DnsCache.Resolver {

    // --------- 国内 ---------
    // DNSPod
    public static final String UDP_DNSPOD_DNS_SERVER_IP_1 = "119.29.29.29";
    public static final String UDP_DNSPOD_DNS_SERVER_IP_2 = "182.254.116.116";

    // 114 DNS
    public static final String UDP_114_DNS_SERVER_IP_1 = "114.114.114.114";
    public static final String UDP_114_DNS_SERVER_IP_2 = "114.114.115.115";

    // 阿里云 DNS
    public static final String UDP_ALIYUN_DNS_SERVER_IP_1 = "223.5.5.5";
    public static final String UDP_ALIYUN_DNS_SERVER_IP_2 = "223.6.6.6";

    // 百度 DNS
    public static final String UDP_BAIDU_DNS_SERVER_IP = "180.76.76.76";

    // --------- 国外 ---------
    // Google DNS
    public static final String UDP_GOOGLE_DNS_SERVER_IP_1 = "8.8.8.8";
    public static final String UDP_GOOGLE_DNS_SERVER_IP_2 = "8.8.4.4";

    // Open DNS
    public static final String UDP_OPEN_DNS_SERVER_IP = "208.67.220.220";

    private final static int DNS_SERVER_PORT = 53;

    private final static int TIME_OUT = 2000;

    private static final int BUF_SIZE = 1024;

    private final String serverIP;
    private final boolean includeIPv6;

    public UDPResolver(String serverIP) {
        this(serverIP, false);
    }

    public UDPResolver(String serverIP, boolean includeIPv6) {
        this.serverIP = serverIP;
        this.includeIPv6 = includeIPv6;
    }

    @Override
    public DnsCache.Entry getDns(String host) {
        try {
            return getDnsActual(host);
        } catch (Exception e) {
        }
        return null;
    }

    private DnsCache.Entry getDnsActual(String host) {
        try {
            DatagramSocket socket = new DatagramSocket(0);
            socket.setSoTimeout(TIME_OUT);

            ByteArrayOutputStream outBuf = new ByteArrayOutputStream(BUF_SIZE);
            DataOutputStream output = new DataOutputStream(outBuf);
            DnsRecordCodec.encodeQuery(host, includeIPv6, output);

            InetAddress address = InetAddress.getByName(serverIP);
            DatagramPacket request = new DatagramPacket(outBuf.toByteArray(), outBuf.size(), address, DNS_SERVER_PORT);

            socket.send(request);

            byte[] inBuf = new byte[BUF_SIZE];
            ByteArrayInputStream inBufArray = new ByteArrayInputStream(inBuf);
            DataInputStream input = new DataInputStream(inBufArray);
            DatagramPacket response = new DatagramPacket(inBuf, inBuf.length);

            socket.receive(response);

            DnsCache.Entry entry = DnsRecordCodec.decodeAnswers(host, input);

            socket.close();

            return entry;
        } catch (SocketException e) {
        } catch (IOException e) {
        }
        return null;
    }

    @Override
    public DnsCache.Type getType() {
        return DnsCache.Type.UDP;//super.getType();
    }

    /**
     * 摘自 okhttp-dnsoverhttps
     */
    private static final class DnsRecordCodec {
        private static final byte SERVFAIL = 2;
        private static final byte NXDOMAIN = 3;
        private static final int TYPE_A = 0x0001;
        private static final int TYPE_AAAA = 0x001c;
        private static final int TYPE_PTR = 0x000c;
        private static final Charset ASCII = Charset.forName("ASCII");

        private DnsRecordCodec() {
        }

        public static void encodeQuery(String host, boolean includeIPv6, DataOutputStream output) throws IOException {
            Buffer buf = new Buffer();

            buf.writeShort(0); // query id
            buf.writeShort(256); // flags with recursion
            buf.writeShort(includeIPv6 ? 2 : 1); // question count
            buf.writeShort(0); // answerCount
            buf.writeShort(0); // authorityResourceCount
            buf.writeShort(0); // additional

            Buffer nameBuf = new Buffer();
            final String[] labels = host.split("\\.");
            for (String label : labels) {
                long utf8ByteCount = Utf8.size(label);
                if (utf8ByteCount != label.length()) {
                    throw new IllegalArgumentException("non-ascii hostname: " + host);
                }
                nameBuf.writeByte((byte) utf8ByteCount);
                nameBuf.writeUtf8(label);
            }
            nameBuf.writeByte(0); // end

            nameBuf.copyTo(buf, 0, nameBuf.size());
            buf.writeShort(TYPE_A);
            buf.writeShort(1); // CLASS_IN

            if (includeIPv6) {
                nameBuf.copyTo(buf, 0, nameBuf.size());
                buf.writeShort(TYPE_AAAA);
                buf.writeShort(1); // CLASS_IN
            }

            output.write(buf.readByteArray());
        }

        public static DnsCache.Entry decodeAnswers(String host, DataInputStream input) throws IOException {
            DnsCache.Entry entry = new DnsCache.Entry(host);

            Buffer buf = new Buffer();
            buf.writeAll(Okio.source(input));
            buf.readShort(); // query id

            final int flags = buf.readShort() & 0xffff;
            if (flags >> 15 == 0) {
                throw new IllegalArgumentException("not a response");
            }

            byte responseCode = (byte) (flags & 0xf);

            if (responseCode == NXDOMAIN) {
                throw new UnknownHostException(host + ": NXDOMAIN");
            } else if (responseCode == SERVFAIL) {
                throw new UnknownHostException(host + ": SERVFAIL");
            }

            final int questionCount = buf.readShort() & 0xffff;
            final int answerCount = buf.readShort() & 0xffff;
            buf.readShort(); // authority record count
            buf.readShort(); // additional record count

            for (int i = 0; i < questionCount; i++) {
                skipName(buf); // name
                buf.readShort(); // type
                buf.readShort(); // class
            }

            for (int i = 0; i < answerCount; i++) {
                skipName(buf); // name

                int type = buf.readShort() & 0xffff;
                buf.readShort(); // class
                final long ttl = buf.readInt() & 0xffffffffL; // ttl
                final int length = buf.readShort() & 0xffff;

                if (type == TYPE_A || type == TYPE_AAAA) {
                    byte[] bytes = new byte[length];
                    buf.read(bytes);
                    entry.addIP(InetAddress.getByAddress(bytes), ttl);
                } else {
                    buf.skip(length);
                }
            }

            return entry;
        }

        private static void skipName(Buffer in) throws EOFException {
            // 0 - 63 bytes
            int length = in.readByte();

            if (length < 0) {
                // compressed name pointer, first two bits are 1
                // drop second byte of compression offset
                in.skip(1);
            } else {
                while (length > 0) {
                    // skip each part of the domain name
                    in.skip(length);
                    length = in.readByte();
                }
            }
        }
    }
}
