package net.engineerAnsh.journalApp.Service;

import lombok.RequiredArgsConstructor;

import net.engineerAnsh.journalApp.Dto.auth.LoginResponseDto;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Utils.JwtUtils;
import net.engineerAnsh.journalApp.exception.exceptions.UnauthorizedException;
import net.engineerAnsh.journalApp.mapper.UserMapper;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2TokenExchangeService {

    private final JwtUtils jwtUtils;

    private final UserService userService;

    private final UserMapper userMapper;

    /**
     * Exchanges the short-lived OAuth handoff JWT
     * for the normal JournalFlow login response.
     *
     * This keeps the frontend authentication contract
     * identical to the existing username/password login.
     */

    public LoginResponseDto exchangeToken(
            String token
    ) {

        if (
                token == null ||
                        token.isBlank()
        ) {
            throw new UnauthorizedException(
                    "Google authentication session is missing or expired."
            );
        }

        /*
         * Validate before using the token.
         */
        try {

            if (!jwtUtils.validateToken(token)) {
                throw new UnauthorizedException(
                        "Google authentication session is invalid or expired."
                );
            }

            String username =
                    jwtUtils.extractUsername(
                            token
                    );

            if (
                    username == null ||
                            username.isBlank()
            ) {
                throw new IllegalArgumentException(
                        "OAuth token does not contain a valid user."
                );
            }

            /*
             * Fetch the actual JournalFlow user.
             */
            User user =
                    userService.findUserByUserName(
                            username
                    );

            if (user == null) {
                throw new IllegalArgumentException(
                        "Authenticated user could not be found."
                );
            }

            /*
             * Return exactly the same response shape
             * as the normal login endpoint.
             */
            return LoginResponseDto.builder()
                    .accessToken(token)
                    .user(
                            userMapper.toSummaryDto(
                                    user
                            )
                    )
                    .build();

        } catch (IllegalArgumentException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "OAuth authentication could not be completed."
            );
        }
    }
}