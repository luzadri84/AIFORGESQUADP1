package local.booking.errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataAccessException;
@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    ProblemDetail invalid(Exception e) { return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Solicitud inválida. Revise campos, fechas con offset y rango."); }
    @ExceptionHandler({org.springframework.web.servlet.resource.NoResourceFoundException.class,
        org.springframework.web.servlet.NoHandlerFoundException.class})
    ProblemDetail missing(Exception e) { return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,"Recurso no encontrado."); }
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ProblemDetail> method(org.springframework.web.HttpRequestMethodNotSupportedException e) {
        var headers=new HttpHeaders();
        if(e.getSupportedHttpMethods()!=null) headers.setAllow(e.getSupportedHttpMethods());
        return new ResponseEntity<>(ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED,"Método no permitido."),headers,HttpStatus.METHOD_NOT_ALLOWED);
    }
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    ProblemDetail media(Exception e) { return ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE,"Tipo de contenido no soportado."); }
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail business(ResponseStatusException e) { return ProblemDetail.forStatusAndDetail(e.getStatusCode(),e.getReason()); }
    @ExceptionHandler(DataAccessException.class)
    ProblemDetail data(DataAccessException e) { return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"Operación temporalmente no disponible. Consulte sus reservas antes de reintentar."); }
    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception e) {
        org.slf4j.LoggerFactory.getLogger(ApiErrors.class).error("Unexpected API failure: {}",e.getClass().getSimpleName());
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,"Error interno. Consulte sus reservas antes de reintentar.");
    }
}
