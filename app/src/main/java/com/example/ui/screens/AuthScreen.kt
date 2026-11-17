package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.auth.GoogleAuthHelper
import com.example.auth.PhoneAuthHelper
import com.example.data.toBengaliDigits
import com.example.ui.components.KrishiLogoEmblem
import com.example.ui.theme.BorderGreenTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DeepInk
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.ui.theme.MintBackground
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftMintCard
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    initialPhone: String = "",
    onPhoneEntered: (String) -> Unit = {},
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isGoogleLoading by remember { mutableStateOf(false) }
    var isPhoneLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    var phoneInput by remember { mutableStateOf(initialPhone) }
    var formattedPhone by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var resendSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        GoogleAuthHelper.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    LaunchedEffect(resendSeconds) {
        if (resendSeconds > 0) {
            delay(1000L)
            resendSeconds -= 1
        }
    }

    val isAnyLoading = isGoogleLoading || isPhoneLoading

    fun triggerSendOtp(isResend: Boolean = false) {
        errorMessage = null
        infoMessage = null
        isPhoneLoading = true
        PhoneAuthHelper.sendOtp(
            context = context,
            rawPhone = phoneInput,
            resendToken = if (isResend) resendToken else null,
            scope = scope,
            onCodeSent = { verId, token, e164 ->
                isPhoneLoading = false
                verificationId = verId
                resendToken = token
                formattedPhone = e164
                resendSeconds = 60
                infoMessage = "$e164 নম্বরে ৬ সংখ্যার OTP কোড পাঠানো হয়েছে।"
            },
            onAutoVerified = {
                isPhoneLoading = false
                onAuthSuccess()
            },
            onError = { msg, _ ->
                isPhoneLoading = false
                errorMessage = msg
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepForestGreen, ForestGreen, MintBackground, MintBackground)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 22.dp)
                .widthIn(max = 520.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            KrishiLogoEmblem(size = 80.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "কৃষি বাজার",
                style = MaterialTheme.typography.displaySmall,
                color = PureWhite
            )
            Text(
                text = "Krishi Bazar • স্মার্ট কৃষির জন্য, স্মার্ট সমাধান",
                style = MaterialTheme.typography.titleSmall,
                color = LimeAccent,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "নিরাপদ লগইন ও নিবন্ধন",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DeepInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "মোবাইল নম্বর (OTP) অথবা Google অ্যাকাউন্ট দিয়ে কৃষক বা দোকানদার হিসেবে নিবন্ধন করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSage,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Number Registration / Login Section
                    Surface(
                        color = SoftMintCard,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BorderGreenTint),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.PhoneAndroid,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "মোবাইল নম্বর দিয়ে রেজিস্ট্রেশন / লগইন",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepForestGreen
                                )
                            }

                            if (verificationId == null) {
                                OutlinedTextField(
                                    value = phoneInput,
                                    onValueChange = { next ->
                                        phoneInput = next
                                        onPhoneEntered(next)
                                    },
                                    label = { Text("মোবাইল নম্বর দিন") },
                                    placeholder = { Text("017XXXXXXXX") },
                                    supportingText = {
                                        Text("বাংলাদেশি ১১ সংখ্যার নম্বর (যেমন: 01712345678)")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.PhoneAndroid,
                                            contentDescription = null,
                                            tint = ForestGreen
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    enabled = !isAnyLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_phone_input")
                                )

                                Button(
                                    onClick = { triggerSendOtp(isResend = false) },
                                    enabled = !isAnyLoading,
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldGreen,
                                        contentColor = PureWhite
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("phone_send_otp_button")
                                ) {
                                    if (isPhoneLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = PureWhite,
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Sms,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "OTP কোড পাঠান",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "নম্বর: $formattedPhone",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = DeepInk
                                    )
                                    TextButton(
                                        onClick = {
                                            verificationId = null
                                            otpCode = ""
                                            errorMessage = null
                                            infoMessage = null
                                        },
                                        modifier = Modifier.testTag("phone_change_number_button")
                                    ) {
                                        Text("পরিবর্তন করুন", color = ForestGreen)
                                    }
                                }

                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { raw ->
                                        val clean = PhoneAuthHelper.toAsciiDigits(raw).filter { it.isDigit() }
                                        if (clean.length <= 6) otpCode = clean
                                    },
                                    label = { Text("৬ সংখ্যার OTP কোড লিখুন") },
                                    placeholder = { Text("123456") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                    enabled = !isAnyLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("phone_otp_input")
                                )

                                Button(
                                    onClick = {
                                        val activeVerId = verificationId ?: return@Button
                                        errorMessage = null
                                        isPhoneLoading = true
                                        PhoneAuthHelper.verifyOtp(
                                            verificationId = activeVerId,
                                            otpCode = otpCode,
                                            scope = scope,
                                            onSuccess = {
                                                isPhoneLoading = false
                                                onAuthSuccess()
                                            },
                                            onError = { msg ->
                                                isPhoneLoading = false
                                                errorMessage = msg
                                            }
                                        )
                                    },
                                    enabled = !isAnyLoading,
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldGreen,
                                        contentColor = PureWhite
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("phone_verify_otp_button")
                                ) {
                                    if (isPhoneLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            color = PureWhite,
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "OTP যাচাই করে নিবন্ধন করুন",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { triggerSendOtp(isResend = true) },
                                        enabled = resendSeconds == 0 && !isAnyLoading,
                                        modifier = Modifier.testTag("phone_resend_otp_button")
                                    ) {
                                        Text(
                                            text = if (resendSeconds > 0) {
                                                "পুনরায় পাঠান (${resendSeconds.toBengaliDigits()} সে.)"
                                            } else {
                                                "পুনরায় OTP পাঠান"
                                            },
                                            color = if (resendSeconds == 0) ForestGreen else MutedSage
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = infoMessage != null) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = SoftMintCard,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, EmeraldGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = infoMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepForestGreen,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = errorMessage != null) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGreenTint)
                        Text(
                            text = "  অথবা  ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MutedSage
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGreenTint)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            onPhoneEntered(phoneInput)
                            errorMessage = null
                            isGoogleLoading = true
                            GoogleAuthHelper.onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isGoogleLoading = false
                                    onAuthSuccess()
                                },
                                onAuthError = { msg ->
                                    isGoogleLoading = false
                                    errorMessage = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    isGoogleLoading = false
                                }
                            )
                        },
                        enabled = !isAnyLoading,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.5.dp, ForestGreen),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = PureWhite,
                            contentColor = DeepForestGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isGoogleLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = ForestGreen,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google দিয়ে নিরাপদ লগইন ও নিবন্ধন",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FeatureBullet(
                        icon = Icons.Filled.VerifiedUser,
                        title = "অনুমোদিত ও যাচাইকৃত কৃষি পণ্য",
                        subtitle = "কীটনাশক, সার, বীজ ও সেচ সরঞ্জামের সঠিক নিরাপত্তা তথ্য"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FeatureBullet(
                        icon = Icons.Filled.Storefront,
                        title = "কৃষক ও দোকানদার ড্যাশবোর্ড",
                        subtitle = "সহজ অর্ডার ট্র্যাকিং, স্টক ও বিক্রির হিসাব"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FeatureBullet(
                        icon = Icons.Filled.Shield,
                        title = "গোপনীয়তা ও ডেটা সুরক্ষা",
                        subtitle = "আপনার ব্যক্তিগত তথ্য নিরাপদ ক্লাউডে সুরক্ষিত থাকে"
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureBullet(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SoftMintCard, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ForestGreen,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = DeepInk
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MutedSage
            )
        }
    }
}
