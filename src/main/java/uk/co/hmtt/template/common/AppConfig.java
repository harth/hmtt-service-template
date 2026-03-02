package uk.co.hmtt.template.common;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Application-wide configuration: Spring Security and Micrometer aspects.
 *
 * <p><strong>Security note:</strong> the in-memory user defined here is for local development only.
 * Replace with OAuth 2 / JWT authentication before deploying to a real environment.
 */
@Configuration
@EnableWebSecurity
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class AppConfig {

  private static final String[] PUBLIC_PATHS = {"/swagger-ui/**", "/v3/api-docs/**"};

  @Bean
  @Order(1)
  public SecurityFilterChain managementSecurityFilterChain(final HttpSecurity http)
      throws Exception {
    http.securityMatcher(EndpointRequest.toAnyEndpoint())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class))
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(PUBLIC_PATHS).permitAll().anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  // TODO: Replace with a proper UserDetailsService backed by a database or IdP.
  @Bean
  public UserDetailsService userDetailsService(final PasswordEncoder encoder) {
    final UserDetails user =
        User.builder().username("user").password(encoder.encode("password")).roles("USER").build();
    return new InMemoryUserDetailsManager(user);
  }

  @Bean
  public static PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /** Enables the {@code @Timed} annotation on Spring beans. */
  @Bean
  public TimedAspect timedAspect(final MeterRegistry registry) {
    return new TimedAspect(registry);
  }
}
