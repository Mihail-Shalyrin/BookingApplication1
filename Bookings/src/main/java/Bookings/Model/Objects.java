package Bookings.Model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@NoArgsConstructor( force=true)
@RequiredArgsConstructor
public class Objects {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long id;
    private final String name;
    private final String floor;
    private  final Type type;
    private  final String spot;
    @ManyToOne
    private Users reservedForUser;
    @ManyToOne
    private Project reservedForProject;
//    @ManyToOne
//    private Office office;
    public enum Type{ROOM,MEETING,HALL}
}
