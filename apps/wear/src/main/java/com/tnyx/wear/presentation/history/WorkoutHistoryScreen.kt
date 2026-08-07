package com.tnyx.wear.presentation.history

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import coil.compose.rememberAsyncImagePainter
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.tnyx.wear.theme.TextWhite
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.tnyx.wear.R
import com.tnyx.wear.theme.CardBackground
import com.tnyx.wear.theme.TextGray
import com.tnyx.wear.theme.TextWhite
import com.tnyx.wear.theme.WearTypography
import org.json.JSONArray

data class WorkoutLogItem(
    val id: String,
    val type: String,            // e.g. "Running", "Bench Press"
    val dateHeader: String,      // e.g. "Monday, Jul 6"
    val dayTotalDuration: String,// e.g. "45m"
    val startTime: String,       // e.g. "7:30 AM"
    val durationVal: String,     // e.g. "45:12"
    val durationUnit: String,    // e.g. "min"
    val secondaryLabel: String,  // Dynamic label: "Distance" or "Volume" or "Calories"
    val secondaryVal: String,    // e.g. "5.2" or "2,400"
    val secondaryUnit: String,   // e.g. "km" or "kg"
    val iconResId: Int,          // Fallback resource
    var imageUrl: String? = null // Dynamic local asset or cloud image URL
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

// Helper to load exercises JSON and map titles to gender-specific local/cloud URLs
fun loadExercisesFromJson(context: Context, isFemale: Boolean = false): Map<String, String> {
    val exerciseImageMap = mutableMapOf<String, String>()
    try {
        val jsonString = context.resources.openRawResource(R.raw.app_exercises)
            .bufferedReader()
            .use { it.readText() }
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val title = item.optString("title")
            val legacyId = item.optString("legacyId")
            val media = item.optJSONObject("media")
            
            // Check if local asset exists, otherwise use dynamic network URL
            val localAssetPath = "exercise_thumbnails/$legacyId.jpg"
            val imageUrl = if (!legacyId.isNullOrEmpty() && assetFileExists(context, localAssetPath)) {
                "file:///android_asset/$localAssetPath"
            } else {
                val primaryGender = if (isFemale) "female" else "male"
                val secondaryGender = if (isFemale) "male" else "female"
                val genderObj = media?.optJSONObject(primaryGender) ?: media?.optJSONObject(secondaryGender)
                genderObj?.optString("imageUrl") ?: genderObj?.optString("thumbnailUrl")
            }
            
            if (!title.isNullOrEmpty() && !imageUrl.isNullOrEmpty()) {
                exerciseImageMap[title.lowercase().trim()] = imageUrl
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return exerciseImageMap
}

// Fuzzy lookup to resolve exercise URL (e.g. "Bench Press" matches "Barbell Bench Press")
fun lookupExerciseImageUrl(title: String, exerciseMap: Map<String, String>): String? {
    val query = title.lowercase().trim()
    if (exerciseMap.containsKey(query)) {
        return exerciseMap[query]
    }
    return exerciseMap.entries.firstOrNull { it.key.contains(query) }?.value
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun WorkoutHistoryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val columnState = rememberColumnState()
    val context = LocalContext.current

    // Read user gender preference from SharedPreferences
    val sharedPref = remember(context) {
        context.getSharedPreferences("tnyx_health_prefs", Context.MODE_PRIVATE)
    }
    val isFemale = sharedPref.getBoolean("pref_is_female", false)

    // Parse JSON in remember block to prevent blocking main thread
    val exerciseMap = remember(isFemale) {
        loadExercisesFromJson(context, isFemale)
    }
    
    // Grouped weekly logs
    val logs = remember(exerciseMap) {
        listOf(
            WorkoutLogItem(
                id = "1", 
                type = "Running", 
                dateHeader = "Monday, Jul 6", 
                dayTotalDuration = "45m", 
                startTime = "7:30 AM", 
                durationVal = "45:12", 
                durationUnit = "min",
                secondaryLabel = "Distance", 
                secondaryVal = "5.2", 
                secondaryUnit = "km",
                iconResId = R.drawable.ic_workout
            ),
            WorkoutLogItem(
                id = "2", 
                type = "Bench Press", 
                dateHeader = "Sunday, Jul 5", 
                dayTotalDuration = "30m", 
                startTime = "5:15 PM", 
                durationVal = "30:00", 
                durationUnit = "min",
                secondaryLabel = "Volume", 
                secondaryVal = "2,400", 
                secondaryUnit = "kg",
                iconResId = R.drawable.ic_routine
            ),
            WorkoutLogItem(
                id = "3", 
                type = "Running", 
                dateHeader = "Friday, Jul 3", 
                dayTotalDuration = "50m", 
                startTime = "6:00 AM", 
                durationVal = "50:05", 
                durationUnit = "min",
                secondaryLabel = "Distance", 
                secondaryVal = "6.1", 
                secondaryUnit = "km",
                iconResId = R.drawable.ic_workout
            )
        ).map { item ->
            item.apply {
                imageUrl = lookupExerciseImageUrl(type, exerciseMap)
            }
        }
    }

    // Dynamic aggregates based on predominant workout log type (e.g. Cardio vs Strength)
    val totalTime = "2h 05m"
    val totalCalories = "920 kcal"
    val totalSessions = "3 sessions"
    
    // Check if the most recent workout is Strength (Bench Press) or Cardio (Running)
    val isStrengthFocus = logs.firstOrNull()?.secondaryLabel == "Volume"
    val secondaryMetricLabel = if (isStrengthFocus) "Volume" else "Distance"
    val secondaryMetricValue = if (isStrengthFocus) "2,400 kg" else "11.3 km"

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = modifier.fillMaxSize()
        ) {
            // Header Title
            item {
                Text(
                    text = "This week",
                    style = WearTypography.title1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Weekly Summary Grid Section (Directly on Black Background)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // Row 1: Time & Dynamic Secondary Metric (Distance / Volume)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Time", style = WearTypography.body1, color = TextGray)
                            Text(text = totalTime, style = WearTypography.title1, fontSize = 15.sp, color = TextWhite)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = secondaryMetricLabel, style = WearTypography.body1, color = TextGray)
                            Text(text = secondaryMetricValue, style = WearTypography.title1, fontSize = 15.sp, color = TextWhite)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Row 2: Calories & Sessions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Calories", style = WearTypography.body1, color = TextGray)
                            Text(text = totalCalories, style = WearTypography.title1, fontSize = 15.sp, color = TextWhite)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Sessions", style = WearTypography.body1, color = TextGray)
                            Text(text = totalSessions, style = WearTypography.title1, fontSize = 15.sp, color = TextWhite)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Logs List Section
            if (logs.isEmpty()) {
                item {
                    Text(
                        text = "No workouts this week",
                        style = WearTypography.body1,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            } else {
                logs.forEach { log ->
                    // 1. Day Header Row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = log.dateHeader,
                                style = WearTypography.button,
                                color = TextWhite
                            )
                            Text(
                                text = log.dayTotalDuration,
                                style = WearTypography.button,
                                color = TextWhite
                            )
                        }
                    }
                    
                    // 2. Workout Card item
                    item {
                        WorkoutCard(log = log, onClick = { /* Navigation details placeholder */ })
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutCard(
    log: WorkoutLogItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Coil Async Image Painter loads "file:///android_asset/exercise_thumbnails/*.jpg" locally
    val imagePainter = if (!log.imageUrl.isNullOrEmpty()) {
        rememberAsyncImagePainter(
            model = log.imageUrl,
            placeholder = painterResource(id = log.iconResId),
            error = painterResource(id = log.iconResId)
        )
    } else {
        painterResource(id = log.iconResId)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Rounded Exercise Thumbnail image (32dp size, circle shape, white background)
        Image(
            painter = imagePainter,
            contentDescription = log.type,
            modifier = Modifier
                .size(32.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(TextWhite)
                .padding(2.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Right side: All textual metrics aligned vertically to the right of the icon
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            // Line 1: Exercise Name
            Text(
                text = log.type,
                style = WearTypography.title1,
                fontSize = 15.sp,
                color = TextWhite
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Line 2: Duration (Value 18sp bold, Unit 13sp)
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)) {
                        append(log.durationVal)
                    }
                    append(" ")
                    withStyle(style = SpanStyle(fontSize = 13.sp, color = TextGray)) {
                        append(log.durationUnit)
                    }
                }
            )
            
            // Line 3: Secondary Metric (Distance/Volume)
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)) {
                        append(log.secondaryVal)
                    }
                    append(" ")
                    withStyle(style = SpanStyle(fontSize = 13.sp, color = TextGray)) {
                        append(log.secondaryUnit)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Line 4: Start Time
            Text(
                text = log.startTime,
                style = WearTypography.body1,
                fontSize = 12.sp,
                color = TextGray
            )
        }
    }
}
