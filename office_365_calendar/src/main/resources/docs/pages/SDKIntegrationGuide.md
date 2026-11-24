# Microsoft Auth SDK Integration Guide

A step-by-step guide for integrating `microsoft-auth-sdk` into a new Krista extension. Uses the Office 365 Calendar extension as the reference implementation throughout.

---

## Prerequisites

| Requirement | Value (Calendar example) |
|---|---|
| SDK artifact | `app.krista.sdk:microsoft-auth-sdk:1.0.1` |
| Krista APIs | `app.krista:krista-apis:1.0.125-sp1` |
| Annotation processor | `app.krista:extension-impl-anno-processors:1.0.125-sp1` |
| Java | 21+ |

---

## Step 1 — Add the SDK dependency to `build.gradle`

Add two entries: one for the main dependency and one for the resolvable `sdkUi` configuration that extracts the pre-built HTML pages.

```groovy
// In the dependencies block
dependencies {
    implementation 'app.krista.sdk:microsoft-auth-sdk:1.0.1'
    // ... other deps ...
}

// After the dependencies block — resolvable configuration for HTML extraction
configurations {
    sdkUi {
        canBeResolved = true
        transitive = false
    }
}
dependencies {
    sdkUi 'app.krista.sdk:microsoft-auth-sdk:1.0.1'
}
```

---

## Step 2 — Replace the React UI build with SDK HTML extraction

The SDK JAR ships `auth/index.html` and `audit/index.html`. No React/Node build is needed. Add the extraction task and wire it into the build.

```groovy
tasks.register('extractSDKResources', Copy) {
    def sdkJar = configurations.sdkUi.singleFile
    from(zipTree(sdkJar)) {
        include 'auth/index.html'
        include 'audit/index.html'
    }
    into("${project.buildDir}/resources/main")
    includeEmptyDirs = false
}

// Legacy task names kept for backward compatibility with CI scripts
tasks.register('buildAndCopyUI') {
    dependsOn(tasks.named('extractSDKResources'))
}
tasks.register('buildUI') {
    dependsOn(tasks.named('extractSDKResources'))
}
```

Make sure the `jar` task depends on the extraction:

```groovy
tasks.named('jar') {
    dependsOn tasks.named('buildAndCopyUI')
}
```

---

## Step 3 — Create the `AuthConfig` class

This is a **marker class with zero methods**. All configuration is expressed via annotations.

Create `<YourExtension>AuthConfig.java` in the extension's root package.

**Calendar example** (`Office365CalendarAuthConfig.java`):

```java
package app.krista.extensions.essentials.collaboration.office_365_calendar;

import app.krista.sdk.msauth.config.AuthFlow;
import app.krista.sdk.msauth.extension.annotations.*;

@MsAuthExtension(
    extensionType = "calendar",                       // unique ID for this extension
    callbackPathPrefix = "/calendar",                 // OAuth callback path prefix
    supportedFlows = {
        AuthFlow.AUTH_CODE_PUBLIC,
        AuthFlow.AUTH_CODE_PRIVATE,
        AuthFlow.CLIENT_CREDENTIALS_SECRET
    },
    defaultScopes = {"openid", "offline_access",
                     "Calendars.ReadWrite",
                     "Calendars.ReadWrite.Shared",
                     "User.Read"},
    headerTitle = "Office 365 Calendar Authentication",
    headerSubtitle = "Configure how Krista connects to your Microsoft calendar",
    showScopePanel = true,
    supportsOwnCredentials = true,
    enableAuditDashboard = true,
    setupGuideUrl = "../../../static/docs/#/pages/overview"
)
@AuthScope(value = "Calendars.ReadWrite",
    description = "Read and write calendars", required = true,
    catalogRequests = {"Schedule Meeting", "Cancel Event", "Update Event",
                       "Get Event", "Get Upcoming Meetings", "Search Events",
                       "Accept Invitation", "Decline Invitation",
                       "Tentatively Accept Invitation",
                       "Get All Unaccepted Events", "Get Events For Given User",
                       "Add Attendees", "Remove Attendee", "Forward Event"})
@AuthScope(value = "Calendars.ReadWrite.Shared",
    description = "Read and write shared calendars",
    catalogRequests = {"Get Events For Given User (shared)",
                       "Schedule Meeting (shared)"})
@AuthScope(value = "User.Read",
    description = "Read user profile",
    catalogRequests = {"Test Connection", "Health Check"})
@AuditConfig(
    dashboardTitle = "Calendar Audit Dashboard",
    enableCredentialHealth = true,
    enableCharts = true,
    csvExportPrefix = "calendar-audit"
)
@AuditEventGroup(name = "Meeting Events", events = {
    @AuditEvent(type = "MEETING_SCHEDULED",
                description = "New meeting scheduled via Graph API"),
    @AuditEvent(type = "MEETING_CANCELLED",
                description = "Meeting cancelled"),
    @AuditEvent(type = "MEETING_UPDATED",
                description = "Meeting details updated"),
    @AuditEvent(type = "INVITATION_ACCEPTED",
                description = "Meeting invitation accepted"),
    @AuditEvent(type = "INVITATION_DECLINED",
                description = "Meeting invitation declined")
})
public class Office365CalendarAuthConfig {
}
```

