package local.booking.booking;
import java.util.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest @AutoConfigureMockMvc @Transactional
class BookingApiTest {
    static final String PASSWORD=UUID.randomUUID().toString();
    @DynamicPropertySource static void credentials(DynamicPropertyRegistry r) {
        r.add("booking.auth.first.username",()->"api-a");r.add("booking.auth.first.password",()->PASSWORD);
        r.add("booking.auth.second.username",()->"api-b");r.add("booking.auth.second.password",()->PASSWORD);
    }
    @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired BookingRepository bookings;
    String auth(String user) { return "Basic "+Base64.getEncoder().encodeToString((user+":"+PASSWORD).getBytes(StandardCharsets.UTF_8)); }
    record Csrf(MockHttpSession session,String token) { }
    Csrf csrf() throws Exception { var r=mvc.perform(get("/api/csrf")).andReturn();return new Csrf((MockHttpSession)r.getRequest().getSession(false),json.readTree(r.getResponse().getContentAsString()).get("token").asText()); }
    String body(int start,int end,long space) { return "{\"spaceId\":"+space+",\"startsAt\":\"2035-01-01T"+start+":00:00-05:00\",\"endsAt\":\"2035-01-01T"+end+":00:00-05:00\"}"; }
    org.springframework.test.web.servlet.ResultActions create(Csrf c,String user,int start,int end,long space) throws Exception {
        return mvc.perform(post("/api/bookings").session(c.session()).header("X-CSRF-TOKEN",c.token()).header("Authorization",auth(user)).contentType("application/json").content(body(start,end,space)));
    }
    @Test void ownCycleAndCollisionAndCancellation() throws Exception {
        var c=csrf();long before=bookings.count();
        var r=create(c,"api-a",10,11,1).andExpect(status().isCreated()).andReturn();
        long id=json.readTree(r.getResponse().getContentAsString()).get("id").asLong();
        assertThat(bookings.findById(id).orElseThrow().getOwnerId()).isEqualTo("api-a");
        create(c,"api-b",10,11,1).andExpect(status().isConflict());
        create(c,"api-a",11,12,1).andExpect(status().isCreated());
        mvc.perform(get("/api/bookings").header("Authorization",auth("api-a"))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/bookings").header("Authorization",auth("api-b"))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(delete("/api/bookings/"+id).session(c.session()).header("X-CSRF-TOKEN",c.token()).header("Authorization",auth("api-b"))).andExpect(status().isNotFound());
        assertThat(bookings.findById(id).orElseThrow().getStatus()).isEqualTo(BookingStatus.ACTIVE);
        for(int i=0;i<2;i++) mvc.perform(delete("/api/bookings/"+id).session(c.session()).header("X-CSRF-TOKEN",c.token()).header("Authorization",auth("api-a"))).andExpect(status().isNoContent());
        create(c,"api-b",10,11,1).andExpect(status().isCreated());
        assertThat(bookings.count()).isEqualTo(before+3);
    }
    @Test void invalidMissingAndUnauthenticatedRequestsDoNotWrite() throws Exception {
        long before=bookings.count();var c=csrf();
        create(c,"api-a",12,11,1).andExpect(status().isBadRequest());
        create(c,"api-a",10,11,999999).andExpect(status().isNotFound());
        mvc.perform(post("/api/bookings").header("Authorization",auth("api-a")).contentType("application/json").content(body(10,11,1))).andExpect(status().isForbidden());
        mvc.perform(get("/api/bookings")).andExpect(status().isUnauthorized());
        assertThat(bookings.count()).isEqualTo(before);
    }
}
