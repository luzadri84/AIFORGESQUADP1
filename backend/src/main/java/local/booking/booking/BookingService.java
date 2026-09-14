package local.booking.booking;
import java.time.*;
import java.util.List;
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
    public BookingView create(BookingRequest request,String owner) {
        var space=spaces.lockById(request.spaceId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Espacio no encontrado"));
        if(bookings.collisions(space.getId(),request.startsAt(),request.endsAt())>0) throw new ResponseStatusException(HttpStatus.CONFLICT,"El espacio ya está reservado en ese horario");
        return BookingView.of(bookings.saveAndFlush(new Booking(space,owner,request.startsAt(),request.endsAt(),BookingStatus.ACTIVE)));
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
