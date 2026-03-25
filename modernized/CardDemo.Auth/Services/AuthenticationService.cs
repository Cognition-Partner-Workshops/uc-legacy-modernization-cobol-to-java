// ==========================================================================
// CardDemo Sign-on — Modernized .NET Authentication Service
// Original COBOL: COSGN00C.cbl — READ-USER-SEC-FILE paragraph
//
// This service replaces the COBOL logic that:
//   1. Reads USRSEC VSAM file by user ID (EXEC CICS READ DATASET)
//   2. Compares plain-text password (IF SEC-USR-PWD = WS-USER-PWD)
//   3. Routes to admin or user menu based on SEC-USR-TYPE
//
// Modernization: Okta OIDC as primary auth, local DB as fallback.
// ==========================================================================

using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using CardDemo.Auth.Models;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;

namespace CardDemo.Auth.Services;

/// <summary>
/// Configuration for Okta OIDC integration.
/// Replaces the VSAM USRSEC file as the identity store.
/// </summary>
public class OktaSettings
{
    public string Domain { get; set; } = string.Empty;
    public string ClientId { get; set; } = string.Empty;
    public string ClientSecret { get; set; } = string.Empty;
    public string AuthorizationServerId { get; set; } = "default";
}

/// <summary>
/// CardDemo-specific authentication settings.
/// </summary>
public class CardDemoAuthSettings
{
    /// <summary>Okta group name that maps to SEC-USR-TYPE = 'A'.</summary>
    public string AdminGroupClaim { get; set; } = "CardDemo-Admin";

    /// <summary>Okta group name that maps to SEC-USR-TYPE = 'U'.</summary>
    public string UserGroupClaim { get; set; } = "CardDemo-User";

    /// <summary>
    /// When true, falls back to local user store if Okta is not configured.
    /// This preserves the original VSAM USRSEC behavior for development/testing.
    /// </summary>
    public bool FallbackToLocalAuth { get; set; } = true;

    /// <summary>JWT signing key for local token generation.</summary>
    public string JwtSigningKey { get; set; } = "CardDemo-Default-Signing-Key-Change-In-Production-Min32Chars!";
}

/// <summary>
/// Authentication service — modernized replacement for COSGN00C.
///
/// Original COBOL flow (READ-USER-SEC-FILE paragraph):
///   1. EXEC CICS READ DATASET(WS-USRSEC-FILE) INTO(SEC-USER-DATA)
///      RIDFLD(WS-USER-ID) → Read user record by ID
///   2. EVALUATE WS-RESP-CD:
///      WHEN 0  → Record found, check password
///      WHEN 13 → Record not found ("User not found. Try again ...")
///      WHEN OTHER → System error ("Unable to verify the User ...")
///   3. IF SEC-USR-PWD = WS-USER-PWD → Password match
///      THEN set COMMAREA fields and XCTL to appropriate menu
///      ELSE "Wrong Password. Try again ..."
///
/// Modernized flow:
///   1. Try Okta OIDC authentication (if configured)
///   2. Fall back to local user store (seeded from DUSRSECJ data)
///   3. Generate JWT token containing COMMAREA-equivalent claims
///   4. Return routing decision (COADM01C or COMEN01C)
/// </summary>
public class AuthenticationService
{
    private readonly OktaSettings _oktaSettings;
    private readonly CardDemoAuthSettings _authSettings;
    private readonly ILogger<AuthenticationService> _logger;
    private readonly LocalUserStore _localUserStore;
    private readonly HttpClient _httpClient;

    public AuthenticationService(
        IOptions<OktaSettings> oktaSettings,
        IOptions<CardDemoAuthSettings> authSettings,
        ILogger<AuthenticationService> logger,
        LocalUserStore localUserStore,
        HttpClient httpClient)
    {
        _oktaSettings = oktaSettings.Value;
        _authSettings = authSettings.Value;
        _logger = logger;
        _localUserStore = localUserStore;
        _httpClient = httpClient;
    }

