/*
 * Office365Calendar Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.office_365_calendar.mcp;

import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CalendarMcpAuthBridge}.
 *
 * <p>Covers all public methods as well as private helpers ({@code mask},
 * {@code notBlank}, {@code tokenKey}) via reflection to achieve full coverage.</p>
 */
@RunWith(MockitoJUnitRunner.class)
public class CalendarMcpAuthBridgeTest {

    private static final String MASK = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022";

    @Mock
    private Office365CalendarAttributes attrs;

    @Mock
    private RefreshTokenStore tokenStore;

    private CalendarMcpAuthBridge bridge;

    @Before
    public void setUp() {
        bridge = new CalendarMcpAuthBridge(attrs, tokenStore);
    }

    // ── isAuthenticated ─────────────────────────────────────────────────

    @Test
    public void isAuthenticated_nullAttrs_returnsFalse() {
        CalendarMcpAuthBridge nullBridge = new CalendarMcpAuthBridge(null, tokenStore);
        assertFalse(nullBridge.isAuthenticated());
    }

    @Test
    public void isAuthenticated_privateAuthType_allFieldsPresent_returnsTrue() {
        when(attrs.getAuthType()).thenReturn(Constants.PRIVATE);
        when(attrs.getClientId()).thenReturn("client-id-123");
        when(attrs.getClientSecret()).thenReturn("secret-456");
        when(attrs.getTenantId()).thenReturn("tenant-789");

        assertTrue(bridge.isAuthenticated());
    }

    @Test
    public void isAuthenticated_privateAuthType_blankClientId_returnsFalse() {
        when(attrs.getAuthType()).thenReturn(Constants.PRIVATE);
        when(attrs.getClientId()).thenReturn("   ");

        assertFalse(bridge.isAuthenticated());
    }

    @Test
    public void isAuthenticated_privateAuthType_nullClientId_returnsFalse() {
        when(attrs.getAuthType()).thenReturn(Constants.PRIVATE);
        when(attrs.getClientId()).thenReturn(null);

        assertFalse(bridge.isAuthenticated());
    }

    @Test
    public void isAuthenticated_privateAuthType_blankClientSecret_returnsFalse() {
        when(attrs.getAuthType()).thenReturn(Constants.PRIVATE);
        when(attrs.getClientId()).thenReturn("client-id");
        when(attrs.getClientSecret()).thenReturn("");

        assertFalse(bridge.isAuthenticated());
    }

    @Test
    public void isAuthenticated_privateAuthType_blankTenantId_returnsFalse() {
        when(attrs.getAuthType()).thenReturn(Constants.PRIVATE);
        when(attrs.getClientId()).thenReturn("client-id");
        when(attrs.getClientSecret()).thenReturn("secret");
        when(attrs.getTenantId()).thenReturn("  ");

        assertFalse(bridge.isAuthenticated());
    }

    @Test
    public void isAuthenticated_publicAuthType_tokenExists_returnsTrue() {
        when(attrs.getAuthType()).thenReturn("Public");
        when(attrs.getEmail()).thenReturn("user@example.com");
        when(attrs.getClientId()).thenReturn("cid");

        String expectedKey = "user@example.com" + Constants.UNDER_SCORE + "cid" + Constants.UNDER_SCORE + "Public";
        when(tokenStore.get(expectedKey)).thenReturn("refresh-token-value");

        assertTrue(bridge.isAuthenticated());
    }

    @Test
    public void isAuthenticated_publicAuthType_noToken_returnsFalse() {
        when(attrs.getAuthType()).thenReturn("Public");
        when(attrs.getEmail()).thenReturn("user@example.com");
        when(attrs.getClientId()).thenReturn("cid");

        String expectedKey = "user@example.com" + Constants.UNDER_SCORE + "cid" + Constants.UNDER_SCORE + "Public";
        when(tokenStore.get(expectedKey)).thenReturn(null);

        assertFalse(bridge.isAuthenticated());
    }

    @Test
    public void isAuthenticated_publicAuthType_nullEmail_returnsFalse() {
        when(attrs.getAuthType()).thenReturn("Public");
        when(attrs.getEmail()).thenReturn(null);

        assertFalse(bridge.isAuthenticated());
    }

    // ── authSummary ─────────────────────────────────────────────────────

    @Test
    public void authSummary_nullAttrs_returnsNull() {
        CalendarMcpAuthBridge nullBridge = new CalendarMcpAuthBridge(null, tokenStore);
        assertNull(nullBridge.authSummary());
    }

    @Test
    public void authSummary_privateAuthType_clientIdLongerThan4() {
        when(attrs.getAuthType()).thenReturn(Constants.PRIVATE);
        when(attrs.getClientId()).thenReturn("abcdef-1234");

        assertEquals("Service to Service (App: abcd\u2026)", bridge.authSummary());
    }

