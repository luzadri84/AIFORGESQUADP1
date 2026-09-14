package local.booking.booking;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
public record BookingRequest(@NotNull @Positive Long spaceId, @NotNull OffsetDateTime startsAt,
                             @NotNull OffsetDateTime endsAt, @Min(1) @Max(12) Integer occurrences) {
    @AssertTrue @JsonIgnore public boolean isValidRange() {
        return startsAt==null || endsAt==null || startsAt.toInstant().isBefore(endsAt.toInstant());
    }
}
