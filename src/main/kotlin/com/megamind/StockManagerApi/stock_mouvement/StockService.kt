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
import com.itextpdf.text.pdf.PdfDocument
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.megamind.StockManagerApi.product.ProductRepository
import com.megamind.StockManagerApi.sale.SaleResponseDTO
import com.megamind.StockManagerApi.user.UserRepository
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs


@Service
class StockService(
    private val movementRepository: StockMovementRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
    // On en a besoin pour mettre à jour la quantité
) {

    @Transactional // ESSENTIEL ! Garantit que les deux opérations (créer mouvement + update produit) réussissent ou échouent ensemble.
    fun createMovement(request: MovementRequestDTO): StockMovement {
        val product = productRepository.findById(request.productId)
            .orElseThrow { EntityNotFoundException("Product with id ${request.productId} not found.") }

        println("Produit trouvé : ${product.name}")

        val user = userRepository.findById(request.userId)
            .orElseThrow { EntityNotFoundException("User with id ${request.userId} not found.") }

        println("Utilisateur trouvé : ${user.username}")
        // Déterminer la quantité à appliquer (positive ou négative)
        val quantityToApply = when (request.type) {
            MovementType.SALE,
            MovementType.SUPPLIER_RETURN,
            MovementType.INVENTORY_ADJUSTMENT_MINUS,
            MovementType.WASTAGE -> -abs(request.quantity)

            MovementType.SUPPLY,
            MovementType.CUSTOMER_RETURN,
            MovementType.INVENTORY_ADJUSTMENT_PLUS -> abs(request.quantity)
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
            user = user
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


    fun getStockCard(productId: Long): List<StockMovementLineDTO> {
        val product = productRepository.findById(productId)
            .orElseThrow { EntityNotFoundException("Product with id $productId not found.") }

        val movements = movementRepository.findByProductIdOrderByMovementDateAsc(productId)

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
                    notes = movement.notes
                )
            )
        }

        return stockCard
    }


    fun generateStockCardPdf(productId: Long): ByteArray {
        val product = productRepository.findById(productId)
            .orElseThrow { EntityNotFoundException("Product with id $productId not found.") }

        val movements = getStockCard(productId)

        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A4, 36f, 36f, 54f, 36f)
        val writer = PdfWriter.getInstance(document, outputStream)
        document.open()

        // Titre
        val titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
        val normalFont = Font(Font.FontFamily.HELVETICA, 10f)
        val boldFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)

        addHeader(document)
        document.add(Paragraph("Fiche de stock du produit : ${product.name}", titleFont).apply {
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
        val table = PdfPTable(7)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(2f, 1.5f, 1f, 1f, 1f, 2f, 3f))

        val headers = listOf("Date", "Type", "Quantité", "Avant", "Après", "Document", "Notes")
        for (header in headers) {
            val cell = PdfPCell(Phrase(header, boldFont))
            cell.backgroundColor = BaseColor.LIGHT_GRAY
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.setPadding(5f)
            table.addCell(cell)
        }

        for (m in movements) {
            table.addCell(Phrase(m.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont))
            table.addCell(Phrase(m.type.name, normalFont))
            table.addCell(Phrase(m.quantity.toString(), normalFont))
            table.addCell(Phrase(m.stockBefore.toString(), normalFont))
            table.addCell(Phrase(m.stockAfter.toString(), normalFont))
            table.addCell(Phrase(m.sourceDocument ?: "", normalFont))
            table.addCell(Phrase(m.notes ?: "", normalFont))
        }

        document.add(table)
        document.close()
        writer.close()

        return outputStream.toByteArray()
    }


    private fun addHeader(document: Document) {
        val logoPath = ClassPathResource("static/images/logo.png").file.absolutePath
        val logo = Image.getInstance(logoPath)
        val FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)
        val FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, BaseColor.BLACK)
        val FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10f, BaseColor.BLACK)
        val FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, BaseColor.WHITE)
        // Adapter à l'imprimante thermique 80mm
        logo.scaleAbsoluteWidth(120f)  // ~42mm de large
        logo.scaleAbsoluteHeight(120f)
        logo.alignment = Element.ALIGN_LEFT

        document.add(logo)

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