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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ruchitech.carlanuchertab.music.PlaylistWithCount

private object DialogPalette {
    val Scrim = Color(0xCC000000)
    val SurfaceTop = CockpitPalette.SurfaceTop
    val SurfaceBottom = CockpitPalette.SurfaceBottom
    val Border = CockpitPalette.Border
    val Accent = CockpitPalette.Accent
    val TextPrimary = CockpitPalette.TextPrimary
    val TextSecondary = CockpitPalette.TextSecondary
    val TextMuted = CockpitPalette.TextMuted
    val RowFill = CockpitPalette.SurfaceRaised
    val Danger = CockpitPalette.Danger
}

@Composable
fun MusicTextInputDialog(
    title: String,
    subtitle: String? = null,
    initialValue: String,
    fieldLabel: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    val canConfirm = value.trim().isNotEmpty()

    MusicDialogHost(onDismiss = onDismiss) {
        MusicDialogHeader(title = title, subtitle = subtitle, onClose = onDismiss)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text(fieldLabel, fontSize = 12.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp, max = 52.dp),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = dialogTextFieldColors()
        )
        Spacer(modifier = Modifier.height(10.dp))
        MusicDialogActions(
            primaryLabel = confirmLabel,
            onPrimary = { if (canConfirm) onConfirm(value.trim()) },
            primaryEnabled = canConfirm,
            onSecondary = onDismiss
        )
    }
}

@Composable
fun MusicAddToPlaylistDialog(
    trackTitle: String,
    playlists: List<PlaylistWithCount>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (PlaylistWithCount) -> Unit,
    onCreatePlaylist: () -> Unit,
) {
    MusicDialogHost(onDismiss = onDismiss) {
        MusicDialogHeader(
            title = "Add to playlist",
            subtitle = trackTitle,
            onClose = onDismiss
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (playlists.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DialogPalette.RowFill)
                    .border(1.dp, DialogPalette.Border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistPlay,
                    contentDescription = null,
                    tint = DialogPalette.Accent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "No playlists — create one",
                    color = DialogPalette.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            MusicDialogActions(
                primaryLabel = "Create",
                onPrimary = onCreatePlaylist,
                onSecondary = onDismiss
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistPickerRow(
                        name = playlist.name,
                        songCount = playlist.songCount,
                        onClick = { onSelectPlaylist(playlist) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MusicDialogTextButton(label = "Cancel", onClick = onDismiss)
                Spacer(modifier = Modifier.width(6.dp))
                MusicDialogPrimaryChip(
                    label = "New",
                    icon = Icons.Default.Add,
                    onClick = onCreatePlaylist
                )
            }
        }
    }
}

@Composable
fun MusicConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    destructive: Boolean = false,
) {
    MusicDialogHost(onDismiss = onDismiss) {
        MusicDialogHeader(title = title, subtitle = null, onClose = onDismiss)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = DialogPalette.TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MusicDialogTextButton(label = "Cancel", onClick = onDismiss)
            Spacer(modifier = Modifier.width(6.dp))
            if (destructive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DialogPalette.Danger.copy(alpha = 0.9f))
                        .clickable(onClick = onConfirm)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        confirmLabel,
                        color = Color(0xFF1A0808),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            } else {
                MusicDialogPrimaryChip(label = confirmLabel, onClick = onConfirm)
            }
        }
    }
}

@Composable
private fun MusicDialogHost(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DialogPalette.Scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 28.dp, vertical = 24.dp)
                    .widthIn(min = 300.dp, max = 440.dp)
                    .fillMaxWidth(0.72f)
                    .clip(RoundedCornerShape(CockpitDimens.PanelRadius))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DialogPalette.SurfaceTop, DialogPalette.SurfaceBottom)
                        )
                    )
                    .border(1.dp, DialogPalette.Border, RoundedCornerShape(CockpitDimens.PanelRadius))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun PlaylistPickerRow(
    name: String,
    songCount: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DialogPalette.RowFill)
            .border(1.dp, DialogPalette.Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.PlaylistPlay,
            contentDescription = null,
            tint = DialogPalette.Accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = name,
            color = DialogPalette.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$songCount",
            color = DialogPalette.TextMuted,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun MusicDialogHeader(
    title: String,
    subtitle: String?,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = DialogPalette.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = DialogPalette.TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = DialogPalette.TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun MusicDialogActions(
    primaryLabel: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    primaryEnabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MusicDialogTextButton(label = "Cancel", onClick = onSecondary)
        Spacer(modifier = Modifier.width(6.dp))
        MusicDialogPrimaryChip(
            label = primaryLabel,
            onClick = onPrimary,
            enabled = primaryEnabled
        )
    }
}

@Composable
private fun MusicDialogTextButton(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = DialogPalette.TextSecondary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun MusicDialogPrimaryChip(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    val bg = if (enabled) DialogPalette.Accent else DialogPalette.Accent.copy(alpha = 0.35f)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF0A1218),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                label,
                color = Color(0xFF0A1218),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = DialogPalette.TextPrimary,
    unfocusedTextColor = DialogPalette.TextPrimary,
    focusedBorderColor = DialogPalette.Accent,
    unfocusedBorderColor = DialogPalette.Border,
    cursorColor = DialogPalette.Accent,
    focusedLabelColor = DialogPalette.Accent,
    unfocusedLabelColor = DialogPalette.TextMuted,
    focusedContainerColor = DialogPalette.RowFill.copy(alpha = 0.5f),
    unfocusedContainerColor = DialogPalette.RowFill.copy(alpha = 0.35f)
)