### Annotation reference

| Annotation | Purpose |
|---|---|
| `@MsAuthExtension` | Core config: extension type, flows, scopes, UI settings |
| `@AuthScope` (repeatable) | Declares an OAuth scope and which catalog requests require it |
| `@AuditConfig` | Dashboard title, chart/health toggles, CSV prefix |
| `@AuditEventGroup` / `@AuditEvent` | Custom domain event types shown in the dashboard |

---

## Step 4 — Create the `ExtensionDescriptor` class

The SDK discovers extensions via Java's `ServiceLoader`. Create a class implementing `MsAuthExtensionDescriptor`.

```java
package app.krista.extensions.essentials.collaboration.office_365_calendar;

import app.krista.sdk.msauth.extension.registry.MsAuthExtensionDescriptor;

public class Office365CalendarExtensionDescriptor implements MsAuthExtensionDescriptor {
    @Override
    public Class<?> getConfigClass() {
        return Office365CalendarAuthConfig.class;
    }
}
```

---

## Step 5 — Create the SPI registration file

Create the file at:

```
src/main/resources/META-INF/services/app.krista.sdk.msauth.extension.registry.MsAuthExtensionDescriptor
```

Contents — the fully-qualified class name of your descriptor:

```
app.krista.extensions.essentials.collaboration.office_365_calendar.Office365CalendarExtensionDescriptor
```

> **Important**: This is the mechanism that makes the SDK discover your extension at runtime. If this file is missing or the class name is wrong, the SDK will not register the extension and the metadata endpoint will return `"Extension not found"`.

---

## Step 6 — Create the `AuditService` class

A thin delegate around the SDK's `MsAuthAuditService`. This wires the SDK service into HK2 for your extension.

```java
package app.krista.extensions.essentials.collaboration.office_365_calendar.impl.audit;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.util.KeyValueStore;
import app.krista.sdk.msauth.extension.audit.AuditData;
import app.krista.sdk.msauth.extension.audit.AuditRecorder;
import app.krista.sdk.msauth.extension.service.MsAuthAuditService;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;

@Service
public class Office365CalendarAuditService implements AuditRecorder {

    private final MsAuthAuditService delegate;

    @Inject
    public Office365CalendarAuditService(KeyValueStore keyValueStore) {
        // Second argument is the extension type (must match @MsAuthExtension.extensionType)
        this.delegate = new MsAuthAuditService(keyValueStore, "calendar");
    }

    @Override
    public void record(String invokerId, String eventType, String status,
                       String catalogName, String reason, String exceptionClass,
                       Map<String, String> metadata) {
        delegate.record(invokerId, eventType, status,
                        catalogName, reason, exceptionClass, metadata);
    }

    public void recordAuthError(String invokerId, String catalogName,
                                MustAuthorizeException cause) {
        delegate.recordAuthError(invokerId, catalogName, cause);
    }

    public AuditData getData(String invokerId, Long fromTs, Long toTs,
                             String type, String status) {
        return delegate.getData(invokerId, fromTs, toTs, type, status);
    }

    @Override
    public void resetAuthCounters() {
        delegate.resetAuthCounters();
    }

    public void clearRecords(String invokerId, Long fromTs, Long toTs) {
        delegate.clearRecords(invokerId, fromTs, toTs);
    }
}
```