    @Test
    public void authSummary_privateAuthType_clientIdExactly4Chars() {
        when(attrs.getAuthType()).thenReturn(Constants.PRIVATE);
        when(attrs.getClientId()).thenReturn("abcd");

        assertEquals("Service to Service (App: abcd)", bridge.authSummary());
    }

    @Test
    public void authSummary_privateAuthType_clientIdShorterThan4Chars() {
        when(attrs.getAuthType()).thenReturn(Constants.PRIVATE);
        when(attrs.getClientId()).thenReturn("ab");

        assertEquals("Service to Service (App: ab)", bridge.authSummary());
    }

    @Test
    public void authSummary_privateAuthType_nullClientId() {
        when(attrs.getAuthType()).thenReturn(Constants.PRIVATE);
        when(attrs.getClientId()).thenReturn(null);

        assertEquals("Service to Service (App: )", bridge.authSummary());
    }

    @Test
    public void authSummary_publicAuthType_withEmail() {
        when(attrs.getAuthType()).thenReturn("Public");
        when(attrs.getEmail()).thenReturn("user@example.com");

        assertEquals("OAuth 2.0 (user@example.com)", bridge.authSummary());
    }

    @Test
    public void authSummary_publicAuthType_withBlankEmail() {
        when(attrs.getAuthType()).thenReturn("Public");
        when(attrs.getEmail()).thenReturn("   ");

        assertEquals("OAuth 2.0", bridge.authSummary());
    }

    @Test
    public void authSummary_publicAuthType_withNullEmail() {
        when(attrs.getAuthType()).thenReturn("Public");
        when(attrs.getEmail()).thenReturn(null);

        assertEquals("OAuth 2.0", bridge.authSummary());
    }

    // ── siteUrl ─────────────────────────────────────────────────────────

    @Test
    public void siteUrl_returnsGraphUrl() {
        assertEquals("https://graph.microsoft.com", bridge.siteUrl());
    }

    // ── userEmail ───────────────────────────────────────────────────────

    @Test
    public void userEmail_returnsEmail() {
        when(attrs.getEmail()).thenReturn("user@example.com");
        assertEquals("user@example.com", bridge.userEmail());
    }

    @Test
    public void userEmail_nullAttrs_returnsNull() {
        CalendarMcpAuthBridge nullBridge = new CalendarMcpAuthBridge(null, tokenStore);
        assertNull(nullBridge.userEmail());
    }

    // ── isOAuth ─────────────────────────────────────────────────────────

    @Test
    public void isOAuth_returnsTrue() {
        assertTrue(bridge.isOAuth());
    }

    // ── maskedClientId ──────────────────────────────────────────────────

    @Test
    public void maskedClientId_longerThan4_showsFirst4() {
        when(attrs.getClientId()).thenReturn("abcdef-1234");
        assertEquals("abcd" + MASK, bridge.maskedClientId());
    }

    @Test
    public void maskedClientId_exactly4Chars_returnsMask() {
        when(attrs.getClientId()).thenReturn("abcd");
        assertEquals(MASK, bridge.maskedClientId());
    }

    @Test
    public void maskedClientId_shorterThan4_returnsMask() {
        when(attrs.getClientId()).thenReturn("ab");
        assertEquals(MASK, bridge.maskedClientId());
    }

    @Test
    public void maskedClientId_nullClientId_returnsMask() {
        when(attrs.getClientId()).thenReturn(null);
        assertEquals(MASK, bridge.maskedClientId());
    }

    @Test
    public void maskedClientId_blankClientId_returnsMask() {
        when(attrs.getClientId()).thenReturn("   ");
        assertEquals(MASK, bridge.maskedClientId());
    }

    @Test
    public void maskedClientId_nullAttrs_returnsMask() {
        CalendarMcpAuthBridge nullBridge = new CalendarMcpAuthBridge(null, tokenStore);
        assertEquals(MASK, nullBridge.maskedClientId());
    }

    // ── maskedClientSecret ──────────────────────────────────────────────

    @Test
    public void maskedClientSecret_anyValue_returnsMask() {
        when(attrs.getClientSecret()).thenReturn("super-secret-value");
        assertEquals(MASK, bridge.maskedClientSecret());
    }

    @Test
    public void maskedClientSecret_nullValue_returnsMask() {
        when(attrs.getClientSecret()).thenReturn(null);
        assertEquals(MASK, bridge.maskedClientSecret());
    }

