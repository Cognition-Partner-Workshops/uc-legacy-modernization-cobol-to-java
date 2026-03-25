// ==========================================================================
// CardDemo Sign-on — Modernized .NET Models
// Original COBOL: COSGN00C.cbl + CSUSR01Y.cpy + COCOM01Y.cpy
// ==========================================================================

namespace CardDemo.Auth.Models;

/// <summary>
/// Maps to COBOL copybook CSUSR01Y (SEC-USER-DATA).
/// Original VSAM record: USRSEC file, 80 bytes, KSDS keyed on SEC-USR-ID.
///
/// COBOL layout:
///   05 SEC-USR-ID       PIC X(08).
///   05 SEC-USR-FNAME    PIC X(20).
///   05 SEC-USR-LNAME    PIC X(20).
///   05 SEC-USR-PWD      PIC X(08).
///   05 SEC-USR-TYPE     PIC X(01).   'A' = Admin, 'U' = User
///   05 SEC-USR-FILLER   PIC X(23).
/// </summary>
public class SecUserData
{
    /// <summary>SEC-USR-ID — PIC X(08). Primary key in USRSEC VSAM file.</summary>
    public string UserId { get; set; } = string.Empty;

    /// <summary>SEC-USR-FNAME — PIC X(20).</summary>
    public string FirstName { get; set; } = string.Empty;

    /// <summary>SEC-USR-LNAME — PIC X(20).</summary>
    public string LastName { get; set; } = string.Empty;

    /// <summary>SEC-USR-PWD — PIC X(08). Plain-text in COBOL; hashed in .NET.</summary>
    public string PasswordHash { get; set; } = string.Empty;

    /// <summary>
    /// SEC-USR-TYPE — PIC X(01).
    /// 88 CDEMO-USRTYP-ADMIN VALUE 'A'.
    /// 88 CDEMO-USRTYP-USER  VALUE 'U'.
    /// </summary>
    public UserType UserType { get; set; } = UserType.User;

    public bool IsAdmin => UserType == UserType.Admin;
}

/// <summary>
/// Maps to COBOL 88-level conditions on SEC-USR-TYPE.
///   88 CDEMO-USRTYP-ADMIN VALUE 'A'.
///   88 CDEMO-USRTYP-USER  VALUE 'U'.
/// </summary>
public enum UserType
{
    /// <summary>Regular user — routes to COMEN01C (Main Menu).</summary>
    User = 'U',

    /// <summary>Admin user — routes to COADM01C (Admin Menu).</summary>
    Admin = 'A'
}

/// <summary>
/// Login request — replaces BMS map COSGN0A input fields.
///
/// COBOL BMS fields:
///   USERIDI OF COSGN0AI  — User ID input
///   PASSWDI OF COSGN0AI  — Password input
/// </summary>
public class LoginRequest
{
    /// <summary>Maps to USERIDI OF COSGN0AI.</summary>
    public string UserId { get; set; } = string.Empty;

    /// <summary>Maps to PASSWDI OF COSGN0AI.</summary>
    public string Password { get; set; } = string.Empty;
}

/// <summary>
/// Login response — replaces BMS map COSGN0A output fields and XCTL routing.
///
/// In COBOL, successful auth triggers:
///   IF CDEMO-USRTYP-ADMIN → EXEC CICS XCTL PROGRAM('COADM01C')
///   ELSE                  → EXEC CICS XCTL PROGRAM('COMEN01C')
///
/// The RedirectProgram field preserves this routing decision.
/// </summary>
public class LoginResponse
{
    public bool Success { get; set; }

    /// <summary>Maps to WS-MESSAGE — PIC X(80) in COSGN00C.</summary>
    public string Message { get; set; } = string.Empty;

    /// <summary>JWT access token (replaces CARDDEMO-COMMAREA passed via XCTL).</summary>
    public string? Token { get; set; }

    /// <summary>
    /// The program the user should be routed to.
    /// Admin → "COADM01C", User → "COMEN01C".
    /// Preserves the original EXEC CICS XCTL routing logic.
    /// </summary>
    public string? RedirectProgram { get; set; }

    /// <summary>User type: "Admin" or "User".</summary>
    public string? UserType { get; set; }

    /// <summary>Authenticated user ID (maps to CDEMO-USER-ID in COMMAREA).</summary>
    public string? UserId { get; set; }
}

/// <summary>
/// Maps to COBOL copybook COCOM01Y (CARDDEMO-COMMAREA).
/// This is the communication area passed between all CICS online programs.
///
/// Original layout (relevant fields for sign-on):
///   10 CDEMO-FROM-TRANID   PIC X(04).  → "CC00"
///   10 CDEMO-FROM-PROGRAM  PIC X(08).  → "COSGN00C"
///   10 CDEMO-TO-TRANID     PIC X(04).
///   10 CDEMO-TO-PROGRAM    PIC X(08).
///   10 CDEMO-USER-ID       PIC X(08).
///   10 CDEMO-USER-TYPE     PIC X(01).
///   10 CDEMO-PGM-CONTEXT   PIC 9(01).  0=enter, 1=re-enter
/// </summary>
public class CardDemoSession
{
    public string FromTransactionId { get; set; } = "CC00";
    public string FromProgram { get; set; } = "COSGN00C";
    public string ToTransactionId { get; set; } = string.Empty;
    public string ToProgram { get; set; } = string.Empty;
    public string UserId { get; set; } = string.Empty;
    public UserType UserType { get; set; } = UserType.User;
    public int ProgramContext { get; set; } = 0;  // 0=enter, 1=re-enter
}
