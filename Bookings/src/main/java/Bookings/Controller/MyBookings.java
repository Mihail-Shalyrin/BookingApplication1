package Bookings.Controller;

import Bookings.DTO.BookingResponse;
import Bookings.DTO.ErrorResponse;
import Bookings.Model.Bookings;
import Bookings.Model.Users;
import Bookings.Repository.BookingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class MyBookings {

    private final BookingRepository bookingRepository;

    public MyBookings(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<BookingResponse> myBookings(@AuthenticationPrincipal Users user) {
        List<Bookings> bookings = bookingRepository.findByUser_Id(user.getId());
        List<BookingResponse> response = new ArrayList<>();

        for (Bookings booking : bookings) {
            response.add(BookingResponse.from(booking));
        }

        return response;
    }
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id,
                                           @AuthenticationPrincipal Users user) {

        Optional<Bookings> optionalBooking = bookingRepository.findById(id);

        if (optionalBooking.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Bookings booking = optionalBooking.get();

        boolean isOwner = booking.getUser().getId().equals(user.getId());
        boolean isAdmin = user.hasRole("ADMIN") || user.hasRole("SUPERADMIN");

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403)
                    .body(new ErrorResponse("Вы не можете удалить эту бронь"));
        }

        bookingRepository.delete(booking);
        return ResponseEntity.noContent().build();
    }
}