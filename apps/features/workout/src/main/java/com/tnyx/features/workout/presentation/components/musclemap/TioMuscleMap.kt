package com.tnyx.features.workout.presentation.components.musclemap

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.tnyx.features.workout.R
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.tnyx.core.theme.tokens.foundation.TnyxPalette

/** Default secondary muscle highlight tint color (Vibrant Sky Blue from TnyxPalette for high contrast). */
val DefaultSecondaryMuscleColor = TnyxPalette.SkyBlue

/**
 * Render a layered anatomical muscle map with support for primary and secondary highlighted muscles.
 *
 * @param muscleGroups      Primary highlighted muscle keys or aliases.
 * @param secondaryMuscles  Secondary highlighted muscle keys or aliases (tinted with [secondaryColor]).
 * @param variant           Gender variant ([ExerciseMediaVariant.MALE] or [ExerciseMediaVariant.FEMALE]).
 * @param view              Side of body to render ([MuscleMapView.FRONT] or [MuscleMapView.BACK]).
 * @param contentDescription Accessibility description.
 * @param primaryColor      Optional custom tint for primary muscles (null = original red webp asset).
 * @param secondaryColor    Tint color for secondary muscles (default: Lyfta blue #0C6FFF).
 */
@Composable
fun TioMuscleMap(
    muscleGroups: Iterable<String>,
    variant: ExerciseMediaVariant,
    view: MuscleMapView,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    secondaryMuscles: Iterable<String> = emptyList(),
    primaryColor: Color? = null,
    secondaryColor: Color = DefaultSecondaryMuscleColor,
    contentScale: ContentScale = ContentScale.FillBounds,
    alignment: Alignment = Alignment.Center,
) {
    val primaryLayerSet = MuscleMapAssetRegistry.resolve(
        rawMuscleGroups = muscleGroups,
        variant = variant,
    )
    val secondaryLayerSet = MuscleMapAssetRegistry.resolve(
        rawMuscleGroups = secondaryMuscles,
        variant = variant,
    )

    val baseAsset = when (variant) {
        ExerciseMediaVariant.MALE -> if (view == MuscleMapView.FRONT) "front_grey_body_male.webp" else "back_body_male.webp"
        ExerciseMediaVariant.FEMALE -> if (view == MuscleMapView.FRONT) "front_grey_body_female.webp" else "back_body_female.webp"
        ExerciseMediaVariant.NEUTRAL -> "front_grey_body_male.webp"
    }

    val primaryOverlays = when (view) {
        MuscleMapView.FRONT -> primaryLayerSet?.frontOverlayAssets.orEmpty()
        MuscleMapView.BACK -> primaryLayerSet?.backOverlayAssets.orEmpty()
    }

    val secondaryOverlays = when (view) {
        MuscleMapView.FRONT -> secondaryLayerSet?.frontOverlayAssets.orEmpty()
        MuscleMapView.BACK -> secondaryLayerSet?.backOverlayAssets.orEmpty()
    }.filter { it !in primaryOverlays } // Primary takes precedence over secondary

    val allAssetsToLoad = listOf(baseAsset) + primaryOverlays + secondaryOverlays
    val bitmaps by rememberAssetBitmaps(allAssetsToLoad)
    val loadedMap = bitmaps

    if (loadedMap == null || loadedMap[baseAsset] == null) {
        Image(
            painter = painterResource(R.drawable.tio_body_part_placeholder),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit,
            alignment = alignment,
        )
        return
    }

    Box(modifier = modifier) {
        // 1. Render Base Grey Body (0.45f opacity)
        loadedMap[baseAsset]?.let { baseBitmap ->
            Image(
                bitmap = baseBitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                alignment = alignment,
                alpha = 0.45f,
            )
        }

        // 2. Render Secondary Overlays (Tinted with secondaryColor using texture-preserving ColorMatrix)
        val secondaryFilter = remember(secondaryColor) { createMuscleTextureColorFilter(secondaryColor) }
        secondaryOverlays.forEach { assetName ->
            loadedMap[assetName]?.let { overlayBitmap ->
                Image(
                    bitmap = overlayBitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    alignment = alignment,
                    colorFilter = secondaryFilter,
                    alpha = 1.0f,
                )
            }
        }

        // 3. Render Primary Overlays (Red asset / custom primaryColor tint)
        val primaryFilter = remember(primaryColor) { primaryColor?.let { createMuscleTextureColorFilter(it) } }
        primaryOverlays.forEach { assetName ->
            loadedMap[assetName]?.let { overlayBitmap ->
                Image(
                    bitmap = overlayBitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    alignment = alignment,
                    colorFilter = primaryFilter,
                    alpha = 1.0f,
                )
            }
        }
    }
}

/**
 * Creates a texture-preserving ColorFilter for red WebP muscle overlays.
 *
 * Maps the Red channel (which contains muscle fiber details and depth) directly to
 * the target [color] RGB, preserving 100% of muscle fiber lines and contours without
 * getting dark/muddy from simple RGB multiplication.
 */
private fun createMuscleTextureColorFilter(color: Color): ColorFilter {
    val r = color.red
    val g = color.green
    val b = color.blue
    return ColorFilter.colorMatrix(
        androidx.compose.ui.graphics.ColorMatrix(
            floatArrayOf(
                r, 0f, 0f, 0f, 0f,
                g, 0f, 0f, 0f, 0f,
                b, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )
}

@Composable
private fun rememberAssetBitmaps(assetNames: List<String>): State<Map<String, ImageBitmap?>?> {
    val assetManager = LocalContext.current.applicationContext.assets
    return produceState(
        initialValue = null,
        key1 = assetNames,
        key2 = assetManager,
    ) {
        if (assetNames.isEmpty()) {
            value = emptyMap()
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            assetNames.distinct().associateWith { assetName ->
                runCatching {
                    val path = if (assetName.contains("/")) assetName else "musclemap/$assetName"
                    assetManager.open(path).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrElse {
                    runCatching {
                        assetManager.open(assetName).use { stream ->
                            BitmapFactory.decodeStream(stream)?.asImageBitmap()
                        }
                    }.getOrNull()
                }
            }
        }
    }
}
