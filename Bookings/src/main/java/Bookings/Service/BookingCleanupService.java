
package Bookings.Service;

import Bookings.Repository.BookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
@Service
public class BookingCleanupService {

    private final BookingRepository bookingRepository;

    @Autowired
    public BookingCleanupService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    //@Scheduled(fixedRate = 60000)
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void removeExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        bookingRepository.deleteByEndTimeBefore(now);
    }
}