package local.booking.security;

import java.util.UUID;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import jakarta.validation.Valid;
import local.booking.booking.BookingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Import(SecurityContractTest.ContractProbe.class)
class SecurityContractTest {
    static final String PASSWORD = UUID.randomUUID().toString();
    @DynamicPropertySource static void credentials(DynamicPropertyRegistry r) {
        r.add("booking.auth.first.username", () -> "test-a"); r.add("booking.auth.first.password", () -> PASSWORD);
        r.add("booking.auth.second.username", () -> "test-b"); r.add("booking.auth.second.password", () -> PASSWORD);
    }
    @Autowired MockMvc mvc; @Autowired ObjectMapper json;
    String auth(String user) { return "Basic " + Base64.getEncoder().encodeToString((user+":"+PASSWORD).getBytes(StandardCharsets.UTF_8)); }
    @Test void identityRequiresCredentialsAndNeverReturnsPassword() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        for (String user : new String[]{"test-a","test-b"})
            mvc.perform(get("/api/me").header("Authorization",auth(user)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value(user))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/me").header("Authorization",auth("wrong"))).andExpect(status().isUnauthorized());
    }
    @Test void csrfRequiredAndTokenAloneDoesNotAuthenticate() throws Exception {
        mvc.perform(post("/api/contract-probe").header("Authorization",auth("test-a")))
            .andExpect(status().isForbidden());
        var r=mvc.perform(get("/api/csrf")).andExpect(status().isOk()).andReturn();
        var session=(MockHttpSession)r.getRequest().getSession(false);
        String token=json.readTree(r.getResponse().getContentAsString()).get("token").asText();
        String body="{\"spaceId\":1,\"startsAt\":\"2035-01-01T10:00:00-05:00\",\"endsAt\":\"2035-01-01T11:00:00-05:00\"}";
        mvc.perform(post("/api/contract-probe").session(session).header("X-CSRF-TOKEN",token).contentType("application/json").content(body)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/contract-probe").session(session).header("Authorization",auth("test-a")).header("X-CSRF-TOKEN",token).contentType("application/json").content(body)).andExpect(status().isNoContent());
        mvc.perform(post("/api/contract-probe").session(session).header("Authorization",auth("test-a")).header("X-CSRF-TOKEN","bad").contentType("application/json").content(body)).andExpect(status().isForbidden());
    }
    @Test void rejectsInvalidAndUntrustedContractFields() throws Exception {
        var r=mvc.perform(get("/api/csrf")).andReturn();
        var session=(MockHttpSession)r.getRequest().getSession(false);
        String token=json.readTree(r.getResponse().getContentAsString()).get("token").asText();
        String valid="{\"spaceId\":1,\"startsAt\":\"2035-01-01T10:00:00-05:00\",\"endsAt\":\"2035-01-01T11:00:00-05:00\"}";
        for (String body : new String[]{"{}",valid.replace("11:00","09:00"),valid.replace("-05:00",""),valid.replace("1,","0,"),valid.replace("}",",\"userId\":\"other\"}"),valid.replace("}",",\"state\":\"CANCELLED\"}")})
            mvc.perform(post("/api/contract-probe").session(session).header("Authorization",auth("test-a")).header("X-CSRF-TOKEN",token).contentType("application/json").content(body)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Solicitud inválida. Revise campos, fechas con offset y rango."));
    }
    // Test-only endpoint exercises the actual MVC validation/advice without claiming a booking API exists yet.
    @RestController static class ContractProbe {
        @PostMapping("/api/contract-probe") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
        void validate(@Valid @RequestBody BookingRequest request) { }
    }
}
