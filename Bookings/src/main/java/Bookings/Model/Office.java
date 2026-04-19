package Bookings.Model;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
public class Office {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long id;
    private String city;
    private String Department;


}
