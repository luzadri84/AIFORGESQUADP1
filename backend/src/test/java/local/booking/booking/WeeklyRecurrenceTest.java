package local.booking.booking;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;
class WeeklyRecurrenceTest {
 @Test void includesFirstAndKeepsBogotaTimeAcrossMonths(){
  var start=OffsetDateTime.parse("2035-01-29T15:00:00Z");var slots=WeeklyRecurrence.expand(start,start.plusHours(1),4);
  assertThat(slots).hasSize(4);assertThat(slots.getFirst().start().toInstant()).isEqualTo(start.toInstant());
  for(int i=0;i<4;i++){assertThat(slots.get(i).start().getHour()).isEqualTo(10);assertThat(slots.get(i).start().getOffset()).isEqualTo(ZoneOffset.ofHours(-5));assertThat(slots.get(i).start().toLocalDate()).isEqualTo(LocalDate.of(2035,1,29).plusWeeks(i));}
 }
 @ParameterizedTest @ValueSource(ints={0,13,-1}) void rejectsUnboundedCount(int count){
  var start=OffsetDateTime.parse("2035-01-01T10:00:00-05:00");assertThatThrownBy(()->WeeklyRecurrence.expand(start,start.plusHours(1),count)).isInstanceOf(IllegalArgumentException.class);
 }
 @Test void internalOverlapIsDetectable(){
  var start=OffsetDateTime.parse("2035-01-01T10:00:00-05:00");var s=WeeklyRecurrence.expand(start,start.plusDays(8),2);
  assertThat(TimeRange.overlaps(s.get(0).start().toInstant(),s.get(0).end().toInstant(),s.get(1).start().toInstant(),s.get(1).end().toInstant())).isTrue();
 }
}
