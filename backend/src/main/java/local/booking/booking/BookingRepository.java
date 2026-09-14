package local.booking.booking;
import java.time.OffsetDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface BookingRepository extends JpaRepository<Booking,Long> {
    @Query("select count(b) from Booking b where b.space.id=:space and b.status=local.booking.booking.BookingStatus.ACTIVE and b.startsAt < :end and b.endsAt > :start")
    long collisions(@Param("space") Long space,@Param("start") OffsetDateTime start,@Param("end") OffsetDateTime end);
    List<Booking> findByOwnerIdAndStatusAndEndsAtAfterOrderByStartsAtAsc(String owner,BookingStatus status,OffsetDateTime now);
    @Query("select b.space.id from Booking b where b.id=:id and b.ownerId=:owner")
    Optional<Long> ownedSpace(@Param("id") Long id,@Param("owner") String owner);
    Optional<Booking> findByIdAndOwnerId(Long id,String owner);
}
