package Bookings.Repository;

import Bookings.Model.Objects;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomsRepository extends CrudRepository<Objects,Long> {

    @Query("""
        select o from Objects o
        where o.type in :types
          and o.id not in (
              select b.object.id
              from Bookings b
              where b.startTime < :endTime
                and b.endTime > :startTime
          )
    """)
    List<Objects> findFreeRoomsByTypesAndPeriod(@Param("types") List<Objects.Type> types,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);
    List<Objects> findAll();
//    List<Objects> findByTypeIn(List<Objects.Type> types);
    List<Objects> findByTypeInAndReservedForUserIsNull(List<Objects.Type> types);
}

