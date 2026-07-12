package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiManager
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.EcomRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EcomRepository
    private val geminiManager = GeminiManager()

    // Authentication States
    var currentUser by mutableStateOf<UserEntity?>(null)
    var isUserLoggedIn by mutableStateOf(false)
    var loginIsLoading by mutableStateOf(false)
    var loginStatusText by mutableStateOf("")

    // Screen State Channels
    val allOrders: StateFlow<List<OrderEntity>>
    val allClients: StateFlow<List<ClientEntity>>
    val allUsers: StateFlow<List<UserEntity>>
    val allAiMessages: StateFlow<List<AiMessageEntity>>
    val allPayments: StateFlow<List<PaymentEntity>>

    val clientsCount: StateFlow<Int>
    val ordersCount: StateFlow<Int>
    val totalRevenue: StateFlow<Double>

    // UI Interactive States
    var currentScreen by mutableStateOf("dashboard") // dashboard, orders, ai_assistant, payments
    var selectedOrderForEdit by mutableStateOf<OrderEntity?>(null)
    var isAddOrderDialogOpen by mutableStateOf(false)
    var isAddClientDialogOpen by mutableStateOf(false)

    // Form Temporary Inputs
    var orderProduct by mutableStateOf("")
    var orderQuantity by mutableStateOf("1")
    var orderAmount by mutableStateOf("")
    var orderStatus by mutableStateOf("En attente")
    var orderSelectedClientId by mutableStateOf<Long?>(null)

    var clientName by mutableStateOf("")
    var clientPhone by mutableStateOf("")
    var clientAddress by mutableStateOf("")
    var clientCity by mutableStateOf("Dakar")
    var clientSource by mutableStateOf("WhatsApp")

    // AI Feature States
    var selectedAiClientId by mutableStateOf<Long?>(null)
    var aiClientMessageInput by mutableStateOf("")
    var aiGeneratingResponse by mutableStateOf(false)

    // Translation Utility Helper
    var translateInputText by mutableStateOf("")
    var translateOutputText by mutableStateOf("")
    var translatingMsg by mutableStateOf(false)

    // Payments / Sandbox Orange Money / Wave Operations
    var isSimulatingPayment by mutableStateOf(false)
    var testPaymentResult by mutableStateOf<String?>(null)
    var selectedOrderForPaymentId by mutableStateOf<Long?>(null)
    var paymentMethodSelected by mutableStateOf("Wave")

    // Delivery Sandbox Tracking States
    var selectedOrderForTrackingId by mutableStateOf<Long?>(null)
    var trackingDriverName by mutableStateOf("Modou Diop")
    var trackingProgressState by mutableStateOf("Assigné") // "Assigné", "Récupéré par Livreure", "En cours", "Livré"

    // Search and Filters
    var orderSearchQuery by mutableStateOf("")
    var orderFilterStatus by mutableStateOf("Tous")

    init {
        val db = AppDatabase.getDatabase(application)
        repository = EcomRepository(
            db.userDao(),
            db.clientDao(),
            db.orderDao(),
            db.aiMessageDao(),
            db.paymentDao()
        )

        // Connect Flows with lifecycle aware StateFlows
        allOrders = repository.allOrders
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allClients = repository.allClients
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allUsers = repository.allUsers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allAiMessages = repository.allAiMessages
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allPayments = repository.allPayments
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        clientsCount = repository.clientsCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        ordersCount = repository.ordersCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

        totalRevenue = repository.totalRevenue
            .map { it ?: 0.0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

        // Run seed routing and session recovery sequentially inside a single coroutine
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()

            // Session recovery routine for Gmail / Google Sign-In
            val sharedPrefs = application.getSharedPreferences("ecompilot_prefs", Context.MODE_PRIVATE)
            val savedEmail = sharedPrefs.getString("logged_in_email", null)
            if (savedEmail != null) {
                val foundUser = repository.getUserByEmail(savedEmail)
                if (foundUser != null) {
                    currentUser = foundUser
                    isUserLoggedIn = true
                } else {
                    val newUser = UserEntity(
                        name = savedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = savedEmail,
                        phone = "77 123 45 67",
                        role = "CEO/Admin"
                    )
                    repository.insertUser(newUser)
                    currentUser = repository.getUserByEmail(savedEmail) ?: newUser
                    isUserLoggedIn = true
                }
            }
        }
    }

    // --- Order Operations ---
    fun submitNewOrder() {
        val clientId = orderSelectedClientId ?: return
        val qty = orderQuantity.toIntOrNull() ?: 1
        val amt = orderAmount.toDoubleOrNull() ?: 0.0
        val prod = orderProduct

        if (prod.isBlank()) return

        viewModelScope.launch {
            val client = repository.getClientById(clientId)
            if (client != null) {
                val order = OrderEntity(
                    clientId = clientId,
                    clientName = client.name,
                    product = prod,
                    quantity = qty,
                    totalAmount = amt,
                    status = orderStatus
                )
                val orderId = repository.insertOrder(order)

                // Simultaneously insert a pending payment tracking
                repository.insertPayment(
                    PaymentEntity(
                        orderId = orderId,
                        clientName = client.name,
                        method = paymentMethodSelected,
                        amount = amt,
                        status = if (orderStatus == "Livré") "Validé" else "En attente"
                    )
                )

                // Clean states
                clearOrderForm()
                isAddOrderDialogOpen = false
            }
        }
    }

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }

    fun updateOrderStatus(order: OrderEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrder(order.copy(status = newStatus))
            // Also update the associate payment record if status becomes Livré
            val payments = allPayments.value
            val associatePayment = payments.find { it.orderId == order.id }
            if (associatePayment != null && newStatus == "Livré") {
                repository.updatePayment(associatePayment.copy(status = "Validé"))
            }
        }
    }

    fun startEditOrder(order: OrderEntity) {
        selectedOrderForEdit = order
        orderProduct = order.product
        orderQuantity = order.quantity.toString()
        orderAmount = order.totalAmount.toString()
        orderStatus = order.status
        orderSelectedClientId = order.clientId
        isAddOrderDialogOpen = true
    }

    fun submitEditOrder() {
        val o = selectedOrderForEdit ?: return
        val qty = orderQuantity.toIntOrNull() ?: o.quantity
        val amt = orderAmount.toDoubleOrNull() ?: o.totalAmount
        val prod = orderProduct

        viewModelScope.launch {
            val updated = o.copy(
                product = prod,
                quantity = qty,
                totalAmount = amt,
                status = orderStatus
            )
            repository.updateOrder(updated)

            // Update associated summary payment status too
            val payments = allPayments.value
            val associatePayment = payments.find { it.orderId == o.id }
            if (associatePayment != null) {
                repository.updatePayment(
                    associatePayment.copy(
                        amount = amt,
                        status = if (orderStatus == "Livré") "Validé" else "En attente"
                    )
                )
            }

            clearOrderForm()
            selectedOrderForEdit = null
            isAddOrderDialogOpen = false
        }
    }

    fun clearOrderForm() {
        orderProduct = ""
        orderQuantity = "1"
        orderAmount = ""
        orderStatus = "En attente"
        orderSelectedClientId = null
    }

    // --- Client Operations ---
    fun submitNewClient() {
        val name = clientName
        val phone = clientPhone
        val addr = clientAddress
        val city = clientCity
        val src = clientSource

        if (name.isBlank() || phone.isBlank()) return

        viewModelScope.launch {
            repository.insertClient(
                ClientEntity(
                    name = name,
                    phone = phone,
                    address = addr,
                    city = city,
                    socialSource = src
                )
            )
            // Reset
            clientName = ""
            clientPhone = ""
            clientAddress = ""
            isAddClientDialogOpen = false
        }
    }

    // --- AI Assistant Operations ---
    fun sendAutomatedAiMessage() {
        val cid = selectedAiClientId ?: return
        val msg = aiClientMessageInput
        if (msg.isBlank()) return

        aiGeneratingResponse = true

        viewModelScope.launch {
            val client = repository.getClientById(cid)
            val clientName = client?.name ?: "Client inconnu"

            val systemPrompt = """
                Tu es l'assistant intelligent principal de l'application EcomPilot AI.
                Tu gères les messages des clients e-commerce pour un vendeur de boutique basé au Sénégal (Dakar, Thies, etc.).
                Sois extrêmement poli, professionnel et concis. 
                Suggère l'utilisation des solutions de paiement Wave ou Orange Money qui sont très populaires au Sénégal. Noircis ton ton d'une touche locale sénégalaise chaleureuse ("Teranga").
                Réponds en français fluide (ou wolof si l'utilisateur y fait allusion).
            """.trimIndent()

            val promptText = "Le client $clientName t'envoie le message suivant : \"$msg\". Rédige une réponse automatique de re-marketing ou d'assistance appropriée."

            // Call SDK Manager
            val responseText = geminiManager.generateResponse(promptText, systemPrompt)

            // Save Response in SQLite
            repository.insertAiMessage(
                AiMessageEntity(
                    clientId = cid,
                    clientMsgName = clientName,
                    clientMessage = msg,
                    aiResponse = responseText
                )
            )

            // Reset
            aiClientMessageInput = ""
            aiGeneratingResponse = false
        }
    }

    fun translateProductOrTemplate(targetLang: String) {
        val text = translateInputText
        if (text.isBlank()) return

        translatingMsg = true
        viewModelScope.launch {
            val sysPrompt = "Tu es un traducteur expert e-commerce et localisé pour le Sénégal."
            val prompt = "Traduis le message de vente suivant en $targetLang (si c'est Wolof, utilise l'orthographe standard simplifiée, si c'est français, garde les termes de la Terranga locale) : \"$text\""

            val response = geminiManager.generateResponse(prompt, sysPrompt)
            translateOutputText = response
            translatingMsg = false
        }
    }

    // --- Payments and Mobile Money Integration Simulation ---
    fun simulateAfricanPaymentGate(orderId: Long, method: String) {
        isSimulatingPayment = true
        testPaymentResult = null
        selectedOrderForPaymentId = orderId
        paymentMethodSelected = method

        viewModelScope.launch {
            // Fetch order
            val order = repository.getOrderById(orderId)
            if (order != null) {
                // Simulate delay calling Orange Money USSD or Wave API callback Hook
                kotlinx.coroutines.delay(2000)

                // Perform update status
                val updatedOrder = order.copy(status = "Livré")
                repository.updateOrder(updatedOrder)

                // Update Payments List
                val payments = allPayments.value
                val targetPay = payments.find { it.orderId == orderId }
                if (targetPay != null) {
                    repository.updatePayment(targetPay.copy(status = "Validé", method = method))
                }

                testPaymentResult = "Succès ! Le paiement de ${order.totalAmount} FCFA par $method pour la commande #${order.id} (${order.product}) a été validé avec succès par la passerelle de Dakar."
            } else {
                testPaymentResult = "Erreur : Commande non trouvée dans la base de données."
            }
            isSimulatingPayment = false
        }
    }

    // --- Delivery Tracking System ---
    fun assignAndTrackDelivery(orderId: Long, deliveryMan: String) {
        selectedOrderForTrackingId = orderId
        trackingDriverName = deliveryMan
        trackingProgressState = "Assigné"
    }

    fun incrementTrackingProgress() {
        trackingProgressState = when (trackingProgressState) {
            "Assigné" -> "Récupéré par Livreur"
            "Récupéré par Livreur" -> "En cours"
            "En cours" -> {
                // When status is delivered, we can also set the order entity as Livré in Room!
                selectedOrderForTrackingId?.let { oid ->
                    viewModelScope.launch {
                        val order = repository.getOrderById(oid)
                        if (order != null) {
                            repository.updateOrder(order.copy(status = "Livré"))
                            val payments = allPayments.value
                            val assoc = payments.find { it.orderId == oid }
                            if (assoc != null) {
                                repository.updatePayment(assoc.copy(status = "Validé"))
                            }
                        }
                    }
                }
                "Livré"
            }
            else -> "Assigné"
        }
    }

    // --- Google/Gmail Authentication Flows ---
    fun sAuxLoginGoogle(email: String, displayName: String, phoneVal: String, roleVal: String) {
        loginIsLoading = true
        loginStatusText = "Appel des Services Google d'identification..."
        viewModelScope.launch {
            delay(1000)
            loginStatusText = "Algorithme OAuth 2.0 sécurisé en cours..."
            delay(1000)
            loginStatusText = "Synchronisation de la base de données locale..."
            delay(500)
            
            // Check if user already exists
            var existingUser = repository.getUserByEmail(email)
            
            if (existingUser == null) {
                val newUser = UserEntity(
                    name = displayName,
                    email = email,
                    phone = phoneVal.ifBlank { "77 000 00 00" },
                    role = roleVal.ifBlank { "CEO/Admin" }
                )
                repository.insertUser(newUser)
                // Get newly inserted user back
                existingUser = repository.getUserByEmail(email) ?: newUser
            }
            
            // Persist Session
            val sharedPrefs = getApplication<Application>().getSharedPreferences("ecompilot_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().putString("logged_in_email", email).apply()
            
            currentUser = existingUser
            isUserLoggedIn = true
            loginIsLoading = false
            loginStatusText = ""
        }
    }

    fun logout() {
        viewModelScope.launch {
            val sharedPrefs = getApplication<Application>().getSharedPreferences("ecompilot_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().remove("logged_in_email").apply()
            currentUser = null
            isUserLoggedIn = false
        }
    }
}
