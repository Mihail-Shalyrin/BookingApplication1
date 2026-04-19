package Bookings.Service;

import Bookings.Model.RefreshToken;
import Bookings.Model.Users;
import Bookings.Repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

//    public RefreshToken createToken(Users user, String tokenValue) {
//        RefreshToken token = new RefreshToken();
//        token.setUser(user);
//        token.setToken(tokenValue);
//        token.setRevoked(false);
//        return refreshTokenRepository.save(token);
//    }
@Transactional
public RefreshToken createToken(Users user, String tokenValue) {
    RefreshToken token = refreshTokenRepository.findByUser(user)
            .orElse(new RefreshToken());

    token.setUser(user);
    token.setToken(tokenValue);
    token.setRevoked(false);

    return refreshTokenRepository.save(token);
}

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElse(null);
    }

    @Transactional
    public void revokeToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue).orElse(null);
        if (token != null) {
            token.setRevoked(true);
        }
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);
        for (RefreshToken token : tokens) {
            token.setRevoked(true);
        }
    }
    @Transactional
    public void revokeStoredToken(RefreshToken token) {
        token.setRevoked(true);
    }

}