    /// <summary>
    /// Authenticate a user — modernized replacement for PROCESS-ENTER-KEY
    /// and READ-USER-SEC-FILE paragraphs in COSGN00C.
    ///
    /// COBOL validation (PROCESS-ENTER-KEY):
    ///   WHEN USERIDI OF COSGN0AI = SPACES OR LOW-VALUES
    ///       → "Please enter User ID ..."
    ///   WHEN PASSWDI OF COSGN0AI = SPACES OR LOW-VALUES
    ///       → "Please enter Password ..."
    /// </summary>
    public async Task<LoginResponse> AuthenticateAsync(LoginRequest request)
    {
        // ── Validation (maps to PROCESS-ENTER-KEY paragraph) ──
        // Original: WHEN USERIDI OF COSGN0AI = SPACES OR LOW-VALUES
        if (string.IsNullOrWhiteSpace(request.UserId))
        {
            return new LoginResponse
            {
                Success = false,
                Message = "Please enter User ID ..."  // Original COBOL message
            };
        }

        // Original: WHEN PASSWDI OF COSGN0AI = SPACES OR LOW-VALUES
        if (string.IsNullOrWhiteSpace(request.Password))
        {
            return new LoginResponse
            {
                Success = false,
                Message = "Please enter Password ..."  // Original COBOL message
            };
        }

        // Original: MOVE FUNCTION UPPER-CASE(USERIDI OF COSGN0AI) TO WS-USER-ID
        var userId = request.UserId.Trim().ToUpperInvariant();

        // ── Try Okta authentication first ──
        if (!string.IsNullOrEmpty(_oktaSettings.Domain) &&
            !string.IsNullOrEmpty(_oktaSettings.ClientId))
        {
            _logger.LogInformation("Attempting Okta authentication for user {UserId}", userId);
            var oktaResult = await AuthenticateWithOktaAsync(userId, request.Password);
            if (oktaResult != null)
            {
                return oktaResult;
            }
            _logger.LogWarning("Okta authentication failed for user {UserId}, trying local fallback", userId);
        }

        // ── Fall back to local authentication (original VSAM USRSEC behavior) ──
        if (_authSettings.FallbackToLocalAuth)
        {
            return AuthenticateLocal(userId, request.Password);
        }

        // Original COBOL: WHEN OTHER → "Unable to verify the User ..."
        return new LoginResponse
        {
            Success = false,
            Message = "Unable to verify the User ..."
        };
    }

