package Bookings.Controller;

import Bookings.Model.Objects;
import Bookings.Model.Users;
import Bookings.Repository.RoomsRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/main")
public class MainPage {

    private final RoomsRepository roomRep;

    public MainPage(RoomsRepository roomRep) {
        this.roomRep = roomRep;
    }

    @GetMapping("/rooms")
    @PreAuthorize("hasRole('USER')")
    public List<Objects> getAvailableRooms(@AuthenticationPrincipal Users user) {
        return getAllowedRooms(user);
    }

    private List<Objects> getAllowedRooms(Users user) {
//        List<Objects.Type> allowedTypes;
        return roomRep.findByTypeInAndReservedForUserIsNull(getAllowedTypes(user));
    }
    private List<Objects.Type> getAllowedTypes(Users user) {
        if (user.hasRole("SUPERADMIN")) {
            return List.of(Objects.Type.values());
        } else if (user.hasRole("ADMIN")) {
            return List.of(Objects.Type.ROOM, Objects.Type.MEETING);
        } else {
            return List.of(Objects.Type.ROOM);
        }
    }

    @GetMapping("/rooms/available")
    @PreAuthorize("hasRole('USER')")
    public List<Objects> getAvailableRoomsByPeriod(
            @AuthenticationPrincipal Users user,
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,
            @RequestParam(value = "type", required = false) Objects.Type type
    ) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("начало должно быть раньше конца");
        }
        List<Objects.Type> allowedTypes = getAllowedTypes(user);
        if (type != null) {
            if (!allowedTypes.contains(type)) {
                throw new IllegalArgumentException("это команат недоступна для вашей роли");
            }
            allowedTypes = List.of(type);
        }

        return roomRep.findFreeRoomsByTypesAndPeriod(allowedTypes, start, end);
    }
}
