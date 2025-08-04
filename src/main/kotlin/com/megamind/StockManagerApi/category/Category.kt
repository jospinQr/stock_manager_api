package com.megamind.StockManagerApi.category

import jakarta.persistence.*


@Entity
@Table(name = "categories")
data class Category(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(unique = true)
    val name: String,
    val description: String?= null
)