package local.booking.booking;
import local.booking.space.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.sql.DataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessResourceFailureException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class OracleConcurrencyTest {
 static final String PASSWORD=UUID.randomUUID().toString();
 static final String USER="race-"+UUID.randomUUID();
 @DynamicPropertySource static void credentials(DynamicPropertyRegistry r){
  r.add("booking.auth.first.username",()->USER);r.add("booking.auth.first.password",()->PASSWORD);
  r.add("booking.auth.second.username",()->"other-"+USER);r.add("booking.auth.second.password",()->PASSWORD);
 }
 @LocalServerPort int port;
 @Autowired DataSource dataSource; @Autowired JdbcTemplate jdbc;
 @SpyBean SpaceRepository spaces; @SpyBean BookingRepository bookings;
 long spaceId;
 @BeforeEach void fixture(){spaceId=spaces.saveAndFlush(new Space("TEST concurrency",SpaceType.ROOM,2,"Automated test")).getId();}
 @AfterEach void cleanup(){reset(spaces,bookings);jdbc.update("DELETE FROM BKG_BOOKING WHERE SPACE_ID=?",spaceId);jdbc.update("DELETE FROM BKG_SPACE WHERE ID=?",spaceId);}
 record Client(HttpClient http,String auth,String token) { }
 Client client(String user)throws Exception{
  var http=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
  String auth="Basic "+Base64.getEncoder().encodeToString((user+":"+PASSWORD).getBytes(StandardCharsets.UTF_8));
  var r=http.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/csrf")).header("Authorization",auth).build(),HttpResponse.BodyHandlers.ofString());
  return new Client(http,auth,new ObjectMapper().readTree(r.body()).get("token").asText());
 }
 CompletableFuture<HttpResponse<String>> create(Client c,int count){
  String body="{\"spaceId\":"+spaceId+",\"startsAt\":\"2036-04-01T10:00:00-05:00\",\"endsAt\":\"2036-04-01T11:00:00-05:00\",\"occurrences\":"+count+"}";
  return c.http.sendAsync(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/bookings")).header("Authorization",c.auth).header("X-CSRF-TOKEN",c.token).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
 }
 @Test void twoRequestsWaitForSameEmptySpaceThenOnlyOneCommits() throws Exception {
  var entered=new CountDownLatch(2);
  var realSpaces=mockingDetails(spaces).getMockCreationSettings().getDefaultAnswer();
  doAnswer(inv->{entered.countDown();return realSpaces.answer(inv);}).when(spaces).lockById(spaceId);
  var a=client(USER);var b=client("other-"+USER);
  CompletableFuture<HttpResponse<String>> first,second;
  try(var connection=dataSource.getConnection()){
   connection.setAutoCommit(false);
   try(var statement=connection.prepareStatement("SELECT ID FROM BKG_SPACE WHERE ID=? FOR UPDATE")){
    statement.setLong(1,spaceId);try(var rows=statement.executeQuery()){assertThat(rows.next()).isTrue();}
   }
   first=create(a,1);second=create(b,1);
   assertThat(entered.await(10,TimeUnit.SECONDS)).isTrue();
   assertThat(first.isDone()).isFalse();assertThat(second.isDone()).isFalse();
   connection.commit();
  }
  assertThat(List.of(first.get(30,TimeUnit.SECONDS).statusCode(),second.get(30,TimeUnit.SECONDS).statusCode())).containsExactlyInAnyOrder(201,409);
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM BKG_BOOKING WHERE SPACE_ID=? AND STATUS='ACTIVE'",Integer.class,spaceId)).isEqualTo(1);
 }
 @Test void injectedTechnicalFailureRollsBackRealFirstFlush() throws Exception {
  var firstWritten=new AtomicBoolean();
  var realBookings=mockingDetails(bookings).getMockCreationSettings().getDefaultAnswer();
  doAnswer(inv->{
   realBookings.answer(inv);
   assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM BKG_BOOKING WHERE SPACE_ID=?",Integer.class,spaceId)).isEqualTo(1);
   firstWritten.set(true);throw new DataAccessResourceFailureException("controlled test failure after actual Oracle flush");
  }).when(bookings).saveAndFlush(any(Booking.class));
  var response=create(client(USER),4).get(30,TimeUnit.SECONDS);
  assertThat(response.statusCode()).isEqualTo(503);
   assertThat(response.headers().firstValue("Cache-Control").orElse("")).contains("no-store");
   assertThat(response.headers().firstValue("X-Content-Type-Options")).contains("nosniff");
   assertThat(response.headers().firstValue("X-Frame-Options")).contains("DENY");assertThat(response.body()).doesNotContain("controlled test failure");
  assertThat(firstWritten.get()).isTrue();
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM BKG_BOOKING WHERE SPACE_ID=?",Integer.class,spaceId)).isZero();
 }
 @Test void auditLockTimeoutReturns503AndDoesNotCommit() throws Exception {
  var c=client(USER);
  CompletableFuture<HttpResponse<String>> request=null;
  try(var connection=dataSource.getConnection()){
   connection.setAutoCommit(false);
   try(var statement=connection.prepareStatement("SELECT ID FROM BKG_SPACE WHERE ID=? FOR UPDATE")){
    statement.setLong(1,spaceId);try(var rows=statement.executeQuery()){assertThat(rows.next()).isTrue();}
   }
   long started=System.nanoTime();
   request=create(c,4);
   var response=request.get(14,TimeUnit.SECONDS);
   long elapsed=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-started);
   assertThat(response.statusCode()).isEqualTo(503);
   assertThat(response.headers().firstValue("Cache-Control").orElse("")).contains("no-store");
   assertThat(response.headers().firstValue("X-Content-Type-Options")).contains("nosniff");
   assertThat(response.headers().firstValue("X-Frame-Options")).contains("DENY");
   assertThat(response.body()).doesNotContain("ORA-", "SELECT", "Exception");
   assertThat(elapsed).isBetween(8000L,14000L);
   assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM BKG_BOOKING WHERE SPACE_ID=?",Integer.class,spaceId)).isZero();
   connection.rollback();
  } finally {
   // Release the independent JDBC lock before awaiting any unexpected late completion.
   if(request!=null&&!request.isDone())request.get(20,TimeUnit.SECONDS);
  }
 }
}
