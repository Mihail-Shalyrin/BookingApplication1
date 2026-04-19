package Bookings.Controller;

import Bookings.DTO.*;
import Bookings.Model.Bookings;
import Bookings.Model.Objects;
import Bookings.Model.Project;
import Bookings.Model.UserProject;
import Bookings.Model.Users;
import Bookings.Repository.BookingRepository;
import Bookings.Repository.ProjectRepository;
import Bookings.Repository.RoomsRepository;
import Bookings.Repository.UserProjectRepository;
import Bookings.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RoomsRepository roomsRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserProjectRepository userProjectRepository;

    public AdminController(RoomsRepository roomsRepository,
                           BookingRepository bookingRepository,
                           UserRepository userRepository,
                           ProjectRepository projectRepository,
                           UserProjectRepository userProjectRepository) {
        this.roomsRepository = roomsRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.userProjectRepository = userProjectRepository;
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @GetMapping("/rooms")
    public Iterable<Objects> getAllRooms() {
        return roomsRepository.findAll();
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @GetMapping("/users")
    public Iterable<Users> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/reserve")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> reservePlace(@RequestBody ReserveSpot spot) {

        if (spot.getObjectId() == null || spot.getUserId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("требуеются id пользователя и объекта"));
        }

        Objects object = roomsRepository.findById(spot.getObjectId()).orElse(null);
        Users user = userRepository.findById(spot.getUserId()).orElse(null);

        if (object == null || user == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Объект или пользователь не найдены"));
        }

        if (object.getReservedForUser() != null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Место зарезервировано за другим пользователем!"));
        }

        if (object.getReservedForProject() != null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Место уже зарезервировано за проектом!"));
        }

        object.setReservedForUser(user);
        roomsRepository.save(object);

        return ResponseEntity.ok(object);
    }

    @PutMapping("/unreserve/{objectId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> unreservePlace(@PathVariable Long objectId) {

        Objects object = roomsRepository.findById(objectId).orElse(null);

        if (object == null) {
            return ResponseEntity.notFound().build();
        }

        object.setReservedForUser(null);
        object.setReservedForProject(null);
        roomsRepository.save(object);

        return ResponseEntity.ok(object);
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public List<Bookings> getBookings(@RequestParam(required = false) String username) {

        List<Bookings> bookings;

        if (username == null || username.isBlank()) {
            bookings = (List<Bookings>) bookingRepository.findAll();
        } else {
            bookings = bookingRepository.findByUsernameContaining(username);
        }

        return bookings;
    }

    @DeleteMapping("/bookings/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {

        Bookings booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        bookingRepository.delete(booking);
        return ResponseEntity.noContent().build();
    }



    @PostMapping("/projects")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> createProject(@RequestBody CreateProjectRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Требуется название проекта"));
        }

        if (projectRepository.existsByName(request.getName())) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Проект с таким названием уже существует"));
        }

        Project project = new Project(request.getName(), request.getDescription());
        projectRepository.save(project);

        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @GetMapping("/projects")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @PostMapping("/projects/users")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> addUserToProject(@RequestBody AddUserToProjectRequest request) {

        if (request.getUserId() == null || request.getProjectId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("требуеются id пользователя и проекта"));
        }

        Users user = userRepository.findById(request.getUserId()).orElse(null);
        Project project = projectRepository.findById(request.getProjectId()).orElse(null);

        if (user == null || project == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("пользователь или проект не найдены"));
        }

        boolean alreadyExists = userProjectRepository.existsByUserIdAndProjectId(
                request.getUserId(),
                request.getProjectId()
        );

        if (alreadyExists) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Пользователь уже есть в проекте"));
        }

        UserProject userProject = new UserProject();
        userProject.setUser(user);
        userProject.setProject(project);

        userProjectRepository.save(userProject);

        return ResponseEntity.status(HttpStatus.CREATED).body(userProject);
    }

    @GetMapping("/projects/{projectId}/users")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> getProjectUsers(@PathVariable Long projectId) {

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        List<UserProject> relations = userProjectRepository.findByProjectId(projectId);
        List<Users> users = new ArrayList<>();

        for (UserProject relation : relations) {
            users.add(relation.getUser());
        }

        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/projects/{projectId}/users/{userId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> removeUserFromProject(@PathVariable Long projectId,
                                                   @PathVariable Long userId) {

        boolean exists = userProjectRepository.existsByUserIdAndProjectId(userId, projectId);
        if (!exists) {
            return ResponseEntity.notFound().build();
        }

        userProjectRepository.deleteByUserIdAndProjectId(userId, projectId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/reserve-project")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @Transactional
    public ResponseEntity<?> reservePlaceForProject(@RequestBody ReserveProjectSpot spot) {

        if (spot.getObjectId() == null || spot.getProjectId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("требуются id объекта и проекта"));
        }

        Objects object = roomsRepository.findById(spot.getObjectId()).orElse(null);
        Project project = projectRepository.findById(spot.getProjectId()).orElse(null);

        if (object == null || project == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("объект или проект не найдены"));
        }

        if (object.getReservedForUser() != null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("это место уже зарезервировано за пользователем"));
        }

        if (object.getReservedForProject() != null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("это место уже зарезервировано за проектом"));
        }

        object.setReservedForProject(project);
        roomsRepository.save(object);

        return ResponseEntity.ok(object);
    }
    @DeleteMapping("/projects/{projectId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> deleteProject(@PathVariable Long projectId) {

        Project project = projectRepository.findById(projectId).orElse(null);

        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        // 1. удалить связи user-project
        List<UserProject> relations = userProjectRepository.findByProjectId(projectId);
        userProjectRepository.deleteAll(relations);

        // 2. снять резерв у объектов
        List<Objects> objects = (List<Objects>) roomsRepository.findAll();

        for (Objects object : objects) {
            if (object.getReservedForProject() != null &&
                    object.getReservedForProject().getId().equals(projectId)) {

                object.setReservedForProject(null);
                object.setReservedForUser(null);
                roomsRepository.save(object);
            }
        }

        // 3. удалить проект
        projectRepository.delete(project);

        return ResponseEntity.noContent().build();
    }
}