---

## Step 7 — Create the `AuditResource` class

Extend the SDK's `AbstractAuditResource`. You implement three abstract methods plus build the health response specific to your extension.

```java
package app.krista.extensions.essentials.collaboration.office_365_calendar.api;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.office_365_calendar.impl.audit.Office365CalendarAuditService;
import app.krista.sdk.msauth.extension.audit.AbstractAuditResource;
import app.krista.sdk.msauth.extension.audit.AuditData;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.*;

@Path("/audit")
public class Office365CalendarAuditResource extends AbstractAuditResource {

    private final Office365CalendarAuditService auditService;
    // inject whatever stores you need for the health panel

    @Inject
    public Office365CalendarAuditResource(
            Office365CalendarAuditService auditService,
            /* your attribute store, token store, etc. */
            Invoker invoker) {
        super(invoker.getInvokerId());
        this.auditService = auditService;
    }

    @Override
    protected AuditData getAuditData(String invokerId,
            Long from, Long to, String type, String status) {
        return auditService.getData(invokerId, from, to, type, status);
    }

    @Override
    protected void clearAuditRecords(String invokerId, Long from, Long to) {
        auditService.clearRecords(invokerId, from, to);
    }

    @Override
    protected Map<String, Object> buildHealthResponse() {
        Map<String, Object> health = new HashMap<>();
        // Populate with credential and token information:
        //   authType, email, masked tenantId/clientId, hasRefreshToken, etc.
        // Count auth-related events:
        //   TOKEN_REFRESHED, TOKEN_EXPIRY_DETECTED, INVALID_TOKEN_DETECTED,
        //   CLIENT_SECRET_EXPIRY_DETECTED, AUTH_CONFIG_FAILURE, etc.
        return health;
    }
}
```

**Inherited endpoints** (provided by `AbstractAuditResource`):

| Method | Path | Description |
|---|---|---|
| GET | `/audit/` | Serves the audit dashboard HTML |
| GET | `/audit/data` | Returns JSON audit records (supports `from`, `to`, `type`, `status` query params) |
| GET | `/audit/health` | Returns the health panel JSON |
| DELETE | `/audit/data` | Clears records in the given time range |

---

## Step 8 — Add the metadata endpoint to the API resource

The SDK's auth and audit HTML pages fetch extension metadata via a REST endpoint. Add this endpoint to your existing API resource class (e.g., `AuthenticationResource`):

```java
@GET
@Path("/msauth/metadata/{extensionId}")
@javax.ws.rs.Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
public String getExtensionMetadata(@PathParam("extensionId") String extensionId) {
    app.krista.sdk.msauth.extension.registry.ExtensionRegistry.initialize();
    return app.krista.sdk.msauth.extension.registry.ExtensionRegistry.get(extensionId)
            .map(m -> m.toUiJson())
            .orElse("{\"error\":\"Extension not found: " + extensionId + "\"}");
}
```

---

## Step 9 — Add auth tab and audit tab HTML endpoints

These endpoints serve the pre-built HTML extracted from the SDK JAR in Step 2.

```java
/** Serves SDK auth tab HTML. */
@GET
@Path("/authentication/{subPath:.*}")
public InputStream authenticationTab(@PathParam("subPath") String subPath) {
    String filePath = "auth/" + (subPath.isEmpty() ? "index.html" : subPath);
    return getClass().getClassLoader().getResourceAsStream(filePath);
}
```

The audit tab HTML is served automatically by `AbstractAuditResource` at `/audit/`.

