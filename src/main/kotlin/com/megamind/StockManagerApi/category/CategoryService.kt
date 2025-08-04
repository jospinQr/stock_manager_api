package com.megamind.StockManagerApi.category

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class CategoryService(private val repository: CategoryRepository) {


    fun saveCategory(category: Category): Category = repository.save(category)

    fun deleteCategory(id: Long) {

        if (!repository.existsById(id)) {
            throw EntityNotFoundException("category don't exist")
        }
        return repository.deleteById(id)

    }

    fun findAllCategories(): List<Category> = repository.findAll()

}