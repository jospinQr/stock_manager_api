package com.megamind.StockManagerApi.customer


import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

import org.springframework.stereotype.Service

@Service
class CustomerService(private val customerRepository: CustomerRepository) {

    // Mapper pour convertir entre Entité et DTO
    private fun Customer.toResponseDTO(): CustomerResponseDTO = CustomerResponseDTO(
        id = this.id,
        name = this.name,
        email = this.email,
        phone = this.phone,
        company = this.company,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )

    /**
     * Créer un nouveau client
     */
    fun createCustomer(request: CustomerRequestDTO): CustomerResponseDTO {
        // Validation de l'unicité de l'email
        request.email?.let { email ->
            if (customerRepository.existsByEmailIgnoreCase(email)) {
                throw DataIntegrityViolationException("Email '$email' is already registered.")
            }
        }

        // Validation de l'unicité du téléphone
        request.phone?.takeIf { it.isNotBlank() }?.let { phone ->
            if (customerRepository.existsByPhone(phone)) {
                throw DataIntegrityViolationException("Phone number '$phone' is already registered.")
            }
        }

        val customer = Customer(
            name = request.name,
            email = request.email,
            phone = request.phone,
            company = request.company,
            )

        return customerRepository.save(customer).toResponseDTO()
    }

    /**
     * Mettre à jour complètement un client
     */
    @Transactional
    fun updateCustomer(customerId: Long, request: CustomerRequestDTO): CustomerResponseDTO {
        val existingCustomer = customerRepository.findById(customerId)
            .orElseThrow { EntityNotFoundException("Customer with id $customerId not found.") }

        // Validation de l'unicité de l'email
        request.email?.let { email ->
            customerRepository.findByEmailIgnoreCase(email).ifPresent { foundCustomer ->
                if (foundCustomer.id != existingCustomer.id) {
                    throw DataIntegrityViolationException("Email '$email' is already registered by another customer.")
                }
            }
        }

        // Validation de l'unicité du téléphone
        request.phone?.takeIf { it.isNotBlank() }?.let { phone ->
            customerRepository.findByPhone(phone).ifPresent { foundCustomer ->
                if (foundCustomer.id != existingCustomer.id) {
                    throw DataIntegrityViolationException("Phone number '$phone' is already registered by another customer.")
                }
            }
        }

        val updatedCustomer = existingCustomer.copy(
            name = request.name,
            email = request.email,
            phone = request.phone,
            company = request.company,

            )

        return customerRepository.save(updatedCustomer).toResponseDTO()
    }

    /**
     * Mettre à jour partiellement un client
     */
    @Transactional
    fun patchCustomer(customerId: Long, patch: CustomerPatchDTO): CustomerResponseDTO {
        val existingCustomer = customerRepository.findById(customerId)
            .orElseThrow { EntityNotFoundException("Customer with id $customerId not found.") }

        // Validation de l'unicité de l'email si modifié
        if (patch.email != null && patch.email != existingCustomer.email) {
            customerRepository.findByEmailIgnoreCase(patch.email).ifPresent { foundCustomer ->
                if (foundCustomer.id != existingCustomer.id) {
                    throw DataIntegrityViolationException("Email '${patch.email}' is already registered by another customer.")
                }
            }
        }

        // Validation de l'unicité du téléphone si modifié
        if (patch.phone != null && patch.phone != existingCustomer.phone) {
            patch.phone.takeIf { it.isNotBlank() }?.let { phone ->
                customerRepository.findByPhone(phone).ifPresent { foundCustomer ->
                    if (foundCustomer.id != existingCustomer.id) {
                        throw DataIntegrityViolationException("Phone number '$phone' is already registered by another customer.")
                    }
                }
            }
        }

        val patchedCustomer = existingCustomer.copy(
            name = patch.name ?: existingCustomer.name,
            email = patch.email ?: existingCustomer.email,
            phone = patch.phone ?: existingCustomer.phone,
            company = patch.company ?: existingCustomer.company,

            )

        return customerRepository.save(patchedCustomer).toResponseDTO()
    }

    /**
     * Supprimer un client
     */
    fun deleteCustomer(customerId: Long) {
        if (!customerRepository.existsById(customerId)) {
            throw EntityNotFoundException("Customer with id $customerId not found.")
        }
        customerRepository.deleteById(customerId)
    }

    /**
     * Trouver un client par son ID
     */
    fun findById(id: Long): CustomerResponseDTO = customerRepository.findById(id)
        .orElseThrow { EntityNotFoundException("Customer with id $id not found.") }
        .toResponseDTO()

    /**
     * Trouver un client par email
     */
    fun findByEmail(email: String): CustomerResponseDTO =
        customerRepository.findByEmailIgnoreCase(email)
            .orElseThrow { EntityNotFoundException("Customer with email '$email' not found.") }
            .toResponseDTO()

    /**
     * Trouver un client par téléphone
     */
    fun findByPhone(phone: String): CustomerResponseDTO =
        customerRepository.findByPhone(phone)
            .orElseThrow { EntityNotFoundException("Customer with phone '$phone' not found.") }
            .toResponseDTO()

    /**
     * Trouver tous les clients (paginé)
     */
    fun findAll(pageable: Pageable): Page<CustomerResponseDTO> {
        return customerRepository.findAll(pageable).map { it.toResponseDTO() }
    }


    /**
     * Recherche de clients par nom
     */
    fun searchByName(name: String, pageable: Pageable): Page<CustomerResponseDTO> {
        return customerRepository.findByNameContainingIgnoreCase(name, pageable).map { it.toResponseDTO() }
    }


}