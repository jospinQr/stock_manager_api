package com.megamind.StockManagerApi.ai

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api/v1/chat")
class AiControlleur(private val aiService: AiService) {


    @PostMapping("/message")
    fun sendMessage(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        val response = aiService.sendMessage(request.message)
        return ResponseEntity.ok(ChatResponse(response))
    }


    @PostMapping("/totalStock")
    fun askStockTotal(): ResponseEntity<ChatResponse> {

        val response = aiService.askStockTotal()
        return ResponseEntity.ok(ChatResponse(response))

    }


}

data class ChatRequest(val message: String)
data class ChatResponse(val response: String)