package eu.telecom.teleshop.notification.repository;

import eu.telecom.teleshop.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long deleteByUserId(Long userId);
    List<Notification> findByUserId(Long userId);
}
