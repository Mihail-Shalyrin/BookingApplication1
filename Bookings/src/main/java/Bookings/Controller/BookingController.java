package Bookings.Controller;

import Bookings.DTO.BookingRequest;
import Bookings.DTO.BookingResponse;
import Bookings.DTO.ErrorResponse;
import Bookings.Model.BookingMode;
import Bookings.Model.Bookings;
import Bookings.Model.Objects;
import Bookings.Model.Users;
import Bookings.Repository.BookingRepository;
import Bookings.Repository.RoomsRepository;
import Bookings.Repository.UserProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import Bookings.Repository.UserProjectRepository;
@RestController
@RequestMapping("/api/bookings")
@Slf4j
public class BookingController {

    private final BookingRepository bookingRepository;
    private final RoomsRepository roomsRepository;
    private final UserProjectRepository userProjectRepository;
    private static final LocalTime WORKDAY_END = LocalTime.of(18, 0);
    private static final LocalTime FUTURE_BOOKING_TIME_LIMIT = LocalTime.of(16, 0);
    private static final int WEEKLY_BOOKING_OCCURRENCES = 2;

    @Autowired
    public BookingController(BookingRepository bookingRepository,
                                 RoomsRepository roomsRepository,
                             UserProjectRepository userProjectRepository
                            ) {
        this.bookingRepository = bookingRepository;
        this.roomsRepository = roomsRepository;
        this.userProjectRepository = userProjectRepository;
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request,
                                           @AuthenticationPrincipal Users user) {

        if (request.getObjectId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("требуется id объекта"));
        }

        if (request.getBookingMode() == null) {
            request.setBookingMode(BookingMode.ONE_TIME);
        }

        Objects object = roomsRepository.findById(request.getObjectId())
                .orElseThrow(() -> new IllegalArgumentException("объект не найден"));

