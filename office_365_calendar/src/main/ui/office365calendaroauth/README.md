# Office 365 Calendar OAuth UI

This is the React-based authentication UI for the Office 365 Calendar extension.

## Features

- Public and Private authentication modes
- Test connection functionality
- Save credentials functionality
- Toast notifications for user feedback
- Responsive design

## Development

To run the development server:

```bash
npm start
```

To build for production:

```bash
npm run build
```

## Authentication Modes

### Public Mode
- Uses pre-configured client credentials
- Only requires email address
- Suitable for general use

### Private Mode
- Requires custom Azure App registration
- Needs Client ID, Client Secret, and Tenant ID
- Suitable for enterprise environments

## API Endpoints

The UI communicates with the following backend endpoints:

- `GET /getAuthKey` - Retrieves the current authentication type
- `GET /getCredentials?authType={type}` - Retrieves stored credentials
- `POST /testConnection` - Tests the connection with provided credentials
- `POST /saveCredentials` - Saves the credentials after validation