---

## Step 10 — Register the AuditResource in the JAX-RS Application class

Add the audit resource alongside the authentication resource:

```java
package app.krista.extensions.essentials.collaboration.office_365_calendar.api;

import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

@Service
@ApplicationPath("calendar")
@ContractsProvided(Application.class)
public class Office365CalendarApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
            AuthenticationResource.class,
            Office365CalendarAuditResource.class    // <-- add this
        );
    }
}
```

---

## Step 11 — Update the Extension class to expose custom tabs

Add the `@InvokerRequest(CUSTOM_TABS)` method to your Extension class. The tab map keys are display names; values are relative paths to the HTML.

```java
@InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
public Map<String, String> customTabs() {
    return Map.of(
        "Authentication",             "rest/calendar/authentication/",
        "Documentation",              "static/docs",
        "Office 365 Calendar Audit",  "rest/calendar/audit/"
    );
}
```

The paths follow the pattern `rest/<applicationPath>/<resourcePath>/`.

---

## Step 12 — Wire audit recording into catalog methods

In each catalog area class:

1. **Inject** `Office365CalendarAuditService` and `Invoker` (to get `invokerId`)
2. **Create** an `audited()` helper that wraps the catalog action with TRIGGERED/SUCCESS/FAILURE recording
3. **Wrap** each catalog method body with `return audited("Catalog Name", () -> { ... })`

### Constructor injection

```java
private final Office365CalendarAuditService auditService;
private final String invokerId;

@Inject
public MeetingsArea(/* other deps, */ Office365CalendarAuditService auditService,
                    Invoker invoker) {
    // ... other assignments ...
    this.auditService = auditService;
    this.invokerId = invoker.getInvokerId();
}
```

### The `audited()` wrapper

```java
private ExtensionResponse audited(String catalogName,
        java.util.function.Supplier<ExtensionResponse> action) {
    auditService.record(invokerId, "CATALOG_REQUEST_TRIGGERED",
                        "triggered", catalogName, null, null, null);
    try {
        ExtensionResponse response = action.get();
        if (response != null
                && response.getResult() == ExtensionResponse.Result.FAILURE) {
            auditService.record(invokerId, "CATALOG_REQUEST_FAILURE",
                                "failure", catalogName, null, null, null);
        } else {
            auditService.record(invokerId, "CATALOG_REQUEST_SUCCESS",
                                "success", catalogName, null, null, null);
        }
        return response;
    } catch (MustAuthorizeException mae) {
        auditService.recordAuthError(invokerId, catalogName, mae);
        throw mae;
    } catch (Exception e) {
        auditService.record(invokerId, "CATALOG_REQUEST_FAILURE",
                            "failure", catalogName, e.getMessage(),
                            e.getClass().getSimpleName(), null);
        throw new RuntimeException(e);
    }
}
```

### Usage in a catalog method

```java
public ExtensionResponse cancelEvent(String eventId, Boolean allowRetry) {
    return audited("Cancel Event", () -> {
        // ... existing implementation ...
    });
}
```

---

## Step 13 — Fix `AuthenticationResponse` field naming

The SDK's auth tab JavaScript expects the JSON success field to be serialized as `isSuccess`, not `success`. Declare the field as:

```java
public class AuthenticationResponse {
    private final boolean isSuccess;    // NOT "success"
    private final String errorMessage;
    private final String url;

    // constructor, getters ...
}
```

If you use a library like Gson, the field name `isSuccess` serializes to `"isSuccess"` in JSON. If you named the field `success`, the auth tab will not detect a successful save and will keep spinning.

---

## Step 14 — Reset auth counters after credential save

When credentials are saved successfully, call `auditService.resetAuthCounters()` to clear stale auth error records. This ensures the audit dashboard health panel reflects the new credential state.

In the `saveCredentials` endpoint:

```java
boolean isSaved = attributeStore.save(attributes, invokerId);
if (isSaved) {
    auditService.resetAuthCounters();
}
```

---

## Common Pitfalls

