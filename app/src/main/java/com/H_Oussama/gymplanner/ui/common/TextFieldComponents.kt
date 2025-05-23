package com.H_Oussama.gymplanner.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Standard text field with label, optional helper text, and error handling
 */
@Composable
fun StandardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isEnabled: Boolean = true,
    readOnly: Boolean = false
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder) }
            } else null,
            isError = isError,
            leadingIcon = if (leadingIcon != null) {
                { Icon(imageVector = leadingIcon, contentDescription = null) }
            } else null,
            trailingIcon = if (isError) {
                {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = errorText,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else if (trailingIcon != null) {
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(imageVector = trailingIcon, contentDescription = null)
                    }
                }
            } else null,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (imeAction == ImeAction.Done) onImeAction() },
                onGo = { if (imeAction == ImeAction.Go) onImeAction() },
                onNext = { if (imeAction == ImeAction.Next) onImeAction() },
                onSearch = { if (imeAction == ImeAction.Search) onImeAction() },
                onSend = { if (imeAction == ImeAction.Send) onImeAction() }
            ),
            enabled = isEnabled,
            readOnly = readOnly,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (helperText != null && !isError) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        } else if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Filled text field with label, optional helper text, and error handling
 */
@Composable
fun FilledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isEnabled: Boolean = true,
    readOnly: Boolean = false
) {
    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder) }
            } else null,
            isError = isError,
            leadingIcon = if (leadingIcon != null) {
                { Icon(imageVector = leadingIcon, contentDescription = null) }
            } else null,
            trailingIcon = if (isError) {
                {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = errorText,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else if (trailingIcon != null) {
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(imageVector = trailingIcon, contentDescription = null)
                    }
                }
            } else null,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (imeAction == ImeAction.Done) onImeAction() },
                onGo = { if (imeAction == ImeAction.Go) onImeAction() },
                onNext = { if (imeAction == ImeAction.Next) onImeAction() },
                onSearch = { if (imeAction == ImeAction.Search) onImeAction() },
                onSend = { if (imeAction == ImeAction.Send) onImeAction() }
            ),
            enabled = isEnabled,
            readOnly = readOnly,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (helperText != null && !isError) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        } else if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Password text field with toggle visibility icon
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    leadingIcon: ImageVector? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    isEnabled: Boolean = true,
    outlined: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    val visualTransformation = if (passwordVisible) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }
    
    val trailingIcon = if (passwordVisible) {
        Icons.Default.VisibilityOff
    } else {
        Icons.Default.Visibility
    }
    
    if (outlined) {
        StandardTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            helperText = helperText,
            errorText = errorText,
            isError = isError,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            onTrailingIconClick = { passwordVisible = !passwordVisible },
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
            onImeAction = onImeAction,
            isEnabled = isEnabled,
            modifier = modifier,
            singleLine = true
        )
    } else {
        FilledTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            helperText = helperText,
            errorText = errorText,
            isError = isError,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            onTrailingIconClick = { passwordVisible = !passwordVisible },
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
            onImeAction = onImeAction,
            isEnabled = isEnabled,
            modifier = modifier,
            singleLine = true
        )
    }
}

/**
 * Number input field with validation
 */
@Composable
fun NumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    leadingIcon: ImageVector? = null,
    isDecimal: Boolean = false,
    minValue: Double? = null,
    maxValue: Double? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    isEnabled: Boolean = true,
    readOnly: Boolean = false,
    outlined: Boolean = true
) {
    val filteredValue = { input: String ->
        val pattern = if (isDecimal) {
            Regex("^\\d*\\.?\\d*\$")
        } else {
            Regex("^\\d*\$")
        }
        
        val filtered = if (input.matches(pattern)) input else value
        
        if (minValue != null || maxValue != null) {
            val numValue = filtered.toDoubleOrNull() ?: 0.0
            
            when {
                minValue != null && maxValue != null && (numValue < minValue || numValue > maxValue) -> value
                minValue != null && numValue < minValue -> value
                maxValue != null && numValue > maxValue -> value
                else -> filtered
            }
        } else {
            filtered
        }
    }
    
    val keyboardType = if (isDecimal) {
        KeyboardType.Decimal
    } else {
        KeyboardType.Number
    }
    
    if (outlined) {
        StandardTextField(
            value = value,
            onValueChange = { onValueChange(filteredValue(it)) },
            label = label,
            placeholder = placeholder,
            helperText = helperText,
            errorText = errorText,
            isError = isError,
            leadingIcon = leadingIcon,
            keyboardType = keyboardType,
            imeAction = imeAction,
            onImeAction = onImeAction,
            isEnabled = isEnabled,
            readOnly = readOnly,
            modifier = modifier,
            singleLine = true
        )
    } else {
        FilledTextField(
            value = value,
            onValueChange = { onValueChange(filteredValue(it)) },
            label = label,
            placeholder = placeholder,
            helperText = helperText,
            errorText = errorText,
            isError = isError,
            leadingIcon = leadingIcon,
            keyboardType = keyboardType,
            imeAction = imeAction,
            onImeAction = onImeAction,
            isEnabled = isEnabled,
            readOnly = readOnly,
            modifier = modifier,
            singleLine = true
        )
    }
}

/**
 * Search text field with appropriate styling
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onSearch: () -> Unit = {},
    leadingIcon: ImageVector? = Icons.Default.Info,
    isEnabled: Boolean = true
) {
    StandardTextField(
        value = value,
        onValueChange = onValueChange,
        label = "",
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Search,
        onImeAction = onSearch,
        isEnabled = isEnabled,
        modifier = modifier
    )
} 