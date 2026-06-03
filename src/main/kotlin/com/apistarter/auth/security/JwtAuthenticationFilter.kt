package com.apistarter.auth.security

import com.apistarter.user.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = request.getBearerToken()
        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            val claims = jwtService.validateAccessToken(token)
            if (claims != null) {
                val user = userRepository.findWithRolesById(claims.userId).orElse(null)
                if (user != null && user.enabled) {
                    val principal = UserPrincipal.from(user)
                    SecurityContextHolder.getContext().authentication =
                        UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.authorities,
                        )
                }
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun HttpServletRequest.getBearerToken(): String? {
        val header = getHeader("Authorization") ?: return null
        if (!header.startsWith("Bearer ", ignoreCase = true)) return null
        return header.substringAfter(" ").trim().takeIf { it.isNotBlank() }
    }
}
