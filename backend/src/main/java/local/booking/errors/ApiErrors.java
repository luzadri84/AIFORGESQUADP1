package local.booking.errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataAccessException;
@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class})
    ProblemDetail invalid(Exception e) { return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Solicitud inválida. Revise campos, fechas con offset y rango."); }
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail business(ResponseStatusException e) { return ProblemDetail.forStatusAndDetail(e.getStatusCode(),e.getReason()); }
    @ExceptionHandler(DataAccessException.class)
    ProblemDetail data(DataAccessException e) { return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"Operación temporalmente no disponible. Consulte sus reservas antes de reintentar."); }
}
