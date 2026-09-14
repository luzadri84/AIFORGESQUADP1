package local.booking.booking;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import local.booking.space.Space;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

@Entity
@Table(name = "BKG_BOOKING")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_ids")
    @SequenceGenerator(name = "booking_ids", sequenceName = "BKG_BOOKING_SEQ", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SPACE_ID", nullable = false)
    private Space space;
    @Column(name = "OWNER_ID", nullable = false, length = 100)
    private String ownerId;
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    @Column(name = "STARTS_AT", nullable = false, columnDefinition = "TIMESTAMP(9) WITH TIME ZONE")
    private OffsetDateTime startsAt;
    @TimeZoneStorage(TimeZoneStorageType.NATIVE)
    @Column(name = "ENDS_AT", nullable = false, columnDefinition = "TIMESTAMP(9) WITH TIME ZONE")
    private OffsetDateTime endsAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private BookingStatus status;

    protected Booking() { }
    public Booking(Space space, String ownerId, OffsetDateTime startsAt,
                   OffsetDateTime endsAt, BookingStatus status) {
        this.space = space; this.ownerId = ownerId; this.startsAt = startsAt;
        this.endsAt = endsAt; this.status = status;
    }
    public void cancel() { this.status=BookingStatus.CANCELLED; }
    public Long getId() { return id; }
    public Space getSpace() { return space; }
    public String getOwnerId() { return ownerId; }
    public OffsetDateTime getStartsAt() { return startsAt; }
    public OffsetDateTime getEndsAt() { return endsAt; }
    public BookingStatus getStatus() { return status; }
}
