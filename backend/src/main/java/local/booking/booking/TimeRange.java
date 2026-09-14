package local.booking.booking;
import java.time.Instant;
public final class TimeRange {
    private TimeRange() { }
    public static boolean overlaps(Instant aStart,Instant aEnd,Instant bStart,Instant bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }
}
