package com.megamind.StockManagerApi.category

import jakarta.persistence.EntityNotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.function.EntityResponse
import javax.swing.text.html.parser.Entity

@RestController
@RequestMapping("api/v1/category")
class CategoryController(private val service: CategoryService) {


    @PostMapping
    fun save(@RequestBody category: Category): ResponseEntity<Any> {


        return try {

            val response = service.saveCategory(category)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: DataIntegrityViolationException) {
            val errorResponse = mapOf("error" to "Category with this name already exists. ${e.message}")
            ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
        } catch (e: Exception) {
            val errorResponse = mapOf("error" to "Internal server error. ${e.message}")
            ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
        }
    }


    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Any> {

        return try {
            val response = service.deleteCategory(id)
            ResponseEntity.status(HttpStatus.OK).body(response)
        } catch (e: EntityNotFoundException) {

            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category with $id not found")
        }

    }


    @GetMapping
    fun findAllCategory(): ResponseEntity<List<Category>> {

        return try {

            val response = service.findAllCategories()
            ResponseEntity.status(HttpStatus.OK).body(response)

        } catch (e: EntityNotFoundException) {

            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {

            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }


    @GetMapping("/{id}")
    fun findCategorybyid(@PathVariable id: Long): ResponseEntity<Category> {

        return try {
            val response = service.findById(id)
            ResponseEntity.status(HttpStatus.OK).body(response)

        } catch (e: EntityNotFoundException) {

            ResponseEntity(HttpStatus.NOT_FOUND)
        } catch (e: Exception) {

            ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }

    }

}