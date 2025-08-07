package com.megamind.StockManagerApi.product
// Product.kt
import com.megamind.StockManagerApi.category.Category
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class) // Active l'audit JPA
data class Product(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank(message = "Le nom du produit ne doit pas être vide")
    @Column(nullable = false, unique = true)
    val name: String,

    @Column(unique = true)
    val barcode: String?,
    val description: String?,

    @field:PositiveOrZero(message = "Price doit être positif")
    @Column(nullable = false, precision = 19, scale = 4) // Précision pour BigDecimal
    val price: BigDecimal,

    @field:PositiveOrZero(message = "La quantité en stock doit être positif ou 0")
    @Column(nullable = false)
    val quantityInStock: Int,

    @field:PositiveOrZero(message = "Low stock alert must be positive or zero")
    val lowStockAlert: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null,
)

