package com.megamind.StockManagerApi.stock_mouvement

import com.fasterxml.jackson.annotation.JsonFormat
import com.megamind.StockManagerApi.user.User
import java.time.LocalDateTime

data class MovementRequestDTO(
    val productId: Long,
    val quantity: Int,
    val type: MovementType,
    val sourceDocument: String? = null,
    val notes: String? = null,
)


// DTO pour afficher un mouvement de stock dans les réponses API
data class StockMovementResponseDTO(
    val id: Long,
    val productId: Long,
    val productName: String, // Utile pour l'affichage côté client
    val quantity: Int,
    val type: MovementType,
    @JsonFormat(pattern = "dd-MM-yyyy'T'HH:mm:ss") // Formater la date pour la cohérence
    val movementDate: LocalDateTime,
    val sourceDocument: String?,
    val notes: String?,
    val createBy: String?
)

// DTO pour afficher une fiche de stock dans les réponses API


data class StockMovementLineDTO(
    val date: LocalDateTime,
    val type: MovementType,
    val quantity: Int,
    val stockBefore: Int,
    val stockAfter: Int,
    val sourceDocument: String?,
    val notes: String?,
    val createBy: String? = null
)

// DTO pour la pagination par période
data class PeriodPaginationRequest(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val pageSize: Int = 20
)

// DTO pour la réponse paginée par période
data class PeriodPaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPeriod: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val pageSize: Int,
    val hasNextPeriod: Boolean,
    val hasPreviousPeriod: Boolean
)