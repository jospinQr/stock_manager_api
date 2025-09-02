package com.megamind.StockManagerApi.user


import com.megamind.StockManagerApi.JwtService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {


    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        if (userRepository.findByUsername(request.username).isPresent) {
            return ResponseEntity.badRequest().body(AuthResponse("Username déjà utilisé"))
        }

        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            role = request.role,
        )

        userRepository.save(user)

        val token = jwtService.generateToken(user.username, user.role)
        return ResponseEntity.ok(AuthResponse(token))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val user = userRepository.findByUsername(request.username)
            .orElse(null)

        if (user == null || !passwordEncoder.matches(request.password, user.password)) {
            return ResponseEntity.badRequest().body(AuthResponse("Nom ou mot de passe incorrect"))
        }


        val token = jwtService.generateToken(user.username, user.role)
        return ResponseEntity.ok(AuthResponse(token))
    }
}

data class RegisterRequest(
    val username: String,
    val password: String,
    val role: Role,

    )

data class LoginRequest(
    val username: String,
    val password: String,

    )

data class AuthResponse(val token: String)
