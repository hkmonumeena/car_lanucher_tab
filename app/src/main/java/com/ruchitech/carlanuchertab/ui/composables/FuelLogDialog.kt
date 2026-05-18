package com.ruchitech.carlanuchertab.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ruchitech.carlanuchertab.R
import com.ruchitech.carlanuchertab.helper.createFuelLogEntry
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog
import com.ruchitech.carlanuchertab.roomdb.data.FuelQuickFillHints
import java.text.NumberFormat
import java.util.Locale

@Composable
private fun FuelInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            //focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            // unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        ),
        visualTransformation = if (keyboardType == KeyboardType.Number || keyboardType == KeyboardType.Decimal) ThousandSeparatorTransformation()
        else VisualTransformation.None
    )
}

@Composable
private fun FuelDisplayBox(
    value: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevation by animateDpAsState(if (isPressed) 2.dp else 6.dp)
    val borderWidth by animateDpAsState(if (isActive) 2.dp else 1.dp)
    val scale by animateFloatAsState(
        if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = 200f)
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    radius = 24.dp,
                    color = Color(0xFF5D8BF4).copy(alpha = 0.2f)
                ),
                onClick = onClick
            )
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            ),
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(16.dp),
                    clip = true,
                    ambientColor = Color(0xFF3A5A78),
                    spotColor = Color(0xFF102A3F)
                )
                .background(
                    color = Color(0xFF2A3D52),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = borderWidth,
                    color = if (isActive) Color(0xFF5D8BF4)
                    else Color(0xFF3A5A78).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (value.isNotEmpty()) value else "0",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )

                if (isActive) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Edit, // Your edit icon
                        contentDescription = "Edit",
                        tint = Color(0xFF5D8BF4),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NumericKeypad(
    onKeyPressed: (String) -> Unit,
    onDeletePressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "⌫")
    val interactionSource = remember { MutableInteractionSource() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp)
    ) {
        items(keys) { key ->
            val isDelete = key == "⌫"
            val isDot = key == "."
            val isZero = key == "0"

            val backgroundColor by animateColorAsState(
                targetValue = when {
                    isDelete -> Color(0xFFE53935).copy(alpha = 0.2f)  // Soft red for delete
                    isDot -> Color(0xFF5D8BF4).copy(alpha = 0.2f)     // Blue tint for dot
                    else -> Color(0xFF2A3D52).copy(alpha = 0.6f)      // Dark blue-gray for numbers
                },
                animationSpec = tween(durationMillis = 150)
            )

            val textColor by animateColorAsState(
                targetValue = when {
                    isDelete -> Color(0xFFE53935)  // Bright red for delete
                    isDot -> Color(0xFF5D8BF4)    // Blue for dot
                    else -> Color(0xFFE2E8F0)     // Light gray for numbers
                }
            )

            val elevation by animateDpAsState(
                targetValue = 4.dp,
                animationSpec = tween(durationMillis = 150)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(55.dp)
                    .shadow(
                        elevation = elevation,
                        shape = RoundedCornerShape(16.dp),
                        clip = true,
                        ambientColor = Color(0xFF3A5A78),
                        spotColor = Color(0xFF102A3F)
                    )
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF3A5A78).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(
                            bounded = true,
                            color = Color(0xFF5D8BF4).copy(alpha = 0.2f)
                        ),
                        onClick = {
                            if (isDelete) {
                                onDeletePressed()
                            } else {
                                onKeyPressed(key)
                            }
                        }
                    )
                /*          .pointerInput(Unit) {
                              detectTapGestures(
                                  onPress = { *//* You could add haptic feedback here *//* },
                            onTap = {
                                onKeyPressed(key)
                            }
                        )
                    }*/
            ) {
                Text(
                    text = key,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (isDelete) 0.sp else 0.5.sp
                    ),
                    color = textColor,
                    modifier = Modifier
                        .scale(if (isDelete) 0.9f else 1f)
                )
            }
        }
    }
}


private val FuelPresetRupeeAmounts = listOf(200, 400, 450, 500, 600, 700, 800, 1000)

private fun parseFuelField(raw: String): Double? =
    raw.takeIf { it.isNotBlank() && it != "." }?.toDoubleOrNull()

private fun roundTwo(value: Double): Double =
    kotlin.math.round(value * 100.0) / 100.0

private fun formatRupee(value: Double): String {
    if (value <= 0.0 || !value.isFinite()) return ""
    return kotlin.math.round(value).toInt().toString()
}

private fun formatDecimalField(value: Double): String {
    if (value <= 0.0 || !value.isFinite()) return ""
    return String.format(Locale.US, "%.2f", roundTwo(value))
}

/**
 * Keep the field the user is editing; derive exactly one companion field.
 * Priority when amount + price exist → liters; amount + liters → price; price + liters → amount.
 */
