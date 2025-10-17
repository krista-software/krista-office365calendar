# Extension Configuration

## Overview

The Office 365 Calendar Extension requires configuration with Microsoft Entra ID (formerly Azure AD) credentials to authenticate and interact with Office 365 Calendar services. This guide walks you through the configuration process step-by-step.

## Prerequisites

Before configuring the extension, ensure you have:

- ✅ Created a Microsoft Entra ID app registration (see [Creating Office 365 App](pages/CreatingOffice365App.md))
- ✅ Obtained Client ID, Tenant ID, and Client Secret
- ✅ Configured API permissions and granted admin consent
- ✅ Access to your Krista application with appropriate permissions

## Configuration Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| Client ID | Text | Yes | Application (client) ID from Microsoft Entra ID app registration | `a1b2c3d4-e5f6-7890-abcd-ef1234567890` |
| Client Secret | Text (Secured) | Yes | Client secret value from Microsoft Entra ID app registration | `abc123XYZ~def456...` |
| Tenant ID | Text (Secured) | Yes | Directory (tenant) ID from Microsoft Entra ID | `9876fedc-ba98-7654-3210-fedcba987654` |
| Extension Base URL | Text | Yes | Base URL of your Krista instance | `https://your-instance.krista.ai` |
| Authentication Type | Pick One | Yes | Service Account or User Account authentication mode | Service Account |
| Allow Retry | Yes/No | No | Enable automatic retry for failed requests with validation prompts | Yes |

### Parameter Details

#### Client ID
- **Source**: Microsoft Entra ID app registration → Overview → Application (client) ID
- **Format**: GUID (Globally Unique Identifier)
- **Security**: Not sensitive, can be stored in plain text
- **Purpose**: Identifies your application to Microsoft Entra ID

#### Client Secret
- **Source**: Microsoft Entra ID app registration → Certificates & secrets → Client secret value
- **Format**: Alphanumeric string with special characters
- **Security**: **HIGHLY SENSITIVE** - Store securely, never expose in client-side code
- **Expiration**: Secrets expire (typically 6, 12, or 24 months) - set reminders to renew
- **Important**: Copy the value immediately when created; it's only shown once

#### Tenant ID
- **Source**: Microsoft Entra ID app registration → Overview → Directory (tenant) ID
- **Format**: GUID (Globally Unique Identifier)
- **Security**: Secured parameter in Krista
- **Purpose**: Identifies your Microsoft Entra ID tenant/organization

#### Extension Base URL
- **Format**: Full HTTPS URL without trailing slash
- **Example**: `https://your-instance.krista.ai`
- **Purpose**: Used to construct the OAuth redirect URI
- **Redirect URI**: Automatically constructed as `{Extension Base URL}/office365calendar/callback`

#### Authentication Type
- **Options**: 
  - **Service Account**: Uses a single service account for all operations
  - **User Account**: Each user authenticates with their own Office 365 account
- **Default**: Service Account
- **See**: [Authentication](pages/Authentication.md) for detailed comparison

#### Allow Retry
- **Purpose**: Enables validation prompts when input parameters fail validation
- **Behavior**: 
  - **Yes**: Extension prompts user to re-enter invalid parameters
  - **No**: Extension returns error immediately without retry
- **Recommendation**: Set to **Yes** for better user experience

## Step-by-Step Setup

### Step 1: Add the Extension

1. Log in to your Krista application
2. Navigate to the Extensions section
3. Click **Add Extension**
4. Search for "Office 365 Calendar"
5. Click **Add** to add the extension to your application

![Extension URL](../_media/ExtensionConfiguration_extensionUrl.png)

### Step 2: Configure Client ID

1. In the extension configuration page, locate the **Client ID** field
2. Paste the Application (client) ID from your Microsoft Entra ID app registration
3. Verify the GUID format is correct (8-4-4-4-12 hexadecimal digits)

### Step 3: Configure Client Secret

1. Locate the **Client Secret** field (marked as secured)
2. Paste the client secret value you copied when creating the secret
3. **Important**: Ensure you copy the **Value**, not the **Secret ID**
4. The field will be masked after saving for security

### Step 4: Configure Tenant ID

1. Locate the **Tenant ID** field (marked as secured)
2. Paste the Directory (tenant) ID from your Microsoft Entra ID app registration
3. Verify the GUID format is correct

### Step 5: Configure Extension Base URL

1. Locate the **Extension Base URL** field
2. Enter your Krista instance URL (e.g., `https://your-instance.krista.ai`)
3. **Do not** include a trailing slash
4. Ensure the URL uses HTTPS (required for OAuth)

### Step 6: Select Authentication Type

1. Locate the **Authentication Type** field
2. Choose between:
   - **Service Account**: Recommended for automated workflows and system integrations
   - **User Account**: Recommended when operations should be performed as individual users

