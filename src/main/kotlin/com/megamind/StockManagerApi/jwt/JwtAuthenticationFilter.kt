package com.megamind.StockManagerApi


import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.filter.OncePerRequestFilter
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Aucun token ou mal formé → continuer sans bloquer
            filterChain.doFilter(request, response)
            return
        }

        try {
            val jwt = authHeader.removePrefix("Bearer ").trim()
            val username = jwtService.extractUsername(jwt)

            if (username != null && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = userDetailsService.loadUserByUsername(username)

                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )

                SecurityContextHolder.getContext().authentication = authToken
            }

        } catch (e: Exception) {
            println("JWT exception: ${e.message}") // utile en debug
            // Ne bloque pas la requête si le token est mauvais
        }

        filterChain.doFilter(request, response)
    }
}
