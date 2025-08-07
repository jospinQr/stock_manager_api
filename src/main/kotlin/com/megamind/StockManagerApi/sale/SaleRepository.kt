package com.megamind.StockManagerApi.sale

import org.springframework.data.jpa.repository.JpaRepository

interface SaleRepository : JpaRepository<Sale, Long> {
}