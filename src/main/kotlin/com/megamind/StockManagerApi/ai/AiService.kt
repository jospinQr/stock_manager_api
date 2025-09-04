package com.megamind.StockManagerApi.ai

import com.megamind.StockManagerApi.product.ProductRepository
import com.megamind.StockManagerApi.product.ProductService
import com.megamind.StockManagerApi.stock_mouvement.StockService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service


@Service

class AiService(
    private val chatClient: ChatClient,
    val productService: ProductService,
    val movementService: StockService
) {


    fun sendMessage(message: String): String {

        return chatClient
            .prompt(message)
            .user(message)
            .call()
            .content()!!
    }


    // GESTION DE STOCK - Analyse du stock total
    fun askStockTotal(): String {
        val stockData = productService.findAll()


        val prompt = """
            En tant qu'expert-comptable spécialisé en contrôle de gestion des stocks, analysez la situation globale de l'inventaire selon les normes comptables françaises (PCG) et les principes d'audit.

            DONNÉES D'INVENTAIRE:
            ${stockData}

            ANALYSE COMPTABLE DEMANDÉE:

            1. VALORISATION DES STOCKS:
               - Calcul de la valeur totale des stocks au bilan
               - Vérification de l'application du principe du coût ou valeur nette de réalisation (le plus faible)
               - Identification des stocks obsolètes ou à rotation lente nécessitant une dépréciation
               - Contrôle de cohérence des méthodes de valorisation (FIFO, CMUP)

            2. ANALYSE DE ROTATION:
               - Calcul du ratio de rotation des stocks (nombre de fois par an)
               - Durée moyenne de stockage par catégorie de produits
               - Identification des références en surstockage ou rupture
               - Évaluation du coût de possession financier

            3. CONTRÔLES RÉGLEMENTAIRES:
               - Vérification de l'exhaustivité de l'inventaire physique
               - Contrôle de la séparation des exercices (cut-off)
               - Validation des écritures de régularisation de stocks
               - Respect des obligations d'inventaire permanent

            4. RECOMMANDATIONS DE GESTION:
               - Optimisation des niveaux de stock de sécurité
               - Amélioration du système de réapprovisionnement
               - Plan d'écoulement des stocks dormants
               - Mise en place d'indicateurs de pilotage

            RAPPORT ATTENDU:
            Synthèse avec chiffres clés, alertes comptables et plan d'actions pour optimiser la gestion des stocks.
        """

        return chatClient
            .prompt(prompt)
            .call()
            .content()!!
    }


}