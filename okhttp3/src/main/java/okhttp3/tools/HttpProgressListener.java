package okhttp3.tools;

public interface HttpProgressListener {
    void onProgressChanged(String url, String method, long progressBytes, long totalBytes, boolean isDone);
}
