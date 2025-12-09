package comasky.shared;

/**
 * Utility methods for common formatting operations.
 * <p>
 * This class is not instantiable.
 */
public final class Tools {
    private static final long SECONDS_IN_DAY = 86400L;
    private static final long SECONDS_IN_HOUR = 3600L;
    private static final long SECONDS_IN_MINUTE = 60L;
    private static final String UPTIME_FORMAT = "%dd, %02d:%02d:%02d";
    private static final String INVALID_UPTIME = "00:00:00:00";

    private Tools() {
        throw new AssertionError("No comasky.shared.Tools instances for you!");
    }

    /**
     * Formats a duration in seconds as a human-readable uptime string.
     *
     * @param totalSeconds the total number of seconds
     * @return formatted uptime string (e.g. 1d, 02:03:04)
     */
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
