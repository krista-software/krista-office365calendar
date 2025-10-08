# Authentication

## Overview

The Office 365 Calendar Extension uses OAuth 2.0 authentication with Microsoft Entra ID (formerly Azure AD) to securely access Office 365 Calendar services. This document explains the authentication modes, OAuth flows, token management, and security best practices.

## Authentication Modes

The extension supports two authentication modes, each suited for different use cases.

### Service Account Authentication

**Description**: Uses a single service account to authenticate and perform all calendar operations.

**When to Use**:
- Automated workflows without user interaction
- System-to-system integrations
- Scheduled tasks and batch operations
- Centralized calendar management
- Backend processes

**How It Works**:
1. Administrator authenticates once with a service account
2. Extension stores refresh token for the service account
3. All subsequent operations use the service account's credentials
4. All calendar operations appear as performed by the service account

**Advantages**:
- ✅ Simple setup - authenticate once
- ✅ No user interaction required
- ✅ Suitable for automation
- ✅ Consistent permissions
- ✅ Easier to maintain

**Disadvantages**:
- ❌ All operations performed as one user
- ❌ Limited audit trail (all actions appear from service account)
- ❌ Service account needs access to all calendars
- ❌ Less suitable for user-specific operations

**Configuration**: Set "Authentication Type" to "Service Account" (Ask a System = Checked)

![Service Account Authentication](../_media/ExtensionConfiguration_askASystemChecked.png)

### User Account Authentication

**Description**: Each user authenticates with their own Office 365 account.

**When to Use**:
- User-specific calendar operations
- Each user manages their own calendar
- Delegation scenarios
- Personalized calendar access
- Audit trail requirements

**How It Works**:
1. Each user authenticates individually with their Office 365 account
2. Extension stores separate refresh tokens for each user
3. Operations performed in the context of the authenticated user
4. Calendar operations appear as performed by the actual user

**Advantages**:
- ✅ User-specific operations
- ✅ Better audit trail
- ✅ Supports delegation
- ✅ Personalized access
- ✅ Follows principle of least privilege

**Disadvantages**:
- ❌ Each user must authenticate
- ❌ More complex setup
- ❌ Requires user interaction
- ❌ Multiple token management

**Configuration**: Set "Authentication Type" to "User Account" (Ask a System = Unchecked)

![User Account Authentication](../_media/ExtensionConfiguration_askASystemUnchecked.png)

## OAuth 2.0 Scopes and Permissions

The extension requires specific Microsoft Graph API permissions to access calendar data.

### Required Delegated Permissions

| Permission | Scope | Description | Required For |
|------------|-------|-------------|--------------|
| Calendars.ReadWrite | `https://graph.microsoft.com/Calendars.ReadWrite` | Read and write user calendars | All calendar operations |
| Calendars.ReadWrite.Shared | `https://graph.microsoft.com/Calendars.ReadWrite.Shared` | Read and write user and shared calendars | Delegation, shared calendars |
| offline_access | `https://graph.microsoft.com/offline_access` | Maintain access to data | Token refresh |
| openid | `https://graph.microsoft.com/openid` | Sign users in | Authentication |
| profile | `https://graph.microsoft.com/profile` | View users' basic profile | User identification |

### Optional Application Permissions

| Permission | Scope | Description | Required For |
|------------|-------|-------------|--------------|
| Calendars.ReadBasic.All | `https://graph.microsoft.com/Calendars.ReadBasic.All` | Read basic details of calendars in all mailboxes | System-level queries (optional) |

### Permission Levels Explained

**Calendars.ReadWrite**:
- Allows reading and writing to the user's calendar
- Required for: Schedule Meeting, Update Event, Cancel Event, Get Event
- Scope: User's own calendar only

**Calendars.ReadWrite.Shared**:
- Allows reading and writing to shared calendars
- Required for: Delegation scenarios, shared calendar access
- Scope: User's calendar and calendars shared with the user

**offline_access**:
- Allows the extension to refresh access tokens without user interaction
- Critical for: Long-running sessions, automated workflows
- Enables: Refresh token grant

**Why Not Calendars.Read?**:
The extension requires write permissions (Calendars.ReadWrite) because even query operations may need to update metadata or mark events as read. Using read-only permissions would limit functionality.

## OAuth 2.0 Grant Types

The extension uses two OAuth 2.0 grant types for authentication and token management.

### Authorization Code Grant

**Purpose**: Initial authentication to obtain access and refresh tokens

**Grant Type**: `authorization_code`

**Flow**:
1. User clicks "Validate" or "Authenticate" in Krista
2. User is redirected to Microsoft login page
3. User signs in with Office 365 credentials
4. User grants requested permissions (consent screen)
5. Microsoft redirects back to Krista with authorization code
6. Extension exchanges authorization code for tokens

