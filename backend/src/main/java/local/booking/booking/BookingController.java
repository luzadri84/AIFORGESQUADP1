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
    @PostMapping
    public org.springframework.http.ResponseEntity<BookingResult> create(@Valid @RequestBody BookingRequest request,Principal user) {
        var result=service.create(request,user.getName());
        var status=result.created().isEmpty()?HttpStatus.CONFLICT:result.rejected().isEmpty()?HttpStatus.CREATED:HttpStatus.OK;
        return org.springframework.http.ResponseEntity.status(status).body(result);
    }
    @GetMapping public List<BookingView> own(Principal user) { return service.own(user.getName()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id,Principal user) { service.cancel(id,user.getName()); }
}
