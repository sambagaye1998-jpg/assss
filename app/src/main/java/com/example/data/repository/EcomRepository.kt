package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class EcomRepository(
    private val userDao: UserDao,
    private val clientDao: ClientDao,
    private val orderDao: OrderDao,
    private val aiMessageDao: AiMessageDao,
    private val paymentDao: PaymentDao
) {
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allClients: Flow<List<ClientEntity>> = clientDao.getAllClients()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val allAiMessages: Flow<List<AiMessageEntity>> = aiMessageDao.getAllAiMessages()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    val clientsCount: Flow<Int> = clientDao.getClientsCount()
    val ordersCount: Flow<Int> = orderDao.getOrdersCount()
    val totalRevenue: Flow<Double?> = orderDao.getTotalRevenue()

    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)
    suspend fun getAllUsersList(): List<UserEntity> = userDao.getAllUsersList()

    suspend fun insertClient(client: ClientEntity) = clientDao.insertClient(client)
    suspend fun deleteClient(client: ClientEntity) = clientDao.deleteClient(client)
    suspend fun getClientById(id: Long) = clientDao.getClientById(id)

    suspend fun insertOrder(order: OrderEntity): Long = orderDao.insertOrder(order)
    suspend fun updateOrder(order: OrderEntity) = orderDao.updateOrder(order)
    suspend fun deleteOrder(order: OrderEntity) = orderDao.deleteOrder(order)
    suspend fun getOrderById(id: Long) = orderDao.getOrderById(id)

    suspend fun insertAiMessage(message: AiMessageEntity) = aiMessageDao.insertAiMessage(message)
    suspend fun deleteAiMessage(message: AiMessageEntity) = aiMessageDao.deleteAiMessage(message)

    suspend fun insertPayment(payment: PaymentEntity) = paymentDao.insertPayment(payment)
    suspend fun updatePayment(payment: PaymentEntity) = paymentDao.updatePayment(payment)
    suspend fun deletePayment(payment: PaymentEntity) = paymentDao.deletePayment(payment)

    suspend fun seedDatabaseIfEmpty() {
        // We evaluate if clients list is empty to avoid double-seeding.
        val existingClients = clientDao.getAllClientsList()
        if (existingClients.isEmpty()) {
            // Seed a standard admin user
            userDao.insertUser(UserEntity(name = "Samba Gaye", email = "sambagaye@ecompilot.ai", phone = "771234567", role = "CEO/Admin"))
            userDao.insertUser(UserEntity(name = "Diarra Sow", email = "diarra@ecompilot.ai", phone = "762223344", role = "Vendeur"))
            userDao.insertUser(UserEntity(name = "Modou Diop", email = "modou@ecompilot.ai", phone = "705556677", role = "Livreur"))

            // Seed clients
            val c1 = clientDao.insertClient(ClientEntity(name = "Fatou Diop", phone = "77 123 45 67", address = "Dakar Plateau, Immeuble Wolof", city = "Dakar", socialSource = "WhatsApp"))
            val c2 = clientDao.insertClient(ClientEntity(name = "Moussa Ndiaye", phone = "76 987 65 43", address = "Mermoz, Rue de l'Université", city = "Dakar", socialSource = "Tiktok"))
            val c3 = clientDao.insertClient(ClientEntity(name = "Astou Sene", phone = "78 456 12 34", address = "Santhiaba, Thies Ville", city = "Thies", socialSource = "Instagram"))
            val c4 = clientDao.insertClient(ClientEntity(name = "Amadou Diallo", phone = "70 333 44 55", address = "Sor, Pont Faidherbe", city = "Saint-Louis", socialSource = "Shopify"))
            val c5 = clientDao.insertClient(ClientEntity(name = "Khady Fall", phone = "75 888 99 00", address = "Medina, Avenue Blaise Diagne", city = "Dakar", socialSource = "Facebook"))

            // Seed orders
            val o1 = orderDao.insertOrder(OrderEntity(clientId = c1, clientName = "Fatou Diop", product = "Robe Wax Moderne", quantity = 1, totalAmount = 18500.0, status = "Livré"))
            val o2 = orderDao.insertOrder(OrderEntity(clientId = c2, clientName = "Moussa Ndiaye", product = "Chaussures Cuir Artisanal", quantity = 2, totalAmount = 35000.0, status = "Payé & En cours"))
            val o3 = orderDao.insertOrder(OrderEntity(clientId = c3, clientName = "Astou Sene", product = "Sac à main Cuir Premium", quantity = 1, totalAmount = 24000.0, status = "En attente"))
            val o4 = orderDao.insertOrder(OrderEntity(clientId = c4, clientName = "Amadou Diallo", product = "Gamme Parfums Ambre", quantity = 1, totalAmount = 45000.0, status = "Retourné"))

            // Seed AI messages
            aiMessageDao.insertAiMessage(AiMessageEntity(
                clientId = c1,
                clientMsgName = "Fatou Diop",
                clientMessage = "Robe Wax bi amna Dakar?",
                aiResponse = "Bonjour Fatou! Oui, notre Robe Wax Moderne est actuellement disponible en stock à Dakar Plateau. Il nous reste 3 exemplaires. Voulez-vous planifier une livraison aujourd'hui ?"
            ))
            aiMessageDao.insertAiMessage(AiMessageEntity(
                clientId = c2,
                clientMsgName = "Moussa Ndiaye",
                clientMessage = "Taille 42 amna ci mbax ?",
                aiResponse = "Wa alaykoum salam Moussa! Oui, la taille 42 est entièrement disponible en stock. Nous pouvons l'expédier dès maintenant avec paiement à la livraison via Wave."
            ))

            // Seed payments
            paymentDao.insertPayment(PaymentEntity(orderId = o1, clientName = "Fatou Diop", method = "Wave", amount = 18500.0, status = "Validé"))
            paymentDao.insertPayment(PaymentEntity(orderId = o2, clientName = "Moussa Ndiaye", method = "Orange Money", amount = 35000.0, status = "Validé"))
            paymentDao.insertPayment(PaymentEntity(orderId = o3, clientName = "Astou Sene", method = "Wave", amount = 24000.0, status = "En attente"))
        }
    }
}