**Request Example**:
```http
POST https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token
Content-Type: application/x-www-form-urlencoded

client_id={client_id}
&scope=https://graph.microsoft.com/Calendars.ReadWrite
       https://graph.microsoft.com/Calendars.ReadWrite.Shared
       https://graph.microsoft.com/offline_access
       https://graph.microsoft.com/openid
       https://graph.microsoft.com/profile
&code={authorization_code}
&redirect_uri={redirect_uri}
&grant_type=authorization_code
&client_secret={client_secret}
```

**Response Example**:
```json
{
  "token_type": "Bearer",
  "scope": "Calendars.ReadWrite Calendars.ReadWrite.Shared offline_access openid profile",
  "expires_in": 3600,
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "refresh_token": "0.AXoA1234567890abcdef..."
}
```

**Key Points**:
- Used only during initial authentication
- Requires user interaction
- Returns both access token and refresh token
- Access token expires in 1 hour (3600 seconds)

### Refresh Token Grant

**Purpose**: Obtain new access tokens without user interaction

**Grant Type**: `refresh_token`

**Flow**:
1. Access token expires (after 1 hour)
2. Extension automatically uses refresh token
3. Microsoft issues new access token
4. Extension continues operations seamlessly

**Request Example**:
```http
POST https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token
Content-Type: application/x-www-form-urlencoded

client_id={client_id}
&scope=https://graph.microsoft.com/Calendars.ReadWrite
       https://graph.microsoft.com/Calendars.ReadWrite.Shared
       https://graph.microsoft.com/offline_access
&refresh_token={refresh_token}
&grant_type=refresh_token
&client_secret={client_secret}
```

**Response Example**:
```json
{
  "token_type": "Bearer",
  "scope": "Calendars.ReadWrite Calendars.ReadWrite.Shared offline_access",
  "expires_in": 3600,
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "refresh_token": "0.AXoA0987654321fedcba..."
}
```

**Key Points**:
- Used automatically by the extension
- No user interaction required
- Returns new access token and new refresh token
- Enables seamless long-running operations


## Authentication Flow

### Initial Authentication Flow

```
1. User initiates authentication in Krista
   ↓
2. Krista redirects to Microsoft login
   URL: https://login.microsoftonline.com/{tenant}/oauth2/v2.0/authorize
   ↓
3. User signs in with Office 365 credentials
   ↓
4. Microsoft shows consent screen (first time only)
   User grants requested permissions
   ↓
5. Microsoft redirects back to Krista with authorization code
   URL: {Extension Base URL}/office365calendar/callback?code={code}
   ↓
6. Extension exchanges authorization code for tokens
   POST to: https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token
   ↓
7. Extension receives access token and refresh token
   ↓
8. Extension stores tokens securely
   ↓
9. Authentication complete - Extension ready to use
```

### Token Refresh Flow

```
1. Extension makes API call with access token
   ↓
2. Access token expired (after 1 hour)
   ↓
3. Microsoft returns 401 Unauthorized
   ↓
4. Extension automatically uses refresh token
   POST to: https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token
   ↓
5. Microsoft validates refresh token
   ↓
6. Microsoft issues new access token and refresh token
   ↓
7. Extension retries original API call with new access token
   ↓
8. Operation completes successfully
```

## Token Management

### Access Token

**Lifetime**: 1 hour (3600 seconds)

**Purpose**: Authorizes API requests to Microsoft Graph

**Storage**: In-memory during active session

**Expiration Handling**: Automatic refresh using refresh token

**Format**: JWT (JSON Web Token)

**Usage**: Included in Authorization header of API requests
```http
Authorization: Bearer {access_token}
```

### Refresh Token

**Lifetime**:
- **90 days of inactivity** (default for most apps)
- **No expiration** if used regularly (within 90 days)

**Purpose**: Obtain new access tokens without user interaction

**Storage**: Securely stored by Krista extension

**Expiration Handling**: Requires re-authentication if expired

**Security**: More sensitive than access token - stored securely

### Token Expiration Scenarios

#### Scenario 1: Access Token Expiration (Normal)

**Frequency**: Every 1 hour

**Handling**: Automatic refresh using refresh token

**User Impact**: None - seamless

**Extension Behavior**:
1. Detects 401 Unauthorized response
2. Uses refresh token to get new access token
3. Retries original request
4. Operation continues normally

#### Scenario 2: Refresh Token Expiration (Inactivity)

**Frequency**: After 90 days of no use

**Handling**: Requires re-authentication

**User Impact**: Must authenticate again

**Extension Behavior**:
1. Refresh token request fails
2. Extension prompts for re-authentication
3. User must sign in again
4. New tokens issued

**Prevention**: Use the extension at least once every 90 days

