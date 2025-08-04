package com.megamind.StockManagerApi.product

import com.megamind.StockManagerApi.category.Category
import java.time.LocalDateTime
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
data class Product(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val name: String,
    val barcode: String?,
    val description: String?,
    val price: Double,
    val quantityInStock: Int,
    val lowStockAlert: Int,

    @ManyToOne
    val category: Category,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)