![Authentication Type Selection](../_media/ExtensionConfiguration_askASystemChecked.png)

**Service Account Selected** (Ask a System = Checked)

![User Account Selection](../_media/ExtensionConfiguration_askASystemUnchecked.png)

**User Account Selected** (Ask a System = Unchecked)

### Step 7: Configure Allow Retry (Optional)

1. Locate the **Allow Retry** field
2. Set to **Yes** to enable validation prompts
3. Set to **No** for immediate error returns
4. **Recommendation**: Use **Yes** for better user experience

### Step 8: Save Configuration

1. Review all configuration parameters
2. Click **Save** to save the extension configuration
3. The extension is now configured but not yet authenticated

### Step 9: Validate the Extension

1. After saving, click **Validate** or **Authenticate**
2. You will be redirected to Microsoft's login page
3. Sign in with an Office 365 account that has calendar access
4. Grant the requested permissions
5. You will be redirected back to Krista
6. Verify the extension shows as "Authenticated" or "Valid"

### Step 10: Test the Extension

1. Create a simple test workflow using a catalog request (e.g., "Get Next Upcoming Meetings")
2. Execute the workflow
3. Verify the extension successfully connects to Office 365 Calendar
4. Check that data is retrieved correctly

## Redirect URI Configuration

The redirect URI is automatically constructed based on your Extension Base URL:

**Format**: `{Extension Base URL}/office365calendar/callback`

**Example**: If your Extension Base URL is `https://your-instance.krista.ai`, the redirect URI will be:
```
https://your-instance.krista.ai/office365calendar/callback
```

**Important**: This redirect URI must be configured in your Microsoft Entra ID app registration. See [Creating Office 365 App](pages/CreatingOffice365App.md) for details.

### Verifying Redirect URI

1. Go to Azure Portal → Microsoft Entra ID → App registrations
2. Select your app registration
3. Click **Authentication** in the left menu
4. Under **Platform configurations** → **Web**, verify the redirect URI matches exactly
5. If it doesn't match, add the correct redirect URI and save

## Authentication Type Selection

### Service Account Authentication

**When to Use**:
- Automated workflows without user interaction
- System-to-system integrations
- Scheduled tasks and batch operations
- Single calendar management

**Characteristics**:
- One-time authentication with a service account
- All operations performed as the service account
- Simpler setup and maintenance
- Suitable for centralized calendar management

**Permissions Required**:
- Service account must have access to calendars being managed
- May require Application permissions for some operations

### User Account Authentication

**When to Use**:
- User-specific calendar operations
- Each user manages their own calendar
- Delegation and multi-user scenarios
- Personalized calendar access

**Characteristics**:
- Each user authenticates with their own Office 365 account
- Operations performed in the context of the authenticated user
- Better audit trail (operations tracked per user)
- Supports delegation scenarios

**Permissions Required**:
- Delegated permissions (user consent)
- Each user must authenticate individually

**Recommendation**: Use **Service Account** for most scenarios unless you specifically need user-specific authentication.

## Security Considerations

### 1. Client Secret Protection

- **Never expose** the client secret in client-side code or public repositories
- **Store securely** using Krista's secured parameter feature
- **Rotate regularly** (every 12-24 months)
- **Set expiration reminders** to avoid service disruption
- **Use Azure Key Vault** for additional security (optional)

### 2. Least Privilege Principle

- Grant only the minimum required API permissions
- Review permissions regularly
- Remove unused permissions
- Use delegated permissions when possible

### 3. Network Security

- **Always use HTTPS** for Extension Base URL
- Ensure redirect URI uses HTTPS
- Configure firewall rules if needed
- Monitor authentication logs

### 4. Access Control

- Limit who can view/modify extension configuration
- Use role-based access control in Krista
- Audit configuration changes
- Implement approval workflows for sensitive operations

## Troubleshooting

### Issue: "Invalid Client ID"

**Cause**: Client ID is incorrect or malformed

**Solution**:
1. Verify you copied the Application (client) ID, not the Object ID
2. Check for extra spaces or characters
3. Ensure the GUID format is correct (8-4-4-4-12)
4. Verify the app registration exists in Microsoft Entra ID

### Issue: "Invalid Client Secret"

**Cause**: Client secret is incorrect, expired, or not copied correctly

**Solution**:
1. Verify you copied the secret **Value**, not the Secret ID
2. Check if the secret has expired in Microsoft Entra ID
3. Create a new client secret if needed
4. Ensure no extra spaces were copied
5. Update the configuration with the new secret

### Issue: "Redirect URI Mismatch"

**Cause**: Redirect URI in Microsoft Entra ID doesn't match the extension configuration

