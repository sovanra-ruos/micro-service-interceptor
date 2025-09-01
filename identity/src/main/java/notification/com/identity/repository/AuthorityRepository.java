package notification.com.identity.repository;

import notification.com.identity.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Optional<Authority> findByName(String name);

    @Query("SELECT a FROM Authority a WHERE a.name = 'USER'")
    Authority AUTH_USER();

    @Query("SELECT a FROM Authority a WHERE a.name = 'ADMIN'")
    Authority AUTH_ADMIN();

    @Query("SELECT a FROM Authority a WHERE a.name = 'SYSTEM'")
    Authority AUTH_SYSTEM();

    @Query("SELECT a FROM Authority a WHERE a.name = 'EDITOR'")
    Authority AUTH_EDITOR();
}

