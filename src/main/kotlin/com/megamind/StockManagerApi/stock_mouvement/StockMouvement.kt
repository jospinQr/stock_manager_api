package com.megamind.StockManagerApi.stock_mouvement

// StockMovement.kt
import com.megamind.StockManagerApi.product.Product
import com.megamind.StockManagerApi.user.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
data class StockMovement(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    val product: Product,

    // La quantité du mouvement.
    // Positive pour une entrée, négative pour une sortie.
    @Column(nullable = false)
    val quantity: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MovementType,

    @Column(nullable = false, updatable = false)
    val movementDate: LocalDateTime = LocalDateTime.now(),

    // Optionnel : lien vers un document (ex: "FACTURE-00123", "BC-456")
    val sourceDocument: String? = null,

    //  utilisateur ayant effectué le mouvement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    val notes: String? = null
)

// MovementType.kt
enum class MovementType {
    SUPPLY,          // Entrée de stock (approvisionnement fournisseur)
    SALE,            // Sortie de stock (vente client)
    CUSTOMER_RETURN, // Retour client (entrée de stock)
    SUPPLIER_RETURN, // Retour fournisseur (sortie de stock)
    INVENTORY_ADJUSTMENT_PLUS, // Ajustement d'inventaire positif
    INVENTORY_ADJUSTMENT_MINUS,// Ajustement d'inventaire négatif
    WASTAGE           // Perte / Casse (sortie de stock)
}