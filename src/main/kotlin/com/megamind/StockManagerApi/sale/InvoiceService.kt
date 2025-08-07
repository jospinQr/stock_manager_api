package com.megamind.StockManagerApi.sale


import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat

@Service
class InvoiceService {

    private val FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f, BaseColor.BLACK)
    private val FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, BaseColor.BLACK)
    private val FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10f, BaseColor.BLACK)
    private val FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, BaseColor.WHITE)

    fun generateInvoicePdf(sale: SaleResponseDTO): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val document = Document(Rectangle(226f, 600f), 10f, 10f, 10f, 10f)
        val writer = PdfWriter.getInstance(document, outputStream)

        document.open()

        // --- En-tête de la facture ---
        addHeader(document, sale)

        // --- Informations sur le client ---
        addCustomerInfo(document, sale)

        // Ligne de séparation
        document.add(Chunk.NEWLINE)

        // --- Tableau des articles ---
        addItemsList(document, sale)

        // --- Totaux ---
        addTotals(document, sale)

        // --- Pied de page ---
        addFooter(document)


        document.close()
        writer.close()

        return outputStream.toByteArray()
    }


    private fun addHeader(document: Document, sale: SaleResponseDTO) {
        val logoPath = ClassPathResource("static/images/logo.png").file.absolutePath
        val logo = Image.getInstance(logoPath)

        // Adapter à l'imprimante thermique 80mm
        logo.scaleAbsoluteWidth(120f)  // ~42mm de large
        logo.scaleAbsoluteHeight(60f)
        logo.alignment = Element.ALIGN_CENTER

        document.add(logo)

        val companyInfo = Paragraph()
        companyInfo.alignment = Element.ALIGN_CENTER
        companyInfo.add(Phrase("Maseka food\n", FONT_BOLD))
        companyInfo.add(Phrase("123, Av. des Volcans, Goma\n", FONT_NORMAL))
        companyInfo.add(Phrase("RCCM: CD/GOM/XXX\n", FONT_NORMAL))
        companyInfo.add(Phrase("Tél: +243 XXX XXX XXX\n", FONT_NORMAL))

        document.add(companyInfo)
        document.add(Chunk.NEWLINE)
    }


    private fun addCustomerInfo(document: Document, sale: SaleResponseDTO) {
        val title = Paragraph("FACTURE", FONT_TITLE)
        title.alignment = Element.ALIGN_CENTER
        document.add(title)

        val invoiceInfo = Paragraph()
        invoiceInfo.alignment = Element.ALIGN_RIGHT
        invoiceInfo.add(Phrase("Facture N°: F${sale.id.toString().padStart(6, '0')}\n", FONT_BOLD))
        invoiceInfo.add(Phrase("Date: ${sale.date.toLocalDate()}\n", FONT_NORMAL))
        document.add(invoiceInfo)


        val customerInfo = Paragraph()
        customerInfo.add(Phrase("Facturé à :\n", FONT_NORMAL))
        customerInfo.add(Phrase(sale.customerName ?: "Client au comptoir", FONT_BOLD))
        // Ajouter l'adresse du client si disponible
        // customerInfo.add(Phrase("\nAdresse du client...", FONT_NORMAL))

        document.add(customerInfo)
        document.add(Chunk.NEWLINE)
    }

    private fun addItemsList(document: Document, sale: SaleResponseDTO) {
        val priceFormat = DecimalFormat("#,##0.00 $")

        sale.items.forEach { item ->
            val name = Paragraph(item.productName, FONT_BOLD)
            val line = Paragraph(
                "${item.quantity} x ${priceFormat.format(item.unitPrice)} = ${priceFormat.format(item.total)}",
                FONT_NORMAL
            )
            line.alignment = Element.ALIGN_RIGHT

            document.add(name)
            document.add(line)
            document.add(Chunk.NEWLINE)
        }
    }

    private fun addTotals(document: Document, sale: SaleResponseDTO) {
        val priceFormat = DecimalFormat("#,##0.00 $")
        val totalLine = Paragraph("TOTAL : ${priceFormat.format(sale.totalAmount)}", FONT_BOLD)
        totalLine.alignment = Element.ALIGN_RIGHT
        document.add(Chunk.NEWLINE)
        document.add(totalLine)
    }

    private fun addFooter(document: Document) {
        val footer = Paragraph("\nMerci pour votre achat!", FONT_NORMAL)
        footer.alignment = Element.ALIGN_CENTER
        document.add(footer)
    }
}
// Fonctions utilitaires pour créer des cellules

