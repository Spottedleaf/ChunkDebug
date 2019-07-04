package ca.spottedleaf.chunkdebug.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class Util {

    private static double scaleX;
    private static double scaleY;

    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable runnable) {
            return new Thread(runnable, "Chunk debug executor service");
        }
    });

    public static void setMonitorSize(final int x, final int y) {
        scaleX = (x / 1920.0);
        scaleY = (y / 1080.0);
    }

    public static int scaleX(final int pixels) {
        return (int)(pixels * scaleX);
    }

    public static int scaleY(final int pixels) {
        return (int)(pixels * scaleY);
    }

    public static int scale(final int pixels) {
        return (int)(pixels * (scaleX + scaleY)/2.0);
    }

    private Util() {
        throw new RuntimeException();
    }
}