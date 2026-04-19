package Bookings.DbConfig;

import Bookings.Model.Objects;
import Bookings.Model.Roles;
import Bookings.Model.Users;
import Bookings.Repository.RolesRepository;
import Bookings.Repository.UserRepository;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;


@Configuration

public class RolesLoader{
    @Bean
    @Order(1)  // Сначала роли!
    public ApplicationRunner dataLoader2(RolesRepository rolesRepository) {
        return args -> {
            System.out.println("BEFORE: " + rolesRepository.count());

            rolesRepository.saveAll(List.of(
                    new Roles(Roles.Role.ADMIN),
                    new Roles(Roles.Role.USER),
                    new Roles(Roles.Role.SUPERADMIN)
            ));

            System.out.println("AFTER: " + rolesRepository.count());
            rolesRepository.findAll().forEach(System.out::println);
        };
    }

}
