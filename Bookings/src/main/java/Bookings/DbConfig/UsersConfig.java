package Bookings.DbConfig;


import Bookings.Model.Roles;
import Bookings.Model.Users;
import Bookings.Repository.RolesRepository;
import Bookings.Repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class UsersConfig {
    @Bean
    @Order(2)  // Сначала роли!
    public ApplicationRunner dataLoader1(UserRepository userRepo,
                                         RolesRepository roleRepo,
                                         PasswordEncoder encoder) {
        return args -> {
            // 1. НАХОДИМ существующие роли по ID!
            Roles userRole = roleRepo.findByRole(Roles.Role.USER).orElseThrow();  // id=1
            Roles adminRole = roleRepo.findByRole(Roles.Role.ADMIN).orElseThrow(); // id=2
            Roles superAdminRole = roleRepo.findByRole(Roles.Role.SUPERADMIN).orElseThrow(); // id=3

            // 2. ССЫЛАЕМСЯ на СУЩЕСТВУЮЩИЕ роли!
            Users user1 = new Users("user1", "Olegov", encoder.encode("1234"), "", "");
            user1.setRoles(List.of(userRole));  // id=1, id=2!
            Users user2 = new Users("user2", "Olegov", encoder.encode("1234"), "", "");
            user2.setRoles(List.of(userRole));  // id=1, id=2!

            Users admin1 = new Users("admin1", "Ivanov", encoder.encode("1234"), "", "");
            admin1.setRoles(List.of(userRole, adminRole));  // id=1, id=2!
            Users admin2 = new Users("admin2", "Ivanov", encoder.encode("1234"), "", "");
            admin2.setRoles(List.of(userRole, adminRole));  // id=1, id=2!


            Users super1 = new Users("super1", "Dimov", encoder.encode("1234"), "", "");
            super1.setRoles(List.of(userRole, adminRole,superAdminRole));  // id=1, id=2!
            Users super2 = new Users("super2", "Dimov", encoder.encode("1234"), "", "");
            super2.setRoles(List.of(userRole, adminRole,superAdminRole));  // id=1, id=2!

//            userRepo.save(oleg);  // Только users + users_roles!
//            userRepo.save(ivan);
//            userRepo.save(dima);
            userRepo.saveAll(List.of(user1,user2,admin1,admin2,super1,super2));
        };
    }

}