#### Scenario 3: Token Revocation (User Action)

**Cause**: User revokes app permissions in Office 365

**Handling**: Requires re-authentication

**User Impact**: Must authenticate again

**Extension Behavior**:
1. API requests fail with 401 Unauthorized
2. Refresh token also invalid
3. Extension prompts for re-authentication

#### Scenario 4: Token Revocation (Password Change)

**Cause**: User changes Office 365 password

**Handling**: Tokens may be revoked, requires re-authentication

**User Impact**: Must authenticate again

**Extension Behavior**:
1. Existing tokens become invalid
2. Extension prompts for re-authentication
3. User signs in with new password

#### Scenario 5: Token Revocation (Security Event)

**Cause**: Microsoft detects suspicious activity

**Handling**: Tokens revoked immediately

**User Impact**: Must authenticate again

**Extension Behavior**:
1. All tokens invalidated
2. Extension prompts for re-authentication
3. May require additional security verification

#### Scenario 6: Token Limit Exceeded

**Cause**: Too many refresh tokens issued (Microsoft limit: ~50 per user per app)

**Handling**: Oldest tokens automatically revoked

**User Impact**: May need to re-authenticate

**Prevention**: Use Service Account mode for automated workflows


## Security Best Practices

### 1. Protect Client Secret

- ✅ Store in secured parameters
- ✅ Never expose in client-side code
- ✅ Rotate regularly (every 12-24 months)
- ✅ Use Azure Key Vault for additional security
- ❌ Never commit to source control
- ❌ Never log or display in error messages

### 2. Use HTTPS Only

- ✅ Always use HTTPS for Extension Base URL
- ✅ Ensure redirect URI uses HTTPS
- ✅ Verify SSL certificates are valid
- ❌ Never use HTTP for OAuth flows

### 3. Validate Redirect URI

- ✅ Configure exact redirect URI in Microsoft Entra ID
- ✅ Verify redirect URI matches extension configuration
- ✅ Use specific URIs, not wildcards
- ❌ Never use localhost in production

### 4. Implement Least Privilege

- ✅ Request only required permissions
- ✅ Use delegated permissions when possible
- ✅ Review permissions regularly
- ❌ Don't request unnecessary scopes

### 5. Monitor Authentication

- ✅ Review sign-in logs in Microsoft Entra ID
- ✅ Monitor for unusual activity
- ✅ Set up alerts for failed authentications
- ✅ Audit token usage

### 6. Handle Token Expiration

- ✅ Implement automatic token refresh
- ✅ Handle refresh token expiration gracefully
- ✅ Provide clear re-authentication prompts
- ✅ Log token refresh failures

### 7. Secure Token Storage

- ✅ Store tokens encrypted
- ✅ Use secure storage mechanisms
- ✅ Never log tokens
- ✅ Clear tokens on logout

## Troubleshooting Authentication

### Issue: "Redirect URI Mismatch"

**Cause**: Redirect URI doesn't match Microsoft Entra ID configuration

**Solution**:
1. Verify Extension Base URL in Krista configuration
2. Check redirect URI in Microsoft Entra ID: `{Base URL}/office365calendar/callback`
3. Ensure exact match (case-sensitive, no trailing slash)
4. Update Microsoft Entra ID if needed

### Issue: "Invalid Client"

**Cause**: Client ID or Client Secret is incorrect

**Solution**:
1. Verify Client ID from Microsoft Entra ID app registration
2. Verify Client Secret (create new if needed)
3. Check for extra spaces or characters
4. Ensure client secret hasn't expired

### Issue: "Consent Required"

**Cause**: User hasn't granted permissions or admin consent missing

**Solution**:
1. User must complete consent screen during authentication
2. For organization-wide: Admin must grant consent in Microsoft Entra ID
3. Verify all required permissions are configured
4. Re-authenticate to trigger consent screen

### Issue: "Token Expired"

**Cause**: Refresh token expired or revoked

**Solution**:
1. Re-authenticate the extension
2. Ensure offline_access permission is granted
3. Use extension regularly (within 90 days)
4. Check if user changed password or revoked access

### Issue: "Insufficient Permissions"

**Cause**: Required API permissions not granted

**Solution**:
1. Verify all required permissions in Microsoft Entra ID
2. Ensure admin consent has been granted
3. Wait a few minutes for permissions to propagate
4. Re-authenticate after adding permissions

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Configure the extension
- [Creating Office 365 App](pages/CreatingOffice365App.md) - Microsoft Entra ID app registration
- [Microsoft Identity Platform Documentation](https://learn.microsoft.com/en-us/azure/active-directory/develop/) - Official OAuth documentation
- [Microsoft Graph Permissions Reference](https://learn.microsoft.com/en-us/graph/permissions-reference) - Complete permissions list
