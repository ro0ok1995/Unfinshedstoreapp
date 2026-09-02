package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Restore
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.example.core.model.CountryConstants
import com.example.core.model.Customer
import com.example.core.model.Money
import com.example.core.validation.PhoneValidator
import com.example.core.model.Product
import com.example.data.localization.LocalStrings
import com.example.ui.theme.LocalAppThemeColors
import com.example.ui.theme.FinancialDebt
import com.example.ui.theme.FinancialDebtContainer
import com.example.ui.theme.FinancialPayment
import com.example.ui.theme.FinancialPaymentContainer

@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themeColors = LocalAppThemeColors.current
    ThemedHeaderBox(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (navigationIcon != null) {
                    navigationIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                } else if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag("app_header_back_button")
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        ),
                        maxLines = 1
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            if (actions != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    actions()
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF757575), fontSize = 13.sp)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_bar_input"),
        shape = RoundedCornerShape(14.dp),
        singleLine = true
    )
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val (label, bgColor, textColor) = when (status.lowercase()) {
        "active" -> Triple(strings.active, Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "archived" -> Triple(strings.archived, Color(0xFFFFF3E0), Color(0xFFE65100))
        "deleted" -> Triple(strings.deleted, Color(0xFFFFEBEE), Color(0xFFC62828))
        "completed" -> Triple(strings.completedTransactions, Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "cancelled" -> Triple(strings.cancelledTransactions, Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Triple(status, Color(0xFFF5F5F5), Color(0xFF616161))
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
fun LoadingDialog(isLoading: Boolean = true) {
    val strings = LocalStrings.current
    if (isLoading) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = null,
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = strings.loading,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// Dialog for Recording Customer Payment
@Composable
fun RecordPaymentDialog(
    customer: Customer,
    currentDebt: Money,
    onDismiss: () -> Unit,
    onConfirm: (amount: Money, note: String) -> Unit
) {
    val strings = LocalStrings.current
    var amountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.recordPaymentTitle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FinancialDebtContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = customer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.currentDebt + ":",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = currentDebt.format(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = FinancialDebt
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text(strings.paymentAmount + " (₪) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            amountText = currentDebt.formatWithoutSymbol()
                            errorMessage = null
                        }
                    ) {
                        Text(
                            text = strings.payFullDebt,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text(strings.notes + " (${strings.optional})") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val money = Money.fromShekels(amountText)
                    if (!money.isPositive()) {
                        errorMessage = strings.amountMustBeGreaterThanZero
                        return@Button
                    }
                    if (money > currentDebt) {
                        errorMessage = strings.paymentExceedsDebtError
                        return@Button
                    }
                    onConfirm(money, notesText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancialPayment),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_payment_btn")
            ) {
                Text(strings.confirmAndSave, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// Add/Edit Customer Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerDialog(
    customer: Customer? = null,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    val strings = LocalStrings.current
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var countryCode by remember { mutableStateOf(customer?.countryCode ?: CountryConstants.DEFAULT_COUNTRY_CODE) }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var countryDropdownExpanded by remember { mutableStateOf(false) }

    val countryOptions = listOf(
        "+970" to "فلسطين (+970)",
        "+972" to "الداخل (+972)",
        "+962" to "الأردن (+962)",
        "+20" to "مصر (+20)",
        "+966" to "السعودية (+966)",
        "+971" to "الإمارات (+971)",
        "+1" to "أمريكا (+1)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (customer == null) strings.addCustomer else strings.editCustomerDialogTitle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text(strings.customerName + " *") },
                    isError = nameError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Country Code and Phone Number Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = countryDropdownExpanded,
                        onExpandedChange = { countryDropdownExpanded = !countryDropdownExpanded },
                        modifier = Modifier.width(110.dp)
                    ) {
                        OutlinedTextField(
                            value = countryCode,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الدولة", fontSize = 11.sp) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        ExposedDropdownMenu(
                            expanded = countryDropdownExpanded,
                            onDismissRequest = { countryDropdownExpanded = false }
                        ) {
                            countryOptions.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontSize = 13.sp) },
                                    onClick = {
                                        countryCode = code
                                        countryDropdownExpanded = false
                                        if (phone.isNotBlank()) {
                                            val res = PhoneValidator.validatePhone(phone, code)
                                            phoneError = if (!res.isValid) (res as? com.example.core.validation.ValidationResult.Error)?.defaultMessage else null
                                        }
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = null
                        },
                        label = { Text(strings.phoneNumber + " (${strings.optional})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = phoneError != null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("customer_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                if (phoneError != null) {
                    Text(
                        text = phoneError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(strings.customerAddress + " (${strings.optional})") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_address_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().length < 2) {
                        nameError = true
                        return@Button
                    }
                    if (phone.trim().isNotEmpty()) {
                        val validation = PhoneValidator.validatePhone(phone, countryCode)
                        if (!validation.isValid) {
                            phoneError = (validation as? com.example.core.validation.ValidationResult.Error)?.defaultMessage ?: "رقم الهاتف غير صحيح"
                            return@Button
                        }
                    }
                    val updated = customer?.copy(
                        name = name.trim(),
                        phone = phone.trim(),
                        countryCode = countryCode,
                        address = address.trim()
                    ) ?: Customer(
                        name = name.trim(),
                        phone = phone.trim(),
                        countryCode = countryCode,
                        address = address.trim()
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_customer_btn")
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * Decodes and displays a product image from local internal storage.
 * If imagePath is null or cannot be read, falls back to a clean placeholder icon.
 */
@Composable
fun ProductImage(
    imagePath: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholderIcon: ImageVector = Icons.Default.Inventory2,
    placeholderTint: Color = MaterialTheme.colorScheme.primary,
    placeholderBackground: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val bitmap = remember(imagePath) {
        if (!imagePath.isNullOrBlank()) {
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else null
            } catch (e: Exception) {
                null
            }
        } else null
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(placeholderBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = placeholderIcon,
                contentDescription = contentDescription,
                tint = placeholderTint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Copies picked image from content URI into private app files directory.
 */
fun saveImageLocally(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val imagesDir = File(context.filesDir, "product_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val fileName = "product_${System.currentTimeMillis()}.jpg"
        val file = File(imagesDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

// Add/Edit Product Dialog with Image Picker
@Composable
fun AddEditProductDialog(
    product: Product? = null,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    var name by remember { mutableStateOf(product?.name ?: "") }
    var priceText by remember { mutableStateOf(if (product != null) product.price.formatWithoutSymbol() else "") }
    var imagePath by remember { mutableStateOf(product?.imagePath) }
    var nameError by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val localSavedPath = saveImageLocally(context, uri)
            if (localSavedPath != null) {
                imagePath = localSavedPath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) strings.addProduct else strings.editProductDialogTitle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Product Image Preview and Selection
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    ProductImage(
                        imagePath = imagePath,
                        modifier = Modifier.size(100.dp),
                        contentDescription = strings.productImage
                    )

                    // Overlay icon/badge for adding/changing image
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (imagePath.isNullOrBlank()) Icons.Default.AddPhotoAlternate else Icons.Default.Edit,
                            contentDescription = strings.selectProductImage,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.testTag("btn_pick_product_image")
                    ) {
                        Text(
                            text = if (imagePath.isNullOrBlank()) strings.selectProductImage else strings.changeProductImage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!imagePath.isNullOrBlank()) {
                        TextButton(
                            onClick = { imagePath = null },
                            modifier = Modifier.testTag("btn_remove_product_image")
                        ) {
                            Text(
                                text = strings.removeProductImage,
                                fontSize = 12.sp,
                                color = FinancialDebt,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text(strings.productName + " *") },
                    isError = nameError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(strings.defaultPrice + " (₪) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_price_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val money = Money.fromShekels(priceText)
                    val updated = product?.copy(
                        name = name.trim(),
                        price = money,
                        imagePath = imagePath
                    ) ?: Product(
                        name = name.trim(),
                        price = money,
                        imagePath = imagePath
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_product_btn")
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// Direct numeric input dialog to edit quantity via keyboard
@Composable
fun EditQuantityDialog(
    initialQuantity: Double,
    productName: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val strings = LocalStrings.current
    var qtyText by remember {
        mutableStateOf(
            if (initialQuantity <= 0.0) ""
            else if (initialQuantity % 1.0 == 0.0) initialQuantity.toInt().toString()
            else initialQuantity.toString()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.editQuantity,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!productName.isNullOrBlank()) {
                    Text(
                        text = productName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = strings.enterQuantity,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quantity_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("0") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = qtyText.toDoubleOrNull() ?: 0.0
                    onConfirm(if (parsed < 0.0) 0.0 else parsed)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_qty_btn")
            ) {
                Text(strings.confirm, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// Restore Transaction Dialog
@Composable
fun RestoreTransactionDialog(
    transactionId: Long,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val strings = LocalStrings.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.restoreTransaction,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Text(
                text = strings.restoreTransactionPrompt,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_restore_tx_btn")
            ) {
                Text(strings.confirm, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// Cancel Transaction Dialog
@Composable
fun CancelTransactionDialog(
    transactionId: Long,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    val strings = LocalStrings.current
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.cancelTransaction,
                fontWeight = FontWeight.Bold,
                color = FinancialDebt,
                fontSize = 16.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.cancelTransactionPrompt,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(strings.cancellationReason + " (${strings.optional})") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cancel_reason_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                colors = ButtonDefaults.buttonColors(containerColor = FinancialDebt),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_cancel_btn")
            ) {
                Text(strings.confirm, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
