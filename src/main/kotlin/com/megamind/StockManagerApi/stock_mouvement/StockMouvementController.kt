package com.megamind.StockManagerApi.stock_mouvement

// Dans votre package controller : StockController.kt

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_PDF
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

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

    /**
     * Récupère une liste paginée des entrées de stock par période
     * @param startDate Date de début de la période (format: yyyy-MM-ddTHH:mm:ss)
     * @param endDate Date de fin de la période (format: yyyy-MM-ddTHH:mm:ss)
     * @param pageSize Taille de la page (défaut: 20)
     * @return Liste paginée des entrées de stock pour la période spécifiée
     */
    @GetMapping("/entries/period")
    fun getStockEntriesByPeriod(
        @RequestParam startDate: LocalDateTime,
        @RequestParam endDate: LocalDateTime,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): ResponseEntity<PeriodPaginatedResponse<StockMovementResponseDTO>> {
        val entriesResponse = stockService.getStockEntriesByPeriod(startDate, endDate, pageSize)

        val response = PeriodPaginatedResponse(
            content = entriesResponse.content.map { it.toResponseDTO() },
            totalElements = entriesResponse.totalElements,
            totalPages = entriesResponse.totalPages,
            currentPeriod = entriesResponse.currentPeriod,
            startDate = entriesResponse.startDate,
            endDate = entriesResponse.endDate,
            pageSize = entriesResponse.pageSize,
            hasNextPeriod = entriesResponse.hasNextPeriod,
            hasPreviousPeriod = entriesResponse.hasPreviousPeriod
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Récupère une liste paginée des sorties de stock par période
     * @param startDate Date de début de la période (format: yyyy-MM-ddTHH:mm:ss)
     * @param endDate Date de fin de la période (format: yyyy-MM-ddTHH:mm:ss)
     * @param pageSize Taille de la page (défaut: 20)
     * @return Liste paginée des sorties de stock pour la période spécifiée
     */
    @GetMapping("/exits/period")
    fun getStockExitsByPeriod(
        @RequestParam startDate: LocalDateTime,
        @RequestParam endDate: LocalDateTime,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): ResponseEntity<PeriodPaginatedResponse<StockMovementResponseDTO>> {
        val exitsResponse = stockService.getStockExitsByPeriod(startDate, endDate, pageSize)

        val response = PeriodPaginatedResponse(
            content = exitsResponse.content.map { it.toResponseDTO() },
            totalElements = exitsResponse.totalElements,
            totalPages = exitsResponse.totalPages,
            currentPeriod = exitsResponse.currentPeriod,
            startDate = exitsResponse.startDate,
            endDate = exitsResponse.endDate,
            pageSize = exitsResponse.pageSize,
            hasNextPeriod = exitsResponse.hasNextPeriod,
            hasPreviousPeriod = exitsResponse.hasPreviousPeriod
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Récupère les entrées de stock pour une période prédéfinie
     * @param periodType Type de période (DAY, WEEK, MONTH)
     * @param referenceDate Date de référence (optionnel, défaut: aujourd'hui)
     * @param pageSize Taille de la page (défaut: 20)
     * @return Liste paginée des entrées de stock pour la période
     */
    @GetMapping("/entries/{periodType}")
    fun getStockEntriesByPeriodType(
        @PathVariable periodType: String,
        @RequestParam(required = false) referenceDate: LocalDateTime?,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): ResponseEntity<PeriodPaginatedResponse<StockMovementResponseDTO>> {
        val refDate = referenceDate ?: LocalDateTime.now()
        val (startDate, endDate) = stockService.generatePeriod(periodType, refDate)

        val entriesResponse = stockService.getStockEntriesByPeriod(startDate, endDate, pageSize)

        val response = PeriodPaginatedResponse(
            content = entriesResponse.content.map { it.toResponseDTO() },
            totalElements = entriesResponse.totalElements,
            totalPages = entriesResponse.totalPages,
            currentPeriod = entriesResponse.currentPeriod,
            startDate = entriesResponse.startDate,
            endDate = entriesResponse.endDate,
            pageSize = entriesResponse.pageSize,
            hasNextPeriod = entriesResponse.hasNextPeriod,
            hasPreviousPeriod = entriesResponse.hasPreviousPeriod
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Récupère les sorties de stock pour une période prédéfinie
     * @param periodType Type de période (DAY, WEEK, MONTH)
     * @param referenceDate Date de référence (optionnel, défaut: aujourd'hui)
     * @param pageSize Taille de la page (défaut: 20)
     * @return Liste paginée des sorties de stock pour la période
     */
    @GetMapping("/exits/{periodType}")
    fun getStockExitsByPeriodType(
        @PathVariable periodType: String,
        @RequestParam(required = false) referenceDate: LocalDateTime?,
        @RequestParam(defaultValue = "20") pageSize: Int
    ): ResponseEntity<PeriodPaginatedResponse<StockMovementResponseDTO>> {
        val refDate = referenceDate ?: LocalDateTime.now()
        val (startDate, endDate) = stockService.generatePeriod(periodType, refDate)

        val exitsResponse = stockService.getStockExitsByPeriod(startDate, endDate, pageSize)

        val response = PeriodPaginatedResponse(
            content = exitsResponse.content.map { it.toResponseDTO() },
            totalElements = exitsResponse.totalElements,
            totalPages = exitsResponse.totalPages,
            currentPeriod = exitsResponse.currentPeriod,
            startDate = exitsResponse.startDate,
            endDate = exitsResponse.endDate,
            pageSize = exitsResponse.pageSize,
            hasNextPeriod = exitsResponse.hasNextPeriod,
            hasPreviousPeriod = exitsResponse.hasPreviousPeriod
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Récupère la période suivante pour la navigation
     * @param startDate Date de début de la période actuelle
     * @param endDate Date de fin de la période actuelle
     * @return Période suivante
     */
    @GetMapping("/period/next")
    fun getNextPeriod(
        @RequestParam startDate: LocalDateTime,
        @RequestParam endDate: LocalDateTime
    ): ResponseEntity<Map<String, LocalDateTime>> {
        val (nextStart, nextEnd) = stockService.getNextPeriod(startDate, endDate)
        return ResponseEntity.ok(
            mapOf(
                "startDate" to nextStart,
                "endDate" to nextEnd
            )
        )
    }

    /**
     * Récupère la période précédente pour la navigation
     * @param startDate Date de début de la période actuelle
     * @param endDate Date de fin de la période actuelle
     * @return Période précédente
     */
    @GetMapping("/period/previous")
    fun getPreviousPeriod(
        @RequestParam startDate: LocalDateTime,
        @RequestParam endDate: LocalDateTime
    ): ResponseEntity<Map<String, LocalDateTime>> {
        val (prevStart, prevEnd) = stockService.getPreviousPeriod(startDate, endDate)
        return ResponseEntity.ok(
            mapOf(
                "startDate" to prevStart,
                "endDate" to prevEnd
            )
        )
    }


    @GetMapping("products/{id}/stock-card")
    fun getStockCard(
        @PathVariable id: Long,
        @RequestParam startDate: LocalDateTime,
        @RequestParam endDate: LocalDateTime
    ): ResponseEntity<List<StockMovementLineDTO>> {
        val stockCard = stockService.getStockCard(id, startDate, endDate)
        return ResponseEntity.ok(stockCard)
    }


    @GetMapping("products/{id}/stock-card/pdf")
    fun downloadStockCardPdf(
        @PathVariable id: Long,
        @RequestParam startDate: LocalDateTime,
        @RequestParam endDate: LocalDateTime
    ): ResponseEntity<ByteArray> {
        val pdfBytes = stockService.generateStockCardPdf(id, startDate, endDate)
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
            createBy = this.createBy
        )
    }


}