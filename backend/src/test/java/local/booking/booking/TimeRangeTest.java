package local.booking.booking;
import java.time.Instant;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;
class TimeRangeTest {
    @ParameterizedTest @CsvSource({"9,11,true","11,13,true","10,12,true","9,13,true","10,11,true","11,12,true","8,10,false","12,14,false","6,8,false","14,16,false"})
    void semiOpenIntervals(int start,int end,boolean expected) {
        assertThat(TimeRange.overlaps(Instant.ofEpochSecond(10),Instant.ofEpochSecond(12),Instant.ofEpochSecond(start),Instant.ofEpochSecond(end))).isEqualTo(expected);
    }
}
