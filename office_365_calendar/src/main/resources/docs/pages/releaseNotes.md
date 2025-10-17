# Release Notes - Office 365 Calendar Extension

## Version 2.0.19

### Release Information

- **Developer Name**: Vaibhav Choudhary
- **Krista Service APIs Java**: 1.0.118
- **Global Catalog Version**: GC-2025.10.4

### What's New

#### User Experience Improvements
- **Enhanced Error Messages**: Completely revamped all error messages throughout the extension to be more user-friendly and actionable
- **Improved Troubleshooting**: Added comprehensive troubleshooting section to ExtensionConfiguration.md with common error scenarios and solutions
- **Better Validation Messages**: Updated validator error messages in EmailValidator, EventIdValidator, DateValidator, DaysOfWeekValidator, and TimeZoneValidator

#### Code Quality & Testing
- **Comprehensive Unit Test Coverage**: Added 180 unit tests covering all static public methods across the extension
- **Production-Ready Code Review**: Completed comprehensive code review against 9 criteria (Correctness, OOP Principles, Code Quality, Security, Code Duplication, Regression Risk, Performance, Unit Test Coverage, Comments & Documentation)
- **Zero Critical Issues**: Code review confirmed production-ready status with no blocking issues

### Technical Improvements

- **Error Message Constants**: Updated 50+ error message constants in Constants.java for better user experience
- **Validation Enhancement**: Improved error handling across all validator classes with specific, actionable guidance
- **Test Framework**: Implemented comprehensive JUnit 4 test suite with proper Arrange-Act-Assert patterns
- **Documentation Updates**: Enhanced troubleshooting documentation with step-by-step solutions

### Issues Resolved

- Improved user experience with technical error messages replaced by clear, actionable guidance
- Enhanced code maintainability with comprehensive test coverage
- Better error handling for common validation scenarios
- Clearer documentation for troubleshooting authentication and configuration issues

### Code Review Summary

**Overall Assessment**: ✅ **Approved for Production**
- **Critical Issues**: 0
- **High Priority Issues**: 0
- **Medium Priority Issues**: 3 (future optimizations)
- **Low Priority Issues**: 4 (minor improvements)
- **Total Test Coverage**: 180 unit tests with BUILD SUCCESSFUL

---

## Version 2.0.18

### Release Information

- **Developer Name**: Vrushali Gaikwad
- **Krista Service APIs Java**: 1.0.115
- **Global Catalog Version**: GC-2025.10.2

### What's New

#### Enhanced Authentication Support
- Improved authentication flow for switching between Public and Private authentication modes
- Better handling of Microsoft Entra ID (formerly Azure AD) authentication transitions
- Enhanced token management and refresh mechanisms

#### Delegation Support Improvements
- Added comprehensive support for delegated email in error handling flow
- Improved error messages when delegation permissions are insufficient
- Better validation for organizer and delegate email parameters

### Issues Resolved

### Technical Improvements

- Enhanced validation logic for delegation scenarios
- Improved error messages for authentication failures
- Better handling of Microsoft Entra ID token expiration
- Optimized retry logic for failed requests
- Enhanced logging for troubleshooting delegation issues

### Microsoft Entra ID Integration

This version fully supports Microsoft Entra ID (formerly known as Azure Active Directory) with:
- Updated authentication flows
- Enhanced permission validation
- Improved token management
- Better error handling for Microsoft Entra ID-specific scenarios

### Known Limitations

- Maximum 500 attendees per meeting (Office 365 limitation)
- Maximum 150 MB total attachment size per meeting
- Recurring events limited to 999 occurrences
- Delegation requires proper Office 365 calendar sharing permissions

### Upgrade Notes

When upgrading to version 2.0.16:

1. **Authentication Configuration**: Review your authentication settings if switching between Public and Private modes
2. **Delegation Permissions**: Verify calendar sharing permissions are properly configured for delegation features
3. **Microsoft Entra ID**: Ensure your app registration in Microsoft Entra ID has the required permissions
4. **Testing**: Test delegation scenarios if you use Organizer Email or Delegate Email parameters

### API Permissions Required

Ensure your Microsoft Entra ID app registration has the following permissions:

**Delegated Permissions**:
- Calendars.ReadWrite
- Calendars.ReadWrite.Shared
- offline_access
- openid
- profile

**Application Permissions** (Optional):
- Calendars.ReadBasic.All

### Support and Documentation

For detailed information about features and configuration:

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication](pages/Authentication.md)
- [Creating Office 365 App](pages/CreatingOffice365App.md)
- [Catalog Requests Documentation](../README.md)

### Getting Help

If you encounter issues:

1. Review the troubleshooting sections in the documentation
2. Verify your Microsoft Entra ID app registration and permissions
3. Check Office 365 service health status
4. Contact your Krista administrator for assistance

---

## Previous Versions

### Version 2.0.15 and Earlier

For information about previous versions, please contact your Krista administrator or refer to the legacy documentation.
