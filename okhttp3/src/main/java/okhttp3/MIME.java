package okhttp3;

public final class MIME {

    public static final MediaType TEXT_PLAIN = MediaType.parse("text/plain; charset=UTF-8");

    public static final MediaType APPLICATION_APK = MediaType.parse("application/vnd.android.package-archive");
    public static final MediaType APPLICATION_JSON = MediaType.parse("application/json");
    public static final MediaType APPLICATION_ZIP = MediaType.parse("application/zip");
    public static final MediaType APPLICATION_GZ = MediaType.parse("application/x-gzip");
    public static final MediaType APPLICATION_OCTET_STREAM = MediaType.parse("application/octet-stream");

    public static final MediaType IMAGE_PNG = MediaType.parse("image/png");
    public static final MediaType IMAGE_JPEG = MediaType.parse("image/jpeg");

    private MIME() {
    }
}
