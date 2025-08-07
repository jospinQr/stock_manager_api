package com.megamind.StockManagerApi.product

import com.megamind.StockManagerApi.category.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

import java.util.Optional

interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {


    // Retourne List<Product> directement. Une liste vide est un résultat valide.
    fun findByCategory(category: Category, pageable: Pageable): Page<Product>

    // Recherche par nom (insensible à la casse)
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Product>

    // Recherche par code-barres
    fun findByBarcode(barcode: String): Optional<Product>

    // **Fonctionnalité métier clé : Trouver les produits en stock bas**
    @Query("SELECT p FROM Product p WHERE p.quantityInStock <= p.lowStockAlert")
    fun findLowStockProducts(): List<Product>

    // **Fonctionnalité métier clé : Trouver les produits en rupture de stock**
    fun findByQuantityInStockEquals(quantity: Int): List<Product>

    // **Fonctionnalité métier clé : Trouver les produits en stock bas**
    fun findByNameIgnoreCase(name: String): Optional<Product>




}