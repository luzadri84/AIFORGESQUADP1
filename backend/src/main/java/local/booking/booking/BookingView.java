package local.booking.booking;
import java.time.OffsetDateTime;
public record BookingView(Long id,Long spaceId,String spaceName,OffsetDateTime startsAt,OffsetDateTime endsAt,BookingStatus status) {
    public static BookingView of(Booking b) { return new BookingView(b.getId(),b.getSpace().getId(),b.getSpace().getName(),b.getStartsAt(),b.getEndsAt(),b.getStatus()); }
}
