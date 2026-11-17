package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.auth.PhoneAuthHelper
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.theme.BorderGreenTint
import com.example.ui.theme.DeepForestGreen
import com.example.ui.theme.DeepInk
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintBackground
import com.example.ui.theme.MutedSage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftMintCard

@Composable
fun RegistrationScreen(
    existingProfile: UserProfile?,
    defaultName: String,
    defaultPhone: String = "",
    isPhoneVerified: Boolean = false,
    isBusy: Boolean,
    onSubmit: (
        name: String,
        phone: String,
        district: String,
        role: UserRole,
        shopName: String,
        shopAddress: String,
        nidOrLicense: String
    ) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    var selectedRole by remember(existingProfile) {
        mutableStateOf(UserRole.fromRaw(existingProfile?.role))
    }
    var name by remember(existingProfile) {
        mutableStateOf(existingProfile?.name?.ifBlank { defaultName } ?: defaultName)
    }
    var phone by remember(existingProfile, defaultPhone) {
        mutableStateOf(existingProfile?.phone?.ifBlank { defaultPhone } ?: defaultPhone)
    }
    var district by remember(existingProfile) { mutableStateOf(existingProfile?.district ?: "") }
    var shopName by remember(existingProfile) { mutableStateOf(existingProfile?.shopName ?: "") }
    var shopAddress by remember(existingProfile) { mutableStateOf(existingProfile?.shopAddress ?: "") }
    var nidOrLicense by remember(existingProfile) { mutableStateOf(existingProfile?.nidOrLicense ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MintBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 560.dp)) {
            Text(
                text = if (existingProfile == null) "অ্যাকাউন্ট নিবন্ধন করুন" else "প্রোফাইল সম্পাদনা",
                style = MaterialTheme.typography.headlineMedium,
                color = ForestGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "আপনার পরিচয় নির্বাচন করুন এবং মোবাইল নম্বরসহ প্রয়োজনীয় তথ্য দিন",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSage
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleOptionCard(
                    title = "কৃষক অ্যাকাউন্ট",
                    subtitle = "পণ্য ক্রয় ও অর্ডার ট্র্যাকিং",
                    selected = selectedRole == UserRole.FARMER,
                    icon = Icons.Filled.Agriculture,
                    onClick = { selectedRole = UserRole.FARMER },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("role_farmer_card")
                )
                RoleOptionCard(
                    title = "দোকানদার অ্যাকাউন্ট",
                    subtitle = "পণ্য বিক্রি ও স্টক পরিচালনা",
                    selected = selectedRole == UserRole.SELLER,
                    icon = Icons.Filled.Storefront,
                    onClick = { selectedRole = UserRole.SELLER },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("role_seller_card")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, BorderGreenTint)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("আপনার পূর্ণ নাম *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_name_input")
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("মোবাইল নম্বর (যেমন: 017XXXXXXXX) *") },
                        supportingText = if (isPhoneVerified) {
                            {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "OTP দ্বারা যাচাইকৃত মোবাইল নম্বর",
                                        color = DeepForestGreen
                                    )
                                }
                            }
                        } else {
                            { Text("১১ সংখ্যার বাংলাদেশি নম্বর দিন (01XXXXXXXXX)") }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_phone_input")
                    )

                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("জেলা / উপজেলা *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_district_input")
                    )

                    if (selectedRole == UserRole.SELLER) {
                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("দোকানের নাম *") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_shop_name_input")
                        )
                        OutlinedTextField(
                            value = shopAddress,
                            onValueChange = { shopAddress = it },
                            label = { Text("দোকানের পূর্ণ ঠিকানা *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_shop_address_input")
                        )
                        OutlinedTextField(
                            value = nidOrLicense,
                            onValueChange = { nidOrLicense = it },
                            label = { Text("ট্রেড লাইসেন্স / এনআইডি নম্বর *") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_nid_input")
                        )
                    }

                    if (validationError != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = validationError.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            val formattedPhone = PhoneAuthHelper.formatDisplayPhone(phone)
                            when {
                                name.trim().length < 2 -> validationError = "কমপক্ষে ২ অক্ষরের নাম লিখুন।"
                                !PhoneAuthHelper.isValidPhoneForProfile(phone) ->
                                    validationError = "সঠিক ১১ সংখ্যার বাংলাদেশি মোবাইল নম্বর দিন (যেমন: 01712345678)।"
                                district.trim().length < 2 -> validationError = "জেলার নাম লিখুন।"
                                selectedRole == UserRole.SELLER && shopName.trim().length < 2 ->
                                    validationError = "দোকানের নাম লিখুন।"
                                selectedRole == UserRole.SELLER && shopAddress.trim().length < 3 ->
                                    validationError = "দোকানের ঠিকানা লিখুন।"
                                else -> {
                                    validationError = null
                                    onSubmit(
                                        name,
                                        formattedPhone,
                                        district,
                                        selectedRole,
                                        shopName,
                                        shopAddress,
                                        nidOrLicense
                                    )
                                }
                            }
                        },
                        enabled = !isBusy,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("reg_submit_button")
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = PureWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (existingProfile == null) "নিবন্ধন সম্পন্ন করুন" else "তথ্য হালনাগাদ করুন",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (onCancel != null) {
                        Button(
                            onClick = onCancel,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftMintCard,
                                contentColor = ForestGreen
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("ফিরে যান")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) SoftMintCard else PureWhite
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) EmeraldGreen else BorderGreenTint
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) ForestGreen else MutedSage,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
