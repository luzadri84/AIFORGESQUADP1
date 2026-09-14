package local.booking.booking;
import java.time.OffsetDateTime;
import java.util.List;
public record BookingResult(List<BookingView> created,List<Rejected> rejected) {
 public record Rejected(OffsetDateTime startsAt,OffsetDateTime endsAt,String reason) { }
}
