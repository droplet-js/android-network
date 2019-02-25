package okhttp3.android.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@SuppressWarnings("deprecation")
public final class DiskCacheUtils {
    private static final long MIN_DISK_CACHE_SIZE = 16 * 1024 * 1024; // 16MB
    private static final long MAX_DISK_CACHE_SIZE = 256 * 1024 * 1024; // 256MB

    public static long calculateDiskCacheSize(File dir) {
        long size = MIN_DISK_CACHE_SIZE;

        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long available = statFs.getBlockCountLong() * statFs.getBlockSizeLong();
            // Target 10% of the total space.
            size = available / 10;
        } catch (IllegalArgumentException ignored) {
        }

        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
    }

    public static int calculateMemoryCacheSize(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean largeHeap = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap) {
            memoryClass = ActivityManagerHoneycomb.getLargeMemoryClass(am);
        }
        // Target ~10% of the available heap.
        return (int) (1024L * 1024L * memoryClass / 4);
    }

    public static long getTotalMemory() {
        String meminfoPath = "/proc/meminfo";// 系统内存信息文件
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(meminfoPath);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            String msg = localBufferedReader.readLine();
            String[] array = msg.split("\\s+");
            if (array != null && array.length >= 2) {
                initial_memory = Integer.valueOf(array[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            }
            localBufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return initial_memory;
    }

    public static boolean shouldUseLruCache() {
        return getTotalMemory() >= 0.9F * 1 * 1024L * 1024L * 1024L;// 1GB内存，真实有效大约只有 90%
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static class ActivityManagerHoneycomb {
        static int getLargeMemoryClass(ActivityManager activityManager) {
            return activityManager.getLargeMemoryClass();
        }
    }
}
