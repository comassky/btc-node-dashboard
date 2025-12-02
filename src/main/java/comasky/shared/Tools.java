package comasky.shared;

public class Tools {
    private static final long SECONDS_IN_DAY = 86400;
    private static final long SECONDS_IN_HOUR = 3600;
    private static final long SECONDS_IN_MINUTE = 60;
    private static final String UPTIME_FORMAT = "%dd, %02d:%02d:%02d";
    private static final String INVALID_UPTIME = "00:00:00:00";

    public static String formatUptime(long totalSeconds) {
        if (totalSeconds < 0) {
            return INVALID_UPTIME;
        }
        long days = totalSeconds / SECONDS_IN_DAY;
        long hours = (totalSeconds % SECONDS_IN_DAY) / SECONDS_IN_HOUR;
        long minutes = (totalSeconds % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE;
        long seconds = totalSeconds % SECONDS_IN_MINUTE;
        return String.format(UPTIME_FORMAT, days, hours, minutes, seconds);
    }
}
