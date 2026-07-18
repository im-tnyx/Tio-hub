package com.tnyx.features.workout.presentation.components.musclemap

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.tnyx.features.workout.R
import com.tnyx.shared.workout.domain.model.ExerciseMediaVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TioMuscleMap(
    muscleGroups: Iterable<String>,
    variant: ExerciseMediaVariant,
    view: MuscleMapView,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val layerSet = MuscleMapAssetRegistry.resolve(
        rawMuscleGroups = muscleGroups,
        variant = variant,
    )
    val assetNames = layerSet?.assetsFor(view).orEmpty()
    val bitmaps by rememberAssetBitmaps(assetNames)
    val loadedLayers = bitmaps

    if (assetNames.isEmpty() || loadedLayers == null || loadedLayers.firstOrNull() == null) {
        Image(
            painter = painterResource(R.drawable.tio_body_part_placeholder),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
        return
    }

    Box(modifier = modifier) {
        loadedLayers.forEachIndexed { index, bitmap ->
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = contentDescription.takeIf { index == 0 },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
        }
    }
}

@Composable
private fun rememberAssetBitmaps(assetNames: List<String>): State<List<ImageBitmap?>?> {
    val assetManager = LocalContext.current.applicationContext.assets
    return produceState(
        initialValue = null,
        key1 = assetNames,
        key2 = assetManager,
    ) {
        if (assetNames.isEmpty()) {
            value = emptyList()
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            assetNames.map { assetName ->
                runCatching {
                    assetManager.open(assetName).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }
}
