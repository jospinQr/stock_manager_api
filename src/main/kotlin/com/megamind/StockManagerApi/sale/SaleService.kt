package com.megamind.StockManagerApi.sale

import com.megamind.StockManagerApi.customer.CustomerRepository
import com.megamind.StockManagerApi.product.ProductRepository
import com.megamind.StockManagerApi.stock_mouvement.MovementRequestDTO
import com.megamind.StockManagerApi.stock_mouvement.MovementType
import com.megamind.StockManagerApi.stock_mouvement.StockService
import com.megamind.StockManagerApi.user.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SaleService(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val userRepository: UserRepository,
    private val stockService: StockService // <- ic
) {

    fun createSale(
        customerId: Long?,
        items: List<SaleItemRequestDTO>,
        currentUsername: String
    ): SaleResponseDTO {
        // Récupérer l'utilisateur courant
        val user = userRepository.findByUsername(currentUsername).orElseThrow {
            throw IllegalArgumentException("Utilisateur non trouvé")
        }

        // Récupérer le client (optionnel)
        val customer = customerId?.let {
            customerRepository.findById(it).orElseThrow { IllegalArgumentException("Client non trouvé") }
        }

        // Créer la vente
        val sale = Sale(
            date = LocalDateTime.now(),
            customer = customer,
            paymentStatus = PaymentStatus.PAID,
            createdBy = user
        )


        // Traiter les articles
        val saleItems = items.map { dto ->
            val product = productRepository.findById(dto.productId)
                .orElseThrow { IllegalArgumentException("Produit ${dto.productId} introuvable") }

            if (product.quantityInStock < dto.quantity) {
                throw IllegalStateException("Stock insuffisant pour le produit ${product.name}")
            }



            stockService.createMovement(
                MovementRequestDTO(
                    productId = product.id,
                    userId = user.id,
                    quantity = dto.quantity,
                    type = MovementType.SALE,
                    sourceDocument = "VENTE-${sale.id ?: "temp"}", // si ID pas encore défini
                    notes = "Mouvement généré automatiquement lors de la vente"
                )
            )

            SaleItem(
                product = product,
                quantity = dto.quantity,
                unitPrice = dto.unitPrice.toBigDecimal(),
                discount = dto.unitPrice.toBigDecimal() * dto.quantity.toBigDecimal(),
                sale = sale
            )
        }.toMutableList()

        sale.items.addAll(saleItems)

        // Sauvegarder la vente
        val savedSale = saleRepository.save(sale)

        // Mapper vers un DTO de réponse
        return savedSale.toResponseDTO()
    }

    fun getSaleById(id: Long): SaleResponseDTO {
        val sale = saleRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Vente $id non trouvée") }

        return sale.toResponseDTO()
    }

    fun listAllSales(): List<SaleResponseDTO> {
        return saleRepository.findAll()
            .map { it.toResponseDTO() }
    }


    private fun Sale.toResponseDTO() = SaleResponseDTO(
        id = this.id,
        date = this.date,
        customerName = this.customer?.name,
        items = this.items.map {
            SaleItemDTO(
                productName = it.product.name,
                quantity = it.quantity,
                unitPrice = it.unitPrice.toDouble(),
                total = it.discount.toDouble()
            )
        },
        totalAmount = this.items.sumOf { it.discount.toDouble() },
        paymentStatus = this.paymentStatus,
        createdBy = this.createdBy
    )
}
