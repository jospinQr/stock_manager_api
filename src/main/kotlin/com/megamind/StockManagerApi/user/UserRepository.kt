package com.megamind.StockManagerApi.user

import com.megamind.StockManagerApi.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>
}
