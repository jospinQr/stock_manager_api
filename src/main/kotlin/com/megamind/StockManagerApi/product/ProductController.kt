package com.megamind.StockManagerApi.product

import com.megamind.StockManagerApi.category.CategoryService
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.yaml.snakeyaml.nodes.NodeId

// ProductController.kt
@RestController
@RequestMapping("/api/v1/products") // Convention: nom de la ressource au pluriel
class ProductController(private val service: ProductService, private val categoryService: CategoryService) {

    // POST /api/v1/products
    @PostMapping
    fun create(@Valid @RequestBody request: ProductRequestDTO): ResponseEntity<ProductResponseDTO> {
        val product = service.createProduct(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(product)
    }

    // GET /api/v1/products?page=0&size=10&sort=name,asc
    @GetMapping
    fun getAll(pageable: Pageable): ResponseEntity<Page<ProductResponseDTO>> {
        val products = service.findAll(pageable)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/all")
    fun getAlll(pageable: Pageable): ResponseEntity<Page<ProductResponseDTO>> {
        val products = service.findAll(pageable)
        return ResponseEntity.ok(products)
    }


    // GET /api/v1/products/{id}
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ProductResponseDTO> {
        return ResponseEntity.ok(service.findById(id))
    }


    // GET /api/v1/products/{barcode}
    @GetMapping("/byBarcode")
    fun getByBarcode(@RequestParam("value") barcode: String): ResponseEntity<ProductResponseDTO> {
        return ResponseEntity.ok(service.findByBarCode(barcode))
    }


    //PATCH  /api/v1/products/{id}
    @PatchMapping("/{id}")
    fun patchUpdate(
        @PathVariable id: Long,
        @RequestBody patchRequest: ProductPatchDTO
    ): ResponseEntity<ProductResponseDTO> {
        val updatedProduct = service.patchProduct(id, patchRequest)
        return ResponseEntity.ok(updatedProduct)
    }

    // PUT /api/v1/products/{id}
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: ProductRequestDTO
    ): ResponseEntity<ProductResponseDTO> {
        val updatedProduct = service.updateProduct(id, request)
        return ResponseEntity.ok(updatedProduct)
    }

    // DELETE /api/v1/products/{id}
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.deleteProduct(id)
        return ResponseEntity.noContent().build() // HttpStatus 204 No Content est plus approprié pour un DELETE réussi
    }


    // POST /api/v1/products/{id}/add-stock
    @PostMapping("/{id}/add-stock")
    fun addStock(@PathVariable id: Long, @RequestParam quantity: Int): ResponseEntity<ProductResponseDTO> {
        val updatedProduct = service.addStock(id, quantity)
        return ResponseEntity.ok(updatedProduct)
    }

    // POST /api/v1/products/{id}/remove-stock
    @PostMapping("/{id}/remove-stock")
    fun removeStock(@PathVariable id: Long, @RequestParam quantity: Int): ResponseEntity<ProductResponseDTO> {
        val updatedProduct = service.removeStock(id, quantity)
        return ResponseEntity.ok(updatedProduct)
    }

    // GET /api/v1/products/low-stock
    @GetMapping("/low-stock")
    fun getLowStock(): ResponseEntity<List<ProductResponseDTO>> {
        return ResponseEntity.ok(service.getLowStockProducts())
    }

    // GET /api/v1/products/search?name=...
    @GetMapping("/search")
    fun searchByName(@RequestParam name: String, pageable: Pageable): ResponseEntity<Page<ProductResponseDTO>> {
        return ResponseEntity.ok(service.searchByName(name, pageable))
    }


    @GetMapping("/category/{categoryId}")
    fun getProductsByCategory(
        @PathVariable categoryId: Long,
        pageable: Pageable
    ): Page<ProductResponseDTO> {
        val category = categoryService.findById(categoryId)
        return service.findByCategory(category, pageable)
    }
}

