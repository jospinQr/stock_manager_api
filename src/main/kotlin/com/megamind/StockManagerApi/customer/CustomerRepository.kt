package com.megamind.StockManagerApi.customer

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

import java.util.Optional

interface CustomerRepository : JpaRepository<Customer, Long> {


    // Recherche par email (unique)
    fun findByEmailIgnoreCase(email: String): Optional<Customer>

    // Recherche par nom (ignore case)
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Customer>

    // Recherche par téléphone
    fun findByPhone(phone: String): Optional<Customer>

    // Vérifier l'existence par email
    fun existsByEmailIgnoreCase(email: String): Boolean

    // Vérifier l'existence par téléphone
    fun existsByPhone(phone: String): Boolean

}