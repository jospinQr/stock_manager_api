package com.megamind.StockManagerApi.sale

import com.megamind.StockManagerApi.customer.CustomerRepository
import com.megamind.StockManagerApi.product.ProductRepository
import com.megamind.StockManagerApi.stock_mouvement.MovementRequestDTO
import com.megamind.StockManagerApi.stock_mouvement.MovementType
import com.megamind.StockManagerApi.stock_mouvement.StockService
import com.megamind.StockManagerApi.user.UserDTO
import com.megamind.StockManagerApi.user.UserRepository
import com.megamind.StockManagerApi.utlis.PaginatedResponse
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.core.context.SecurityContextHolder
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
    ): SaleResponseDTO {

        // Récupérer le client (optionnel)
        val customer = customerId?.let {
            customerRepository.findById(it).orElseThrow { IllegalArgumentException("Client non trouvé") }
        }

        // Récupérer le user
        val username = SecurityContextHolder.getContext().authentication.name

        // Créer la vente
        val sale = Sale(
            date = LocalDateTime.now(),
            customer = customer,
            paymentStatus = PaymentStatus.PAID,
            createdBy = username

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
                    quantity = dto.quantity,
                    type = MovementType.VENTE,
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

    fun findAll(): List<SaleResponseDTO> {
        return saleRepository.findAll()
            .map { it.toResponseDTO() }
    }

    fun findPaginatedSale(pageable: Pageable): PaginatedResponse<SaleResponseDTO> {
        val salesPage: Page<SaleResponseDTO> = saleRepository.findAll(pageable).map { it.toResponseDTO() }

        return PaginatedResponse(
            content = salesPage.content,
            totalElements = salesPage.totalElements,
            pageNumber = salesPage.number,
            pageSize = salesPage.size,
            totalPages = salesPage.totalPages


        )
    }


    fun findByDateBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): PaginatedResponse<SaleResponseDTO> {
        val salesPage = saleRepository.findByDateBetween(startDate, endDate, pageable)
            .map { it.toResponseDTO() }

        return PaginatedResponse(
            content = salesPage.content,
            totalElements = salesPage.totalElements,
            pageNumber = salesPage.number,
            pageSize = salesPage.size,
            totalPages = salesPage.totalPages


        )

    }


    fun findbyProduct(productId: Long, pageable: Pageable): PaginatedResponse<SaleResponseDTO> {

        val existProduct = productRepository.findById(productId).orElseThrow {
            EntityNotFoundException()
        }
        val salesPage = saleRepository.findByProduct(existProduct, pageable)
            .map { it.toResponseDTO() }
        return PaginatedResponse(
            content = salesPage.content,
            totalElements = salesPage.totalElements,
            pageNumber = salesPage.number,
            pageSize = salesPage.size,
            totalPages = salesPage.totalPages


        )
    }

    fun getTop10Products(): List<TopSaleProductDto> {
        val pageable = PageRequest.of(0, 10)
        return saleRepository.findTopSellingProducts(pageable)

    }

    private fun Sale.toResponseDTO() = SaleResponseDTO(
        id = this.id,
        date = this.date,
        customerName = this.customer?.name ?: "",
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
