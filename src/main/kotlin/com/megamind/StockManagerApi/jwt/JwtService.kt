package com.megamind.StockManagerApi

import com.megamind.StockManagerApi.user.Role
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey


@Service
class JwtService {

    private val secret = "9D6FC88ACBDB8D26B4D0F4E78E9F89A7" // Doit être une clé en base64 ou assez longue
    private val key: SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(Base64.getEncoder().encodeToString(secret.toByteArray())))

    fun generateToken(username: String, role: Role): String {
        return Jwts.builder()
            .subject(username)
            .claim("role", role)

            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1h
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun extractUsername(token: String): String? {
        return try {
            val claims = Jwts.parser()  // plus de parserBuilder()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload

            claims.subject
        } catch (e: Exception) {
            null
        }
    }
}

