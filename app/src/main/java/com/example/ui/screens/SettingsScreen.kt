package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlockedRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.StarGold

@Composable
fun SettingsScreen(
    isSoundEnabled: Boolean,
    isHapticsEnabled: Boolean,
    isDarkMode: Boolean,
    difficulty: String,
    onToggleSound: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSetDifficulty: (String) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showHowToPlayModal by remember { mutableStateOf(false) }
    var showAboutModal by remember { mutableStateOf(false) }
    var showPrivacyRightsModal by remember { mutableStateOf(false) }
    var showPrivacyPrefsModal by remember { mutableStateOf(false) }
    var removeAdsModal by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Progress?", fontWeight = FontWeight.Bold) },
            text = { Text("This will clear all completed levels and reset your stars. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlockedRed),
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text("Reset All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showHowToPlayModal) {
        HowToPlayDialog(onDismiss = { showHowToPlayModal = false })
    }

    if (showAboutModal) {
        AboutDialog(onDismiss = { showAboutModal = false })
    }

    if (showPrivacyRightsModal) {
        PrivacyRightsDialog(onDismiss = { showPrivacyRightsModal = false })
    }

    if (showPrivacyPrefsModal) {
        PrivacyPreferencesDialog(onDismiss = { showPrivacyPrefsModal = false })
    }

    if (removeAdsModal) {
        AlertDialog(
            onDismissRequest = { removeAdsModal = false },
            title = { Text("Remove Ads", fontWeight = FontWeight.Bold) },
            text = { Text("Ad-free mode is already fully active for this version of Go Arrow Puzzle!") },
            confirmButton = {
                Button(onClick = { removeAdsModal = false }) {
                    Text("Awesome")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Top Bar (Options + Done button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(48.dp))
            Text(
                text = "Options",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_done_button")
            ) {
                Text(
                    text = "Done",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 1: Settings & Preferences (Sound, Haptics, Dark Mode)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                SettingToggleItem(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    iconTint = ElectricBlue,
                    title = "Sound Effects",
                    checked = isSoundEnabled,
                    onCheckedChange = onToggleSound,
                    tag = "sound_switch"
                )
                SettingToggleItem(
                    icon = Icons.Default.Vibration,
                    iconTint = StarGold,
                    title = "Haptic Feedback",
                    checked = isHapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    tag = "haptics_switch"
                )
                SettingToggleItem(
                    icon = Icons.Default.DarkMode,
                    iconTint = Color(0xFF6366F1),
                    title = "Dark Theme",
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode,
                    tag = "dark_mode_switch"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card: Generation Difficulty (Easy, Medium, Hard)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Generation Difficulty",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Adjusts grid size & overlapping arrows",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        val selected = difficulty.equals(diff, ignoreCase = true)
                        Button(
                            onClick = { onSetDifficulty(diff) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("difficulty_$diff"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) ElectricBlue else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 4.dp else 0.dp)
                        ) {
                            Text(
                                text = diff,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 2: Help, About & Privacy (similar to screenshot style with chevron)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                SettingNavigationItem(
                    icon = Icons.Default.Help,
                    iconTint = Color(0xFF10B981),
                    title = "Help & How to Play",
                    onClick = { showHowToPlayModal = true }
                )
                SettingNavigationItem(
                    icon = Icons.Default.Info,
                    iconTint = ElectricBlue,
                    title = "About Game",
                    onClick = { showAboutModal = true }
                )
                SettingNavigationItem(
                    icon = Icons.Default.PrivacyTip,
                    iconTint = Color(0xFF8B5CF6),
                    title = "Privacy Rights",
                    onClick = { showPrivacyRightsModal = true }
                )
                SettingNavigationItem(
                    icon = Icons.Default.Security,
                    iconTint = Color(0xFF0EA5E9),
                    title = "Privacy Preferences",
                    onClick = { showPrivacyPrefsModal = true }
                )
                SettingNavigationItem(
                    icon = Icons.Default.DeleteForever,
                    iconTint = BlockedRed,
                    title = "Reset Game Progress",
                    onClick = { showResetDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 3: Remove Ads
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingNavigationItem(
                    icon = Icons.Default.Block,
                    iconTint = BlockedRed,
                    title = "Remove Ads (Ad-Free Experience)",
                    onClick = { removeAdsModal = true }
                )
            }
        }
    }
}

@Composable
private fun SettingToggleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 14.dp)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ElectricBlue
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}

@Composable
private fun SettingNavigationItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 14.dp)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(22.dp)
        )
    }
}
