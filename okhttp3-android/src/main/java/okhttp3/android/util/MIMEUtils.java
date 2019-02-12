package okhttp3.android.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import okhttp3.MIME;
import okhttp3.MediaType;

public final class MIMEUtils {

    private MIMEUtils() {
    }

    public static MediaType parseMediaType(Context context, Uri uri, String defaultExtension) {
        MediaType mediaType = !TextUtils.isEmpty(defaultExtension) ? MediaType.parse(MimeTypeMap.getSingleton().getMimeTypeFromExtension(defaultExtension)) : null;
        ContentResolver contentResolver = context.getContentResolver();
        String mimeType = contentResolver.getType(uri);
        if (!TextUtils.isEmpty(mimeType)) {
            mediaType = MediaType.parse(mimeType);
        }
        if (mediaType == null) {
            mediaType = MIME.APPLICATION_OCTET_STREAM;
        }
        return mediaType;
    }

    public static String parseExtension(MediaType mediaType) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mediaType.toString());
    }
}
