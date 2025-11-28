package comasky.shared;

public class Tools {
    public static String formatUptime(long totalSeconds) {
        if (totalSeconds < 0) {
            return "00:00:00:00";
        }

        // Constantes en secondes
        final long SECONDS_IN_DAY = 86400;  // 24 * 60 * 60
        final long SECONDS_IN_HOUR = 3600;   // 60 * 60
        final long SECONDS_IN_MINUTE = 60;

        // Calcul des jours, heures, minutes et secondes restantes
        long days = totalSeconds / SECONDS_IN_DAY;
        long remainingSeconds = totalSeconds % SECONDS_IN_DAY;

        long hours = remainingSeconds / SECONDS_IN_HOUR;
        remainingSeconds %= SECONDS_IN_HOUR;

        long minutes = remainingSeconds / SECONDS_IN_MINUTE;
        long seconds = remainingSeconds % SECONDS_IN_MINUTE;

        // Utilisation de String.format pour garantir le format DD:HH:MM:SS (padding avec zéro)
        return String.format("%d jours, %02d:%02d:%02d", days, hours, minutes, seconds);
    }
}
