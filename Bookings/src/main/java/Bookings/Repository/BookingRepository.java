
package Bookings.Repository;

import Bookings.Model.Bookings;
import Bookings.Model.Objects;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends CrudRepository<Bookings, Long> {

   List<Bookings> findByUser_Id(Long userId);
   List<Bookings> findAll();
   void deleteByEndTimeBefore(LocalDateTime dateTime);

   @Query("""
        select count(b) > 0
        from Bookings b
        where b.object.id = :objectId
          and b.startTime < :newEnd
          and b.endTime > :newStart
    """)
   boolean existsOverlappingBooking(@Param("objectId") Long objectId,
                                    @Param("newStart") LocalDateTime newStart,
                                    @Param("newEnd") LocalDateTime newEnd);

//   @Query("""
//        select count(b) > 0
//        from Bookings b
//        where b.user.id = :userId
//          and b.object.type = :type
//          and b.startTime < :newEnd
//          and b.endTime > :newStart
//    """)
//   boolean existsUserBookingByTypeAndPeriod(@Param("userId") Long userId,
//                                            @Param("type") Objects.Type type,
//                                            @Param("newStart") LocalDateTime newStart,
//                                            @Param("newEnd") LocalDateTime newEnd);
   @Query("""
    select count(b) > 0
    from Bookings b
    where b.user.id = :userId
      and b.startTime >= :dayStart
      and b.startTime < :dayEnd
""")
   boolean existsBookingForDay(@Param("userId") Long userId,
                               @Param("dayStart") LocalDateTime dayStart,
                               @Param("dayEnd") LocalDateTime dayEnd);
   @Query("""
    select count(b) > 0
    from Bookings b
    where b.user.id = :userId
      and b.object.type = :type
      and b.startTime >= :dayStart
      and b.startTime < :dayEnd
""")
   boolean existsUserBookingByTypeForDay(@Param("userId") Long userId,
                                         @Param("type") Objects.Type type,
                                         @Param("dayStart") LocalDateTime dayStart,
                                         @Param("dayEnd") LocalDateTime dayEnd);


   @Query("""
        select b
        from Bookings b
        where lower(b.user.username) like lower(concat('%', :username, '%'))
        order by b.startTime
    """)
   List<Bookings> findByUsernameContaining(@Param("username") String username);
}