package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.selection.SelectionContainer
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private fun formatFcfa(amount: Double): String {
    return try {
        String.format(java.util.Locale.US, "%,.0f", amount).replace(",", " ") + " FCFA"
    } catch (e: Exception) {
        "${amount.toInt()} FCFA"
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Collect States from Room flows
    val orders by viewModel.allOrders.collectAsState()
    val clients by viewModel.allClients.collectAsState()
    val aiMessages by viewModel.allAiMessages.collectAsState()
    val payments by viewModel.allPayments.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()

    if (!viewModel.isUserLoggedIn) {
        GoogleAuthPortalScreen(viewModel = viewModel)
    } else {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val isWide = maxWidth >= 760.dp

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EcomDarkBg),
                bottomBar = {
                    if (!isWide) {
                        EcomBottomNavigation(
                            currentScreen = viewModel.currentScreen,
                            onScreenSelected = { viewModel.currentScreen = it }
                        )
                    }
                },
                containerColor = Color.Transparent
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(EcomDarkBg)
                ) {
                    if (isWide) {
                        EcomNavigationRail(
                            currentScreen = viewModel.currentScreen,
                            onScreenSelected = { viewModel.currentScreen = it }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Header Bar with User Account Selector
                        EcomHeader(
                            viewModel = viewModel
                        )

                        // Content Screens
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (viewModel.currentScreen) {
                                "dashboard" -> DashboardScreen(
                                    viewModel = viewModel,
                                    orders = orders,
                                    clients = clients,
                                    aiMessages = aiMessages,
                                    payments = payments,
                                    totalRevenue = totalRevenue
                                )
                                "orders" -> OrdersScreen(
                                    viewModel = viewModel,
                                    orders = orders,
                                    clients = clients
                                )
                                "ai_assistant" -> AiAssistantScreen(
                                    viewModel = viewModel,
                                    clients = clients,
                                    messages = aiMessages
                                )
                                "payments" -> PaymentsAndDeliveriesScreen(
                                    viewModel = viewModel,
                                    orders = orders,
                                    payments = payments
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modal Dialogs for Seeding/Adding Form Objects
        if (viewModel.isAddClientDialogOpen) {
            AddClientDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.isAddClientDialogOpen = false }
            )
        }

        if (viewModel.isAddOrderDialogOpen) {
            AddOrderDialog(
                viewModel = viewModel,
                clients = clients,
                onDismiss = { viewModel.isAddOrderDialogOpen = false }
            )
        }
    }
}

// --- Dynamic Side Navigation Rail for Large Screens ---
@Composable
fun EcomNavigationRail(
    currentScreen: String,
    onScreenSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        containerColor = EcomSlateCard,
        header = {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LightMint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = EcomPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        modifier = modifier
            .fillMaxHeight()
            .drawBehind {
                drawLine(
                    color = EcomBorder,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            }
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        val navItems = listOf(
            Triple("dashboard", "Dashboard", Icons.Default.Dashboard),
            Triple("orders", "Commandes", Icons.Default.ShoppingCart),
            Triple("ai_assistant", "Assistant IA", Icons.Default.Psychology),
            Triple("payments", "Paiements", Icons.Default.AccountBalanceWallet)
        )

        navItems.forEach { (route, label, icon) ->
            val isSelected = currentScreen == route
            NavigationRailItem(
                selected = isSelected,
                onClick = { onScreenSelected(route) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) EcomPrimary else EcomTextSecondary,
                        modifier = Modifier.testTag("nav_icon_$route")
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) EcomTextPrimary else EcomTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = EcomBorder
                )
            )
        }
    }
}

// --- Dynamic Header ---
@Composable
fun EcomHeader(
    viewModel: MainViewModel
) {
    var showProfileDialog by remember { mutableStateOf(false) }
    val currentUser = viewModel.currentUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EcomSlateCard)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .drawBehind {
                drawLine(
                    color = EcomBorder,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(StatSuccess)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EcomPilot AI",
                    color = EcomTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }
            Text(
                text = "Dakar Hub • Connecté",
                color = EcomTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live stats badge for instant tracking
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(EcomDarkBg)
                    .border(1.dp, EcomBorder, RoundedCornerShape(24.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Active status",
                    tint = EcomAccent,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Auto-pilote",
                    color = EcomTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Clickable Google Profile Avatar & User Details
            if (currentUser != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showProfileDialog = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 2.dp)
                    ) {
                        Text(
                            text = currentUser.name,
                            color = EcomTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser.role,
                            color = EcomTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Rounded Profile Letter with Google colorful trim
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(LightIndigo)
                            .border(1.5.dp, EcomAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.name.take(1).uppercase(),
                            color = EcomAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Mon Compte Gmail Profile Details & Logout Dialog
    if (showProfileDialog && currentUser != null) {
        Dialog(onDismissRequest = { showProfileDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EcomSlateCard)
                    .border(1.dp, EcomBorder, RoundedCornerShape(16.dp)),
                color = EcomSlateCard
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Larger profile circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(LightIndigo)
                            .border(2.dp, EcomAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.name.take(1).uppercase(),
                            color = EcomAccent,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser.name,
                        color = EcomTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = currentUser.email,
                        color = EcomAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account specs
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EcomDarkBg)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Statut du compte", color = EcomTextSecondary, fontSize = 11.sp)
                            Text("Vérifié par Google", color = StatSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rôle Teranga", color = EcomTextSecondary, fontSize = 11.sp)
                            Text(currentUser.role, color = EcomTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Contact", color = EcomTextSecondary, fontSize = 11.sp)
                            Text(currentUser.phone.ifBlank { "Non renseigné" }, color = EcomTextPrimary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showProfileDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Fermer", color = EcomTextSecondary, fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                showProfileDialog = false
                                viewModel.logout()
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(44.dp)
                                .testTag("logout_profile_dialog_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatError),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Se déconnecter", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- Polished Stripe Navigation Bar ---
@Composable
fun EcomBottomNavigation(
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = EcomSlateCard,
        tonalElevation = 8.dp,
        modifier = Modifier
            .drawBehind {
                drawLine(
                    color = EcomBorder,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val navItems = listOf(
            Triple("dashboard", "Dashboard", Icons.Default.Dashboard),
            Triple("orders", "Commandes", Icons.Default.ShoppingCart),
            Triple("ai_assistant", "Assistant IA", Icons.Default.Psychology),
            Triple("payments", "Paiements", Icons.Default.AccountBalanceWallet)
        )

        navItems.forEach { (route, label, icon) ->
            val isSelected = currentScreen == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(route) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) EcomPrimary else EcomTextSecondary,
                        modifier = Modifier.testTag("nav_icon_$route")
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) EcomTextPrimary else EcomTextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = EcomBorder
                )
            )
        }
    }
}

// --- Reusable Dashboard Metric Box ---
@Composable
fun RowScope.DashboardMetricBox(
    weight: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    subtitle: String,
    subtitleColor: Color
) {
    Column(
        modifier = Modifier
            .weight(weight)
            .clip(RoundedCornerShape(12.dp))
            .background(EcomSlateCard)
            .border(1.dp, EcomBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            color = EcomTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(text = subtitle, color = subtitleColor, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// --- Screen 1: Dashboard intelligent ---
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    orders: List<OrderEntity>,
    clients: List<ClientEntity>,
    aiMessages: List<AiMessageEntity>,
    payments: List<PaymentEntity>,
    totalRevenue: Double
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 760.dp

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Area (Dashboard core content)
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Tableau de Bord",
                        color = EcomTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Automatisez vos plateformes WhatsApp, TikTok, Instagram et Shopify",
                        color = EcomTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Metrics Row (Multi-layered cards)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardMetricBox(
                            weight = 1.3f,
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconColor = StatSuccess,
                            title = "Chiffre d'Affaires",
                            value = formatFcfa(totalRevenue),
                            subtitle = "Wave & OM validés",
                            subtitleColor = StatSuccess
                        )

                        DashboardMetricBox(
                            weight = 1f,
                            icon = Icons.Default.ShoppingBag,
                            iconColor = EcomAccent,
                            title = "Commandes",
                            value = "${orders.size}",
                            subtitle = "92% taux de livraison",
                            subtitleColor = EcomTextSecondary
                        )

                        DashboardMetricBox(
                            weight = 1f,
                            icon = Icons.Default.PeopleOutline,
                            iconColor = EcomPrimary,
                            title = "CRM Vendeurs",
                            value = "${clients.size}",
                            subtitle = "+2 aujourd'hui",
                            subtitleColor = StatSuccess
                        )
                    }

                    Text(
                        text = "Courbe d'Affaires Globale (Sénégal)",
                        color = EcomTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    SalesPerformanceChart(payments = payments)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Actions d'Urgence",
                        color = EcomTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.isAddOrderDialogOpen = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_order_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Nouvelle Commande", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.isAddClientDialogOpen = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_client_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EcomBorder),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, EcomTextSecondary.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = EcomTextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Enregistrer Client", color = EcomTextPrimary, fontSize = 11.sp)
                        }
                    }
                }

                // Right Area (Alerts supporting pane)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(16.dp))
                        .background(EcomSlateCard)
                        .border(1.dp, EcomBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-Pilote IA",
                            color = EcomTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${aiMessages.size} résolus",
                            color = StatSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AlertsSection(
                        viewModel = viewModel,
                        orders = orders,
                        aiMessages = aiMessages
                    )
                }
            }
        } else {
            // Classic mobile scrolling view
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Tableau de Bord",
                    color = EcomTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Automatisez vos plateformes WhatsApp, TikTok, Instagram et Shopify",
                    color = EcomTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Metrics Row (Multi-layered cards)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardMetricBox(
                        weight = 1.3f,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconColor = StatSuccess,
                        title = "Chiffre d'Affaires",
                        value = formatFcfa(totalRevenue),
                        subtitle = "Wave & OM validés",
                        subtitleColor = StatSuccess
                    )

                    DashboardMetricBox(
                        weight = 1f,
                        icon = Icons.Default.ShoppingBag,
                        iconColor = EcomAccent,
                        title = "Commandes",
                        value = "${orders.size}",
                        subtitle = "92% taux de livraison",
                        subtitleColor = EcomTextSecondary
                    )

                    DashboardMetricBox(
                        weight = 1f,
                        icon = Icons.Default.PeopleOutline,
                        iconColor = EcomPrimary,
                        title = "CRM Vendeurs",
                        value = "${clients.size}",
                        subtitle = "+2 aujourd'hui",
                        subtitleColor = StatSuccess
                    )
                }

                Text(
                    text = "Courbe d'Affaires Globale (Sénégal)",
                    color = EcomTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                SalesPerformanceChart(payments = payments)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alertes de l'Auto-Pilote IA",
                        color = EcomTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${aiMessages.size} résolus",
                        color = StatSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                AlertsSection(
                    viewModel = viewModel,
                    orders = orders,
                    aiMessages = aiMessages
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Actions d'Urgence",
                    color = EcomTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.isAddOrderDialogOpen = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_order_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Nouvelle Commande", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.isAddClientDialogOpen = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_client_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EcomBorder),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EcomTextSecondary.copy(alpha = 0.3f))
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = EcomTextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Enregistrer Client", color = EcomTextPrimary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- Custom Sales Flow Canvas Chart ---
@Composable
fun SalesPerformanceChart(payments: List<PaymentEntity>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(EcomSlateCard)
            .border(1.dp, EcomBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Render subtle grid background
            val gridCount = 5
            for (i in 1..gridCount) {
                val y = height * (i.toFloat() / (gridCount + 1))
                drawLine(
                    color = EcomBorder.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Define points representing simulated African Sales Curve over 6 days
            val points = listOf(
                Offset(0f, height * 0.8f),
                Offset(width * 0.2f, height * 0.72f),
                Offset(width * 0.4f, height * 0.40f),
                Offset(width * 0.6f, height * 0.58f),
                Offset(width * 0.8f, height * 0.20f),
                Offset(width, height * 0.15f)
            )

            // Draw Gradient Area under Curve
            val fillPath = Path().apply {
                moveTo(points.first().x, height)
                for (point in points) {
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, height)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        EcomPrimary.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw line curve
            val strokePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (point in points) {
                    lineTo(point.x, point.y)
                }
            }

            drawPath(
                path = strokePath,
                color = EcomPrimary,
                style = Stroke(width = 4f)
            )

            // Draw beautiful node points
            for (point in points) {
                drawCircle(
                    color = EcomAccent,
                    radius = 5f,
                    center = point
                )
            }
        }

        // Overlay legends
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Lun", color = EcomTextSecondary, fontSize = 9.sp)
            Text("Mar", color = EcomTextSecondary, fontSize = 9.sp)
            Text("Mer", color = EcomTextSecondary, fontSize = 9.sp)
            Text("Jeu", color = EcomTextSecondary, fontSize = 9.sp)
            Text("Ven", color = EcomTextSecondary, fontSize = 9.sp)
            Text("Auj", color = StatSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        // Current Top indicator
        Text(
            text = "Max: 350K FCFA",
            color = EcomTextSecondary,
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        )
    }
}

// --- Live autopilot notifications ---
@Composable
fun AlertsSection(
    viewModel: MainViewModel,
    orders: List<OrderEntity>,
    aiMessages: List<AiMessageEntity>
) {
    val pendingCount = orders.count { it.status == "En attente" }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (pendingCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandOrangeMoney.copy(alpha = 0.1f))
                    .border(1.dp, BrandOrangeMoney.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Pending transactions",
                    tint = BrandOrangeMoney,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Validation Requise ($pendingCount)",
                        color = EcomTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Des commandes sont en attente de paiement Wave ou Orange Money.",
                        color = EcomTextSecondary,
                        fontSize = 11.sp
                    )
                }
                TextButton(
                    onClick = { viewModel.currentScreen = "payments" }
                ) {
                    Text(text = "Résoudre", color = BrandOrangeMoney, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // WhatsApp AI simulator notification
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StatSuccess.copy(alpha = 0.1f))
                .border(1.dp, StatSuccess.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "WhatsApp dynamic alerts",
                tint = StatSuccess,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WhatsApp Auto-pilot",
                    color = EcomTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "L'IA d'EcomPilot peut rédiger des réponses pré-approuvées.",
                    color = EcomTextSecondary,
                    fontSize = 11.sp
                )
            }
            TextButton(
                onClick = { viewModel.currentScreen = "ai_assistant" }
            ) {
                Text(text = "Rédiger", color = StatSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Popular Product Item Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(EcomSlateCard)
                .border(1.dp, EcomBorder, RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(EcomPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = EcomAccent, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Top Produit : Robe Wax Moderne", color = EcomTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Dakar, Diamniadio & Touba", color = EcomTextSecondary, fontSize = 11.sp)
                }
                Text("🔥 Populaire", color = StatSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Screen 2: Commandes list ---
@Composable
fun OrdersScreen(
    viewModel: MainViewModel,
    orders: List<OrderEntity>,
    clients: List<ClientEntity>
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 760.dp

        // Filter actual list
        val filteredOrders = orders.filter { o ->
            val matchSearch = o.product.lowercase().contains(viewModel.orderSearchQuery.lowercase()) ||
                    o.clientName.lowercase().contains(viewModel.orderSearchQuery.lowercase())
            val matchStatus = viewModel.orderFilterStatus == "Tous" || o.status == viewModel.orderFilterStatus
            matchSearch && matchStatus
        }

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Filter and Orders List
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Centre de Commandes",
                                color = EcomTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Gérez vos livraisons et paiements en direct",
                                color = EcomTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.isAddClientDialogOpen = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(EcomBorder)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Add Client", tint = EcomTextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = viewModel.orderSearchQuery,
                        onValueChange = { viewModel.orderSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_orders_field"),
                        placeholder = { Text("Rechercher un produit, client...", color = EcomTextSecondary, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EcomTextSecondary, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = EcomTextPrimary,
                            unfocusedTextColor = EcomTextPrimary,
                            focusedBorderColor = EcomPrimary,
                            unfocusedBorderColor = EcomBorder,
                            focusedContainerColor = EcomSlateCard,
                            unfocusedContainerColor = EcomSlateCard
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filters
                    val filters = listOf("Tous", "En attente", "Payé & En cours", "Livré", "Retourné")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filters) { f ->
                            val active = viewModel.orderFilterStatus == f
                            val color = if (active) EcomPrimary else EcomSlateCard
                            val borderCol = if (active) EcomPrimary else EcomBorder

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(color)
                                    .border(1.dp, borderCol, RoundedCornerShape(30.dp))
                                    .clickable { viewModel.orderFilterStatus = f }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = f,
                                    color = EcomTextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredOrders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FilterList, "Empty", tint = EcomTextSecondary, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Aucune commande trouvée", color = EcomTextSecondary, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredOrders) { order ->
                                val isSelected = viewModel.selectedOrderForTrackingId == order.id
                                val cardBorder = if (isSelected) EcomPrimary else EcomBorder
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(EcomSlateCard)
                                        .border(2.dp, cardBorder, RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectedOrderForTrackingId = order.id }
                                ) {
                                    OrderItemCard(
                                        order = order,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Column: Client Detail CRM Card & Flow Actions (Side-by-Side Detail Sheet)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(16.dp))
                        .background(EcomSlateCard)
                        .border(1.dp, EcomBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    val activeId = viewModel.selectedOrderForTrackingId ?: filteredOrders.firstOrNull()?.id
                    val selectedOrder = orders.find { it.id == activeId }

                    if (selectedOrder != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Fiche De Commande",
                                color = EcomTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EcomPrimary.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Active",
                                    color = EcomPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        HorizontalDivider(color = EcomBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                        Text("Produit", color = EcomTextSecondary, fontSize = 10.sp)
                        Text(
                            text = selectedOrder.product,
                            color = EcomTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Client CRM", color = EcomTextSecondary, fontSize = 10.sp)
                                Text(
                                    text = selectedOrder.clientName,
                                    color = EcomTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Prix global", color = EcomTextSecondary, fontSize = 10.sp)
                                Text(
                                    text = formatFcfa(selectedOrder.totalAmount),
                                    color = EcomAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Produit commandé", color = EcomTextSecondary, fontSize = 10.sp)
                                Text(
                                    text = selectedOrder.product,
                                    color = EcomTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Quantité", color = EcomTextSecondary, fontSize = 10.sp)
                                Text(
                                    text = "${selectedOrder.quantity} x",
                                    color = EcomTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Statut Échéance", color = EcomTextSecondary, fontSize = 10.sp)
                        val badgeColor = when (selectedOrder.status) {
                            "Livré" -> StatSuccess
                            "Payé & En cours" -> EcomAccent
                            "En attente" -> StatPending
                            else -> StatError
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedOrder.status,
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = EcomBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 14.dp))

                        Text("Dispatch Rapide & Actions d'Urgence", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.assignAndTrackDelivery(selectedOrder.id, "Modou Diop") },
                                colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Assigner Livreurs (Modou Diop)", fontSize = 11.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.simulateAfricanPaymentGate(selectedOrder.id, "Wave") },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandWaveBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Simuler Wave", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.simulateAfricanPaymentGate(selectedOrder.id, "Orange Money") },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrangeMoney),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Simuler OM", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.startEditOrder(selectedOrder) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EcomBorder),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, EcomTextSecondary.copy(alpha = 0.2f))
                                ) {
                                    Text("Modifier", color = EcomTextPrimary, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { viewModel.deleteOrder(selectedOrder) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatError.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Supprimer", color = StatError, fontSize = 11.sp)
                                }
                            }
                        }

                        if (viewModel.selectedOrderForTrackingId == selectedOrder.id) {
                            HorizontalDivider(color = EcomBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 14.dp))
                            Text("Suivi Temps Réel de cette Livraison", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            DeliveryStepperCard(viewModel = viewModel)
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sélectionnez un panier à gauche pour inspecter.", color = EcomTextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Mobile (Single Column Scroll View)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Centre de Commandes",
                            color = EcomTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Gérez vos ventes au Sénégal depuis un seul tableau",
                            color = EcomTextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.isAddClientDialogOpen = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(EcomBorder)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Client", tint = EcomTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = viewModel.orderSearchQuery,
                    onValueChange = { viewModel.orderSearchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_orders_field"),
                    placeholder = { Text("Rechercher un produit, client...", color = EcomTextSecondary, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EcomTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = EcomTextPrimary,
                        unfocusedTextColor = EcomTextPrimary,
                        focusedBorderColor = EcomPrimary,
                        unfocusedBorderColor = EcomBorder,
                        focusedContainerColor = EcomSlateCard,
                        unfocusedContainerColor = EcomSlateCard
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                val filters = listOf("Tous", "En attente", "Payé & En cours", "Livré", "Retourné")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { f ->
                        val active = viewModel.orderFilterStatus == f
                        val color = if (active) EcomPrimary else EcomSlateCard
                        val borderCol = if (active) EcomPrimary else EcomBorder

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(color)
                                .border(1.dp, borderCol, RoundedCornerShape(30.dp))
                                .clickable { viewModel.orderFilterStatus = f }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = f,
                                color = EcomTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (filteredOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Empty",
                                tint = EcomTextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Aucune commande trouvée", color = EcomTextSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.isAddOrderDialogOpen = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary)
                            ) {
                                Text("Ajouter une Commande", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredOrders) { order ->
                            OrderItemCard(
                                order = order,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Order card item ---
@Composable
fun OrderItemCard(
    order: OrderEntity,
    viewModel: MainViewModel
) {
    val statusColor = when (order.status) {
        "Livré" -> StatSuccess
        "Payé & En cours" -> EcomAccent
        "En attente" -> StatPending
        else -> StatError
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = EcomSlateCard),
        border = BorderStroke(1.dp, EcomBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: ID and Status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Commande  #${order.id}",
                    color = EcomTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor, RoundedCornerShape(30.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = order.status,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Client & Product
            Text(
                text = order.product,
                color = EcomTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.quantity} x • ${order.clientName}",
                    color = EcomTextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = formatFcfa(order.totalAmount),
                    color = EcomTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Divider slate line
            HorizontalDivider(color = EcomBorder, thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tracking actions
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            viewModel.assignAndTrackDelivery(order.id, "Modou Diop")
                            viewModel.currentScreen = "payments"
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EcomBorder)
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = "Track", tint = EcomAccent, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = { viewModel.startEditOrder(order) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EcomBorder)
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EcomTextPrimary, modifier = Modifier.size(15.dp))
                    }

                    IconButton(
                        onClick = { viewModel.deleteOrder(order) },
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatError.copy(alpha = 0.1f))
                            .size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatError, modifier = Modifier.size(15.dp))
                    }
                }

                // Sandbox Mobile Money Action trigger
                if (order.status != "Livré") {
                    Button(
                        onClick = {
                            viewModel.simulateAfricanPaymentGate(order.id, "Wave")
                            viewModel.currentScreen = "payments"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandWaveBlue),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("pay_wave_btn_${order.id}")
                    ) {
                        Text("Payer Wave", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .border(1.dp, StatSuccess, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatSuccess, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reçu émis", color = StatSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- Screen 3: WhatsApp AI Assistant Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: MainViewModel,
    clients: List<ClientEntity>,
    messages: List<AiMessageEntity>
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 760.dp

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Column 1: WhatsApp Response Automation (Left Pane)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(12.dp))
                        .background(EcomSlateCard)
                        .border(1.dp, EcomBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Auto-Pilot Client WhatsApp",
                        color = EcomTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rédigez des réponses personnalisées grâce au CRM",
                        color = EcomTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text("Sélectionnez un client du CRM", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (clients.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EcomDarkBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucun client enregistré.", color = EcomTextSecondary, fontSize = 11.sp)
                        }
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(clients) { client ->
                                val isSelected = viewModel.selectedAiClientId == client.id
                                val border = if (isSelected) EcomPrimary else EcomBorder
                                val bg = if (isSelected) EcomPrimary.copy(alpha = 0.1f) else EcomDarkBg

                                Column(
                                    modifier = Modifier
                                        .width(95.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .border(1.dp, border, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectedAiClientId = client.id }
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(EcomBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = client.name.take(1).uppercase(),
                                            color = EcomTextPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = client.name,
                                        color = EcomTextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = client.socialSource,
                                        color = if (client.socialSource == "WhatsApp") StatSuccess else EcomAccent,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (viewModel.selectedAiClientId != null) {
                        Text("Message reçu du client", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = viewModel.aiClientMessageInput,
                            onValueChange = { viewModel.aiClientMessageInput = it },
                            placeholder = { Text("Ex: Quel est le prix ou livrez vous ?", color = EcomTextSecondary, fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("ai_client_msg_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = EcomTextPrimary,
                                unfocusedTextColor = EcomTextPrimary,
                                focusedBorderColor = EcomPrimary,
                                unfocusedBorderColor = EcomBorder,
                                focusedContainerColor = EcomDarkBg,
                                unfocusedContainerColor = EcomDarkBg
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Suggestions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val quickPrompts = listOf("Prix et livraison ?", "C'est en stock ?", "Je veux payer Wave")
                            quickPrompts.forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EcomBorder)
                                        .clickable { viewModel.aiClientMessageInput = p }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(p, color = EcomTextPrimary, fontSize = 8.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.sendAutomatedAiMessage() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("run_ai_automation_btn"),
                            enabled = !viewModel.aiGeneratingResponse,
                            colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (viewModel.aiGeneratingResponse) {
                                CircularProgressIndicator(color = EcomTextPrimary, modifier = Modifier.size(14.dp))
                            } else {
                                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Générer avec Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EcomDarkBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Veuillez sélectionner un client ci-dessus.", color = EcomTextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Historique des Réponses IA", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (messages.isEmpty()) {
                        Text("Aucune réponse générée", color = EcomTextSecondary, fontSize = 10.sp)
                    } else {
                        messages.forEach { msg ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EcomDarkBg)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Client: ${msg.clientMsgName}", color = EcomAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Dakar AI", color = StatSuccess, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(text = "Client: \"${msg.clientMessage}\"", color = EcomTextSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = msg.aiResponse, color = EcomTextPrimary, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Column 2: Wolof/French Translator Studio (Right Pane)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(12.dp))
                        .background(EcomSlateCard)
                        .border(1.dp, EcomBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Traducteur Multi-Langues",
                        color = EcomTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Traduisez vos annonces Facebook & status TikTok",
                        color = EcomTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text("Rédigez l'annonce en Français", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = viewModel.translateInputText,
                        onValueChange = { viewModel.translateInputText = it },
                        placeholder = { Text("Ex: Robe disponible à Dakar. Livraison rapide. Payez par Wave.", color = EcomTextSecondary, fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp)
                            .testTag("translate_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = EcomTextPrimary,
                            unfocusedTextColor = EcomTextPrimary,
                            focusedBorderColor = EcomPrimary,
                            unfocusedBorderColor = EcomBorder,
                            focusedContainerColor = EcomDarkBg,
                            unfocusedContainerColor = EcomDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.translateProductOrTemplate("Wolof Sénégalais") },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("trans_btn_wolof"),
                            colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary),
                            enabled = !viewModel.translatingMsg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("En Wolof", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.translateProductOrTemplate("Anglais Business") },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("trans_btn_english"),
                            colors = ButtonDefaults.buttonColors(containerColor = EcomBorder),
                            enabled = !viewModel.translatingMsg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, EcomTextSecondary.copy(alpha = 0.3f))
                        ) {
                            Text("En Anglais", color = EcomTextPrimary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Résultat de la Traduction", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EcomDarkBg)
                            .border(1.dp, EcomBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        if (viewModel.translatingMsg) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = EcomAccent, modifier = Modifier.size(16.dp))
                                Text("Traduction...", color = EcomTextSecondary, fontSize = 10.sp)
                            }
                        } else {
                            if (viewModel.translateOutputText.isBlank()) {
                                Text(
                                    text = "Le résultat s'affichera ici.",
                                    color = EcomTextSecondary,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                SelectionContainer {
                                    Text(
                                        text = viewModel.translateOutputText,
                                        color = EcomTextPrimary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MOBILE Layout (Interactive tabs layout as before)
            var activeTab by remember { mutableStateOf("chat") } // chat / translate

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = "Assistant IA Auto-Pilot",
                        color = EcomTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Automatisez vos réponses clients WhatsApp & Facebook",
                        color = EcomTextSecondary,
                        fontSize = 13.sp
                    )
                }

                // Inner tabs selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EcomSlateCard)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTab == "chat") EcomPrimary else Color.Transparent)
                            .clickable { activeTab = "chat" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "WhatsApp Bot",
                            color = EcomTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTab == "translate") EcomPrimary else Color.Transparent)
                            .clickable { activeTab = "translate" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Wolof Traducteur",
                            color = EcomTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (activeTab == "chat") {
                    // WHATSAPP AI ASSISTANT SIMULATOR
                    Text("1. Sélectionnez un client de votre CRM", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Clients Horizontal layout
                    if (clients.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EcomSlateCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucun client enregistré. Enregistrez un client dans l'onglet Commandes.", color = EcomTextSecondary, fontSize = 12.sp)
                        }
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(clients) { client ->
                                val isSelected = viewModel.selectedAiClientId == client.id
                                val border = if (isSelected) EcomPrimary else EcomBorder
                                val bg = if (isSelected) EcomPrimary.copy(alpha = 0.1f) else EcomSlateCard

                                Column(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(bg)
                                        .border(1.dp, border, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.selectedAiClientId = client.id }
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(EcomBorder),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = client.name.take(1).uppercase(),
                                            color = EcomTextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = client.name,
                                        color = EcomTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = client.socialSource,
                                        color = if (client.socialSource == "WhatsApp") StatSuccess else EcomAccent,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (viewModel.selectedAiClientId != null) {
                        // Customer Message form
                        Text("2. Message reçu du client", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = viewModel.aiClientMessageInput,
                            onValueChange = { viewModel.aiClientMessageInput = it },
                            placeholder = { Text("Ex: Quel est le prix ou livrez vous à Dakar Plateau ?", color = EcomTextSecondary, fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(65.dp)
                                .testTag("ai_client_msg_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = EcomTextPrimary,
                                unfocusedTextColor = EcomTextPrimary,
                                focusedBorderColor = EcomPrimary,
                                unfocusedBorderColor = EcomBorder,
                                focusedContainerColor = EcomSlateCard,
                                unfocusedContainerColor = EcomSlateCard
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Quick pre-filled prompt suggestions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val quickPrompts = listOf("Prix et livraison ?", "C'est en stock ?", "Je veux payer Wave")
                            quickPrompts.forEach { p ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EcomBorder)
                                        .clickable { viewModel.aiClientMessageInput = p }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(p, color = EcomTextPrimary, fontSize = 9.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.sendAutomatedAiMessage() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("run_ai_automation_btn"),
                            enabled = !viewModel.aiGeneratingResponse,
                            colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (viewModel.aiGeneratingResponse) {
                                CircularProgressIndicator(color = EcomTextPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Géneration EcomPilot AI...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Générer Réponse Intelligente (Gemini)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EcomSlateCard)
                                .border(1.dp, EcomBorder, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Veuillez sélectionner un client ci-dessus pour lancer l'IA.", color = EcomTextSecondary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Historique des Réponses IA", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucune réponse générée pour l'instant", color = EcomTextSecondary, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages) { msg ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EcomSlateCard)
                                        .border(1.dp, EcomBorder, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Client: ${msg.clientMsgName}", color = EcomAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "WhatsApp AI Response", color = StatSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Client: \"${msg.clientMessage}\"", color = EcomTextSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EcomDarkBg)
                                            .padding(8.dp)
                                    ) {
                                        Text(text = msg.aiResponse, color = EcomTextPrimary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // TRANSLATION ENGINE CO-PILOT
                    Text("Rédigez l'annonce de vente en Français", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = viewModel.translateInputText,
                        onValueChange = { viewModel.translateInputText = it },
                        placeholder = { Text("Ex: Robe disponible à Dakar. Livraison rapide à 1 500 FCFA. Payez par Wave.", color = EcomTextSecondary, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("translate_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = EcomTextPrimary,
                            unfocusedTextColor = EcomTextPrimary,
                            focusedBorderColor = EcomPrimary,
                            unfocusedBorderColor = EcomBorder,
                            focusedContainerColor = EcomSlateCard,
                            unfocusedContainerColor = EcomSlateCard
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.translateProductOrTemplate("Wolof Sénégalais") },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("trans_btn_wolof"),
                            colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary),
                            enabled = !viewModel.translatingMsg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tradduire en Wolof", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.translateProductOrTemplate("Anglais Business") },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("trans_btn_english"),
                            colors = ButtonDefaults.buttonColors(containerColor = EcomBorder),
                            enabled = !viewModel.translatingMsg,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, EcomTextSecondary.copy(alpha = 0.3f))
                        ) {
                            Text("Tradduire en Anglais", color = EcomTextPrimary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Résultat de la Traduction", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EcomSlateCard)
                            .border(1.dp, EcomBorder, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        if (viewModel.translatingMsg) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = EcomAccent, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Traduction intelligente...", color = EcomTextSecondary, fontSize = 12.sp)
                            }
                        } else {
                            if (viewModel.translateOutputText.isBlank()) {
                                Text(
                                    text = "Le résultat traduit par Gemini s'affichera ici pour vos fiches de commandes Facebook et statuts WhatsApp.",
                                    color = EcomTextSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                Column {
                                    SelectionContainer {
                                        Text(
                                            text = viewModel.translateOutputText,
                                            color = EcomTextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Screen 4: Payments and deliveries screen ---
@Composable
fun PaymentsAndDeliveriesScreen(
    viewModel: MainViewModel,
    orders: List<OrderEntity>,
    payments: List<PaymentEntity>
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = "Paiements & Livreurs",
                color = EcomTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Suivi des wallets mobiles et dispatching des livraisons",
                color = EcomTextSecondary,
                fontSize = 13.sp
            )
        }

        // Live Orange Money + Wave Sandbox integration visual
        Text("Passerelle de Paiements Intégrée", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Wave Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BrandWaveBlue.copy(alpha = 0.15f))
                    .border(1.dp, BrandWaveBlue, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(BrandWaveBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("W", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("API Wave", color = EcomTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Dakar Commission : 1%", color = EcomTextSecondary, fontSize = 9.sp)
                Text("Statut : Connecté", color = StatSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // OM Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BrandOrangeMoney.copy(alpha = 0.15f))
                    .border(1.dp, BrandOrangeMoney, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(BrandOrangeMoney),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("OM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Orange Money", color = EcomTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sénégal USSD : *144#", color = EcomTextSecondary, fontSize = 9.sp)
                Text("Statut : Connecté", color = StatSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Live validation Sandbox simulator
        if (viewModel.isSimulatingPayment) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(EcomSlateCard)
                    .border(1.dp, EcomAccent, RoundedCornerShape(10.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = EcomAccent)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Appel API Wave / Orange Money IPN...", color = EcomTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Dakar Sandbox Gateway simulation en cours", color = EcomTextSecondary, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (viewModel.testPaymentResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StatSuccess.copy(alpha = 0.1f))
                    .border(1.dp, StatSuccess, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = StatSuccess, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notification de Paiement Validé", color = EcomTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = viewModel.testPaymentResult!!,
                        color = EcomTextPrimary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🧾 Le reçu e-commerce a été enregistré automatiquement.",
                        color = EcomTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Stepper section for Live Courier Delivery dispatching
        if (viewModel.selectedOrderForTrackingId != null) {
            Text("Suivi Temps Réel du Livreur", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            DeliveryStepperCard(viewModel = viewModel)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Payments ledger lists from Room
        Text("Grand Livre des Mouvements de Wallets", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (payments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucun paiement enregistré pour l'instant.", color = EcomTextSecondary, fontSize = 11.sp)
            }
        } else {
            payments.forEach { payment ->
                PaymentItemRow(payment = payment)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// --- Stepper UI Component for Driver Courier ---
@Composable
fun DeliveryStepperCard(viewModel: MainViewModel) {
    val steps = listOf("Assigné", "Récupéré par Livreur", "En cours", "Livré")
    val currentStep = viewModel.trackingProgressState
    val activeIndex = steps.indexOf(currentStep)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EcomBorder, RoundedCornerShape(10.dp))
            .testTag("delivery_stepper_card"),
        colors = CardDefaults.cardColors(containerColor = EcomSlateCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Livreur assigné : ${viewModel.trackingDriverName}", color = EcomTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Commande #${viewModel.selectedOrderForTrackingId}", color = EcomTextSecondary, fontSize = 11.sp)
                }

                Button(
                    onClick = { viewModel.incrementTrackingProgress() },
                    colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(text = "Étape Suivante", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Render visual dots
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                steps.forEachIndexed { index, title ->
                    val done = index <= activeIndex
                    val isCurrent = index == activeIndex
                    val circleColor = when {
                        isCurrent -> EcomAccent
                        done -> StatSuccess
                        else -> EcomBorder
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Dot
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(circleColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = title,
                            color = if (done) EcomTextPrimary else EcomTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (isCurrent) {
                            Text(text = "Étape active", color = EcomAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- Payment Item Row ---
@Composable
fun PaymentItemRow(payment: PaymentEntity) {
    val methodColor = when (payment.method) {
        "Wave" -> BrandWaveBlue
        "Orange Money" -> BrandOrangeMoney
        else -> BrandFreeMoney
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(EcomSlateCard)
            .border(1.dp, EcomBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(methodColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = payment.method.take(1),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = payment.clientName, color = EcomTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = "Méthode: ${payment.method}", color = EcomTextSecondary, fontSize = 10.sp)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatFcfa(payment.amount),
                color = EcomTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = payment.status,
                color = if (payment.status == "Validé") StatSuccess else StatPending,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- Add Client modal dialogue ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClientDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcomSlateCard),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EcomBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Enregistrer un Client",
                    color = EcomTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = viewModel.clientName,
                    onValueChange = { viewModel.clientName = it },
                    label = { Text("Nom complet du Client") },
                    modifier = Modifier.fillMaxWidth().testTag("add_client_name"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = EcomTextPrimary, unfocusedTextColor = EcomTextPrimary),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.clientPhone,
                    onValueChange = { viewModel.clientPhone = it },
                    label = { Text("Téléphone (Ex: 77 123 45 67)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_client_phone"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = EcomTextPrimary, unfocusedTextColor = EcomTextPrimary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.clientAddress,
                    onValueChange = { viewModel.clientAddress = it },
                    label = { Text("Adresse exacte (Quartier/Rue)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_client_address"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = EcomTextPrimary, unfocusedTextColor = EcomTextPrimary),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler", color = EcomTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.submitNewClient() },
                        colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary)
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}

// --- Add/Edit Order modal dialogue ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderDialog(
    viewModel: MainViewModel,
    clients: List<ClientEntity>,
    onDismiss: () -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    val isEdit = viewModel.selectedOrderForEdit != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EcomSlateCard),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EcomBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isEdit) "Modifier Commande" else "Nouvelle Commande",
                    color = EcomTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (!isEdit) {
                    // Client Selector Dropdown
                    Text("Destinataire (Sénégal CRM)", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    val selectedClient = clients.find { it.id == viewModel.orderSelectedClientId }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EcomDarkBg)
                            .border(1.dp, EcomBorder, RoundedCornerShape(8.dp))
                            .clickable { expandedDropdown = !expandedDropdown }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedClient?.name ?: "Sélectionner un Client",
                                color = if (selectedClient != null) EcomTextPrimary else EcomTextSecondary,
                                fontSize = 13.sp
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = EcomTextPrimary)
                        }
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .background(EcomSlateCard)
                    ) {
                        if (clients.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Aucun client. Créez-en un d'abord !", color = EcomTextSecondary) },
                                onClick = {}
                            )
                        } else {
                            clients.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.name} (${c.city})", color = EcomTextPrimary) },
                                    onClick = {
                                        viewModel.orderSelectedClientId = c.id
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Product details
                OutlinedTextField(
                    value = viewModel.orderProduct,
                    onValueChange = { viewModel.orderProduct = it },
                    label = { Text("Article / Produit") },
                    placeholder = { Text("Ex: Robe Wax Moderne, Chaussures Cuir") },
                    modifier = Modifier.fillMaxWidth().testTag("add_order_product"),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = EcomTextPrimary, unfocusedTextColor = EcomTextPrimary),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = viewModel.orderQuantity,
                        onValueChange = { viewModel.orderQuantity = it },
                        label = { Text("Qté") },
                        modifier = Modifier.weight(1f).testTag("add_order_qty"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = EcomTextPrimary, unfocusedTextColor = EcomTextPrimary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = viewModel.orderAmount,
                        onValueChange = { viewModel.orderAmount = it },
                        label = { Text("Montant (FCFA)") },
                        modifier = Modifier.weight(2f).testTag("add_order_amount"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = EcomTextPrimary, unfocusedTextColor = EcomTextPrimary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status drop selection
                Text("Statut initial", color = EcomTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                val statusOptions = listOf("En attente", "Payé & En cours", "Livré", "Retourné")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(statusOptions) { status ->
                        val active = viewModel.orderStatus == status
                        val bg = if (active) EcomPrimary else EcomDarkBg
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(bg)
                                .border(1.dp, EcomBorder, RoundedCornerShape(30.dp))
                                .clickable { viewModel.orderStatus = status }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(status, color = EcomTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler", color = EcomTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (isEdit) viewModel.submitEditOrder() else viewModel.submitNewOrder()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary)
                    ) {
                        Text(if (isEdit) "Mettre à jour" else "Valider")
                    }
                }
            }
        }
    }
}
