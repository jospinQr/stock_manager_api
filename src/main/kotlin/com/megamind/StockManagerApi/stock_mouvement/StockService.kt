package com.megamind.StockManagerApi.stock_mouvement

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.megamind.StockManagerApi.product.ProductRepository
import com.megamind.StockManagerApi.user.UserRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs


@Service
class StockService(
    private val movementRepository: StockMovementRepository,
    private val productRepository: ProductRepository,

    ) {

    @Transactional // ESSENTIEL ! Garantit que les deux opérations (créer mouvement + update produit) réussissent ou échouent ensemble.
    fun createMovement(request: MovementRequestDTO): StockMovement {
        val product = productRepository.findById(request.productId)
            .orElseThrow { EntityNotFoundException("Product with id ${request.productId} not found.") }

        val username = SecurityContextHolder.getContext().authentication.name

        // Détermine la quantité à appliquer (positive ou négative)
        val quantityToApply = when (request.type) {
            MovementType.VENTE,
            MovementType.SORTIE,
            MovementType.AJUSTEMENT_INVENTAIRE_MOINS,
            MovementType.PERT -> -abs(request.quantity)

            MovementType.ENTREE,
            MovementType.RETOUR_CLIENT,
            MovementType.ACHAT,
            MovementType.AJUSTEMENT_INVENTAIRE_PLUS -> abs(request.quantity)
        }

        // Vérification du stock pour les sorties
        if (quantityToApply < 0 && product.quantityInStock < abs(quantityToApply)) {
            throw IllegalStateException(
                "Insufficient stock for product ${product.name}. Available: ${product.quantityInStock}, Requested: ${
                    abs(quantityToApply)
                }"
            )
        }

        // 1. Créer et sauvegarder le mouvement
        val movement = StockMovement(
            product = product,
            quantity = quantityToApply,
            type = request.type,
            sourceDocument = request.sourceDocument,
            notes = request.notes,
            createBy = username

        )
        val savedMovement = movementRepository.save(movement)
        print("Mouvement reussi")

        // 2. Mettre à jour la quantité dénormalisée sur le produit
        val updatedProduct = product.copy(
            quantityInStock = product.quantityInStock + quantityToApply
        )
        productRepository.save(updatedProduct)

        return savedMovement
    }

    fun getHistoryForProduct(productId: Long): List<StockMovement> {
        if (!productRepository.existsById(productId)) {
            throw EntityNotFoundException("Product with id $productId not found.")
        }
        return movementRepository.findByProductIdOrderByMovementDateAsc(productId)
    }


    fun getStockCard(productId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<StockMovementLineDTO> {
        productRepository.findById(productId)
            .orElseThrow { EntityNotFoundException("Product with id $productId not found.") }

        val movements = movementRepository
            .findByProductIdAndMovementDateBetweenOrderByMovementDateAsc(productId, startDate, endDate)

        var currentStock = 0
        val stockCard = mutableListOf<StockMovementLineDTO>()

        for (movement in movements) {
            val before = currentStock
            currentStock += movement.quantity
            stockCard.add(
                StockMovementLineDTO(
                    date = movement.movementDate,
                    type = movement.type,
                    quantity = movement.quantity,
                    stockBefore = before,
                    stockAfter = currentStock,
                    sourceDocument = movement.sourceDocument,
                    notes = movement.notes,
                    createBy = movement.createBy
                )
            )
        }

        return stockCard
    }


    fun generateStockCardPdf(productId: Long, startDate: LocalDateTime, endDate: LocalDateTime): ByteArray {
        val product = productRepository.findById(productId)
            .orElseThrow { EntityNotFoundException("Product with id $productId not found.") }

        val movements = getStockCard(productId, startDate, endDate)

        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A4, 36f, 36f, 54f, 36f)
        val writer = PdfWriter.getInstance(document, outputStream)
        document.open()

        // Titre
        val titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
        val normalFont = Font(Font.FontFamily.HELVETICA, 10f)
        val boldFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)

        val startDate = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        val endDate = endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

        addHeader(document)
        document.add(Paragraph("Fiche de stock du produit : ${product.name} ", titleFont).apply {
            alignment = Element.ALIGN_CENTER
            spacingAfter = 10f
        })

        document.add(Paragraph("du $startDate au $endDate").apply {
            alignment = Element.ALIGN_CENTER
            spacingAfter = 10f
        })

        // Date de génération
        val dateGen = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        document.add(Paragraph("Date de génération : $dateGen", normalFont).apply {
            alignment = Element.ALIGN_RIGHT
            spacingAfter = 10f
        })

        // Tableau
        val table = PdfPTable(8)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(2f, 1.5f, 1f, 1f, 1f, 2f, 3f, 2f))

        val headers = listOf("Date", "Type", "Quantité", "Avant", "Après", "Document", "Notes", "Par")
        for (header in headers) {
            val cell = PdfPCell(Phrase(header, boldFont))
            cell.backgroundColor = BaseColor.LIGHT_GRAY
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.setPadding(5f)
            table.addCell(cell)
        }

        for (m in movements) {
            val type = when (m.type) {

                MovementType.VENTE,
                MovementType.SORTIE,
                MovementType.AJUSTEMENT_INVENTAIRE_MOINS,
                MovementType.PERT -> "Sortie"


                MovementType.ACHAT,
                MovementType.ENTREE,
                MovementType.RETOUR_CLIENT,
                MovementType.AJUSTEMENT_INVENTAIRE_PLUS -> "Entrée"

            }
            table.addCell(Phrase(m.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont))
            table.addCell(Phrase(m.type.name, normalFont))
            table.addCell(Phrase(m.quantity.toString(), normalFont))
            table.addCell(Phrase(m.stockBefore.toString(), normalFont))
            table.addCell(Phrase(m.stockAfter.toString(), normalFont))
            table.addCell(Phrase(m.sourceDocument ?: "", normalFont))
            table.addCell(Phrase(m.notes ?: "", normalFont))
            table.addCell(Phrase(m.createBy ?: "", normalFont))
        }

        document.add(table)
        document.close()
        writer.close()

        return outputStream.toByteArray()
    }

    /**
     * Récupère une liste paginée des entrées de stock par période
     * @param startDate Date de début de la période
     * @param endDate Date de fin de la période
     * @param pageSize Taille de la page
     * @return Réponse paginée par période des mouvements d'entrée
     */
    fun getStockEntriesByPeriod(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageSize: Int = 20
    ): PeriodPaginatedResponse<StockMovement> {
        val entryTypes = listOf(
            MovementType.ENTREE,
            MovementType.ACHAT,
            MovementType.RETOUR_CLIENT,
            MovementType.AJUSTEMENT_INVENTAIRE_PLUS
        )

        val pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "movementDate"))
        val entriesPage = movementRepository.findByTypeInAndMovementDateBetweenOrderByMovementDateDesc(
            entryTypes, startDate, endDate, pageable
        )

        val totalElements = movementRepository.countByTypeInAndMovementDateBetween(entryTypes, startDate, endDate)
        val totalPages = (totalElements / pageSize).toInt() + if (totalElements % pageSize > 0) 1 else 0

        return PeriodPaginatedResponse(
            content = entriesPage.content,
            totalElements = totalElements,
            totalPages = totalPages,
            currentPeriod = formatPeriod(startDate, endDate),
            startDate = startDate,
            endDate = endDate,
            pageSize = pageSize,
            hasNextPeriod = hasNextPeriod(startDate, endDate, totalElements, pageSize),
            hasPreviousPeriod = hasPreviousPeriod(startDate, endDate)
        )
    }

    /**
     * Récupère une liste paginée des sorties de stock par période
     * @param startDate Date de début de la période
     * @param endDate Date de fin de la période
     * @param pageSize Taille de la page
     * @return Réponse paginée par période des mouvements de sortie
     */
    fun getStockExitsByPeriod(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageSize: Int = 20
    ): PeriodPaginatedResponse<StockMovement> {
        val exitTypes = listOf(
            MovementType.SORTIE,
            MovementType.VENTE,
            MovementType.AJUSTEMENT_INVENTAIRE_MOINS,
            MovementType.PERT
        )

        val pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "movementDate"))
        val exitsPage = movementRepository.findByTypeInAndMovementDateBetweenOrderByMovementDateDesc(
            exitTypes, startDate, endDate, pageable
        )

        val totalElements = movementRepository.countByTypeInAndMovementDateBetween(exitTypes, startDate, endDate)
        val totalPages = (totalElements / pageSize).toInt() + if (totalElements % pageSize > 0) 1 else 0

        return PeriodPaginatedResponse(
            content = exitsPage.content,
            totalElements = totalElements,
            totalPages = totalPages,
            currentPeriod = formatPeriod(startDate, endDate),
            startDate = startDate,
            endDate = endDate,
            pageSize = pageSize,
            hasNextPeriod = hasNextPeriod(startDate, endDate, totalElements, pageSize),
            hasPreviousPeriod = hasPreviousPeriod(startDate, endDate)
        )
    }

    /**
     * Récupère la période suivante pour la pagination
     * @param currentStartDate Date de début de la période actuelle
     * @param currentEndDate Date de fin de la période actuelle
     * @return Période suivante
     */
    fun getNextPeriod(
        currentStartDate: LocalDateTime,
        currentEndDate: LocalDateTime
    ): Pair<LocalDateTime, LocalDateTime> {
        val duration = java.time.Duration.between(currentStartDate, currentEndDate)
        val nextStartDate = currentEndDate.plusNanos(1)
        val nextEndDate = nextStartDate.plus(duration)
        return Pair(nextStartDate, nextEndDate)
    }

    /**
     * Récupère la période précédente pour la pagination
     * @param currentStartDate Date de début de la période actuelle
     * @param currentEndDate Date de fin de la période actuelle
     * @return Période précédente
     */
    fun getPreviousPeriod(
        currentStartDate: LocalDateTime,
        currentEndDate: LocalDateTime
    ): Pair<LocalDateTime, LocalDateTime> {
        val duration = java.time.Duration.between(currentStartDate, currentEndDate)
        val previousEndDate = currentStartDate.minusNanos(1)
        val previousStartDate = previousEndDate.minus(duration)
        return Pair(previousStartDate, previousEndDate)
    }

    /**
     * Génère des périodes prédéfinies (jour, semaine, mois)
     * @param periodType Type de période
     * @param referenceDate Date de référence
     * @return Période calculée
     */
    fun generatePeriod(
        periodType: String,
        referenceDate: LocalDateTime = LocalDateTime.now()
    ): Pair<LocalDateTime, LocalDateTime> {
        return when (periodType.uppercase()) {
            "DAY" -> {
                val startOfDay = referenceDate.toLocalDate().atStartOfDay()
                val endOfDay = startOfDay.plusDays(1).minusNanos(1)
                Pair(startOfDay, endOfDay)
            }

            "WEEK" -> {
                val startOfWeek = referenceDate.toLocalDate()
                    .with(java.time.DayOfWeek.MONDAY)
                    .atStartOfDay()
                val endOfWeek = startOfWeek.plusWeeks(1).minusNanos(1)
                Pair(startOfWeek, endOfWeek)
            }

            "MONTH" -> {
                val startOfMonth = referenceDate.toLocalDate()
                    .withDayOfMonth(1)
                    .atStartOfDay()
                val endOfMonth = startOfMonth.plusMonths(1).minusNanos(1)
                Pair(startOfMonth, endOfMonth)
            }

            else -> {
                // Par défaut, période d'un jour
                val startOfDay = referenceDate.toLocalDate().atStartOfDay()
                val endOfDay = startOfDay.plusDays(1).minusNanos(1)
                Pair(startOfDay, endOfDay)
            }
        }
    }

    // Méthodes utilitaires privées
    private fun formatPeriod(startDate: LocalDateTime, endDate: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        return "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    }

    private fun hasNextPeriod(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        totalElements: Long,
        pageSize: Int
    ): Boolean {
        return totalElements > pageSize
    }

    private fun hasPreviousPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Boolean {
        // On considère qu'il y a une période précédente si on n'est pas à la date actuelle
        val now = LocalDateTime.now()
        return endDate.isBefore(now)
    }

    private fun addHeader(document: Document) {

        val resource = ClassPathResource("static/images/logo.png")
        resource.inputStream.use { input ->
            val logo = Image.getInstance(input.readBytes())
            logo.scaleAbsoluteWidth(100f)  // ~42mm de large
            logo.scaleAbsoluteHeight(60f)
            logo.alignment = Element.ALIGN_CENTER
            document.add(logo)
        }

        val FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)
        val FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, BaseColor.BLACK)
        val FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10f, BaseColor.BLACK)
        val FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, BaseColor.WHITE)


        val companyInfo = Paragraph()
        companyInfo.alignment = Element.ALIGN_LEFT
        companyInfo.add(Phrase("Maseka food\n", FONT_BOLD))
        companyInfo.add(Phrase("123, Av. des Volcans, Goma\n", FONT_NORMAL))
        companyInfo.add(Phrase("RCCM: CD/GOM/XXX\n", FONT_NORMAL))
        companyInfo.add(Phrase("Tél: +243 XXX XXX XXX\n", FONT_NORMAL))

        document.add(companyInfo)
        document.add(Chunk.NEWLINE)
    }
}