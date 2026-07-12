package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleAuthPortalScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showAccountChooser by remember { mutableStateOf(false) }
    var showCustomRegisterForm by remember { mutableStateOf(false) }

    // Inputs for adding a custom Gmail account
    var customEmail by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var customPhone by remember { mutableStateOf("") }
    var customRole by remember { mutableStateOf("CEO/Admin") }

    val roles = listOf("CEO/Admin", "Vendeur", "Livreur")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EcomDarkBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(16.dp))
                .background(EcomSlateCard)
                .border(1.dp, EcomBorder, RoundedCornerShape(16.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // EcomPilot App Logo Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightMint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EP",
                    color = EcomPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "EcomPilot AI",
                color = EcomTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Assistant Auto-Piloté pour le Sénégal • Teranga",
                color = EcomTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Dynamic Decorative Illustration - E-commerce Hub Graphic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EcomDarkBg)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Drawer circles
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = LightIndigo,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("G", color = EcomAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("← Synchronisé avec →", color = EcomTextSecondary, fontSize = 11.sp)
                        Surface(
                            shape = CircleShape,
                            color = LightMint,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Room", color = EcomPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Base de données & Authentification Cloud prêtes à l'emploi",
                        color = EcomTextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (viewModel.loginIsLoading) {
                // Highly visual login state loading spinner
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator(
                        color = EcomPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.loginStatusText,
                        color = EcomTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Veuillez patienter un moment...",
                        color = EcomTextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                // Interactive Google Registration buttons with 48dp touch targets
                Button(
                    onClick = {
                        showAccountChooser = true
                        showCustomRegisterForm = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_login_primary_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EcomSlateCard),
                    border = BorderStroke(1.dp, EcomBorder),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Drawing miniature Multi-colored Google "G" logo
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "G",
                                color = BrandWaveBlue,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Se connecter avec Google",
                            color = EcomTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        showAccountChooser = true
                        showCustomRegisterForm = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_register_primary_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = "Sign Up",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "S'inscrire avec un compte Gmail",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Small footers about offline fallback / local persistence
                Text(
                    text = "🔒 Données chiffrées localement dans Room SQLite",
                    color = EcomTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // --- Google Accounts Multi-Option Chooser Dialog ---
    if (showAccountChooser) {
        Dialog(onDismissRequest = { showAccountChooser = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EcomSlateCard)
                    .border(1.dp, EcomBorder, RoundedCornerShape(16.dp)),
                color = EcomSlateCard
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choisissez un compte Google",
                        color = EcomTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "pour continuer sur EcomPilot AI",
                        color = EcomTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Predefined simulation contacts for swift testing
                    val suggestedGmails = listOf(
                        Triple("Samba Gaye", "sambagaye1998@gmail.com", "CEO/Admin"),
                        Triple("Diarra Sow", "diarra.sow@gmail.com", "Vendeur"),
                        Triple("Modou Diop", "modou.diop.livreur@gmail.com", "Livreur")
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        suggestedGmails.forEach { (name, email, role) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EcomDarkBg)
                                    .border(1.dp, EcomBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        showAccountChooser = false
                                        viewModel.sAuxLoginGoogle(email, name, "", role)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rounded profile symbol
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EcomPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = name, color = EcomTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = email, color = EcomTextSecondary, fontSize = 11.sp)
                                }
                                // Tiny role chip
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(LightMint)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = role, color = EcomPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Option 4: Custom account details register/login form switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, EcomBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    showAccountChooser = false
                                    showCustomRegisterForm = true
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "+ Utiliser un autre compte Gmail",
                                color = EcomAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    TextButton(onClick = { showAccountChooser = false }) {
                        Text("Annuler", color = EcomTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // --- Elegant Registration Form Dialog inside Google Mock ---
    if (showCustomRegisterForm) {
        Dialog(onDismissRequest = { showCustomRegisterForm = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(EcomSlateCard)
                    .border(1.dp, EcomBorder, RoundedCornerShape(16.dp)),
                color = EcomSlateCard
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Inscription de Compte Gmail",
                        color = EcomTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Dakar Cloud Hub • Entez vos informations",
                        color = EcomTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Email input
                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Adresse Gmail") },
                        placeholder = { Text("ex: sambagaye1998@gmail.com") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_auth_email_field"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcomPrimary,
                            unfocusedBorderColor = EcomBorder
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Name input
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Nom complet") },
                        placeholder = { Text("ex: Samba Gaye") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_auth_name_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcomPrimary,
                            unfocusedBorderColor = EcomBorder
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phone input
                    OutlinedTextField(
                        value = customPhone,
                        onValueChange = { customPhone = it },
                        label = { Text("Téléphone") },
                        placeholder = { Text("ex: 77 123 45 67") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_auth_phone_field"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EcomPrimary,
                            unfocusedBorderColor = EcomBorder
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Role selector label
                    Text(
                        text = "Rôle dans l'organisation",
                        color = EcomTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    )

                    // Quick role pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roles.forEach { r ->
                            val isSel = customRole == r
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) EcomPrimary else EcomDarkBg)
                                    .border(1.dp, if (isSel) EcomPrimary else EcomBorder, RoundedCornerShape(8.dp))
                                    .clickable { customRole = r }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = r,
                                    color = if (isSel) Color.White else EcomTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showCustomRegisterForm = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Annuler", color = EcomTextSecondary, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                if (customEmail.isNotBlank() && customName.isNotBlank()) {
                                    showCustomRegisterForm = false
                                    viewModel.sAuxLoginGoogle(
                                        email = customEmail,
                                        displayName = customName,
                                        phoneVal = customPhone,
                                        roleVal = customRole
                                    )
                                }
                            },
                            modifier = Modifier.weight(1.5f).height(44.dp).testTag("custom_auth_submit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = EcomPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Créer mon compte", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
