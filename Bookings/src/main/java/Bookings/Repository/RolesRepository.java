package Bookings.Repository;

import Bookings.Model.Roles;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RolesRepository extends CrudRepository<Roles,Long> {
    Optional<Roles> findByRole(Roles.Role role);

}