private fun syncFuelFields(
    active: TextFieldType,
    rupee: String,
    price: String,
    liters: String,
): Triple<String, String, String> {
    var r = rupee
    var p = price
    var l = liters

    val rupeeVal = parseFuelField(r)
    val priceVal = parseFuelField(p)
    val litersVal = parseFuelField(l)

    when (active) {
        TextFieldType.AMOUNT -> {
            if (rupeeVal == null || rupeeVal <= 0) return Triple(r, p, l)
            when {
                priceVal != null && priceVal > 0 -> l = formatDecimalField(rupeeVal / priceVal)
                litersVal != null && litersVal > 0 -> p = formatDecimalField(rupeeVal / litersVal)
            }
        }

        TextFieldType.PRICE -> {
            if (priceVal == null || priceVal <= 0) return Triple(r, p, l)
            when {
                rupeeVal != null && rupeeVal > 0 -> l = formatDecimalField(rupeeVal / priceVal)
                litersVal != null && litersVal > 0 -> r = formatRupee(priceVal * litersVal)
            }
        }

        TextFieldType.LITERS -> {
            if (litersVal == null || litersVal <= 0) return Triple(r, p, l)
            when {
                rupeeVal != null && rupeeVal > 0 -> p = formatDecimalField(rupeeVal / litersVal)
                priceVal != null && priceVal > 0 -> r = formatRupee(priceVal * litersVal)
            }
        }

        else -> Unit
    }
    return Triple(r, p, l)
}

