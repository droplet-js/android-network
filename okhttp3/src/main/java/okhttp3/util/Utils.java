package okhttp3.util;

import java.io.IOException;
import java.io.InputStream;

import okio.Buffer;
import okio.Okio;

public final class Utils {

    private Utils() {
    }

    public static byte[] toByteArray(InputStream is) throws IOException {
        Buffer buffer = new Buffer();
        buffer.writeAll(Okio.source(is));
        return buffer.readByteArray();
    }
}
