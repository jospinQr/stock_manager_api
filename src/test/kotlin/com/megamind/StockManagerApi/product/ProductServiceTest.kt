package com.megamind.StockManagerApi.product

import com.megamind.StockManagerApi.category.Category
import com.megamind.StockManagerApi.category.CategoryRepository
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class ProductServiceTest {

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @InjectMocks
    private lateinit var productService: ProductService

    private lateinit var category: Category
    private lateinit var product: Product

    @BeforeEach
    fun setup() {
        category = Category(id = 1L, name = "Tech")
        product = Product(
            id = 1L,
            name = "Laptop",
            barcode = "ABC123",
            description = "Gaming Laptop",
            price = 1200.0.toBigDecimal(),
            quantityInStock = 10,
            lowStockAlert = 3,
            category = category,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }


    @Test
    fun `should create product successfully when category exists`() {
        // 1. Arrange
        val request = ProductRequestDTO(
            name = "Laptop",
            barcode = "ABC123",
            description = "Gaming Laptop",
            price = 1200.0.toBigDecimal(),
            quantityInStock = 10,
            lowStockAlert = 3,
            categoryId = 1L
        )

        val category = Category(id = 1L, name = "Electronics")
        val expectedSavedProduct = Product(
            id = 1L, // Simulate generated ID
            name = "Laptop",
            barcode = "ABC123",
            description = "Gaming Laptop",
            price = 1200.0.toBigDecimal(),
            quantityInStock = 10,
            lowStockAlert = 3,
            category = category
        )

        // Configure mocks to return non-null values
        whenever(categoryRepository.findById(1L)).thenReturn(Optional.of(category))
        whenever(productRepository.save(any())).thenAnswer { invocation ->
            val productToSave = invocation.getArgument<Product>(0)
            productToSave.copy(id = 1L) // Simulate saving with generated ID
        }

        // 2. Act
        val response = productService.createProduct(request)

        // 3. Assert
        assertNotNull(response)
        assertEquals(1L, response.id)
        assertEquals("Laptop", response.name)

        verify(productRepository).save(any())
    }



    @Test
    fun `should throw when category not found in createProduct`() {
        val request = ProductRequestDTO(
            name = "Laptop",
            barcode = "ABC123",
            description = "Gaming Laptop",
            price = 1200.0.toBigDecimal(),
            quantityInStock = 10,
            lowStockAlert = 3,
            categoryId = 999L
        )

        whenever(categoryRepository.findById(999L)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
            productService.createProduct(request)
        }
    }

    @Test
    fun `should add stock correctly`() {
        whenever(productRepository.findById(1L)).thenReturn(Optional.of(product))
        whenever(productRepository.save(any())).thenReturn(product.copy(quantityInStock = 15))

        val result = productService.addStock(1L, 5)

        assertEquals(15, result.quantityInStock)
        verify(productRepository).save(any())
    }

    @Test
    fun `should throw exception when removing too much stock`() {
        whenever(productRepository.findById(1L)).thenReturn(Optional.of(product))

        assertThrows<IllegalStateException> {
            productService.removeStock(1L, 20)
        }
    }

    @Test
    fun `should update product`() {
        val request = ProductRequestDTO(
            name = "Updated Laptop",
            barcode = "ABC123",
            description = "Gaming Laptop Pro",
            price = 1500.0.toBigDecimal(),
            quantityInStock = 5,
            lowStockAlert = 2,
            categoryId = 1L
        )

        whenever(productRepository.findById(1L)).thenReturn(Optional.of(product))
        whenever(productRepository.findByNameIgnoreCase("Updated Laptop")).thenReturn(Optional.empty())
        whenever(productRepository.findByBarcode("ABC123")).thenReturn(Optional.of(product))
        whenever(categoryRepository.findById(1L)).thenReturn(Optional.of(category))
        whenever(productRepository.save(any())).thenReturn(product.copy(name = "Updated Laptop"))

        val result = productService.updateProduct(1L, request)

        assertEquals("Updated Laptop", result.name)
        verify(productRepository).save(any())
    }

    @Test
    fun `should throw exception if barcode already used by another`() {
        val otherProduct = product.copy(id = 2L)
        val request = ProductRequestDTO(
            name = "Laptop",
            barcode = "DUPLICATE",
            description = "Desc",
            price = 1000.0.toBigDecimal(),
            quantityInStock = 5,
            lowStockAlert = 2,
            categoryId = 1L
        )

        whenever(productRepository.findById(1L)).thenReturn(Optional.of(product))
        whenever(productRepository.findByNameIgnoreCase("Laptop")).thenReturn(Optional.empty())
        whenever(productRepository.findByBarcode("DUPLICATE")).thenReturn(Optional.of(otherProduct))

        assertThrows<DataIntegrityViolationException> {
            productService.updateProduct(1L, request)
        }
    }

    @Test
    fun `should find product by id`() {
        whenever(productRepository.findById(1L)).thenReturn(Optional.of(product))

        val result = productService.findById(1L)

        assertEquals(product.name, result.name)
    }

    @Test
    fun `should delete product`() {
        whenever(productRepository.existsById(1L)).thenReturn(true)
        doNothing().`when`(productRepository).deleteById(1L)

        productService.deleteProduct(1L)

        verify(productRepository).deleteById(1L)
    }

    @Test
    fun `should throw when deleting non-existing product`() {
        whenever(productRepository.existsById(1L)).thenReturn(false)

        assertThrows<EntityNotFoundException> {
            productService.deleteProduct(1L)
        }
    }
}
