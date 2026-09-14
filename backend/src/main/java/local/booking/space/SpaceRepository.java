package local.booking.space;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface SpaceRepository extends JpaRepository<Space,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name="jakarta.persistence.lock.timeout",value="10000"))
    @Query("select s from Space s where s.id=:id")
    Optional<Space> lockById(@Param("id") Long id);
}