    @Test
    public void maskedClientSecret_nullAttrs_returnsMask() {
        CalendarMcpAuthBridge nullBridge = new CalendarMcpAuthBridge(null, tokenStore);
        assertEquals(MASK, nullBridge.maskedClientSecret());
    }

    // ── Private: mask (via reflection) ──────────────────────────────────

    @Test
    public void mask_nullValue_returnsMask() throws Exception {
        assertEquals(MASK, invokeMask(null, 4));
    }

    @Test
    public void mask_blankValue_returnsMask() throws Exception {
        assertEquals(MASK, invokeMask("", 4));
    }

    @Test
    public void mask_whitespaceOnly_returnsMask() throws Exception {
        assertEquals(MASK, invokeMask("   ", 4));
    }

    @Test
    public void mask_visibleCharsGreaterThanZero_valueLongerThanVisible() throws Exception {
        assertEquals("abcd" + MASK, invokeMask("abcdef", 4));
    }

    @Test
    public void mask_visibleCharsGreaterThanZero_valueEqualToVisible() throws Exception {
        assertEquals(MASK, invokeMask("abcd", 4));
    }

    @Test
    public void mask_visibleCharsGreaterThanZero_valueShorterThanVisible() throws Exception {
        assertEquals(MASK, invokeMask("ab", 4));
    }

    @Test
    public void mask_visibleCharsZero_returnsMask() throws Exception {
        assertEquals(MASK, invokeMask("some-secret", 0));
    }

    @Test
    public void mask_visibleCharsNegative_returnsMask() throws Exception {
        // visibleChars <= 0 means no visible prefix
        assertEquals(MASK, invokeMask("some-secret", -1));
    }

    // ── Private: notBlank (via reflection) ──────────────────────────────

    @Test
    public void notBlank_null_returnsFalse() throws Exception {
        assertFalse(invokeNotBlank(null));
    }

    @Test
    public void notBlank_empty_returnsFalse() throws Exception {
        assertFalse(invokeNotBlank(""));
    }

    @Test
    public void notBlank_whitespace_returnsFalse() throws Exception {
        assertFalse(invokeNotBlank("   "));
    }

    @Test
    public void notBlank_nonBlank_returnsTrue() throws Exception {
        assertTrue(invokeNotBlank("hello"));
    }

    @Test
    public void notBlank_valueWithSpaces_returnsTrue() throws Exception {
        assertTrue(invokeNotBlank(" hello "));
    }

    // ── Private: tokenKey (via reflection) ──────────────────────────────

    @Test
    public void tokenKey_nullAttrs_returnsNull() throws Exception {
        CalendarMcpAuthBridge nullBridge = new CalendarMcpAuthBridge(null, tokenStore);
        assertNull(invokeTokenKey(nullBridge));
    }

    @Test
    public void tokenKey_nullEmail_returnsNull() throws Exception {
        when(attrs.getEmail()).thenReturn(null);
        assertNull(invokeTokenKey(bridge));
    }

    @Test
    public void tokenKey_validAttrs_returnsFormattedKey() throws Exception {
        when(attrs.getEmail()).thenReturn("user@example.com");
        when(attrs.getClientId()).thenReturn("cid123");
        when(attrs.getAuthType()).thenReturn("Public");

        String expected = "user@example.com" + Constants.UNDER_SCORE + "cid123" + Constants.UNDER_SCORE + "Public";
        assertEquals(expected, invokeTokenKey(bridge));
    }

    @Test
    public void tokenKey_privateAuthType_returnsKeyWithPrivate() throws Exception {
        when(attrs.getEmail()).thenReturn("admin@corp.com");
        when(attrs.getClientId()).thenReturn("my-client");
        when(attrs.getAuthType()).thenReturn(Constants.PRIVATE);

        String expected = "admin@corp.com" + Constants.UNDER_SCORE + "my-client" + Constants.UNDER_SCORE + Constants.PRIVATE;
        assertEquals(expected, invokeTokenKey(bridge));
    }

    // ── Reflection helpers ──────────────────────────────────────────────

    private static String invokeMask(String value, int visibleChars) throws Exception {
        Method method = CalendarMcpAuthBridge.class.getDeclaredMethod("mask", String.class, int.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(null, value, visibleChars);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private static boolean invokeNotBlank(String s) throws Exception {
        Method method = CalendarMcpAuthBridge.class.getDeclaredMethod("notBlank", String.class);
        method.setAccessible(true);
        try {
            return (boolean) method.invoke(null, s);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private static String invokeTokenKey(CalendarMcpAuthBridge instance) throws Exception {
        Method method = CalendarMcpAuthBridge.class.getDeclaredMethod("tokenKey");
        method.setAccessible(true);
        try {
            return (String) method.invoke(instance);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }
}
