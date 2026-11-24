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

package app.krista.extensions.essentials.collaboration.office_365_calendar.impl;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarAttributes;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProvider;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.connectors.GraphServiceClientProviderFactory;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.util.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Tests for the MCP {@code invokeAsUser=true} execution path through {@link MeetingDaoImpl}.
 *
 * <p>The factory constructor ({@code MeetingDaoImpl(GraphServiceClientProviderFactory)}) sets
 * {@code clientProvider} to {@code null}, so every operation that needs a Graph client must
 * call {@code factory.create()} at runtime. These tests verify that delegation contract — that
 * the factory is always consulted when no provider is pre-wired, that the PUBLIC provider
 * produced for {@code invokeAsUser=true} flows through unchanged, and that a
 * {@link MustAuthorizeException} thrown by the factory propagates cleanly to the caller.
 */
@RunWith(MockitoJUnitRunner.class)
public class MeetingDaoImplMcpTest {

    @Mock
    private GraphServiceClientProviderFactory factory;

    @Mock
    private GraphServiceClientProvider mockProvider;

    @Mock
    private Office365CalendarAttributes mockAttributes;

    // -------------------------------------------------------------------------
    // Reflection helper — avoids copy-pasting boilerplate in every test
    // -------------------------------------------------------------------------

    /**
     * Obtains the private {@code resolveClientProvider()} method via reflection and makes it
     * accessible. This is the canonical approach when the unit under test exposes no public
     * facade that cheaply exercises the exact code path of interest without requiring a full
     * Microsoft Graph API stub chain.
     */
    private Method resolveClientProviderMethod() throws NoSuchMethodException {
        Method m = MeetingDaoImpl.class.getDeclaredMethod("resolveClientProvider");
        m.setAccessible(true);
        return m;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * When {@link MeetingDaoImpl} is constructed via the two-argument test shortcut
     * ({@code clientProvider} pre-set), {@code resolveClientProvider()} must return that
     * instance directly and must not interact with the factory at all.
     */
    @Test
    public void testResolveClientProvider_returnsPresetProvider_factoryNotCalled() throws Exception {
        MeetingDaoImpl dao = new MeetingDaoImpl(mockProvider, mockAttributes);

        Method m = resolveClientProviderMethod();
        GraphServiceClientProvider result = (GraphServiceClientProvider) m.invoke(dao);

        Assert.assertSame(mockProvider, result);
        Mockito.verifyNoInteractions(factory);
    }

    /**
     * When {@link MeetingDaoImpl} is constructed via the HK2 factory constructor
     * ({@code clientProvider} starts as {@code null}), {@code resolveClientProvider()} must
     * delegate to {@code factory.create()} and return whatever the factory produces.
     *
     * <p>This is the central contract of the MCP path: the DAO never caches a provider at
     * construction time; it always asks the factory, which in turn inspects
     * {@code RequestContext.invokeAsUser()} to decide whether to return a PUBLIC or PRIVATE
     * provider.
     */
    @Test
    public void testResolveClientProvider_callsFactory_whenClientProviderIsNull() throws Exception {
        Mockito.when(factory.create()).thenReturn(mockProvider);
        MeetingDaoImpl dao = new MeetingDaoImpl(factory);

        Method m = resolveClientProviderMethod();
        GraphServiceClientProvider result = (GraphServiceClientProvider) m.invoke(dao);

        Assert.assertSame(mockProvider, result);
        Mockito.verify(factory).create();
    }

    /**
     * Verifies the {@code invokeAsUser=true} scenario end-to-end through the DAO layer.
     *
     * <p>When the factory is configured for per-user MCP invocations it returns a provider
     * whose embedded {@link Office365CalendarAttributes} carries {@code authType = "Public"}.
     * {@code resolveClientProvider()} must pass that provider through without modification so
     * that callers receive the PUBLIC OAuth client.
     */
    @Test
    public void testResolveClientProvider_invokeAsUserPublicProvider_flowsThroughUnmodified() throws Exception {
        Office365CalendarAttributes publicAttrs = Mockito.mock(Office365CalendarAttributes.class);
        Mockito.when(publicAttrs.getAuthType()).thenReturn(Constants.PUBLIC);
        Mockito.when(mockProvider.getOffice365CalendarAttributes()).thenReturn(publicAttrs);
        Mockito.when(factory.create()).thenReturn(mockProvider);

        MeetingDaoImpl dao = new MeetingDaoImpl(factory);
        Method m = resolveClientProviderMethod();
        GraphServiceClientProvider result = (GraphServiceClientProvider) m.invoke(dao);

        Assert.assertSame(mockProvider, result);
        Assert.assertEquals(Constants.PUBLIC, result.getOffice365CalendarAttributes().getAuthType());
        Mockito.verify(factory).create();
    }

    /**
     * When the factory throws {@link MustAuthorizeException} — which happens for
     * {@code invokeAsUser=false} with no stored credentials — {@code resolveClientProvider()}
     * must propagate it to the caller without wrapping or suppression.
     *
     * <p>Reflection wraps the underlying exception in {@link InvocationTargetException}; the
     * test unwraps and asserts the original exception identity.
     */
    @Test
    public void testResolveClientProvider_maePropagatesFromFactory() throws Exception {
        MustAuthorizeException mae = new MustAuthorizeException("No credentials configured. Please authenticate via the checkbox.");
        Mockito.when(factory.create()).thenThrow(mae);

        MeetingDaoImpl dao = new MeetingDaoImpl(factory);
        Method m = resolveClientProviderMethod();

        try {
            m.invoke(dao);
            Assert.fail("Expected MustAuthorizeException to be thrown");
        } catch (InvocationTargetException e) {
            Assert.assertSame(
                "MustAuthorizeException must propagate without wrapping",
                mae,
                e.getCause()
            );
        }

        Mockito.verify(factory).create();
    }
}
