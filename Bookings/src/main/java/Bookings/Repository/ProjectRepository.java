package Bookings.Repository;

import Bookings.Model.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectRepository extends CrudRepository<Project, Long> {
    List<Project> findAll();
    boolean existsByName(String name);
}