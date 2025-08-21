package com.megamind.StockManagerApi.stock_mouvement

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime


interface StockMovementRepository : JpaRepository<StockMovement, Long> {

    fun findByProductIdOrderByMovementDateAsc(productId: Long): List<StockMovement>


    fun findByProductIdAndMovementDateBetweenOrderByMovementDateAsc(
        productId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<StockMovement>

    // Méthode générique pour filtrer par types de mouvement avec pagination
    fun findByTypeInOrderByMovementDateDesc(
        types: List<MovementType>,
        pageable: Pageable
    ): Page<StockMovement>

    // Méthodes pour récupérer les mouvements par période
    fun findByTypeInAndMovementDateBetweenOrderByMovementDateDesc(
        types: List<MovementType>,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<StockMovement>

    // Méthode pour compter les mouvements par période
    fun countByTypeInAndMovementDateBetween(
        types: List<MovementType>,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Long
}