package Bookings.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor(force = true)
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "project_id"})
)
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Users user;

    @ManyToOne(optional = false)
    private Project project;
}