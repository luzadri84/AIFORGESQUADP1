package local.booking.booking;
import java.security.Principal;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service) { this.service=service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public BookingView create(@Valid @RequestBody BookingRequest request,Principal user) { return service.create(request,user.getName()); }
    @GetMapping public List<BookingView> own(Principal user) { return service.own(user.getName()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id,Principal user) { service.cancel(id,user.getName()); }
}
