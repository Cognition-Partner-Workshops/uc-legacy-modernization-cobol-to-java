// ==========================================================================
// CardDemo Sign-on — Modernized .NET Minimal API
// Original COBOL: COSGN00C.cbl (261 lines, CICS transaction CC00)
//
// This is the entry point for the modernized sign-on service.
// It replaces:
//   - CICS transaction CC00
//   - BMS map COSGN0A / mapset COSGN00
//   - VSAM USRSEC file authentication
//   - EXEC CICS XCTL routing to COADM01C / COMEN01C
//
// Authentication: Okta OIDC (primary) with local fallback.
// ==========================================================================

using CardDemo.Auth.Models;
using CardDemo.Auth.Services;

var builder = WebApplication.CreateBuilder(args);

// ── Configure services ──
builder.Services.Configure<OktaSettings>(
    builder.Configuration.GetSection("Okta"));
builder.Services.Configure<CardDemoAuthSettings>(
    builder.Configuration.GetSection("CardDemo"));

builder.Services.AddSingleton<LocalUserStore>();
builder.Services.AddHttpClient<AuthenticationService>();
builder.Services.AddScoped<AuthenticationService>();

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new()
    {
        Title = "CardDemo Authentication API",
        Version = "v1",
        Description = "Modernized replacement for COSGN00C (CICS Sign-on). " +
                      "Supports Okta OIDC and local authentication with " +
                      "role-based routing (Admin → COADM01C, User → COMEN01C)."
    });
});

var app = builder.Build();

app.UseSwagger();
app.UseSwaggerUI();

// ── POST /api/auth/login ──
// Replaces: CICS transaction CC00, BMS map COSGN0A input, READ-USER-SEC-FILE
//
// Original COBOL flow:
//   1. EXEC CICS RECEIVE MAP('COSGN0A') → get user input
//   2. Validate User ID and Password (not blank)
//   3. EXEC CICS READ DATASET('USRSEC') → lookup user
//   4. Compare password → route to COADM01C or COMEN01C
app.MapPost("/api/auth/login", async (LoginRequest request, AuthenticationService authService) =>
{
    var result = await authService.AuthenticateAsync(request);

    return result.Success
        ? Results.Ok(result)
        : Results.Unauthorized();
})
.WithName("Login")
.WithOpenApi()
.Produces<LoginResponse>(200)
.Produces(401);

// ── GET /api/auth/me ──
// Returns current session info (replaces reading CARDDEMO-COMMAREA)
app.MapGet("/api/auth/me", (HttpContext context) =>
{
    // In production, parse JWT from Authorization header
    // For now, return a placeholder
    return Results.Ok(new
    {
        message = "Use the JWT token from /api/auth/login to authenticate requests.",
        cobolEquivalent = "CARDDEMO-COMMAREA fields are encoded as JWT claims."
    });
})
.WithName("GetCurrentUser")
.WithOpenApi();

// ── POST /api/auth/logout ──
// Replaces: DFHPF3 handling in MAIN-PARA
// Original: MOVE CCDA-MSG-THANK-YOU TO WS-MESSAGE → SEND-PLAIN-TEXT → CICS RETURN
app.MapPost("/api/auth/logout", () =>
{
    return Results.Ok(new
    {
        success = true,
        message = "Thank you for using CardDemo Application..."  // CCDA-MSG-THANK-YOU
    });
})
.WithName("Logout")
.WithOpenApi();

app.Run();
