package Bookings.Controller;

import Bookings.DTO.ErrorResponse;
import Bookings.DTO.JwtResponse;
import Bookings.DTO.LoginRequest;
import Bookings.DTO.LogoutRequest;
import Bookings.DTO.RefreshTokenRequest;
import Bookings.Model.RefreshToken;
import Bookings.Model.Users;
import Bookings.Security.JwtService;
import Bookings.Service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            //
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            Users user = (Users) authentication.getPrincipal();

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            refreshTokenService.createToken(user, refreshToken);

            return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("некорректные данные"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshTokenValue = request.getRefreshToken();

            if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("требуется Refresh token"));
            }

            if (!jwtService.isRefreshToken(refreshTokenValue)) {
                return ResponseEntity.status(401)
                        .body(new ErrorResponse("некорректный refresh token"));
            }

            RefreshToken storedToken = refreshTokenService.findByToken(refreshTokenValue);
            if (storedToken == null || storedToken.isRevoked()) {
                return ResponseEntity.status(401)
                        .body(new ErrorResponse("Refresh token отозван или не найден"));
            }
            String username = jwtService.extractUsername(refreshTokenValue);

            Users user = (Users) userDetailsService.loadUserByUsername(username);

            if (!jwtService.isTokenValid(refreshTokenValue, user)) {
                return ResponseEntity.status(401)
                        .body(new ErrorResponse("Refresh token некорректный или закончился"));
            }

            refreshTokenService.revokeStoredToken(storedToken);
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            refreshTokenService.createToken(user, newRefreshToken);

            return ResponseEntity.ok(new JwtResponse(newAccessToken, newRefreshToken));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("Refresh token is некорректный или закончился"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(" требуется Refresh token"));
        }

        refreshTokenService.revokeToken(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

}