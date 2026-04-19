package Bookings.Model;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PRIVATE,force=true)
@RequiredArgsConstructor
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long id;
    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private  final Role role;
    public enum Role{ USER,ADMIN,SUPERADMIN}

}
