package com.megamind.StockManagerApi.product


// ProductDTOs.kt
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import java.time.LocalDateTime

// DTO pour afficher un produit
data class ProductResponseDTO(
    val id: Long,
    val name: String,
    val barcode: String?,
    val description: String?,
    val price: BigDecimal,
    val quantityInStock: Int,
    val lowStockAlert: Int,
    val categoryName: String, // On affiche juste le nom de la catégorie
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

// DTO pour créer ou mettre à jour un produit
data class ProductRequestDTO(
    @field:NotBlank(message = "Product name cannot be blank")
    val name: String,
    val barcode: String?,
    val description: String?,
    @field:PositiveOrZero(message = "Price must be positive or zero")
    val price: BigDecimal,
    @field:PositiveOrZero(message = "Quantity in stock must be positive or zero")
    val quantityInStock: Int,
    val lowStockAlert: Int,
    val categoryId: Long // On passe juste l'ID de la catégorie
)


// DTO pour créer ou mettre à jour un attribut specifique un produit
data class ProductPatchDTO(
    val name: String? = null,
    val barcode: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val quantityInStock: Int? = null,
    val lowStockAlert: Int? = null,
    val categoryId: Long? = null
)