These are real bugs encountered during the Calendar integration.

### 1. Missing SPI file
**Symptom**: Metadata endpoint returns `{"error":"Extension not found: calendar"}`.
**Cause**: The `META-INF/services/...MsAuthExtensionDescriptor` file is missing or contains a typo.
**Fix**: Verify the file exists and contains the exact fully-qualified class name of your descriptor.

### 2. `ExtensionRegistry.initialize()` not called
**Symptom**: Same as above — extension not found even though the SPI file is correct.
**Cause**: The metadata endpoint did not call `ExtensionRegistry.initialize()` before `ExtensionRegistry.get()`.
**Fix**: Always call `initialize()` before `get()` in the metadata endpoint.

### 3. `isSuccess` vs `success` in `AuthenticationResponse`
**Symptom**: Auth tab spinner never stops after saving credentials, even though the backend returns a success JSON.
**Cause**: The SDK JavaScript checks for `isSuccess` in the JSON response. If the field is named `success`, it is never found.
**Fix**: Name the Java field `isSuccess` (see Step 13).

### 4. Forgetting `resetAuthCounters()` on credential save
**Symptom**: Audit dashboard health panel continues to show auth errors (TOKEN_EXPIRY_DETECTED, etc.) after re-saving valid credentials.
**Cause**: Old auth error records remain in KV store.
**Fix**: Call `auditService.resetAuthCounters()` after a successful save (see Step 14).

### 5. `extensionType` mismatch between AuthConfig and AuditService
**Symptom**: Audit records are stored under a different key than expected; dashboard shows no data.
**Cause**: The `@MsAuthExtension(extensionType = "...")` value doesn't match the second argument to `new MsAuthAuditService(keyValueStore, "...")`.
**Fix**: Use the same string in both places (e.g., `"calendar"`).

### 6. HTML extraction task not wired into the build
**Symptom**: `auth/index.html` or `audit/index.html` not found at runtime; auth tab returns 404.
**Cause**: The `extractSDKResources` task is defined but the `jar` task doesn't depend on it.
**Fix**: Ensure `jar` depends on `buildAndCopyUI` which depends on `extractSDKResources`.

### 7. Missing `@Path("/audit")` on AuditResource
**Symptom**: Audit tab returns 404.
**Cause**: The JAX-RS path annotation is missing on the AuditResource class.
**Fix**: Add `@Path("/audit")` at the class level.

### 8. AuditResource not registered in the Application class
**Symptom**: Audit endpoints return 404 even though the class compiles and `@Path` is correct.
**Cause**: The AuditResource class was not added to `getClasses()` in the JAX-RS Application.
**Fix**: Add it to the `Set.of(...)` return value (see Step 10).

---

## File Checklist

All files that need to be created or modified when integrating the SDK:

### New files

| File | Purpose |
|---|---|
| `<ExtName>AuthConfig.java` | Annotation-only SDK configuration |
| `<ExtName>ExtensionDescriptor.java` | SPI entry point for ServiceLoader |
| `META-INF/services/...MsAuthExtensionDescriptor` | SPI registration |
| `<ExtName>AuditService.java` | Thin delegate around `MsAuthAuditService` |
| `<ExtName>AuditResource.java` | JAX-RS resource extending `AbstractAuditResource` |

### Modified files

| File | Change |
|---|---|
| `build.gradle` | Add SDK dependency, `sdkUi` configuration, `extractSDKResources` task |
| API resource (e.g., `AuthenticationResource.java`) | Add `/msauth/metadata/{extensionId}` and `/authentication/{subPath}` endpoints |
| JAX-RS Application class | Register `AuditResource` in `getClasses()` |
| Extension class | Add `customTabs()` returning Authentication, Audit, and Documentation tabs |
| Catalog area classes | Inject `AuditService` + `Invoker`, wrap methods with `audited()` |
| `AuthenticationResponse.java` | Field named `isSuccess` (not `success`) |
| `saveCredentials` endpoint | Call `auditService.resetAuthCounters()` on success |
