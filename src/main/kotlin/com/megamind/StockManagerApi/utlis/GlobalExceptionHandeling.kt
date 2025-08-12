package com.megamind.StockManagerApi.utlis

import jakarta.persistence.EntityNotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleConflict(e: DataIntegrityViolationException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(mapOf("error" to "Conflit de données : ${e.message}"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to "Erreur serveur. ${e.message}"))
    }


    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(e: EntityNotFoundException): ResponseEntity<Map<String, String>> {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to "Entité non trouvé : ${e.message}"))
    }
}
