package com.megamind.StockManagerApi.stock_mouvement

// Dans votre package controller : StockController.kt

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_PDF
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/stock") // Base path pour les endpoints liés au stock
class StockController(private val stockService: StockService) {

    /**
     * Crée un nouveau mouvement de stock.
     * Cette opération est transactionnelle et met à jour la quantité en stock du produit concerné.
     *
     * @param request Le DTO contenant les détails du mouvement à créer.
     * @return Le mouvement de stock qui a été créé.
     */
    @PostMapping("/movements")
    fun createStockMovement(@RequestBody request: MovementRequestDTO): ResponseEntity<StockMovementResponseDTO> {
        val createdMovement = stockService.createMovement(request)
        // On retourne une réponse 201 CREATED avec le mouvement créé et formaté en DTO
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdMovement.toResponseDTO())
    }

    /**
     * Récupère l'historique complet des mouvements de stock pour un produit spécifique.
     *
     * @param productId L'ID du produit pour lequel on veut l'historique.
     * @return Une liste de tous les mouvements de stock pour ce produit.
     */
    @GetMapping("/products/{productId}/history")
    fun getProductStockHistory(@PathVariable productId: Long): ResponseEntity<List<StockMovementResponseDTO>> {
        val history = stockService.getHistoryForProduct(productId)
        val response = history.map { it.toResponseDTO() }
        return ResponseEntity.ok(response)
    }


    @GetMapping("products/{id}/stock-card")
    fun getStockCard(@PathVariable id: Long): ResponseEntity<List<StockMovementLineDTO>> {
        val stockCard = stockService.getStockCard(id)
        return ResponseEntity.ok(stockCard)
    }


    @GetMapping("products/{id}/stock-card/pdf")
    fun downloadStockCardPdf(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val pdfBytes = stockService.generateStockCardPdf(id)
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=fiche-stock-produit-$id.pdf")
            .contentType(APPLICATION_PDF)
            .body(pdfBytes)
    }

    /**
     * Fonction d'extension privée pour mapper l'entité StockMovement vers son DTO de réponse.
     * Cela évite de polluer l'entité avec des logiques de conversion.
     */
    private fun StockMovement.toResponseDTO(): StockMovementResponseDTO {
        return StockMovementResponseDTO(
            id = this.id,
            productId = this.product.id,
            productName = this.product.name, // Accès au nom du produit
            quantity = this.quantity,
            type = this.type,
            movementDate = this.movementDate,
            sourceDocument = this.sourceDocument,
            notes = this.notes,
            userName = this.user.username
        )
    }


}