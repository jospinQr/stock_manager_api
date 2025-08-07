package com.megamind.StockManagerApi.product

import com.megamind.StockManagerApi.category.Category
import com.megamind.StockManagerApi.category.CategoryRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull


@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {


    // Utilisation de mappers pour convertir entre Entité et DTO
    private fun Product.toResponseDTO(): ProductResponseDTO = ProductResponseDTO(
        id = this.id,
        name = this.name,
        barcode = this.barcode,
        description = this.description,
        price = this.price,
        quantityInStock = this.quantityInStock,
        lowStockAlert = this.lowStockAlert,
        categoryName = this.category.name, // Assurez-vous que category.name est accessible
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )

    fun createProduct(request: ProductRequestDTO): ProductResponseDTO {
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { EntityNotFoundException("Category with id ${request.categoryId} not found.") }

        val product = Product(
            name = request.name,
            barcode = request.barcode,
            description = request.description,
            price = request.price,
            quantityInStock = request.quantityInStock,
            lowStockAlert = request.lowStockAlert,
            category = category
        )
        return productRepository.save(product).toResponseDTO()
    }

    fun delete(productId: Long) {

        if (!productRepository.existsById(productId)) {
            throw EntityNotFoundException("Product with ${productId} don't existe")
        }

        return productRepository.deleteById(productId)

    }


    /**
     * Mettre en jour un attribut specifique du produit
     */

    @Transactional
    fun patchProduct(productId: Long, patch: ProductPatchDTO): ProductResponseDTO {
        val existingProduct = productRepository.findById(productId)
            .orElseThrow { EntityNotFoundException("Product with id $productId not found.") }

        val patchedProduct = existingProduct.copy(

            name = patch.name ?: existingProduct.name,
            barcode = patch.barcode ?: existingProduct.barcode,
            description = patch.description ?: existingProduct.description,
            price = patch.price ?: existingProduct.price,
            quantityInStock = patch.quantityInStock ?: existingProduct.quantityInStock,
            lowStockAlert = patch.lowStockAlert ?: existingProduct.lowStockAlert,

            // Pour la catégorie, c'est un peu plus complexe car c'est un objet.
            category = patch.categoryId?.let { catId ->
                categoryRepository.findById(catId)
                    .orElseThrow { EntityNotFoundException("Category with id $catId not found.") }
            } ?: existingProduct.category
        )

        // **IMPORTANT** : Nous devons ré-appliquer notre logique de validation d'unicité,
        // mais SEULEMENT si le nom ou le code-barres a effectivement été modifié.

        if (patch.name != null && patch.name != existingProduct.name) {
            productRepository.findByNameIgnoreCase(patch.name).ifPresent { foundProduct ->
                if (foundProduct.id != existingProduct.id) {
                    throw DataIntegrityViolationException("Product name '${patch.name}' is already taken.")
                }
            }
        }

        if (patch.barcode != null && patch.barcode != existingProduct.barcode) {
            patch.barcode.takeIf { it.isNotBlank() }?.let { barcode ->
                productRepository.findByBarcode(barcode).ifPresent { foundProduct ->
                    if (foundProduct.id != existingProduct.id) {
                        throw DataIntegrityViolationException("Barcode '$barcode' is already assigned.")
                    }
                }
            }
        }

        return productRepository.save(patchedProduct).toResponseDTO()
    }

    /**
     * Mettre en jour un produit
     */


    @Transactional
    fun updateProduct(productId: Long, request: ProductRequestDTO): ProductResponseDTO {

        // 1. On s'assure que le produit qu'on veut mettre à jour existe bien.
        val existingProduct = productRepository.findById(productId)
            .orElseThrow { EntityNotFoundException("Product with id $productId not found.") }

        // 2. Logique de validation pour les contraintes uniques
        // Vérification du nom
        productRepository.findByNameIgnoreCase(request.name).ifPresent { foundProduct ->
            if (foundProduct.id != existingProduct.id) {
                throw DataIntegrityViolationException("Product name '${request.name}' is already taken by another product.")
            }
        }

        // Vérification du code-barres (uniquement s'il n'est pas nul ou vide)
        request.barcode?.takeIf { it.isNotBlank() }?.let { barcode ->
            productRepository.findByBarcode(barcode).ifPresent { foundProduct ->
                if (foundProduct.id != existingProduct.id) {
                    throw DataIntegrityViolationException("Barcode '$barcode' is already assigned to another product.")
                }
            }
        }

        // 3. Si les validations sont passées, on procède à la mise à jour.
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { EntityNotFoundException("Category with id ${request.categoryId} not found.") }

        val updatedProduct = existingProduct.copy(
            name = request.name,
            barcode = request.barcode,
            description = request.description,
            price = request.price,
            quantityInStock = request.quantityInStock,
            lowStockAlert = request.lowStockAlert,
            category = category
        )

        return productRepository.save(updatedProduct).toResponseDTO()
    }

    /**
     * Touver un produit par son id
     */

    fun findByBarCode(barcode: String): ProductResponseDTO =
        productRepository.findByBarcode(barcode = barcode).orElseThrow {
            EntityNotFoundException("Product with id $barcode not found.")
        }.toResponseDTO()


    /**
     * Touver un produit par son id
     */

    fun findById(id: Long): ProductResponseDTO = productRepository.findById(id)
        .orElseThrow { EntityNotFoundException("Product with id $id not found.") }
        .toResponseDTO()


    /**
     * Touver tous les produits (Paginé).
     */

    fun findAll(pageable: Pageable): Page<ProductResponseDTO> {
        return productRepository.findAll(pageable).map { it.toResponseDTO() }
    }


    /**
     * Touver tous les produits par categorie(Paginé).
     */

    fun findByCategory(pageable: Pageable, category: Category): Page<ProductResponseDTO> {
        return productRepository.findByCategory(category, pageable).map {
            it.toResponseDTO()
        }
    }


    /**
     * Diminue le stock d'un produit (entre en stock).
     */
    fun deleteProduct(productId: Long) {
        if (!productRepository.existsById(productId)) {
            throw EntityNotFoundException("Product with id $productId not found.")
        }
        productRepository.deleteById(productId)
    }


    /**
     * Diminue le stock d'un produit (sortie de stock, vente).
     */

    @Transactional
    fun addStock(productId: Long, quantityToAdd: Int): ProductResponseDTO {
        if (quantityToAdd <= 0) throw IllegalArgumentException("Quantity to add must be positive.")

        val product = productRepository.findById(productId)
            .orElseThrow { EntityNotFoundException("Product with id $productId not found.") }

        val updatedProduct = product.copy(
            quantityInStock = product.quantityInStock + quantityToAdd
        )
        return productRepository.save(updatedProduct).toResponseDTO()
    }

    /**
     * Diminue le stock d'un produit (sortie de stock, vente).
     */
    @Transactional
    fun removeStock(productId: Long, quantityToRemove: Int): ProductResponseDTO {
        if (quantityToRemove <= 0) throw IllegalArgumentException("Quantity to remove must be positive.")

        val product = productRepository.findById(productId)
            .orElseThrow { EntityNotFoundException("Product with id $productId not found.") }

        if (product.quantityInStock < quantityToRemove) {
            throw IllegalStateException("Insufficient stock for product ${product.name}. Available: ${product.quantityInStock}, Requested: $quantityToRemove")
        }

        val updatedProduct = product.copy(
            quantityInStock = product.quantityInStock - quantityToRemove
        )
        return productRepository.save(updatedProduct).toResponseDTO()
    }

    /**
     * Récupère la liste des produits en alerte de stock bas.
     */
    fun getLowStockProducts(): List<ProductResponseDTO> {
        return productRepository.findLowStockProducts().map { it.toResponseDTO() }
    }

    /**
     * Recherche de produits par nom avec pagination.
     */
    fun searchByName(name: String, pageable: Pageable): Page<ProductResponseDTO> {
        return productRepository.findByNameContainingIgnoreCase(name, pageable).map { it.toResponseDTO() }
    }

}