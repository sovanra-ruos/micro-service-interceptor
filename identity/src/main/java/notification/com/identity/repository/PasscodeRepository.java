package notification.com.identity.repository;

import notification.com.identity.domain.Passcode;
import notification.com.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasscodeRepository extends JpaRepository<Passcode, Long> {
    Optional<Passcode> findByToken(String token);
    void deleteByUser(User user);
}
