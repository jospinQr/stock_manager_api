import com.megamind.StockManagerApi.category.Category
import com.megamind.StockManagerApi.product.Product
import com.megamind.StockManagerApi.product.ProductRepository
import com.megamind.StockManagerApi.stock_mouvement.MovementRequestDTO
import com.megamind.StockManagerApi.stock_mouvement.MovementType
import com.megamind.StockManagerApi.stock_mouvement.StockMovement
import com.megamind.StockManagerApi.stock_mouvement.StockMovementRepository
import com.megamind.StockManagerApi.stock_mouvement.StockService
import com.megamind.StockManagerApi.user.User
import com.megamind.StockManagerApi.user.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class StockServiceTest {

    private val movementRepository: StockMovementRepository = mock()
    private val productRepository: ProductRepository = mock()
    private val userRepository : UserRepository = mock()
    private val stockService = StockService(
        movementRepository, productRepository,userRepository

    )

    private val defaultCategory = Category(id = 1L, name = "Catégorie test") // À adapter selon ton entité

    private fun createValidProduct(quantity: Int): Product {
        return Product(
            id = 1L,
            name = "Produit Test",
            barcode = "1234567890",
            description = "Un bon produit",
            price = BigDecimal("100.00"),
            quantityInStock = quantity,
            lowStockAlert = 5,
            category = defaultCategory,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `createMovement should save movement on SUPPLY`() {
        val product = createValidProduct(5)
        val request = MovementRequestDTO(
            productId = 1L,
            quantity = 1,
            type = MovementType.SALE,
            sourceDocument = "BL123",
            notes = "Réapprovisionnement",
            userId = 1,
        )

        whenever(productRepository.findById(1L)).thenReturn(Optional.of(product))
        whenever(movementRepository.save(any())).thenAnswer { invocation ->
            invocation.getArgument<StockMovement>(0)
        }

        val result = stockService.createMovement(request)

        assertEquals(10, result.quantity)
        assertEquals(MovementType.SUPPLY, result.type)
        verify(movementRepository).save(any())
    }

    @Test
    fun `createMovement should throw when stock insufficient for SALE`() {
        val product = createValidProduct(quantity = 2)

        val request = MovementRequestDTO(
            productId = 1L,
            quantity = 5,
            type = MovementType.SALE,
            sourceDocument = "VENTE01",
            notes = "Vente client",
            userId = 1,
        )

        whenever(productRepository.findById(1L)).thenReturn(Optional.of(product))

        assertThrows<IllegalStateException> {
            stockService.createMovement(request)
        }

        verify(movementRepository, never()).save(any())
    }
}