**Solution**:
1. Verify Extension Base URL is correct
2. Check redirect URI in Microsoft Entra ID app registration
3. Ensure exact match: `{Extension Base URL}/office365calendar/callback`
4. Check for trailing slashes (should not have one)
5. Verify HTTPS is used

### Issue: "Authentication Failed"

**Cause**: Various authentication issues

**Solution**:
1. Verify all configuration parameters are correct
2. Check that API permissions are granted in Microsoft Entra ID
3. Ensure admin consent has been provided
4. Verify the service account has calendar access (for Service Account mode)
5. Check Office 365 service health status
6. Review authentication logs in Microsoft Entra ID

### Issue: "Permission Denied"

**Cause**: Insufficient API permissions or missing admin consent

**Solution**:
1. Go to Microsoft Entra ID app registration → API permissions
2. Verify all required permissions are added:
   - Calendars.ReadWrite
   - Calendars.ReadWrite.Shared
   - offline_access
   - openid
   - profile
3. Click "Grant admin consent for [Your Organization]"
4. Wait a few minutes for permissions to propagate
5. Re-authenticate the extension

### Issue: "Token Expired"

**Cause**: Access token has expired and refresh failed

**Solution**:
1. The extension should automatically refresh tokens
2. If automatic refresh fails, re-validate the extension
3. Check that offline_access permission is granted
4. Verify the refresh token hasn't expired (90 days of inactivity)
5. Re-authenticate if necessary

## Best Practices

### 1. Use Descriptive App Names
Name your Microsoft Entra ID app registration clearly (e.g., "Krista Office 365 Calendar - Production")

### 2. Document Configuration
Keep a secure record of:
- Client ID
- Tenant ID
- Client Secret expiration date
- Extension Base URL
- Authentication type chosen

### 3. Set Expiration Reminders
Create calendar reminders 30 days before client secret expiration

### 4. Test in Non-Production First
Configure and test the extension in a development/staging environment before production

### 5. Monitor Authentication
Regularly review sign-in logs in Microsoft Entra ID for unusual activity

### 6. Use Service Account for Automation
For automated workflows, use Service Account authentication with a dedicated service account

### 7. Enable Allow Retry
Set Allow Retry to Yes for better user experience and easier troubleshooting

## Troubleshooting Common Issues

### Authentication Errors

**Issue**: "Unable to authenticate with Office 365"
- **Solution**: Verify your Client ID, Client Secret, and Tenant ID are correct
- **Action**: Check that your Microsoft Entra ID app has the required permissions
- **Tip**: Ensure admin consent has been granted for the application

**Issue**: "Authentication failed. Please verify your credentials"
- **Solution**: Re-enter your credentials and ensure they haven't expired
- **Action**: Check if your Client Secret has expired in Azure portal
- **Tip**: Generate a new Client Secret if the current one has expired

### Permission Errors

**Issue**: "Unable to access your Office 365 account"
- **Solution**: Verify the user has appropriate calendar permissions
- **Action**: Check Microsoft Entra ID app permissions include Calendars.ReadWrite
- **Tip**: Ensure the user has consented to the application permissions

### Input Validation Errors

**Issue**: "Please enter a valid email address"
- **Solution**: Use proper email format (e.g., user@company.com)
- **Action**: Check for typos in email addresses
- **Tip**: Verify the email address exists in your organization

**Issue**: "Please enter a valid time zone"
- **Solution**: Use standard time zone identifiers (e.g., 'America/New_York', 'Europe/London', 'UTC')
- **Action**: Refer to standard IANA time zone database
- **Tip**: Common time zones include 'UTC', 'America/New_York', 'Europe/London', 'Asia/Tokyo'

**Issue**: "The start date must be before or equal to the end date"
- **Solution**: Adjust your date selection to ensure proper chronological order
- **Action**: Double-check your date inputs
- **Tip**: For single-day events, start and end dates can be the same

## Next Steps

After configuring the extension:

1. **Understand Authentication**: Read [Authentication](pages/Authentication.md) to understand OAuth flows
2. **Test Catalog Requests**: Try simple requests like [Get Next Upcoming Meetings](pages/GetNextUpcomingMeetings.md)
3. **Explore Features**: Review available [catalog requests](../README.md#documentation-structure)
4. **Set Up Workflows**: Integrate calendar operations into your Krista workflows

## See Also

- [Creating Office 365 App](pages/CreatingOffice365App.md) - Microsoft Entra ID app registration guide
- [Authentication](pages/Authentication.md) - Understanding OAuth 2.0 authentication
- [Schedule Meeting](pages/ScheduleMeeting.md) - Create calendar events
- [Microsoft Graph Calendar API](https://learn.microsoft.com/en-us/graph/api/resources/calendar) - Official API documentation

