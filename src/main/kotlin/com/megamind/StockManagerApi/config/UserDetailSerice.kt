package com.megamind.StockManagerApi.user


import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.userdetails.User as SecurityUser
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("Utilisateur non trouvé") }

        return SecurityUser
            .withUsername(user.username)
            .password(user.password)
            .roles(user.role.name)
            .build()
    }
}