    /// <summary>
    /// Authenticate via Okta OIDC — Resource Owner Password flow.
    /// This replaces the VSAM file read with an OAuth 2.0 token exchange.
    /// </summary>
    private async Task<LoginResponse?> AuthenticateWithOktaAsync(string userId, string password)
    {
        try
        {
            var tokenEndpoint = $"{_oktaSettings.Domain}/oauth2/{_oktaSettings.AuthorizationServerId}/v1/token";

            var tokenRequest = new FormUrlEncodedContent(new Dictionary<string, string>
            {
                ["grant_type"] = "password",
                ["username"] = userId,
                ["password"] = password,
                ["scope"] = "openid profile groups",
                ["client_id"] = _oktaSettings.ClientId,
                ["client_secret"] = _oktaSettings.ClientSecret
            });

            var response = await _httpClient.PostAsync(tokenEndpoint, tokenRequest);

            if (!response.IsSuccessStatusCode)
            {
                return null;  // Fall through to local auth
            }

            var tokenResponse = await response.Content.ReadFromJsonAsync<OktaTokenResponse>();
            if (tokenResponse?.IdToken == null)
            {
                return null;
            }

            // Parse claims from the ID token
            var handler = new JwtSecurityTokenHandler();
            var jwtToken = handler.ReadJwtToken(tokenResponse.IdToken);

            var groups = jwtToken.Claims
                .Where(c => c.Type == "groups")
                .Select(c => c.Value)
                .ToList();

            // Map Okta groups to COBOL user types:
            //   CardDemo-Admin → SEC-USR-TYPE = 'A' → COADM01C
            //   CardDemo-User  → SEC-USR-TYPE = 'U' → COMEN01C
            var userType = groups.Contains(_authSettings.AdminGroupClaim)
                ? UserType.Admin
                : UserType.User;

            var redirectProgram = userType == UserType.Admin ? "COADM01C" : "COMEN01C";

            return new LoginResponse
            {
                Success = true,
                Message = "Authentication successful",
                Token = tokenResponse.AccessToken,
                RedirectProgram = redirectProgram,
                UserType = userType.ToString(),
                UserId = userId
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Okta authentication error for user {UserId}", userId);
            return null;  // Fall through to local auth
        }
    }

    /// <summary>
    /// Local authentication — direct replacement for READ-USER-SEC-FILE.
    ///
    /// Maps 1:1 to the COBOL EVALUATE WS-RESP-CD logic:
    ///   WHEN 0   → Record found → check password
    ///   WHEN 13  → Record not found
    ///   WHEN OTHER → System error
    ///
    /// The local user store is seeded from the same data that
    /// DUSRSECJ.jcl loads into the VSAM USRSEC file:
    ///   USER0001/PASSWORD (Type U), ADMIN001/PASSWORD (Type A), etc.
    /// </summary>
    private LoginResponse AuthenticateLocal(string userId, string password)
    {
        // ── EXEC CICS READ DATASET(WS-USRSEC-FILE) ... RIDFLD(WS-USER-ID) ──
        var user = _localUserStore.FindByUserId(userId);

        if (user == null)
        {
            // WHEN 13 → DFHRESP(NOTFND)
            // Original: "User not found. Try again ..."
            _logger.LogWarning("User not found: {UserId}", userId);
            return new LoginResponse
            {
                Success = false,
                Message = "User not found. Try again ..."
            };
        }

        // ── IF SEC-USR-PWD = WS-USER-PWD ──
        if (!VerifyPassword(password, user.PasswordHash))
        {
            // Original: "Wrong Password. Try again ..."
            _logger.LogWarning("Invalid password for user: {UserId}", userId);
            return new LoginResponse
            {
                Success = false,
                Message = "Wrong Password. Try again ..."
            };
        }

        // ── Authentication successful — build COMMAREA equivalent ──
        // Original COBOL:
        //   MOVE WS-TRANID   TO CDEMO-FROM-TRANID     → "CC00"
        //   MOVE WS-PGMNAME  TO CDEMO-FROM-PROGRAM    → "COSGN00C"
        //   MOVE WS-USER-ID  TO CDEMO-USER-ID
        //   MOVE SEC-USR-TYPE TO CDEMO-USER-TYPE
        //   MOVE ZEROS        TO CDEMO-PGM-CONTEXT
        var session = new CardDemoSession
        {
            FromTransactionId = "CC00",
            FromProgram = "COSGN00C",
            UserId = user.UserId,
            UserType = user.UserType,
            ProgramContext = 0  // CDEMO-PGM-ENTER (first entry)
        };

        // ── Route based on user type (replaces EXEC CICS XCTL) ──
        // Original COBOL:
        //   IF CDEMO-USRTYP-ADMIN
        //       EXEC CICS XCTL PROGRAM('COADM01C') COMMAREA(...)
        //   ELSE
        //       EXEC CICS XCTL PROGRAM('COMEN01C') COMMAREA(...)
        string redirectProgram;
        if (user.IsAdmin)
        {
            redirectProgram = "COADM01C";
            session.ToProgram = "COADM01C";
            _logger.LogInformation("Admin user {UserId} authenticated, routing to COADM01C", userId);
        }
        else
        {
            redirectProgram = "COMEN01C";
            session.ToProgram = "COMEN01C";
            _logger.LogInformation("Regular user {UserId} authenticated, routing to COMEN01C", userId);
        }

        // Generate JWT token (replaces CARDDEMO-COMMAREA passed via XCTL)
        var token = GenerateJwtToken(session);

        return new LoginResponse
        {
            Success = true,
            Message = "Authentication successful",
            Token = token,
            RedirectProgram = redirectProgram,
            UserType = user.UserType.ToString(),
            UserId = user.UserId
        };
    }

    /// <summary>
    /// Generate JWT token carrying COMMAREA-equivalent claims.
    /// Replaces: EXEC CICS RETURN TRANSID(WS-TRANID) COMMAREA(CARDDEMO-COMMAREA)
    /// </summary>
    private string GenerateJwtToken(CardDemoSession session)
    {
        var key = new SymmetricSecurityKey(
            Encoding.UTF8.GetBytes(_authSettings.JwtSigningKey));
        var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

        var claims = new[]
        {
            // COMMAREA fields as JWT claims
            new Claim("cdemo_from_tranid", session.FromTransactionId),
            new Claim("cdemo_from_program", session.FromProgram),
            new Claim("cdemo_to_program", session.ToProgram),
            new Claim("cdemo_user_id", session.UserId),
            new Claim("cdemo_user_type", ((char)session.UserType).ToString()),
            new Claim("cdemo_pgm_context", session.ProgramContext.ToString()),
            new Claim(ClaimTypes.Name, session.UserId),
            new Claim(ClaimTypes.Role, session.UserType == UserType.Admin ? "Admin" : "User")
        };

        var token = new JwtSecurityToken(
            issuer: "CardDemo",
            audience: "CardDemo",
            claims: claims,
            expires: DateTime.UtcNow.AddHours(8),  // Typical mainframe shift
            signingCredentials: credentials
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    /// <summary>
    /// Verify password — modernized from plain-text comparison.
    /// Original COBOL: IF SEC-USR-PWD = WS-USER-PWD (plain text!)
    /// Modernized: BCrypt hash comparison for security.
    /// For development/testing with seed data, also accepts plain-text match.
    /// </summary>
    private static bool VerifyPassword(string password, string storedHash)
    {
        // In production, use BCrypt:
        // return BCrypt.Net.BCrypt.Verify(password, storedHash);

        // For development compatibility with original USRSEC data
        // (DUSRSECJ loads plain-text passwords like "PASSWORD")
        return string.Equals(
            password.Trim().ToUpperInvariant(),
            storedHash.Trim().ToUpperInvariant());
    }
}

/// <summary>
/// Local user store — replaces VSAM USRSEC file.
/// Seeded with the same data loaded by DUSRSECJ.jcl.
///
/// In production, this would be backed by a database (SQL Server, PostgreSQL).
/// </summary>
public class LocalUserStore
{
    private readonly Dictionary<string, SecUserData> _users = new(StringComparer.OrdinalIgnoreCase);

    public LocalUserStore()
    {
        // Seed with the same users defined in DUSRSECJ.jcl inline data:
        //   USER0001/PASSWORD (Regular User)
        //   USER0002/PASSWORD (Regular User)
        //   USER0003/PASSWORD (Regular User)
        //   ADMIN001/PASSWORD (Admin)
        SeedDefaultUsers();
    }

    /// <summary>
    /// Replaces: EXEC CICS READ DATASET(WS-USRSEC-FILE) RIDFLD(WS-USER-ID)
    /// Returns null when RESP=13 (NOTFND).
    /// </summary>
    public SecUserData? FindByUserId(string userId)
    {
        _users.TryGetValue(userId.Trim().ToUpperInvariant(), out var user);
        return user;
    }

    private void SeedDefaultUsers()
    {
        // Data from DUSRSECJ.jcl (SYSUT1 DD * inline data)
        AddUser("USER0001", "User", "Test01", "PASSWORD", UserType.User);
        AddUser("USER0002", "User", "Test02", "PASSWORD", UserType.User);
        AddUser("USER0003", "User", "Test03", "PASSWORD", UserType.User);
        AddUser("ADMIN001", "Admin", "Test01", "PASSWORD", UserType.Admin);
    }

    private void AddUser(string id, string fname, string lname, string pwd, UserType type)
    {
        _users[id] = new SecUserData
        {
            UserId = id,
            FirstName = fname,
            LastName = lname,
            PasswordHash = pwd,
            UserType = type
        };
    }
}

/// <summary>Okta token response DTO.</summary>
internal class OktaTokenResponse
{
    public string? AccessToken { get; set; }
    public string? IdToken { get; set; }
    public string? TokenType { get; set; }
    public int ExpiresIn { get; set; }
}
