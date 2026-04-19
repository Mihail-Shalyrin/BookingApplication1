package Bookings.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000, unique = true, nullable = false)
    private String token;

    private boolean revoked = false;

    @ManyToOne(optional = false)
    private Users user;
}