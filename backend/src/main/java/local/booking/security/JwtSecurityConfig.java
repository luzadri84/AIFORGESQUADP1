package local.booking.security;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
@Configuration @ConditionalOnProperty(name="booking.auth.mode",havingValue="jwt")
public class JwtSecurityConfig {
 @Bean JwtDecoder decoder(@Value("${booking.jwt.issuer}") String issuer,@Value("${booking.jwt.jwk-set-uri}") String jwks,@Value("${booking.jwt.audience}") String audience){
  var decoder=NimbusJwtDecoder.withJwkSetUri(jwks).build();decoder.setJwtValidator(validator(issuer,audience));return decoder;
 }
 public static OAuth2TokenValidator<Jwt> validator(String issuer,String audience){
  return new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer),
   new JwtClaimValidator<List<String>>("aud",aud -> aud!=null && aud.contains(audience)),
   new JwtClaimValidator<String>("sub",sub -> sub!=null && !sub.isBlank()));
 }
 public static String principalName(Jwt jwt){
  try{return "jwt:"+HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((jwt.getIssuer()+"\n"+jwt.getSubject()).getBytes(StandardCharsets.UTF_8)));}
  catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
 }
 @Bean SecurityFilterChain jwtSecurity(HttpSecurity http,JwtDecoder decoder)throws Exception{
  http.headers(h -> h.withObjectPostProcessor(EagerSecurityHeaders.processor()))
            .authorizeHttpRequests(a->a.requestMatchers("/api/csrf").permitAll().anyRequest().authenticated())
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .requestCache(c->c.disable())
   .csrf(c->c.sessionAuthenticationStrategy(new org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy()))
   .oauth2ResourceServer(o->o.jwt(j->j.decoder(decoder).jwtAuthenticationConverter(jwt->new JwtAuthenticationToken(jwt,List.of(),principalName(jwt)))));
  return http.build();
 }
}
