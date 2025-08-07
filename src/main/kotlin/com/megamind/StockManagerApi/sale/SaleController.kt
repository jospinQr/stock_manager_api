package com.megamind.StockManagerApi.sale


import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.http.HttpHeaders
import java.security.Principal

@RestController
@RequestMapping("/api/v1/sales") // Utilisation d'un préfixe de versionnage pour l'API
class SaleController(private val saleService: SaleService, private val invoiceService: InvoiceService) {

    /**
     * Crée une nouvelle vente.
     * @param saleRequestDTO Les données de la vente à créer.
     * @param principal L'utilisateur actuellement authentifié, injecté par Spring Security.
     * @return Une réponse 201 Created avec la vente créée dans le corps et l'URL de la nouvelle ressource dans l'en-tête "Location".
     */
    @PostMapping
    fun createSale(
        @Valid @RequestBody saleRequestDTO: SaleRequestDTO,
        principal: Principal // Manière sécurisée d'obtenir l'utilisateur courant
    ): ResponseEntity<ByteArray> {
        val createdSale = saleService.createSale(
            customerId = saleRequestDTO.customerId,
            items = saleRequestDTO.items,
            currentUsername = principal.name // On utilise le nom de l'utilisateur authentifié
        )

        // 2. Générer le PDF en utilisant le service de facturation
        val pdfBytes = invoiceService.generateInvoicePdf(createdSale)

        val invoiceFileName = "facture-F${createdSale.id.toString().padStart(6, '0')}.pdf"

        // 3. Préparer les en-têtes de la réponse HTTP
        val headers = org.springframework.http.HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF

        // 'inline' pour afficher dans le navigateur, 'attachment' pour forcer le téléchargement
        headers.setContentDispositionFormData("inline", invoiceFileName)
        headers.cacheControl = "must-revalidate, post-check=0, pre-check=0"

        return ResponseEntity(pdfBytes, headers, HttpStatus.OK)
    }

    /**
     * Récupère toutes les ventes.
     * @return Une liste de toutes les ventes.
     */
    @GetMapping
    fun getAllSales(): ResponseEntity<List<SaleResponseDTO>> {
        val sales = saleService.listAllSales()
        return ResponseEntity.ok(sales)
    }

    /**
     * Récupère une vente par son identifiant unique.
     * @param id L'identifiant de la vente.
     * @return La vente correspondante si elle existe, sinon une erreur 404 est gérée par le `ControllerAdvice`.
     */
    @GetMapping("/{id}")
    fun getSaleById(@PathVariable id: Long): ResponseEntity<SaleResponseDTO> {
        val sale = saleService.getSaleById(id)
        return ResponseEntity.ok(sale)
    }


    /**
     * Génère et renvoie le PDF de la facture pour une vente donnée.
     * @param id L'identifiant de la vente.
     * @return Une réponse HTTP contenant le fichier PDF.
     */
    @GetMapping("/{id}/invoice")
    fun getSaleInvoice(@PathVariable id: Long): ResponseEntity<ByteArray> {
        // 1. Récupérer les données de la vente
        val sale = saleService.getSaleById(id)

        // 2. Générer le PDF en utilisant le service de facturation
        val pdfBytes = invoiceService.generateInvoicePdf(sale)

        val invoiceFileName = "facture-F${sale.id.toString().padStart(6, '0')}.pdf"

        // 3. Préparer les en-têtes de la réponse HTTP
        val headers = org.springframework.http.HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF

        // 'inline' pour afficher dans le navigateur, 'attachment' pour forcer le téléchargement
        headers.setContentDispositionFormData("inline", invoiceFileName)
        headers.cacheControl = "must-revalidate, post-check=0, pre-check=0"

        return ResponseEntity(pdfBytes, headers, HttpStatus.OK)
    }
}