@Composable
fun FuelLogDialog(
    onDismiss: () -> Unit,
    onSubmit: (FuelLog) -> Unit,
    quickFillHints: FuelQuickFillHints? = null,
) {
    var rupee by remember { mutableStateOf("") }
    var fuelPrice by remember { mutableStateOf("") }
    var liters by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var activeField by remember { mutableStateOf<TextFieldType?>(null) }
    val rupeeVal = rupee.toIntOrNull()
    val priceVal = fuelPrice.toFloatOrNull()
    val litersVal = liters.toFloatOrNull()
    val isFormValid = rupeeVal != null && rupeeVal > 0

    fun applyFieldSync(
        active: TextFieldType,
        newRupee: String = rupee,
        newPrice: String = fuelPrice,
        newLiters: String = liters,
    ) {
        val (r, p, l) = syncFuelFields(active, newRupee, newPrice, newLiters)
        rupee = r
        fuelPrice = p
        liters = l
    }

    LaunchedEffect(Unit) {
        activeField = TextFieldType.AMOUNT
        quickFillHints?.lastPricePerLiter?.takeIf { it > 0f }?.let { p ->
            fuelPrice = formatDecimalField(p.toDouble())
        }
    }

    val onKeyPressed = { key: String ->
        when (activeField) {
            TextFieldType.AMOUNT -> {
                if (key != ".") {
                    val newRupee = if (rupee == "0") key else rupee + key
                    applyFieldSync(TextFieldType.AMOUNT, newRupee = newRupee)
                }
            }

            TextFieldType.PRICE -> {
                if (fuelPrice.contains(".") && fuelPrice.substringAfter(".").length >= 2 && key != "⌫") {
                    // max 2 decimal places while typing
                } else if (key != "." || !fuelPrice.contains(".")) {
                    val newPrice = if (fuelPrice == "0") key else fuelPrice + key
                    applyFieldSync(TextFieldType.PRICE, newPrice = newPrice)
                }
            }

            TextFieldType.LITERS -> {
                if (liters.contains(".") && liters.substringAfter(".").length >= 2 && key != "⌫") {
                    // max 2 decimal places while typing
                } else if (key != "." || !liters.contains(".")) {
                    val newLiters = if (liters == "0") key else liters + key
                    applyFieldSync(TextFieldType.LITERS, newLiters = newLiters)
                }
            }

            else -> {}
        }
    }

    val onDeletePressed = {
        when (activeField) {
            TextFieldType.AMOUNT -> {
                val newRupee = if (rupee.length <= 1) "" else rupee.dropLast(1)
                if (newRupee.isEmpty()) {
                    rupee = ""
                    liters = ""
                } else {
                    applyFieldSync(TextFieldType.AMOUNT, newRupee = newRupee)
                }
            }

            TextFieldType.PRICE -> {
                val newPrice = if (fuelPrice.length <= 1) "" else fuelPrice.dropLast(1)
                if (newPrice.isEmpty() || newPrice == ".") {
                    fuelPrice = ""
                    liters = ""
                } else {
                    applyFieldSync(TextFieldType.PRICE, newPrice = newPrice)
                }
            }

            TextFieldType.LITERS -> {
                val newLiters = if (liters.length <= 1) "" else liters.dropLast(1)
                if (newLiters.isEmpty() || newLiters == ".") {
                    liters = ""
                } else {
                    applyFieldSync(TextFieldType.LITERS, newLiters = newLiters)
                }
            }

            else -> {}
        }
    }
    // Background dimming
    BackHandler(enabled = true, onBack = onDismiss)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 24.dp,
            color = Color(0xFF1E293B) // Dark blue-gray
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Form fields
                    Column(
                        modifier = Modifier.weight(0.5f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FuelDisplayBox(
                            value = rupee,
                            label = "Amount (₹)",
                            isActive = activeField == TextFieldType.AMOUNT,
                            onClick = {

                                activeField = TextFieldType.AMOUNT
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FuelPresetRupeeAmounts.forEach { preset ->
                                OutlinedButton(
                                    onClick = {
                                        activeField = TextFieldType.AMOUNT
                                        applyFieldSync(
                                            TextFieldType.AMOUNT,
                                            newRupee = preset.toString(),
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    border = BorderStroke(1.dp, Color(0xFF3A5A78)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFE2E8F0),
                                    ),
                                ) {
                                    Text("₹$preset", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        quickFillHints?.lastPricePerLiter?.takeIf { it > 0f }?.let { lastPrice ->
                            OutlinedButton(
                                onClick = {
                                    activeField = TextFieldType.PRICE
                                    val priceStr = formatDecimalField(lastPrice.toDouble())
                                    if (rupee.isNotEmpty()) {
                                        applyFieldSync(
                                            TextFieldType.PRICE,
                                            newPrice = priceStr,
                                        )
                                    } else {
                                        quickFillHints.lastLiters?.takeIf { it > 0f }?.let { lastL ->
                                            applyFieldSync(
                                                TextFieldType.PRICE,
                                                newPrice = priceStr,
                                                newLiters = formatDecimalField(lastL.toDouble()),
                                            )
                                        } ?: run {
                                            fuelPrice = priceStr
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF5D8BF4).copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF5D8BF4),
                                ),
                            ) {
                                Text(
                                    "Same as last (₹%.2f/L)".format(lastPrice),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FuelDisplayBox(
                                value = fuelPrice,
                                label = "Price / Litre",
                                isActive = activeField == TextFieldType.PRICE,
                                onClick = { activeField = TextFieldType.PRICE },
                                modifier = Modifier.weight(1f)
                            )

                            FuelDisplayBox(
                                value = liters,
                                label = "Litres (optional)",
                                isActive = activeField == TextFieldType.LITERS,
                                onClick = { activeField = TextFieldType.LITERS },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Location field (still using TextField as it's text input)



                        LocationDisplayBox(
                            value = location,
                            onValueChange = {
                                activeField = TextFieldType.LOCATION
                                location = it
                            },
                            label = "Location (optional)",
                            isActive = activeField == TextFieldType.LOCATION,
                            keyboardType = KeyboardType.Text
                        )

                        Spacer(modifier = Modifier.height(0.dp))
                    }
                    // Numeric Keypad
                    Column(
                        modifier = Modifier.weight(0.5f), verticalArrangement = Arrangement.Center
                    ) {
                        NumericKeypad(
                            onKeyPressed = onKeyPressed,
                            onDeletePressed = onDeletePressed,
                            modifier = Modifier.fillMaxWidth()
                        )

                    }
                }

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cancel Button - Now with more prominent outline
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp), // Taller for better touch target
                        shape = RoundedCornerShape(12.dp), // More rounded
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF94A3B8), // Cool gray
                            disabledContentColor = Color(0xFF94A3B8).copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = Color(0xFF3A5A78) // Metallic blue border
                        ),
                  /*      elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 1.dp,
                            pressedElevation = 0.dp
                        )*/
                    ) {
                        Text(
                            "CANCEL",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    // Save Button - More vibrant and prominent
                    Button(
                        onClick = {
                            val log = createFuelLogEntry(
                                rupeeInput = rupeeVal,
                                fuelPriceInput = priceVal,
                                litersInput = litersVal,
                                location = location.takeIf { it.isNotBlank() }
                            )

                            if (log == null) {
                                error = "Please check your entries"
                            } else {
                                onSubmit(log)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp), // Consistent height
                        enabled = isFormValid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5D8BF4), // Vibrant blue
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF5D8BF4).copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
               /*         elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 3.dp,
                            disabledElevation = 2.dp
                        )*/
                    ) {
                        Text(
                            "SAVE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }            }
        }
    }
}

@Composable
private fun LocationDisplayBox(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isActive: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor by animateColorAsState(
        if (isActive) Color(0xFF5D8BF4) else Color(0xFF3A5A78).copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 200)
    )
    val elevation by animateDpAsState(if (isActive) 8.dp else 4.dp)

    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            ),
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(16.dp),
                    clip = true,
                    ambientColor = Color(0xFF3A5A78),
                    spotColor = Color(0xFF102A3F)
                )
                .background(
                    color = Color(0xFF2A3D52),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFFE2E8F0),
                    fontSize = 18.sp
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done
                ),
                cursorBrush = SolidColor(Color(0xFF5D8BF4)),
                decorationBox = { innerTextField ->
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
             /*           if (value.isEmpty() && !isActive) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                            )
                        } else {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isActive) Color(0xFF5D8BF4)
                                    else Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            innerTextField()
                        }*/

                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                interactionSource = interactionSource
            )

            if (isActive && value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

private enum class TextFieldType {
    AMOUNT, PRICE, LITERS, LOCATION
}

// Adds comma formatting for numbers
class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(text = AnnotatedString(text.text.toLongOrNull()?.let {
            NumberFormat.getNumberInstance().format(it)
        } ?: text.text), offsetMapping = OffsetMapping.Identity)
    }
}