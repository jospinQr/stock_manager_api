package com.megamind.StockManagerApi.sale

import com.megamind.StockManagerApi.order.OrderReportService
import com.megamind.StockManagerApi.product.Product
import com.megamind.StockManagerApi.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

import java.time.LocalDateTime

interface SaleRepository : JpaRepository<Sale, Long> {


    fun findByDateBetween(startDate: LocalDateTime, endDateTime: LocalDateTime, pageable: Pageable): Page<Sale>


    fun findAllByCreatedBy(user: User, pageable: Pageable): Page<Sale>


    @Query("SELECT DISTINCT s FROM Sale s JOIN s.items si WHERE si.product = :product")
    fun findByProduct(@Param("product") product: Product, pageable: Pageable): Page<Sale>

    @Query("""
    SELECT p.id AS productId, 
           p.name AS productName, 
           SUM(si.quantity) AS totalQuantity
    FROM SaleItem si
    JOIN si.product p
    GROUP BY p.id, p.name
    ORDER BY SUM(si.quantity) DESC
""")
    fun findTopSellingProducts(pageable: Pageable): List<TopSaleProductDto>


}

