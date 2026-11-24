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

package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.Office365CalendarAttributeStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.field.NamedValuedField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GraphServiceClientProviderFactoryTest {

    @Mock private RefreshTokenStore refreshTokenStore;
    @Mock private Office365CalendarAttributeStore attributeStore;
    @Mock private RequestContext requestContext;
    @Mock private AuthorizationContext authorizationContext;
    @Mock private Office365CalendarAttributes mockAttributes;

    private GraphServiceClientProviderFactory factory;

    @Before
    public void setUp() {
        factory = new GraphServiceClientProviderFactory(refreshTokenStore, attributeStore, requestContext, authorizationContext);
    }

    // ========================================================================
    // createAttributes
    // ========================================================================

    @Test
    public void testCreateAttributes_SavesAndReturnsId() {
        when(attributeStore.save(mockAttributes)).thenReturn("generated-uuid");

        String result = factory.createAttributes(mockAttributes);

        assertEquals("generated-uuid", result);
        verify(attributeStore).save(mockAttributes);
    }

    // ========================================================================
    // create(String authContextId)
    // ========================================================================

    @Test
    public void testCreate_WithAuthContextId_Success() {
        when(attributeStore.load("auth-123")).thenReturn(mockAttributes);

        GraphServiceClientProvider result = factory.create("auth-123");

        assertNotNull(result);
        assertSame(mockAttributes, result.getOffice365CalendarAttributes());
    }

    @Test(expected = MustAuthorizeException.class)
    public void testCreate_WithAuthContextId_NullAttributes_ThrowsMustAuthorize() {
        when(attributeStore.load("auth-123")).thenReturn(null);

        factory.create("auth-123");
    }

    @Test
    public void testCreate_WithAuthContextId_VerifyExceptionMessage() {
        when(attributeStore.load("missing")).thenReturn(null);

        try {
            factory.create("missing");
            fail("Expected MustAuthorizeException");
        } catch (MustAuthorizeException e) {
            assertEquals("No credentials configured. Please authenticate via the checkbox.", e.getMessage());
        }
    }

    // ========================================================================
    // create() - no args
    // ========================================================================

    @Test
    public void testCreate_NoArgs_DelegatesWithRequestContext() {
        when(requestContext.getInvokerId()).thenReturn("invoker-1");
        when(attributeStore.load("invoker-1")).thenReturn(mockAttributes);

        GraphServiceClientProvider result = factory.create();

        assertNotNull(result);
        assertSame(mockAttributes, result.getOffice365CalendarAttributes());
    }

    @Test(expected = MustAuthorizeException.class)
    public void testCreate_NoArgs_NullAttributes_ThrowsMustAuthorize() {
        when(requestContext.getInvokerId()).thenReturn("invoker-1");
        when(attributeStore.load("invoker-1")).thenReturn(null);

        factory.create();
    }

    // ========================================================================
    // create(RequestContext)
    // ========================================================================

    @Test
    public void testCreate_WithRequestContext_Success() {
        RequestContext ctx = mock(RequestContext.class);
        when(ctx.getInvokerId()).thenReturn("invoker-2");
        when(attributeStore.load("invoker-2")).thenReturn(mockAttributes);

        GraphServiceClientProvider result = factory.create(ctx);

        assertNotNull(result);
        assertSame(mockAttributes, result.getOffice365CalendarAttributes());
    }

    @Test(expected = MustAuthorizeException.class)
    public void testCreate_WithRequestContext_NullAttributes_ThrowsMustAuthorize() {
        RequestContext ctx = mock(RequestContext.class);
        when(ctx.getInvokerId()).thenReturn("invoker-2");
        when(attributeStore.load("invoker-2")).thenReturn(null);

        factory.create(ctx);
    }

    @Test
    public void testCreate_WithRequestContext_VerifyExceptionMessage() {
        RequestContext ctx = mock(RequestContext.class);
        when(ctx.getInvokerId()).thenReturn("invoker-3");
        when(attributeStore.load("invoker-3")).thenReturn(null);

        try {
            factory.create(ctx);
            fail("Expected MustAuthorizeException");
        } catch (MustAuthorizeException e) {
            assertEquals("No credentials configured. Please authenticate via the checkbox.", e.getMessage());
        }
    }

    // ========================================================================
    // Verify provider construction details
    // ========================================================================

    @Test
    public void testCreate_WithAuthContextId_ProviderHasCorrectAttributes() {
        when(attributeStore.load("ctx-456")).thenReturn(mockAttributes);

        GraphServiceClientProvider provider = factory.create("ctx-456");

        assertSame(mockAttributes, provider.getOffice365CalendarAttributes());
    }

    @Test
    public void testCreate_WithRequestContext_ProviderHasCorrectAttributes() {
        RequestContext ctx = mock(RequestContext.class);
        when(ctx.getInvokerId()).thenReturn("invoker-4");
        when(attributeStore.load("invoker-4")).thenReturn(mockAttributes);

        GraphServiceClientProvider provider = factory.create(ctx);

        assertSame(mockAttributes, provider.getOffice365CalendarAttributes());
    }

    @Test
    public void testCreate_NoArgs_UsesInjectedRequestContext() {
        when(requestContext.getInvokerId()).thenReturn("default-invoker");
        when(attributeStore.load("default-invoker")).thenReturn(mockAttributes);

        GraphServiceClientProvider provider = factory.create();

        assertNotNull(provider);
        verify(requestContext).getInvokerId();
        verify(attributeStore).load("default-invoker");
    }


    @Test
    public void testCreate_WithRequestContext_InvokeAsUser_NullAttrs_ReturnsProviderWithDefaultPublicAttrs() {
        // Per-user MCP / Have-People-auth without Setup → factory returns a provider built on
        // default Public attrs from config.properties (clientId/clientSecret null in constructor
        // triggers loadPublicConfig). Provider's existing getRefTokenStoreKey logic keys RT
        // under wsContactId — RT storage shape unchanged from baseline.
        RequestContext ctx = mock(RequestContext.class);
        when(ctx.getInvokerId()).thenReturn("INV_test-invoker");
        when(ctx.invokeAsUser()).thenReturn(true);

        GraphServiceClientProvider provider = factory.create(ctx);

        assertNotNull("Factory must return a provider, not throw, for invokeAsUser+null-attrs", provider);
        Office365CalendarAttributes attrs = provider.getOffice365CalendarAttributes();
        assertNotNull("Provider must hold non-null attrs", attrs);
        assertEquals("Per-user fallback must be PUBLIC auth type", Constants.PUBLIC, attrs.getAuthType());
        // attributeStore.save is NOT called — no fresh authContextId, no MCP-shape MAE.
        verify(attributeStore, never()).save(any(Office365CalendarAttributes.class));
    }

    @Test
    public void testCreate_WithRequestContext_InvokeAsUser_False_ThrowsBasicException() {
        RequestContext ctx = mock(RequestContext.class);
        when(ctx.getInvokerId()).thenReturn("non-mcp-invoker");
        when(ctx.invokeAsUser()).thenReturn(false);
        when(attributeStore.load("non-mcp-invoker")).thenReturn(null);

        try {
            factory.create(ctx);
            fail("Expected MustAuthorizeException");
        } catch (MustAuthorizeException e) {
            // Non-MCP: basic exception without 3 details
            assertTrue("Non-MCP exception should have no details",
                    e.getDetails() == null || e.getDetails().isEmpty());
        }
    }

    @Test
    public void testCreate_WithAuthContextId_NullAttributes_Has2Details() {
        when(attributeStore.load("ctx-missing")).thenReturn(null);

        try {
            factory.create("ctx-missing");
            fail("Expected MustAuthorizeException");
        } catch (MustAuthorizeException e) {
            List<NamedValuedField> details = e.getDetails();
            assertNotNull("Details list should not be null", details);
            assertEquals("Should have 2 details", 2, details.size());

            NamedValuedField userIdField = details.stream()
                    .filter(f -> Constants.USER_ID.equals(f.getName()))
                    .findFirst().orElse(null);
            assertNotNull("USER_ID detail should be present", userIdField);
            assertEquals("ctx-missing", userIdField.getValue());

            NamedValuedField authContextIdField = details.stream()
                    .filter(f -> Constants.AUTH_CONTEXT_ID.equals(f.getName()))
                    .findFirst().orElse(null);
            assertNotNull("AUTH_CONTEXT_ID detail should be present", authContextIdField);
            assertEquals("ctx-missing", authContextIdField.getValue());
        }
    }

    // ========================================================================
    // invokeAsUser=true fix — store bypass tests
    // ========================================================================

    // Bug fix: before the fix, when invokeAsUser=true AND Private attrs were stored,
    // the factory would return a provider backed by those Private attrs and authenticate
    // as the service principal instead of the real user. The fix checks invokeAsUser()
    // FIRST and returns a PUBLIC provider regardless of what the store contains.
    @Test
    public void testCreate_WithRequestContext_InvokeAsUser_PrivateAttrsPresent_ReturnsPublicProvider() {
        // Given: a context with invokeAsUser=true, and the store holds Private attrs
        RequestContext ctx = mock(RequestContext.class);
        when(ctx.invokeAsUser()).thenReturn(true);
        // NOTE: attributeStore.load is intentionally NOT stubbed — the fix must never reach it.
        // Mockito lenient mode means an unstubbed call would return null and is harmless,
        // but the verify(never()) below is the real assertion.

        // When
        GraphServiceClientProvider provider = factory.create(ctx);

        // Then: provider is non-null and carries PUBLIC auth type (not the stored Private attrs)
        assertNotNull("Factory must return a provider when invokeAsUser=true", provider);
        Office365CalendarAttributes attrs = provider.getOffice365CalendarAttributes();
        assertNotNull("Provider must hold non-null attrs for invokeAsUser path", attrs);
        assertEquals("invokeAsUser must force PUBLIC auth type regardless of stored attrs",
                Constants.PUBLIC, attrs.getAuthType());
        // The attribute store must never be consulted when invokeAsUser=true
        verify(attributeStore, never()).load(anyString());
    }

    // Documents that the attribute store is fully bypassed (neither read nor written)
    // for any request that arrives on the per-user MCP path (invokeAsUser=true).
    @Test
    public void testCreate_WithRequestContext_InvokeAsUser_True_StoreNeverConsulted() {
        // Given: any invokeAsUser=true context
        RequestContext ctx = mock(RequestContext.class);
        when(ctx.invokeAsUser()).thenReturn(true);

        // When
        GraphServiceClientProvider provider = factory.create(ctx);

        // Then: the store is never read or written
        assertNotNull("Provider must be returned for invokeAsUser=true", provider);
        verify(attributeStore, never()).load(anyString());
        verify(attributeStore, never()).save(any(Office365CalendarAttributes.class));
    }

    // create() delegates to create(requestContext); this test confirms the delegation
    // carries the invokeAsUser=true shortcut all the way through the no-arg entry point.
    @Test
    public void testCreate_NoArgs_InvokeAsUser_True_ReturnsPublicProvider() {
        // Given: the injected requestContext has invokeAsUser=true
        when(requestContext.invokeAsUser()).thenReturn(true);
        // NOTE: attributeStore.load is intentionally NOT stubbed here.

        // When
        GraphServiceClientProvider provider = factory.create();

        // Then: PUBLIC provider returned, store never consulted
        assertNotNull("No-arg create() must return a provider when invokeAsUser=true", provider);
        Office365CalendarAttributes attrs = provider.getOffice365CalendarAttributes();
        assertNotNull("Provider must hold non-null attrs for invokeAsUser path", attrs);
        assertEquals("No-arg invokeAsUser path must yield PUBLIC auth type",
                Constants.PUBLIC, attrs.getAuthType());
        verify(attributeStore, never()).load(anyString());
    }

    // Confirms the non-MCP (invokeAsUser=false) path is not affected by the fix:
    // stored Private attrs are still loaded and returned as-is.
    @Test
    public void testCreate_WithRequestContext_InvokeAsUser_False_AttrsPresent_ReturnsStoredAttrs() {
        // Given: invokeAsUser=false and Private attrs are stored for this invoker
        RequestContext ctx = mock(RequestContext.class);
        when(ctx.getInvokerId()).thenReturn("system-invoker");
        when(ctx.invokeAsUser()).thenReturn(false);
        Office365CalendarAttributes storedAttrs = mock(Office365CalendarAttributes.class);
        when(attributeStore.load("system-invoker")).thenReturn(storedAttrs);

        // When
        GraphServiceClientProvider provider = factory.create(ctx);

        // Then: provider is non-null and holds exactly the stored attrs (system-principal path unchanged)
        assertNotNull("Non-MCP path must return a provider when attrs are present", provider);
        assertSame("Non-MCP path must return the stored Private attrs without modification",
                storedAttrs, provider.getOffice365CalendarAttributes());
        verify(attributeStore).load("system-invoker");
    }
}
