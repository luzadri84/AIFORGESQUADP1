package local.booking.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.NullSecurityContextRepository;

@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="booking.auth.mode",havingValue="basic",matchIfMissing=true)
public class SecurityConfig {
    @Bean UserDetailsService users(@Value("${booking.auth.first.username}") String first,
            @Value("${booking.auth.first.password}") String firstPassword,
            @Value("${booking.auth.second.username}") String second,
            @Value("${booking.auth.second.password}") String secondPassword) {
        if (first.equals(second) || firstPassword.length()<16 || secondPassword.length()<16)
            throw new IllegalStateException("Dos usuarios distintos y claves externas de al menos 16 caracteres requeridos");
        var encoder=new BCryptPasswordEncoder();
        return new InMemoryUserDetailsManager(
            User.withUsername(first).password("{bcrypt}"+encoder.encode(firstPassword)).roles("USER").build(),
            User.withUsername(second).password("{bcrypt}"+encoder.encode(secondPassword)).roles("USER").build());
    }
    @Bean SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a.requestMatchers("/api/csrf").permitAll().anyRequest().authenticated())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(s -> s.securityContextRepository(new NullSecurityContextRepository()))
            .requestCache(c -> c.disable())
            .csrf(c -> c.sessionAuthenticationStrategy(new org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy()))
            .httpBasic(b -> b.authenticationEntryPoint((req,res,e) -> problem(res,401,"Autenticación requerida")))
            .exceptionHandling(e -> e.authenticationEntryPoint((req,res,x) -> problem(res,401,"Autenticación requerida"))
                .accessDeniedHandler((req,res,x) -> problem(res,403,"Acceso denegado o token CSRF inválido")));
        // Basic is revalidated every request; do not rotate its CSRF-only session token on every authentication.
        // XOR validation and the HttpSession CSRF repository remain enabled.
        return http.build();
    }
    private static void problem(jakarta.servlet.http.HttpServletResponse res,int status,String title) throws java.io.IOException {
        res.setStatus(status); res.setContentType("application/problem+json"); res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\":"+status+",\"title\":\""+title+"\"}");
    }
}
