package Bookings.DbConfig;

import Bookings.Model.Objects;
import Bookings.Model.Office;
import Bookings.Repository.RoomsRepository;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration

public class DBConfig {
    @Bean
    @Order(3)
    public ApplicationRunner dataLoader(RoomsRepository roomRep){

        return args -> {
            roomRep.save(new Objects("table","1", Objects.Type.ROOM,"1A")
                    );
            roomRep.save(new Objects("table","2", Objects.Type.ROOM,"12")
            );
            roomRep.save(new Objects("meeting","2", Objects.Type.MEETING,"2A")
            );
            roomRep.save(new Objects("meeting","3", Objects.Type.MEETING,"3A")
            );
            roomRep.save(new Objects("hall","3", Objects.Type.HALL,"3A")
            );
            roomRep.save(new Objects("hall","4", Objects.Type.HALL,"4A")
            );
        };

    }
}
