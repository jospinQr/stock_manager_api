package com.megamind.StockManagerApi.customer

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Entity
data class Customer(


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:NotBlank
    val name: String,
    @field:Email
    val email: String? = null,
    val phone: String? = null


)