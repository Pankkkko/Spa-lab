package cl.duoc.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

        JwtGrantedAuthoritiesConverter scopesConverter =
            new JwtGrantedAuthoritiesConverter();

        scopesConverter.setAuthorityPrefix("SCOPE_");
        scopesConverter.setAuthoritiesClaimName("scp");

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
            jwt -> {

                Collection<GrantedAuthority> authorities =
                    new ArrayList<>(
                        scopesConverter.convert(jwt)
                    );

                List<String> roles =
                    jwt.getClaimAsStringList("roles");

                if (roles != null) {
                    for (String role : roles) {
                        authorities.add(
                            new SimpleGrantedAuthority(
                                "ROLE_" + role
                            )
                        );
                    }
                }

                return authorities;
            }
        );

        return converter;
    }

    @Bean
    SecurityFilterChain security(
        HttpSecurity http,
        JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        return http
            .csrf(csrf -> csrf.disable())

            .cors(Customizer.withDefaults())

            .sessionManagement(
                s -> s.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // =================================================
                // PEDIDOS DEL CLIENTE
                // Cliente y Admin pueden consultar pedidos de cliente
                // =================================================

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/pedidos/cliente/**"
                )
                .hasAnyRole("Admin", "Cliente")


                // =================================================
                // CLIENTES
                // Solo Admin
                // =================================================

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/clientes/**"
                )
                .hasRole("Admin")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/clientes"
                )
                .hasRole("Admin")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/clientes/**"
                )
                .hasRole("Admin")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/clientes/**"
                )
                .hasRole("Admin")


                // =================================================
                // PEDIDOS
                // Solo Admin para gestión general
                // =================================================

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/pedidos"
                )
                .hasRole("Admin")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/pedidos/{id}"
                )
                .hasRole("Admin")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/pedidos"
                )
                .hasRole("Admin")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/pedidos/**"
                )
                .hasRole("Admin")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/pedidos/**"
                )
                .hasRole("Admin")


                // =================================================
                // TODO LO DEMÁS
                // =================================================

                .anyRequest()
                .denyAll()
            )

            .oauth2ResourceServer(
                oauth -> oauth
                    .jwt(
                        jwt -> jwt
                            .jwtAuthenticationConverter(
                                jwtAuthenticationConverter
                            )
                    )
            )

            .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
            new CorsConfiguration();

        configuration.setAllowedOrigins(
            List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type"
            )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            configuration
        );

        return source;
    }
}