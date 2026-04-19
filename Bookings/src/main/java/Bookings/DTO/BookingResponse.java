package Bookings.DTO;

import Bookings.Model.Bookings;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long objectId;
    private String objectName;
    private String objectType;
    private Long userId;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String bookingMode;

    public static BookingResponse from(Bookings booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getObject().getId(),
                booking.getObject().getName(),
                booking.getObject().getType().name(),
                booking.getUser().getId(),
                booking.getUser().getUsername(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getBookingMode().name()
        );
    }
}