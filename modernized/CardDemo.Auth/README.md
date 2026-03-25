# COSGN00C → .NET Conversion (Sign-on with Okta Authentication)

> **Original COBOL:** `app/cbl/COSGN00C.cbl` (261 lines)
> **Converted to:** ASP.NET Core 8 Minimal API with Okta OIDC

## Overview

This is a modernized .NET conversion of the CardDemo sign-on program (`COSGN00C`).
The original program authenticates users against a VSAM `USRSEC` file and routes
them to either the Admin menu (`COADM01C`) or User menu (`COMEN01C`) based on
their user type. This conversion replaces the VSAM lookup with **Okta OpenID Connect**
authentication while preserving the original business logic for role-based routing.

## COBOL → .NET Mapping

| COBOL Concept | .NET Equivalent |
|--------------|-----------------|
| CICS Transaction `CC00` | HTTP endpoint `POST /api/auth/login` |
| VSAM `USRSEC` file read | Okta OIDC token exchange + local user store fallback |
| `SEC-USR-TYPE = 'A'` (Admin) | Okta group claim `CardDemo-Admin` or local role |
| `SEC-USR-TYPE = 'U'` (User) | Okta group claim `CardDemo-User` or local role |
| `CARDDEMO-COMMAREA` | JWT claims / session state |
| BMS Map `COSGN0A` | JSON API response (frontend-agnostic) |
| `EXEC CICS XCTL PROGRAM('COADM01C')` | Redirect URL or route claim in JWT |
| `EXEC CICS XCTL PROGRAM('COMEN01C')` | Redirect URL or route claim in JWT |
| `DFHCOMMAREA` (program-to-program) | JWT token with embedded claims |

## Configuration

Set the following in `appsettings.json` or environment variables:

```json
{
  "Okta": {
    "Domain": "https://your-org.okta.com",
    "ClientId": "your-client-id",
    "ClientSecret": "your-client-secret",
    "AuthorizationServerId": "default"
  },
  "CardDemo": {
    "AdminGroupClaim": "CardDemo-Admin",
    "UserGroupClaim": "CardDemo-User",
    "FallbackToLocalAuth": true
  }
}
```

## Running

```bash
cd modernized/CardDemo.Auth
dotnet restore
dotnet run
```

The API will be available at `https://localhost:5001`.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Authenticate user (local fallback or Okta redirect) |
| GET | `/api/auth/okta/callback` | Okta OIDC callback |
| GET | `/api/auth/me` | Get current user info and role |
| POST | `/api/auth/logout` | End session |
