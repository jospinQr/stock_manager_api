package com.megamind.StockManagerApi.customer


import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CustomerRequestDTO(


    @field:NotBlank(message = "Customer name est required")
    val name: String,

    @field:Email(message = "Email doit etre valid")
    val email: String? = null,

    @field:Size(max = 20, message = "phone ne doit pas depasser 20 characters")
    val phone: String? = null,

    @field:Size(max = 100, message = "Company ne doit pas depasser 100 characters")
    val company: String? = null,


)



data class CustomerResponseDTO(
    val id: Long,
    val name: String,
    val email: String?,
    val phone: String?,
    val company: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)



data class CustomerPatchDTO(
    @field:Size(max = 100, message = "Name must not exceed 100 characters")
    val name: String? = null,

    @field:Email(message = "Email should be valid")
    @field:Size(max = 100, message = "Email must not exceed 100 characters")
    val email: String? = null,

    @field:Size(max = 20, message = "Phone must not exceed 20 characters")
    val phone: String? = null,

    @field:Size(max = 100, message = "Company must not exceed 100 characters")
    val company: String? = null,

)
