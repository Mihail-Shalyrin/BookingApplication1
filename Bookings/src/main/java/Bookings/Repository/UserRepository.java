package Bookings.Repository;

import Bookings.Model.Users;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<Users,Long> {
    Users findByUsername(String name);
    List<Users> findAll();


}
