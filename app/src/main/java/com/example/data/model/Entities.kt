package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val role: String, // "Admin", "Vendeur", "Livreur"
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String,
    val city: String,
    val socialSource: String // "WhatsApp", "Facebook", "Tiktok", "Instagram", "Shopify"
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val clientName: String, // De-normalized for easier display
    val product: String,
    val quantity: Int,
    val totalAmount: Double,
    val status: String, // "En attente", "Payé & En cours", "Livré", "Retourné", "Annulé"
    val dateOrder: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val clientMsgName: String, // De-normalized representation of client
    val clientMessage: String,
    val aiResponse: String,
    val dateMsg: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val clientName: String, // De-normalized representation of order's client
    val method: String, // "Wave", "Orange Money", "Free Money"
    val amount: Double,
    val status: String // "Validé", "En attente", "Échoué"
)
