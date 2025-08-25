package com.megamind.StockManagerApi.customer


import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/customers")
class CustomerController(private val customerService: CustomerService) {

    // POST /api/v1/customers
    @PostMapping
    fun create(@Valid @RequestBody request: CustomerRequestDTO): ResponseEntity<CustomerResponseDTO> {
        val customer = customerService.createCustomer(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(customer)
    }

    // GET /api/v1/customers?page=0&size=10&sort=name,asc
    @GetMapping
    fun getAll(pageable: Pageable): ResponseEntity<Page<CustomerResponseDTO>> {
        val customers = customerService.findAll(pageable)
        return ResponseEntity.ok(customers)
    }

    // GET /api/v1/customers/{id}
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<CustomerResponseDTO> {
        return ResponseEntity.ok(customerService.findById(id))
    }

    // GET /api/v1/customers/by-email?email=...
    @GetMapping("/by-email")
    fun getByEmail(@RequestParam email: String): ResponseEntity<CustomerResponseDTO> {
        return ResponseEntity.ok(customerService.findByEmail(email))
    }

    // GET /api/v1/customers/by-phone?phone=...
    @GetMapping("/by-phone")
    fun getByPhone(@RequestParam phone: String): ResponseEntity<CustomerResponseDTO> {
        return ResponseEntity.ok(customerService.findByPhone(phone))
    }

    // PATCH /api/v1/customers/{id}
    @PatchMapping("/{id}")
    fun patchUpdate(
        @PathVariable id: Long,
        @RequestBody patchRequest: CustomerPatchDTO
    ): ResponseEntity<CustomerResponseDTO> {
        val updatedCustomer = customerService.patchCustomer(id, patchRequest)
        return ResponseEntity.ok(updatedCustomer)
    }

    // PUT /api/v1/customers/{id}
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: CustomerRequestDTO
    ): ResponseEntity<CustomerResponseDTO> {
        val updatedCustomer = customerService.updateCustomer(id, request)
        return ResponseEntity.ok(updatedCustomer)
    }

    // DELETE /api/v1/customers/{id}
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        customerService.deleteCustomer(id)
        return ResponseEntity.noContent().build()
    }


    // GET /api/v1/customers/search?name=...
    @GetMapping("/search")
    fun searchByName(@RequestParam name: String, pageable: Pageable): ResponseEntity<Page<CustomerResponseDTO>> {
        return ResponseEntity.ok(customerService.searchByName(name, pageable))
    }


}