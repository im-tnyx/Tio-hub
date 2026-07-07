package com.tnyx.wear.presentation.workout

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import coil.compose.rememberAsyncImagePainter
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.tnyx.wear.R
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite
import com.tnyx.wear.theme.WearTypography
import kotlinx.coroutines.delay
import org.json.JSONArray

data class ExerciseItem(
    val id: String,
    val title: String,
    val legacyId: String?,
    val imageUrl: String?
)

data class RoutineItem(
    val id: String,
    val title: String,
    val description: String
)

// Helper to verify if asset file exists
fun assetFileExists(context: Context, filename: String): Boolean {
    return try {
        context.assets.open(filename).use { }
        true
    } catch (e: Exception) {
        false
    }
}

// Dynamic JSON loader to load all exercises from app_exercises.json based on user gender preference
fun loadAllExercisesFromJson(context: Context, isFemale: Boolean = false): List<ExerciseItem> {
    val list = mutableListOf<ExerciseItem>()
    try {
        val jsonString = context.resources.openRawResource(R.raw.app_exercises)
            .bufferedReader()
            .use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val id = item.optString("id")
            val title = item.optString("title")
            val legacyId = item.optString("legacyId")
            val media = item.optJSONObject("media")
            
            // Check if local asset exists, otherwise fallback to network URL
            val localAssetPath = "exercise_thumbnails/$legacyId.jpg"
            val imageUrl = if (!legacyId.isNullOrEmpty() && assetFileExists(context, localAssetPath)) {
                "file:///android_asset/$localAssetPath"
            } else {
                val primaryGender = if (isFemale) "female" else "male"
                val secondaryGender = if (isFemale) "male" else "female"
                val genderObj = media?.optJSONObject(primaryGender) ?: media?.optJSONObject(secondaryGender)
                genderObj?.optString("imageUrl") ?: genderObj?.optString("thumbnailUrl")
            }
            
            if (!title.isNullOrEmpty()) {
                list.add(ExerciseItem(id, title, legacyId, imageUrl))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list.sortedBy { it.title }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun WorkoutListScreen(
    onNavigateBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val columnState = rememberColumnState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Read user gender preference from SharedPreferences
    val sharedPref = remember(context) {
        context.getSharedPreferences("tnyx_health_prefs", Context.MODE_PRIVATE)
    }
    val isFemale = sharedPref.getBoolean("pref_is_female", false)
    
    // Parse dynamic exercise list
    val exercises = remember(isFemale) {
        loadAllExercisesFromJson(context, isFemale)
    }

    // Syncing state variables
    var isSyncing by remember { mutableStateOf(false) }
    var syncText by remember { mutableStateOf("Sync workout from phone") }

    // Launch temporary sync routine simulation when pressed
    LaunchedEffect(isSyncing) {
        if (isSyncing) {
            syncText = "Syncing live session..."
            delay(1200) // Simulates phone-to-watch sync
            isSyncing = false
            syncText = "Live workout synced!"
            delay(1000)
            syncText = "Sync workout from phone"
        }
    }

    // Hevy routines listing actual exercises as subtitles
    val routines = listOf(
        RoutineItem("r1", "Upper Body A", "Bench Press, Overhead Press, Bicep Curl"),
        RoutineItem("r2", "Lower Body B", "Squat, Leg Press, Calf Raise"),
        RoutineItem("r3", "5K Run/Cardio", "Treadmill Running, Stretching")
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = modifier.fillMaxSize()
        ) {
            // Header Title
            item {
                Text(
                    text = "Work out",
                    style = WearTypography.title1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Hevy-style Live Sync Card: Blue background, bold title, and dynamic status subtitle
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xFF1D60FC)) // Hevy's official brand blue accent
                            .clickable { isSyncing = true }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sync),
                            contentDescription = "Sync",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Live Sync",
                                style = WearTypography.title1,
                                fontSize = 14.sp,
                                color = Color.White,
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = syncText,
                                style = WearTypography.body1,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }

            // SECTION 1: Routines
            item {
                Text(
                    text = "Routines",
                    style = WearTypography.body1,
                    color = TextGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 4.dp),
                    textAlign = TextAlign.Start
                )
            }

            items(routines.size) { index ->
                val routine = routines[index]
                RoutineListItem(
                    routine = routine,
                    onClick = { onStartWorkout(routine.title) }
                )
            }

            // SECTION 2: Exercises
            item {
                Text(
                    text = "Exercises",
                    style = WearTypography.body1,
                    color = TextGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 4.dp),
                    textAlign = TextAlign.Start
                )
            }

            items(exercises.size) { index ->
                val exercise = exercises[index]
                ExerciseListItem(
                    exercise = exercise,
                    onClick = { onStartWorkout(exercise.title) }
                )
            }
        }
    }
}

@Composable
fun RoutineListItem(
    routine: RoutineItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Chip(
        onClick = onClick,
        label = {
            Text(
                text = routine.title,
                style = WearTypography.title1,
                fontSize = 15.sp,
                color = TextWhite,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        },
        secondaryLabel = {
            Text(
                text = routine.description,
                style = WearTypography.body1,
                fontSize = 11.sp, // slightly smaller details list
                color = TextGray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        },
        icon = {
            Image(
                painter = painterResource(id = R.drawable.ic_routine),
                contentDescription = routine.title,
                modifier = Modifier.size(24.dp)
            )
        },
        colors = ChipDefaults.secondaryChipColors(),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    )
}

@Composable
fun ExerciseListItem(
    exercise: ExerciseItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isStrength = exercise.imageUrl?.contains("routine") == true
    val defaultIcon = if (isStrength) R.drawable.ic_routine else R.drawable.ic_workout
    
    val imagePainter = rememberAsyncImagePainter(
        model = exercise.imageUrl,
        placeholder = painterResource(id = defaultIcon),
        error = painterResource(id = defaultIcon)
    )

    Chip(
        onClick = onClick,
        label = {
            Text(
                text = exercise.title,
                style = WearTypography.title1,
                fontSize = 15.sp,
                color = TextWhite,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        },
        icon = {
            Image(
                painter = imagePainter,
                contentDescription = exercise.title,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp),
                contentScale = ContentScale.Fit
            )
        },
        colors = ChipDefaults.secondaryChipColors(),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    )
}
