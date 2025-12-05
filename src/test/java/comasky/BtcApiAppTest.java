package comasky;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class BtcApiAppTest {

    @Test
    void testMaskPassword_normalPassword() throws Exception {
        BtcApiApp app = new BtcApiApp();
        Method method = BtcApiApp.class.getDeclaredMethod("maskPassword", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(app, "mySecurePassword123");
        
        assertEquals("my****23", result);
        assertFalse(result.contains("Secure"));
        assertFalse(result.contains("Password"));
    }

    @Test
    void testMaskPassword_shortPassword() throws Exception {
        BtcApiApp app = new BtcApiApp();
        Method method = BtcApiApp.class.getDeclaredMethod("maskPassword", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(app, "1234");
        
        assertEquals("****", result);
    }

    @Test
    void testMaskPassword_veryShortPassword() throws Exception {
        BtcApiApp app = new BtcApiApp();
        Method method = BtcApiApp.class.getDeclaredMethod("maskPassword", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(app, "ab");
        
        assertEquals("****", result);
    }

    @Test
    void testMaskPassword_emptyPassword() throws Exception {
        BtcApiApp app = new BtcApiApp();
        Method method = BtcApiApp.class.getDeclaredMethod("maskPassword", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(app, "");
        
        assertEquals("[NOT SET]", result);
    }

    @Test
    void testMaskPassword_nullPassword() throws Exception {
        BtcApiApp app = new BtcApiApp();
        Method method = BtcApiApp.class.getDeclaredMethod("maskPassword", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(app, (String) null);
        
        assertEquals("[NOT SET]", result);
    }

    @Test
    void testMaskPassword_longPassword() throws Exception {
        BtcApiApp app = new BtcApiApp();
        Method method = BtcApiApp.class.getDeclaredMethod("maskPassword", String.class);
        method.setAccessible(true);
        
        String longPassword = "thisIsAVeryLongPasswordWith1234567890";
        String result = (String) method.invoke(app, longPassword);
        
        assertTrue(result.startsWith("th"));
        assertTrue(result.endsWith("90"));
        assertTrue(result.contains("****"));
        assertEquals("th****90", result);
    }

    @Test
    void testMaskPassword_specialCharacters() throws Exception {
        BtcApiApp app = new BtcApiApp();
        Method method = BtcApiApp.class.getDeclaredMethod("maskPassword", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(app, "P@ssw0rd!");
        
        assertEquals("P@****d!", result);
    }

    @Test
    void testMaskPassword_fiveCharacters() throws Exception {
        BtcApiApp app = new BtcApiApp();
        Method method = BtcApiApp.class.getDeclaredMethod("maskPassword", String.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(app, "12345");
        
        assertEquals("12****45", result);
    }
}
