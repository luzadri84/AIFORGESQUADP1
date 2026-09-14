package local.booking.security;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import static org.assertj.core.api.Assertions.*;
class JwtValidationTest {
 Jwt jwt(String issuer,String audience,String sub,Instant expiry){return Jwt.withTokenValue("test-claims-only").header("alg","RS256").issuer(issuer).subject(sub).audience(List.of(audience)).issuedAt(Instant.now().minusSeconds(600)).expiresAt(expiry).build();}
 @Test void validatesIssuerAudienceSubjectAndTime(){
  var validator=JwtSecurityConfig.validator("https://issuer.example","booking");
  assertThat(validator.validate(jwt("https://issuer.example","booking","person",Instant.now().plusSeconds(60))).hasErrors()).isFalse();
  for(var invalid:List.of(jwt("https://wrong.example","booking","person",Instant.now().plusSeconds(60)),jwt("https://issuer.example","other","person",Instant.now().plusSeconds(60)),jwt("https://issuer.example","booking","",Instant.now().plusSeconds(60)),jwt("https://issuer.example","booking","person",Instant.now().minusSeconds(300))))assertThat(validator.validate(invalid).hasErrors()).isTrue();
 }
 @Test void enterpriseIdentityIsStableBoundedAndSeparate(){
  var a=jwt("https://issuer.example","booking","person",Instant.now().plusSeconds(60));
  assertThat(JwtSecurityConfig.principalName(a)).hasSize(68).startsWith("jwt:").isEqualTo(JwtSecurityConfig.principalName(a));
  assertThat(JwtSecurityConfig.principalName(a)).isNotEqualTo(JwtSecurityConfig.principalName(jwt("https://other.example","booking","person",Instant.now().plusSeconds(60))));
 }
}
