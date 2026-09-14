package local.booking.booking;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import local.booking.space.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

/** Audit regressions: failing contracts are deliberately retained, never masked. */
@SpringBootTest @AutoConfigureMockMvc(print=MockMvcPrint.NONE) @Transactional
class AuditSecurityTest {
    static final String PASSWORD=UUID.randomUUID().toString();
    @DynamicPropertySource static void credentials(DynamicPropertyRegistry r) {
        r.add("booking.auth.first.username",()->"audit-a");r.add("booking.auth.first.password",()->PASSWORD);
        r.add("booking.auth.second.username",()->"audit-b");r.add("booking.auth.second.password",()->PASSWORD);
    }
    @Autowired MockMvc mvc; @Autowired ObjectMapper json;
    @Autowired SpaceRepository spaces; @Autowired BookingRepository bookings;
    long space;
    @BeforeEach void fixture(){space=spaces.saveAndFlush(new Space("AUDIT "+UUID.randomUUID(),SpaceType.ROOM,2,"Audit transaction rollback")).getId();}
    String auth(String user){return "Basic "+Base64.getEncoder().encodeToString((user+":"+PASSWORD).getBytes(StandardCharsets.UTF_8));}
    record Csrf(MockHttpSession session,String token){}
    Csrf csrf()throws Exception{var r=mvc.perform(get("/api/csrf")).andReturn();return new Csrf((MockHttpSession)r.getRequest().getSession(false),json.readTree(r.getResponse().getContentAsString()).get("token").asText());}
    String body(){return "{\"spaceId\":"+space+",\"startsAt\":\"2039-06-01T10:00:00-05:00\",\"endsAt\":\"2039-06-01T11:00:00-05:00\",\"occurrences\":1}";}
    MockHttpServletRequestBuilder secured(MockHttpServletRequestBuilder request,String user,Csrf c){return request.header("Authorization",auth(user)).session(c.session()).header("X-CSRF-TOKEN",c.token());}
    long create(String user,Csrf c)throws Exception{return json.readTree(mvc.perform(secured(post("/api/bookings"),user,c).contentType("application/json").content(body())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("created").get(0).get("id").asLong();}

    @Test void authenticationIsCheckedAfterValidCsrf()throws Exception{
        var c=csrf();long count=bookings.count();
        for(String credentials:List.of("","Basic bm9ib2R5OmludmFsaWQ=")){
            mvc.perform(post("/api/bookings").session(c.session()).header("X-CSRF-TOKEN",c.token()).header("Authorization",credentials).contentType("application/json").content(body())).andExpect(status().isUnauthorized());
            mvc.perform(get("/api/bookings").header("Authorization",credentials)).andExpect(status().isUnauthorized());
        }
        assertThat(bookings.count()).isEqualTo(count);
    }
    @Test void csrfProtectsCreateAndCancelAndIsBoundToSession()throws Exception{
        var c=csrf();var other=csrf();long count=bookings.count();
        for(String token:List.of("","invalid",other.token())){
            mvc.perform(post("/api/bookings").session(c.session()).header("Authorization",auth("audit-a")).header("X-CSRF-TOKEN",token).contentType("application/json").content(body())).andExpect(status().isForbidden());
            assertThat(bookings.count()).isEqualTo(count);
        }
        long id=create("audit-a",c);
        for(String token:List.of("","invalid",other.token())){
            mvc.perform(delete("/api/bookings/"+id).session(c.session()).header("Authorization",auth("audit-a")).header("X-CSRF-TOKEN",token)).andExpect(status().isForbidden());
            assertThat(bookings.findById(id).orElseThrow().getStatus()).isEqualTo(BookingStatus.ACTIVE);
        }
        mvc.perform(secured(delete("/api/bookings/"+id),"audit-a",c)).andExpect(status().isNoContent());
        assertThat(bookings.findById(id).orElseThrow().getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }
    @ParameterizedTest @ValueSource(strings={"audit-a","audit-b"})
    void ownershipBothDirectionsWithValidAttackerCsrf(String owner)throws Exception{
        String attacker=owner.equals("audit-a")?"audit-b":"audit-a";
        var a=csrf();var b=csrf();long id=create(owner,a);
        mvc.perform(get("/api/bookings").header("Authorization",auth(attacker)).param("ownerId",owner).param("userId",owner)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(secured(delete("/api/bookings/"+id),attacker,b)).andExpect(status().isNotFound());
        var row=bookings.findById(id).orElseThrow();assertThat(row.getOwnerId()).isEqualTo(owner);assertThat(row.getStatus()).isEqualTo(BookingStatus.ACTIVE);
        var conflict=mvc.perform(secured(post("/api/bookings"),attacker,b).contentType("application/json").content(body())).andExpect(status().isConflict()).andReturn();
        assertThat(conflict.getResponse().getContentAsString()).doesNotContain(owner,"ownerId");
        for(int i=0;i<2;i++)mvc.perform(secured(delete("/api/bookings/"+id),owner,a)).andExpect(status().isNoContent());
        assertThat(bookings.findById(id).orElseThrow().getStatus()).isEqualTo(BookingStatus.CANCELLED);
        long replacement=create(attacker,b);assertThat(bookings.findById(replacement).orElseThrow().getOwnerId()).isEqualTo(attacker);
    }
    @ParameterizedTest @ValueSource(strings={"ownerId","userId","usuarioId","status","state","id"})
    void rejectsServerControlledFields(String field)throws Exception{
        long count=bookings.count();var c=csrf();String forged=body().replace("}",",\""+field+"\":\"audit-b\"}");
        mvc.perform(secured(post("/api/bookings"),"audit-a",c).contentType("application/json").content(forged)).andExpect(status().isBadRequest());
        assertThat(bookings.count()).isEqualTo(count);
    }
    @ParameterizedTest @ValueSource(strings={"equal","reversed","missing","malformed","no-offset","sql-string"})
    void invalidInputsDoNotWrite(String scenario)throws Exception{
        var node=json.readTree(body());var o=(com.fasterxml.jackson.databind.node.ObjectNode)node;
        switch(scenario){
            case "equal"->o.put("endsAt",o.get("startsAt").asText());
            case "reversed"->o.put("endsAt","2039-06-01T09:00:00-05:00");
            case "missing"->o.remove("startsAt");
            case "malformed"->o.put("startsAt","not-a-date");
            case "no-offset"->o.put("startsAt","2039-06-01T10:00:00");
            case "sql-string"->o.put("spaceId","1 OR 1=1");
        }
        long count=bookings.count();mvc.perform(secured(post("/api/bookings"),"audit-a",csrf()).contentType("application/json").content(json.writeValueAsString(o))).andExpect(status().isBadRequest());assertThat(bookings.count()).isEqualTo(count);
    }
    @Test void activeMeansOwnAndNotEndedAndOffsetsPreserveInstant()throws Exception{
        var s=spaces.getReferenceById(space);var now=OffsetDateTime.now();
        bookings.saveAndFlush(new Booking(s,"audit-a",now.minusHours(1),now.plusHours(1),BookingStatus.ACTIVE));
        bookings.saveAndFlush(new Booking(s,"audit-a",now.minusDays(2),now.minusDays(1),BookingStatus.ACTIVE));
        bookings.saveAndFlush(new Booking(s,"audit-a",now.plusDays(2),now.plusDays(3),BookingStatus.CANCELLED));
        mvc.perform(get("/api/bookings").header("Authorization",auth("audit-a"))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        String offsetBody=body().replace("2039-06-01T10:00:00-05:00","2039-06-01T15:00:00Z");
        long id=json.readTree(mvc.perform(secured(post("/api/bookings"),"audit-b",csrf()).contentType("application/json").content(offsetBody)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("created").get(0).get("id").asLong();
        assertThat(bookings.findById(id).orElseThrow().getStartsAt().toInstant()).isEqualTo(java.time.Instant.parse("2039-06-01T15:00:00Z"));
    }
    @Test void documentedPastPolicyDoesNotInventRestriction()throws Exception{
        var past=body().replace("2039-06-01","2001-06-01");long before=bookings.count();
        mvc.perform(secured(post("/api/bookings"),"audit-a",csrf()).contentType("application/json").content(past)).andExpect(status().isCreated());
        assertThat(bookings.count()).isEqualTo(before+1);
        mvc.perform(get("/api/bookings").header("Authorization",auth("audit-a"))).andExpect(jsonPath("$.length()").value(0));
    }
    @Test void foreignOriginDoesNotGainCorsAndCannotWriteWithoutCsrf()throws Exception{
        mvc.perform(options("/api/bookings").header("Origin","https://audit.invalid").header("Access-Control-Request-Method","POST")).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        long count=bookings.count();mvc.perform(post("/api/bookings").header("Origin","https://audit.invalid").header("Authorization",auth("audit-a")).contentType("application/json").content(body())).andExpect(status().isForbidden());assertThat(bookings.count()).isEqualTo(count);
    }
    @Test @Tag("audit-finding") void missingResourceShouldBe404Not500()throws Exception{
        mvc.perform(get("/api/route-that-does-not-exist").header("Authorization",auth("audit-b"))).andExpect(status().isNotFound());
    }
    @Test void getOnCancellationRouteIs405WithAllow()throws Exception{
        mvc.perform(get("/api/bookings/99999999").header("Authorization",auth("audit-b")))
            .andExpect(status().isMethodNotAllowed()).andExpect(header().string("Allow",org.hamcrest.Matchers.containsString("DELETE")));
    }
    @ParameterizedTest @ValueSource(strings={"1.0","1e0","\"1\"","0","13"})
    void integerTokenContractAndBounds(String value)throws Exception{
        long before=bookings.count();
        mvc.perform(secured(post("/api/bookings"),"audit-a",csrf()).contentType("application/json")
            .content(body().replace("\"occurrences\":1","\"occurrences\":"+value))).andExpect(status().isBadRequest());
        assertThat(bookings.count()).isEqualTo(before);
    }
    @Test void nullOccurrencesAndValidIntegerKeepDefaults()throws Exception{
        var response=mvc.perform(secured(post("/api/bookings"),"audit-a",csrf()).contentType("application/json")
            .content(body().replace("\"occurrences\":1","\"occurrences\":null"))).andExpect(status().isCreated()).andReturn();
        assertThat(json.readTree(response.getResponse().getContentAsString()).get("created").size()).isEqualTo(1);
    }
    @Test @Tag("audit-finding") void malformedIdentifierShouldBe400Not500()throws Exception{
        mvc.perform(secured(delete("/api/bookings/not-a-number"),"audit-a",csrf())).andExpect(status().isBadRequest());
    }
    @Test @Tag("audit-finding") void unsupportedEditShouldBe405Not500()throws Exception{
        mvc.perform(secured(put("/api/bookings/99999999"),"audit-a",csrf()).contentType("application/json").content(body())).andExpect(status().isMethodNotAllowed()).andExpect(header().string("Allow",org.hamcrest.Matchers.containsString("DELETE")));
    }
    @ParameterizedTest @ValueSource(strings={"occurrences","spaceId"}) @Tag("audit-finding")
    void fractionalIntegersMustNotCreateReservations(String field)throws Exception{
        var node=(com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(body());node.put(field,field.equals("spaceId")?space+0.75:1.75);long before=bookings.count();
        mvc.perform(secured(post("/api/bookings"),"audit-a",csrf()).contentType("application/json").content(json.writeValueAsString(node))).andExpect(status().isBadRequest());assertThat(bookings.count()).isEqualTo(before);
    }
    @Test @Tag("audit-finding") void openApiDocumentsSecurityAndRealPostStatuses()throws Exception{
        var result=mvc.perform(get("/v3/api-docs").header("Authorization",auth("audit-a"))).andExpect(status().isOk()).andReturn();
        var doc=json.readTree(result.getResponse().getContentAsString());
        assertThat(doc.at("/paths/~1api~1bookings/post/responses").has("201")).isTrue();
        assertThat(doc.at("/paths/~1api~1bookings/post/responses").has("409")).isTrue();
        assertThat(doc.at("/components/securitySchemes").isMissingNode()).isFalse();
    }
}
