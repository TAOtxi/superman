package cn.taotxi.Superman.util;

public class TickUtils {
    private static long lastNanoTick = 0L;
    private static long lastNanoTime = 0L;
    private static double measuredMSPT = -1.0D;
    private static double measuredTPS = -1.0D;
    private static double averageTPS = 0D;

    public static void updateNanoTick(long timeUpdate) {
        final long currentTime = System.nanoTime();
        final long elapsed = timeUpdate - lastNanoTick;

        measuredMSPT = ((double) (currentTime - lastNanoTime) / (double) elapsed) / 1000000D;
        measuredTPS = measuredMSPT <= 50 ? 20 : (1000D / measuredMSPT);
        lastNanoTick = timeUpdate;
        lastNanoTime = currentTime;

        // TODO: 逻辑待优化
        averageTPS = (averageTPS * 0.8D) + (measuredTPS * 0.2D);
    }

    public static double getMeasuredTps() {
        return measuredTPS;
    }
    
    public static double getAverageTps() {
        return averageTPS;
    }
}
