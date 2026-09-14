package local.booking.security;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpSecurityTest {
 static final String PASSWORD=UUID.randomUUID().toString();
 @DynamicPropertySource static void credentials(DynamicPropertyRegistry r){
  r.add("booking.auth.first.username",()->"http-a");r.add("booking.auth.first.password",()->PASSWORD);
  r.add("booking.auth.second.username",()->"http-b");r.add("booking.auth.second.password",()->PASSWORD);
 }
 @LocalServerPort int port;
 @Test void parallelReadsKeepCsrfSessionUsableAndCookieDoesNotAuthenticate() throws Exception {
  var client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
  String auth="Basic "+Base64.getEncoder().encodeToString(("http-a:"+PASSWORD).getBytes(StandardCharsets.UTF_8));
  String base="http://localhost:"+port;
  var csrf=client.send(HttpRequest.newBuilder(URI.create(base+"/api/csrf")).header("Authorization",auth).GET().build(),HttpResponse.BodyHandlers.ofString());
  assertThat(csrf.statusCode()).isEqualTo(200);
  String token=new ObjectMapper().readTree(csrf.body()).get("token").asText();
  var a=client.sendAsync(HttpRequest.newBuilder(URI.create(base+"/api/me")).header("Authorization",auth).GET().build(),HttpResponse.BodyHandlers.ofString());
  var b=client.sendAsync(HttpRequest.newBuilder(URI.create(base+"/api/spaces")).header("Authorization",auth).GET().build(),HttpResponse.BodyHandlers.ofString());
  assertThat(a.join().statusCode()).isEqualTo(200);assertThat(b.join().statusCode()).isEqualTo(200);
  for(int i=0;i<2;i++){
   var invalid=client.send(HttpRequest.newBuilder(URI.create(base+"/api/bookings")).header("Authorization",auth).header("X-CSRF-TOKEN",token).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString("{}")).build(),HttpResponse.BodyHandlers.ofString());
   assertThat(invalid.statusCode()).isEqualTo(400);
  }
  assertThat(client.send(HttpRequest.newBuilder(URI.create(base+"/api/me")).GET().build(),HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(401);
 }
}
