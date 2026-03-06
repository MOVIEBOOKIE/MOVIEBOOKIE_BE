package project.luckybooky.domain.adminUser.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import project.luckybooky.domain.adminUser.entity.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByName(String name);
}
