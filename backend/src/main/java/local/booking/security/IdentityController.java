package local.booking.security;
import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;
@RestController @RequestMapping("/api")
public class IdentityController {
    @GetMapping("/me") public Map<String,String> me(Principal principal) { return Map.of("username",principal.getName()); }
    @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken token) { return Map.of("token",token.getToken(),"headerName",token.getHeaderName()); }
}
