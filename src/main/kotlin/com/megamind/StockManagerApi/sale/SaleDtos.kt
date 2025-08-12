package com.megamind.StockManagerApi.sale

import com.megamind.StockManagerApi.user.User
import com.megamind.StockManagerApi.user.UserDTO
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDateTime

data class SaleItemRequestDTO(
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double
)


// DTO pour la requête de création de vente
data class SaleRequestDTO(
    val customerId: Long?, // Le client est optionnel
    @field:NotEmpty(message = "La liste des articles ne peut pas être vide.")
    val items: List<SaleItemRequestDTO>,

)


data class SaleResponseDTO(
    val id: Long,
    val date: LocalDateTime,
    val customerName: String?,
    val items: List<SaleItemDTO>,
    val totalAmount: Double,
    val paymentStatus: PaymentStatus,
    val createdBy: UserDTO
)

data class SaleItemDTO(
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double
)


