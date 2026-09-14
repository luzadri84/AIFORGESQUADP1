package local.booking.booking;
import java.time.*;
import java.util.List;
import java.util.ArrayList;
import local.booking.space.SpaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
public class BookingService {
    private final BookingRepository bookings; private final SpaceRepository spaces; private final Clock clock;
    public BookingService(BookingRepository bookings,SpaceRepository spaces,Clock clock) { this.bookings=bookings;this.spaces=spaces;this.clock=clock; }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public BookingResult create(BookingRequest request,String owner) {
        var slots=WeeklyRecurrence.expand(request.startsAt(),request.endsAt(),request.occurrences()==null?1:request.occurrences());
        var space=spaces.lockById(request.spaceId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Espacio no encontrado"));
        var created=new ArrayList<BookingView>();var rejected=new ArrayList<BookingResult.Rejected>();
        for(var slot:slots){
            if(bookings.collisions(space.getId(),slot.start(),slot.end())>0){
                rejected.add(new BookingResult.Rejected(slot.start(),slot.end(),"Horario no disponible"));
            }else{
                created.add(BookingView.of(bookings.saveAndFlush(new Booking(space,owner,slot.start(),slot.end(),BookingStatus.ACTIVE))));
            }
        }
        return new BookingResult(List.copyOf(created),List.copyOf(rejected));
    }
    @Transactional(readOnly=true)
    public List<BookingView> own(String owner) {
        return bookings.findByOwnerIdAndStatusAndEndsAtAfterOrderByStartsAtAsc(owner,BookingStatus.ACTIVE,OffsetDateTime.now(clock)).stream().map(BookingView::of).toList();
    }
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public void cancel(Long id,String owner) {
        Long spaceId=bookings.ownedSpace(id,owner).orElseThrow(BookingService::missing);
        spaces.lockById(spaceId).orElseThrow(BookingService::missing);
        var booking=bookings.findByIdAndOwnerId(id,owner).orElseThrow(BookingService::missing);
        booking.cancel(); bookings.flush();
    }
    private static ResponseStatusException missing() { return new ResponseStatusException(HttpStatus.NOT_FOUND,"Reserva no encontrada"); }
}
