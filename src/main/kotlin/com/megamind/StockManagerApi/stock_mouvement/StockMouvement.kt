package com.megamind.StockManagerApi.stock_mouvement

// StockMovement.kt
import com.megamind.StockManagerApi.product.Product
import com.megamind.StockManagerApi.user.User
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
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


    val createBy: String?=null,

    val notes: String? = null
)

// MovementType.kt
enum class MovementType {


    ENTREE,
    SORTIE,// Entrée de stock (approvisionnement fournisseur)
    VENTE,            // Sortie de stock (vente client)
    ACHAT,
    AJUSTEMENT_INVENTAIRE_PLUS, // Ajustement d'inventaire positif
    AJUSTEMENT_INVENTAIRE_MOINS,// Ajustement d'inventaire négatif
    PERT,
    RETOUR_CLIENT

}