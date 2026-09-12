package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.engineerAnsh.journalApp.Dto.auth.LoginResponseDto;
import net.engineerAnsh.journalApp.Dto.common.MessageResponseDto;
import net.engineerAnsh.journalApp.Dto.auth.LoginRequestDto;
import net.engineerAnsh.journalApp.Dto.auth.RegisterRequestDto;

import net.engineerAnsh.journalApp.Service.AuthService;
import net.engineerAnsh.journalApp.Service.OAuth2TokenExchangeService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Auth APIs",
        description = "login or signup"
)
public class AuthController {

    private static final String OAUTH_TOKEN_COOKIE = "JOURNALFLOW_OAUTH_TOKEN";
    private final AuthService authService;
    private final OAuth2TokenExchangeService oauth2TokenExchangeService;

    @Value("${app.oauth2.cookie.secure:false}")
    private boolean oauthCookieSecure;

    @Value("${app.oauth2.cookie.same-site:Lax}")
    private String oauthCookieSameSite;

    @Operation(
            summary =
                    "Registers a new user into the system"
    )
    @PostMapping("/signup")
    public ResponseEntity<MessageResponseDto> signUp(
            @RequestBody
            @Valid
            RegisterRequestDto registerRequestDto
    ) {

        authService.signup(
                registerRequestDto
        );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        MessageResponseDto.builder()
                                .message(
                                        "User created successfully."
                                )
                                .build()
                );
    }

    @PostMapping("/login")
    @Operation(
            summary =
                    "Authenticates a user and returns an access token"
    )
    public ResponseEntity<LoginResponseDto> login(
            @Valid
            @RequestBody
            LoginRequestDto request
    ) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    /**
     * ----------------------------------------
     * GOOGLE OAUTH2 TOKEN EXCHANGE
     * ----------------------------------------
     * <p>
     * The temporary JWT is stored in an HttpOnly
     * cookie by GoogleOAuth2SuccessHandler.
     * <p>
     * The frontend cannot read that cookie.
     * Instead, it calls this endpoint and the
     * backend performs the exchange.
     */
    @PostMapping("/oauth2/exchange")
    @Operation(
            summary =
                    "Exchanges the temporary Google OAuth authentication for a JournalFlow access token"
    )
    public ResponseEntity<LoginResponseDto> exchangeOAuthToken(
            @CookieValue(
                    name = OAUTH_TOKEN_COOKIE,
                    required = false
            )
            String oauthToken
    ) {

        LoginResponseDto response =
                oauth2TokenExchangeService
                        .exchangeToken(
                                oauthToken
                        );

        /*
         * ----------------------------------------
         * Clear the temporary OAuth cookie.
         * ----------------------------------------
         */
        ResponseCookie clearCookie =
                ResponseCookie
                        .from(
                                OAUTH_TOKEN_COOKIE,
                                ""
                        )
                        .httpOnly(true)
                        .secure(oauthCookieSecure)
                        .sameSite(oauthCookieSameSite)
                        .path("/")
                        .maxAge(0)
                        .build();

        return ResponseEntity
                .ok()
                .header(
                        "Set-Cookie",
                        clearCookie.toString()
                )
                .body(response);
    }
}