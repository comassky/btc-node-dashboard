package comasky;

import comasky.shared.Tools;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolsTest {

    @Test
    void testFormatUptime_seconds() {
        assertEquals("0d, 00:00:45", Tools.formatUptime(45));
    }

    @Test
    void testFormatUptime_minutes() {
        assertEquals("0d, 00:05:30", Tools.formatUptime(330));
    }

    @Test
    void testFormatUptime_hours() {
        assertEquals("0d, 02:15:00", Tools.formatUptime(8100));
    }

    @Test
    void testFormatUptime_days() {
        assertEquals("3d, 04:00:00", Tools.formatUptime(273600));
    }

    @Test
    void testFormatUptime_daysHoursMinutes() {
        assertEquals("1d, 02:30:00", Tools.formatUptime(95400));
    }

    @Test
    void testFormatUptime_zero() {
        assertEquals("0d, 00:00:00", Tools.formatUptime(0));
    }

    @Test
    void testFormatUptime_exactDay() {
        assertEquals("1d, 00:00:00", Tools.formatUptime(86400));
    }

    @Test
    void testFormatUptime_exactHour() {
        assertEquals("0d, 01:00:00", Tools.formatUptime(3600));
    }

    @Test
    void testFormatUptime_exactMinute() {
        assertEquals("0d, 00:01:00", Tools.formatUptime(60));
    }
}
