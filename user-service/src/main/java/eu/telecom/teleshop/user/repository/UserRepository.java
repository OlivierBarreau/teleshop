package eu.telecom.teleshop.user.repository;

import eu.telecom.teleshop.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
