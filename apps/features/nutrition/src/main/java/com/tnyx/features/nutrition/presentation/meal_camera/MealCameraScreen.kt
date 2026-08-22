package com.tnyx.features.nutrition.presentation.meal_camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.tnyx.core.theme.TnyxTheme
import com.tnyx.core.theme.tokens.components.TnyxHeaderSize
import com.tnyx.core.ui.components.buttons.TnyxPrimaryButton
import com.tnyx.core.ui.components.buttons.TnyxSecondaryButton
import com.tnyx.core.ui.components.cards.TnyxCard
import com.tnyx.core.ui.components.cards.TnyxCardVariant
import com.tnyx.core.ui.components.layouts.TnyxScreenHeader
import java.io.File

@Composable
fun MealCameraScreen(
    state: MealCameraUiState,
    cameraPreview: @Composable () -> Unit,
    onBack: () -> Unit,
    onRequestPermission: () -> Unit,
    onFlash: () -> Unit,
    onGallery: () -> Unit,
    onCapture: () -> Unit,
    onBarcode: () -> Unit,
    onRetry: () -> Unit,
    onDone: () -> Unit,
) {
    val dimens = TnyxTheme.dimens
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TnyxTheme.colors.background)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TnyxScreenHeader(
                title = "Capture your meal",
                size = TnyxHeaderSize.Standard,
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                onNavigationClick = onBack,
                uppercaseTitle = false,
                reserveNavigationSpace = false,
                actions = {
                    if (state.capturedPhotoPath == null && state.hasFlash) {
                        IconButton(onClick = onFlash, enabled = state.isCameraReady) {
                            Icon(
                                imageVector = if (state.isFlashEnabled) {
                                    Icons.Rounded.FlashOn
                                } else {
                                    Icons.Rounded.FlashOff
                                },
                                contentDescription = "Toggle flash",
                                tint = TnyxTheme.colors.textPrimary,
                            )
                        }
                    }
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(TnyxTheme.colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.capturedPhotoPath != null -> AsyncImage(
                        model = File(state.capturedPhotoPath),
                        contentDescription = "Captured meal",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                    state.hasCameraPermission -> cameraPreview()
                    state.isPermissionResolved -> CameraPermissionContent(
                        onRequestPermission = onRequestPermission,
                        onGallery = onGallery,
                    )
                    else -> CircularProgressIndicator(color = TnyxTheme.colors.primary)
                }

                state.errorMessage?.let { message ->
                    TnyxCard(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(dimens.SpaceM),
                        variant = TnyxCardVariant.Surface,
                    ) {
                        Text(
                            text = message,
                            style = TnyxTheme.typography.bodyMedium,
                            color = TnyxTheme.colors.error,
                        )
                    }
                }

                if (state.isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TnyxTheme.colors.background.copy(alpha = 0.72f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimens.SpaceM),
                        ) {
                            CircularProgressIndicator(color = TnyxTheme.colors.primary)
                            Text(
                                text = "Analyzing your meal...",
                                style = TnyxTheme.typography.titleMedium,
                                color = TnyxTheme.colors.textPrimary,
                            )
                        }
                    }
                }

                if (state.isResolvingBarcode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TnyxTheme.colors.background.copy(alpha = 0.72f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimens.SpaceM),
                        ) {
                            CircularProgressIndicator(color = TnyxTheme.colors.primary)
                            Text(
                                text = "Finding barcode product...",
                                style = TnyxTheme.typography.titleMedium,
                                color = TnyxTheme.colors.textPrimary,
                            )
                        }
                    }
                }

                if (state.isBarcodeMode && state.capturedPhotoPath == null) {
                    BarcodeScanGuide()
                }
            }

            if (state.capturedPhotoPath == null) {
                LiveCameraControls(
                    captureEnabled = state.isCameraReady &&
                        !state.isBarcodeMode &&
                        !state.isResolvingBarcode,
                    barcodeSelected = state.isBarcodeMode,
                    onGallery = onGallery,
                    onCapture = onCapture,
                    onBarcode = onBarcode,
                )
            } else {
                CapturedPhotoControls(
                    enabled = !state.isAnalyzing,
                    onRetry = onRetry,
                    onDone = onDone,
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionContent(
    onRequestPermission: () -> Unit,
    onGallery: () -> Unit,
) {
    val dimens = TnyxTheme.dimens
    TnyxCard(
        modifier = Modifier.padding(dimens.SpaceL),
        variant = TnyxCardVariant.Surface,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(dimens.SpaceM)) {
            Text(
                text = "Allow camera access to photograph your meal.",
                style = TnyxTheme.typography.bodyLarge,
                color = TnyxTheme.colors.textPrimary,
            )
            TnyxPrimaryButton(
                text = "Allow camera",
                onPressed = onRequestPermission,
                expand = true,
                leading = {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                },
            )
            TnyxSecondaryButton(
                text = "Choose from gallery",
                onPressed = onGallery,
                expand = true,
                leading = {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                },
            )
        }
    }
}

@Composable
private fun LiveCameraControls(
    captureEnabled: Boolean,
    barcodeSelected: Boolean,
    onGallery: () -> Unit,
    onCapture: () -> Unit,
    onBarcode: () -> Unit,
) {
    val dimens = TnyxTheme.dimens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TnyxTheme.colors.background)
            .navigationBarsPadding()
            .padding(horizontal = dimens.SpaceL, vertical = dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            IconButton(onClick = onGallery) {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = "Choose meal photo from gallery",
                    tint = TnyxTheme.colors.textPrimary,
                    modifier = Modifier.size(dimens.IconL),
                )
            }
        }
        Surface(
            onClick = onCapture,
            enabled = captureEnabled,
            shape = CircleShape,
            color = TnyxTheme.colors.primaryButtonContainer,
            modifier = Modifier
                .size(dimens.SpaceHuge)
                .border(
                    width = dimens.BorderMedium,
                    color = TnyxTheme.colors.textPrimary,
                    shape = CircleShape,
                )
                .padding(dimens.SpaceXS)
                .clip(CircleShape),
        ) {}
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            IconButton(onClick = onBarcode) {
                Icon(
                    imageVector = Icons.Rounded.QrCodeScanner,
                    contentDescription = "Scan food barcode",
                    tint = if (barcodeSelected) {
                        TnyxTheme.colors.primary
                    } else {
                        TnyxTheme.colors.textPrimary
                    },
                    modifier = Modifier.size(dimens.IconL),
                )
            }
        }
    }
}

@Composable
private fun BarcodeScanGuide() {
    val dimens = TnyxTheme.dimens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.SpaceXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.SpaceM),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .border(
                    width = dimens.BorderMedium,
                    color = TnyxTheme.colors.primary,
                    shape = TnyxTheme.shapes.Material.large,
                ),
        )
        TnyxCard(variant = TnyxCardVariant.Surface) {
            Text(
                text = "Place the food barcode inside the frame",
                style = TnyxTheme.typography.bodyMedium,
                color = TnyxTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun CapturedPhotoControls(
    enabled: Boolean,
    onRetry: () -> Unit,
    onDone: () -> Unit,
) {
    val dimens = TnyxTheme.dimens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TnyxTheme.colors.background)
            .navigationBarsPadding()
            .padding(horizontal = dimens.SpaceM, vertical = dimens.SpaceM),
        horizontalArrangement = Arrangement.spacedBy(dimens.SpaceSM),
    ) {
        TnyxSecondaryButton(
            text = "Retry",
            onPressed = onRetry,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            leading = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
        )
        TnyxPrimaryButton(
            text = "Done",
            onPressed = onDone,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            leading = { Icon(Icons.Rounded.Check, contentDescription = null) },
        )
    }
}
