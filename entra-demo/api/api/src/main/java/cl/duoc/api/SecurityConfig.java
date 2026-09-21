package cl.duoc.api;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(
            "https://login.microsoftonline.com/49551105-7651-4748-a01b-22b28daeb087/v2.0"
        );
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
        scopesConverter.setAuthorityPrefix("SCOPE_");
        scopesConverter.setAuthoritiesClaimName("scp");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(scopesConverter);
        return converter;
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ============ GET: requiere pedidos.read ============
                .requestMatchers(HttpMethod.GET, "/api/data")
                    .hasAuthority("SCOPE_pedidos.read")
                .requestMatchers(HttpMethod.GET, "/api/clientes/**")
                    .hasAuthority("SCOPE_pedidos.read")
                .requestMatchers(HttpMethod.GET, "/api/pedidos/**")
                    .hasAuthority("SCOPE_pedidos.read")

                // ============ POST: requiere pedidos.write ============
                .requestMatchers(HttpMethod.POST, "/api/clientes")
                    .hasAuthority("SCOPE_pedidos.write")
                .requestMatchers(HttpMethod.POST, "/api/pedidos")
                    .hasAuthority("SCOPE_pedidos.write")

                // ============ PUT: requiere pedidos.write ============
                .requestMatchers(HttpMethod.PUT, "/api/clientes/**")
                    .hasAuthority("SCOPE_pedidos.write")
                .requestMatchers(HttpMethod.PUT, "/api/pedidos/**")
                    .hasAuthority("SCOPE_pedidos.write")

                // ============ DELETE: requiere pedidos.write ============
                .requestMatchers(HttpMethod.DELETE, "/api/clientes/**")
                    .hasAuthority("SCOPE_pedidos.write")
                .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**")
                    .hasAuthority("SCOPE_pedidos.write")

                // Todo lo demás denegado
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            )
            .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}