        if (request.getStartTime() == null || request.getEndTime() == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("требуется начало и конец брони"));
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("конец должен быть позже начала"));
        }

        if (request.getBookingMode() == BookingMode.ONE_TIME) {
            return handleOneTimeBooking(request, object, user);
        }

        if (request.getBookingMode() == BookingMode.WEEKLY) {
            return handleWeeklyBooking(request, object, user);
        }

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("неизвестный тип брони"));
    }

    private ResponseEntity<?> handleOneTimeBooking(BookingRequest request,
                                                   Objects object,
                                                   Users user) {

        Bookings booking = new Bookings();
        booking.setObject(object);
        booking.setUser(user);
        booking.setBookingMode(BookingMode.ONE_TIME);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());

        String error = validateBooking(booking, user);
        if (error != null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(error));
        }

        bookingRepository.save(booking);

        log.info("ONE_TIME booking saved: ID={}, ObjectID={}, UserID={}",
                booking.getId(),
                booking.getObject().getId(),
                user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BookingResponse.from(booking));
    }

    private ResponseEntity<?> handleWeeklyBooking(BookingRequest request,
                                                  Objects object,
                                                  Users user) {

        List<Bookings> bookingsToSave = new ArrayList<>();

        for (int i = 0; i < WEEKLY_BOOKING_OCCURRENCES; i++) {
            Bookings weeklyBooking = new Bookings();
            weeklyBooking.setObject(object);
            weeklyBooking.setUser(user);
            weeklyBooking.setBookingMode(BookingMode.WEEKLY);
            weeklyBooking.setStartTime(request.getStartTime().plusWeeks(i));
            weeklyBooking.setEndTime(request.getEndTime().plusWeeks(i));

            String error = validateBooking(weeklyBooking, user);
            if (error != null) {
                if (i == 0) {
                    return ResponseEntity.badRequest()
                            .body(new ErrorResponse("Current week booking error: " + error));
                } else {
                    return ResponseEntity.badRequest()
                            .body(new ErrorResponse("Next week booking error: " + error));
                }
            }

            bookingsToSave.add(weeklyBooking);
        }

        bookingRepository.saveAll(bookingsToSave);

        bookingsToSave.forEach(saved ->
                log.info("WEEKLY booking saved: ObjectID={}, UserID={}, Start={}, End={}",
                        saved.getObject().getId(),
                        user.getId(),
                        saved.getStartTime(),
                        saved.getEndTime())
        );

        List<BookingResponse> response = bookingsToSave.stream()
                .map(BookingResponse::from)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String validateBooking(Bookings booking, Users user) {
        Objects object = booking.getObject();
        Objects.Type type = object.getType();

        // reserved-место
        if (object.getReservedForUser() != null) {
            boolean isOwner = object.getReservedForUser().getId().equals(user.getId());
//            boolean isPrivileged = user.hasRole("ADMIN") || user.hasRole("SUPERADMIN");

            if (!isOwner ) {
                return "Место уже зарезервировано за другим пользователем";
            }
        }
        if (object.getReservedForProject() != null) {
            boolean inProject = userProjectRepository.existsByUserIdAndProjectId(
                    user.getId(),
                    object.getReservedForProject().getId()
            );

            if (!inProject) {
                return "Место уже зарезервировано за другим проектом";
            }
        }

        // допустимая длительность
        if (!isValidDuration(type, booking.getStartTime(), booking.getEndTime())) {
            return getDurationErrorMessage(type);
        }

        // конфликт по самому месту
        boolean roomConflict = bookingRepository.existsOverlappingBooking(
                object.getId(),
                booking.getStartTime(),
                booking.getEndTime()
        );

        if (roomConflict) {
            return "На указанный период времени место уже занято";
        }

        if (!user.hasRole("ADMIN") && !user.hasRole("SUPERADMIN")) {
            LocalDate bookingDate = booking.getStartTime().toLocalDate();
            LocalDateTime dayStart = bookingDate.atStartOfDay();
            LocalDateTime dayEnd = bookingDate.plusDays(1).atStartOfDay();

            boolean hasSameTypeBookingForDay = bookingRepository.existsUserBookingByTypeForDay(
                    user.getId(),
                    type,
                    dayStart,
                    dayEnd
            );

            if (hasSameTypeBookingForDay) {
                if (type == Objects.Type.ROOM) {
                    return "У вас уже забронирован стол на текущий день";
                } else if (type == Objects.Type.MEETING) {
                    return "У вас уже забронирована переговорная комната на текущий день";
                } else {
                    return "У вас уже есть бронь на текущий день";
                }
            }
        }

        if (!user.hasRole("ADMIN") && !user.hasRole("SUPERADMIN")) {
            LocalDate today = LocalDate.now();
            LocalDate requestedDate = booking.getStartTime().toLocalDate();
            LocalTime nowTime = LocalTime.now();

            LocalDateTime dayStart = today.atStartOfDay();
            LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

            boolean hasBookingToday = bookingRepository.existsBookingForDay(
                    user.getId(),
                    dayStart,
                    dayEnd
            );

            if (requestedDate.isAfter(today)
                    && hasBookingToday
                    && nowTime.isBefore(FUTURE_BOOKING_TIME_LIMIT)) {
                return "Бронирование на следующий день доступно только после 16:00 при наличии брони на текущий день";
            }
        }

        return null;
    }

    private boolean isValidDuration(Objects.Type type,
                                    LocalDateTime start,
                                    LocalDateTime end) {

        long minutes = Duration.between(start, end).toMinutes();

        if (type == Objects.Type.ROOM) {
            boolean oneHour = minutes == 60;
            boolean twoHours = minutes == 120;
            boolean untilEndOfDay = end.toLocalTime().equals(WORKDAY_END) && minutes >= 60;
            return oneHour || twoHours || untilEndOfDay;
        }

        if (type == Objects.Type.MEETING) {
            return minutes == 30 || minutes == 60 || minutes == 90;
        }

        if (type == Objects.Type.HALL) {
            return true;
        }
        return false;
    }

    private String getDurationErrorMessage(Objects.Type type) {
        if (type == Objects.Type.ROOM) {
            return "для комнаты разрешены промежутки в : 1 час, 2 часа или до конца рабочего дня";

        }

        if (type == Objects.Type.MEETING) {
            return "Для переговорной комнаты разрешены промежутки в : 30 минут, 1 час, 1.5 часа или до конца рабочего дня";
        }

        return "Неккоректная продолжительность брони";
    }
}