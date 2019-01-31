package okhttp3.tools;

public interface HttpProgressListener {
    public void onProgressChanged(String url, String method, long progressBytes, long totalBytes, boolean isDone);
}
