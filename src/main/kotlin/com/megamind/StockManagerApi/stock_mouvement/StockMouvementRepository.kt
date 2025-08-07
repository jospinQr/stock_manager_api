package com.megamind.StockManagerApi.stock_mouvement

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime


interface StockMovementRepository : JpaRepository<StockMovement, Long> {

    fun findByProductIdOrderByMovementDateAsc(productId: Long): List<StockMovement>


    fun findByProductIdAndMovementDateBetweenOrderByMovementDateAsc(
        productId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<StockMovement>

}