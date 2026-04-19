package Bookings.Repository;

import Bookings.Model.UserProject;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserProjectRepository extends CrudRepository<UserProject, Long> {

//    boolean existsByUserIdAndProjectId(Long userId, Long projectId);
    boolean existsByUserIdAndProjectId(Long userId, Long projectId);

    List<UserProject> findByProjectId(Long projectId);

    List<UserProject> findByUserId(Long userId);
    @Transactional
    void deleteByUserIdAndProjectId(Long userId, Long projectId);

}