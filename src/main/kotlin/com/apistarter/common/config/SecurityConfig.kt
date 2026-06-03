package com.apistarter.common.config

import com.apistarter.auth.security.JwtAuthenticationFilter
import com.apistarter.common.response.ApiResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: ObjectProvider<JwtAuthenticationFilter>,
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/actuator/health",
                        "/actuator/info",
                        "/api/v1/health",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                    ).permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/auth/register",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/logout",
                    ).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated()
            }
            .httpBasic { basic -> basic.disable() }
            .formLogin { form -> form.disable() }
            .logout { logout -> logout.disable() }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { request, response, _ ->
                        response.status = HttpStatus.UNAUTHORIZED.value()
                        response.contentType = MediaType.APPLICATION_JSON_VALUE
                        objectMapper.writeValue(
                            response.writer,
                            ApiResponse.failure(
                                message = "Authentication required",
                                path = request.requestURI,
                            ),
                        )
                    }
                    .accessDeniedHandler { request, response, _ ->
                        response.status = HttpStatus.FORBIDDEN.value()
                        response.contentType = MediaType.APPLICATION_JSON_VALUE
                        objectMapper.writeValue(
                            response.writer,
                            ApiResponse.failure(
                                message = "Access denied",
                                path = request.requestURI,
                            ),
                        )
                    }
            }

        jwtAuthenticationFilter.ifAvailable { filter ->
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter::class.java)
        }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
