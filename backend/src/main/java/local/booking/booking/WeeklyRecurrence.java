package local.booking.booking;
import java.time.*;
import java.util.*;
public final class WeeklyRecurrence {
 private static final ZoneId BOGOTA=ZoneId.of("America/Bogota");
 private WeeklyRecurrence() { }
 public record Slot(OffsetDateTime start,OffsetDateTime end) { }
 public static List<Slot> expand(OffsetDateTime start,OffsetDateTime end,int count){
  if(count<1 || count>12 || !start.toInstant().isBefore(end.toInstant())) throw new IllegalArgumentException("Rango o cantidad inválidos");
  var localStart=start.atZoneSameInstant(BOGOTA);var localEnd=end.atZoneSameInstant(BOGOTA);
  List<Slot> result=new ArrayList<>();for(int i=0;i<count;i++)result.add(new Slot(localStart.plusWeeks(i).toOffsetDateTime(),localEnd.plusWeeks(i).toOffsetDateTime()));
  return List.copyOf(result);
 }
}
