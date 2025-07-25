package project.luckybooky.domain.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.entity.UserType;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Query("SELECT u.userType FROM User u WHERE u.id = :userId")
    UserType findUserTypeByUserId(@Param("userId") Long userId);

    boolean existsByEmailAndUserTypeIsNull(String email);

}
