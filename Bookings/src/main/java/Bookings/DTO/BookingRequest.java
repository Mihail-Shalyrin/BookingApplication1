package Bookings.DTO;

import Bookings.Model.BookingMode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private Long objectId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingMode bookingMode;
}