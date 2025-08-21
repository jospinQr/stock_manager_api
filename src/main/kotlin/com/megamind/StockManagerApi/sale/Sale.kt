package com.megamind.StockManagerApi.sale

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.megamind.StockManagerApi.customer.Customer
import com.megamind.StockManagerApi.user.User
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime


@Entity

data class Sale(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotNull
    @field:PastOrPresent
    val date: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    val customer: Customer? = null,

    @OneToMany(mappedBy = "sale", cascade = [CascadeType.ALL], orphanRemoval = true)

    val items: MutableList<SaleItem> = mutableListOf(),

    val createdBy: String? = null,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,


    )

enum class PaymentStatus {
    PAID, UNPAID, PARTIALLY_PAID
}
