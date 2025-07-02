package com.ruchitech.carlanuchertab.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.ruchitech.carlanuchertab.helper.createFuelLogEntry
import com.ruchitech.carlanuchertab.roomdb.data.FuelLog
import java.text.NumberFormat

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
    Column(modifier = modifier.clickable { onClick() }) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = if (value.isNotEmpty()) value else "0",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = if (value.isNotEmpty()) 18.sp else 14.sp
                )
            )
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

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(keys) { key ->
            val isDelete = key == "⌫"
            val isZero = key == "0"

            val backgroundColor = when {
                isDelete -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                else -> MaterialTheme.colorScheme.surfaceContainerHigh
            }

            val textColor = when {
                isDelete -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSurface
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(55.dp) // Slightly larger for better touch targets
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(12.dp) // More rounded corners
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable() {
                        if (isDelete) {
                            onDeletePressed()
                        } else {
                            onKeyPressed(key)
                        }
                    }
            ) {
                Text(
                    text = key,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun FuelLogDialog(
    onDismiss: () -> Unit,
    onSubmit: (FuelLog) -> Unit,
) {
    var rupee by remember { mutableStateOf("") }
    var fuelPrice by remember { mutableStateOf("") }
    var liters by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var activeField by remember { mutableStateOf<TextFieldType?>(null) }
    listOf(200, 400, 450, 500, 600, 700, 800, 1000)
    val rupeeVal = rupee.toIntOrNull()
    val priceVal = fuelPrice.toFloatOrNull()
    val litersVal = liters.toFloatOrNull()
    val isFormValid = rupeeVal != null && rupeeVal > 0

    LaunchedEffect(true) {
        activeField = TextFieldType.AMOUNT
    }

    val onKeyPressed = { key: String ->
        when (activeField) {
            TextFieldType.AMOUNT -> {
                if (key != ".") {
                    val newRupee = if (rupee == "0") key else rupee + key
                    rupee = newRupee

                    when {
                        fuelPrice.isNotEmpty() && liters.isNotEmpty() -> {
                            fuelPrice = (newRupee.toDoubleOrNull()
                                ?.div(liters.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                                ?.let { "%.2f".format(it) } ?: ""
                        }

                        fuelPrice.isNotEmpty() -> {
                            liters = (newRupee.toDoubleOrNull()
                                ?.div(fuelPrice.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                                ?.let { "%.2f".format(it) } ?: ""
                        }

                        liters.isNotEmpty() -> {
                            fuelPrice = (newRupee.toDoubleOrNull()
                                ?.div(liters.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                                ?.let { "%.2f".format(it) } ?: ""
                        }
                    }
                }
            }

            TextFieldType.PRICE -> {
                if (fuelPrice.contains(".") && fuelPrice.substringAfter(".").length >= 2 && key != "⌫") {

                } else if (key != "." || !fuelPrice.contains(".")) {
                    val newPrice = if (fuelPrice == "0") key else fuelPrice + key
                    fuelPrice = newPrice

                    when {
                        rupee.isNotEmpty() && liters.isNotEmpty() -> {
                            rupee = (newPrice.toDoubleOrNull()
                                ?.times(liters.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                                ?.let { "%.0f".format(it) } ?: ""
                        }

                        rupee.isNotEmpty() -> {
                            liters = (rupee.toDoubleOrNull()
                                ?.div(newPrice.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                                ?.let { "%.2f".format(it) } ?: ""
                        }

                        liters.isNotEmpty() -> {
                            rupee = (newPrice.toDoubleOrNull()
                                ?.times(liters.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                                ?.let { "%.0f".format(it) } ?: ""
                        }
                    }
                }
            }

            TextFieldType.LITERS -> {
                // Prevent more than 2 decimal places
                if (liters.contains(".") && liters.substringAfter(".").length >= 2 && key != "⌫") {
                    // Do nothing if already has 2 decimal places
                } else if (key != "." || !liters.contains(".")) {
                    val newLiters = if (liters == "0") key else liters + key
                    val litersValue = newLiters.toDoubleOrNull() ?: 0.0

                    if (litersValue > 37.0) {
                        // Reset both amount and liters if exceeds 37
                        rupee = ""
                        liters = ""
                    } else {
                        liters = newLiters

                        when {

                            rupee.isNotEmpty() -> {
                                fuelPrice = (rupee.toDoubleOrNull()
                                    ?.div(litersValue.takeIf { it > 0 }
                                        ?: 1.0))?.let { "%.2f".format(it) } ?: ""
                            }

                            fuelPrice.isNotEmpty() -> {
                                rupee = (fuelPrice.toDoubleOrNull()
                                    ?.times(litersValue))?.takeIf { it > 0 }
                                    ?.let { "%.0f".format(it) } ?: ""
                            }
                        }
                    }
                }
            }

            else -> {}
        }
    }

    val onDeletePressed = {
        when (activeField) {
            TextFieldType.AMOUNT -> {
                val newRupee = if (rupee.length <= 1) "" else rupee.dropLast(1)
                rupee = newRupee

                when {

                    fuelPrice.isNotEmpty() && liters.isNotEmpty() -> {
                        fuelPrice = (newRupee.toDoubleOrNull()
                            ?.div(liters.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                            ?.let { "%.2f".format(it) } ?: ""
                    }

                    newRupee.isEmpty() && (fuelPrice.isNotEmpty() || liters.isNotEmpty()) -> {
                        fuelPrice = ""
                        liters = ""
                    }
                    // Recalculate based on what's available
                    fuelPrice.isNotEmpty() -> {
                        liters = (newRupee.toDoubleOrNull()
                            ?.div(fuelPrice.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                            ?.let { "%.2f".format(it) } ?: ""
                    }

                    liters.isNotEmpty() -> {
                        fuelPrice = (newRupee.toDoubleOrNull()
                            ?.div(liters.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                            ?.let { "%.2f".format(it) } ?: ""
                    }
                }
            }

            TextFieldType.PRICE -> {
                val newPrice = if (fuelPrice.length <= 1) "" else fuelPrice.dropLast(1)
                fuelPrice = newPrice

                when {
                    // Clear dependent fields if price is cleared
                    newPrice.isEmpty() && (rupee.isNotEmpty() || liters.isNotEmpty()) -> {
                        rupee = ""
                        liters = ""
                    }
                    // Recalculate based on what's available
                    rupee.isNotEmpty() -> {
                        liters = (rupee.toDoubleOrNull()
                            ?.div(newPrice.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                            ?.let { "%.2f".format(it) } ?: ""
                    }

                    liters.isNotEmpty() -> {
                        rupee = (newPrice.toDoubleOrNull()
                            ?.times(liters.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                            ?.let { "%.0f".format(it) } ?: ""
                    }
                }
            }

            TextFieldType.LITERS -> {
                val newLiters = if (liters.length <= 1) "" else liters.dropLast(1)
                liters = newLiters

                when {
                    // Clear dependent fields if liters is cleared
                    newLiters.isEmpty() && (rupee.isNotEmpty() || fuelPrice.isNotEmpty()) -> {
                        rupee = ""
                        fuelPrice = ""
                    }
                    // Recalculate based on what's available
                    rupee.isNotEmpty() -> {
                        fuelPrice = (rupee.toDoubleOrNull()
                            ?.div(newLiters.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                            ?.let { "%.2f".format(it) } ?: ""
                    }

                    fuelPrice.isNotEmpty() -> {
                        rupee = (fuelPrice.toDoubleOrNull()
                            ?.times(newLiters.toDoubleOrNull() ?: 1.0))?.takeIf { it > 0 }
                            ?.let { "%.0f".format(it) } ?: ""
                    }
                }
            }

            else -> {}
        }
    }
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header with suggestions
                /*                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    items(suggestions) { amount ->
                                        AssistChip(
                                            onClick = {
                                                rupee = amount.toString()
                                                activeField = TextFieldType.AMOUNT
                                                val newRupee = amount.toString()
                                                rupee = newRupee.toString()

                                                when {
                                                    fuelPrice.isNotEmpty() && liters.isNotEmpty() -> {
                                                        fuelPrice =
                                                            (newRupee.toDoubleOrNull()
                                                                ?.div(liters.toDoubleOrNull() ?: 1.0))
                                                                ?.takeIf { it > 0 }
                                                                ?.let { "%.2f".format(it) } ?: ""
                                                    }

                                                    fuelPrice.isNotEmpty() -> {
                                                        liters =
                                                            (newRupee.toDoubleOrNull()
                                                                ?.div(fuelPrice.toDoubleOrNull() ?: 1.0))
                                                                ?.takeIf { it > 0 }
                                                                ?.let { "%.2f".format(it) } ?: ""
                                                    }

                                                    liters.isNotEmpty() -> {
                                                        fuelPrice =
                                                            (newRupee.toDoubleOrNull()
                                                                ?.div(liters.toDoubleOrNull() ?: 1.0))
                                                                ?.takeIf { it > 0 }
                                                                ?.let { "%.2f".format(it) } ?: ""
                                                    }
                                                }
                                            },
                                            label = { Text("₹$amount") },
                                            shape = RoundedCornerShape(50),
                                            border = AssistChipDefaults.assistChipBorder(true),
                                            colors = AssistChipDefaults.assistChipColors(
                                                labelColor = MaterialTheme.colorScheme.onSurface,
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            modifier = Modifier.padding(end = 5.dp)
                                        )
                                    }
                                }*/

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


                        FuelDisplayBox2(
                            value = location,
                            onValueChange = { location = it },
                            label = "Location (optional)",
                            isActive = activeField == TextFieldType.PRICE,
                            keyboardType = KeyboardType.Decimal
                        )

                        Spacer(modifier = Modifier.height(0.dp))/*
                                                Button(
                                                    onClick = {
                                                        val log = createFuelLogEntry(
                                                            rupeeInput = rupeeVal,
                                                            fuelPriceInput = priceVal,
                                                            litersInput = litersVal,
                                                            location = location.takeIf { it.isNotBlank() }
                                                        )

                                                        if (log == null) {
                                                            error = "Invalid input combination"
                                                        } else {
                                                            onSubmit(log)
                                                            onDismiss()
                                                        }
                                                    },
                                                    enabled = isFormValid,
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(52.dp)
                                                ) {
                                                    Text("Add", style = MaterialTheme.typography.titleMedium)
                                                }

                                                TextButton(
                                                    onClick = onDismiss,
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(52.dp)
                                                ) {
                                                    Text("Cancel", style = MaterialTheme.typography.titleMedium)
                                                }*/
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
                        .padding(horizontal = 16.dp)
                        .padding(top = 20.dp)
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Save Button
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        onClick = {
                            val log = createFuelLogEntry(
                                rupeeInput = rupeeVal,
                                fuelPriceInput = priceVal,
                                litersInput = litersVal,
                                location = location.takeIf { it.isNotBlank() })

                            if (log == null) {
                                error = "Invalid input combination"
                            } else {
                                onSubmit(log)
                                onDismiss()
                            }
                        },
                        enabled = isFormValid,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32), // Darker green
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp, pressedElevation = 2.dp
                        )
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    // Cancel Button
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xFF2E7D32), // Same green as save
                            containerColor = Color.White
                            // border = BorderStroke(1.dp, Color(0xFF2E7D32))
                        )
                    ) {
                        Text(
                            text = "Cancel", style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium, color = Color.Red
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FuelDisplayBox2(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isActive: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label, style = MaterialTheme.typography.labelMedium
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType, imeAction = ImeAction.Done
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                focusedIndicatorColor = if (isActive) MaterialTheme.colorScheme.primary
                else Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            interactionSource = remember { MutableInteractionSource() }.apply {
                if (isActive) {
                    LaunchedEffect(Unit) {
                        // Optional: Focus handling logic
                    }
                }
            })
    }
}

private enum class TextFieldType {
    AMOUNT, PRICE, LITERS
}

// Adds comma formatting for numbers
class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(text = AnnotatedString(text.text.toLongOrNull()?.let {
            NumberFormat.getNumberInstance().format(it)
        } ?: text.text), offsetMapping = OffsetMapping.Identity)
    }
}