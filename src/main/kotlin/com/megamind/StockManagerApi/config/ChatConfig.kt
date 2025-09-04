package com.megamind.StockManagerApi.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.deepseek.DeepSeekChatModel

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatConfiguration {

    @Bean
    fun chatClient(chatModel: DeepSeekChatModel): ChatClient {
        return ChatClient.builder(chatModel)
            .defaultSystem("Vous êtes un assistant IA spécialisé dans la gestion de stock et commercial plus l'analyse de données. Répondez toujours en français de manière claire et précise.")
            .build()
    }
}