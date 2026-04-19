package Bookings.Repository;

import Bookings.Model.RefreshToken;
import Bookings.Model.Users;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUserId(Long userId);
    Optional<RefreshToken> findByUser(Users user);
}