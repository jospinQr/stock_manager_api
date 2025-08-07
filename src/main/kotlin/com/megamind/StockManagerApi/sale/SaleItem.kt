package com.megamind.StockManagerApi.sale

import com.megamind.StockManagerApi.product.Product
import jakarta.persistence.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Entity
data class SaleItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    val sale: Sale,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @field:NotNull
    @field:Min(1)
    val quantity: Int,

    @field:NotNull
    val unitPrice: BigDecimal,

    val discount: BigDecimal = BigDecimal.ZERO
)
