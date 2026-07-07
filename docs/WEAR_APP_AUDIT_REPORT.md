# Tio-hub Wear OS Watch App Audit & Architecture Report

यह ऑडिट रिपोर्ट **Tio-hub** मल्टी-मॉड्यूल एंड्रॉइड प्रोजेक्ट के अंतर्गत **Wear OS (स्मार्टवॉच) ऐप** को सैमसंग स्टाइल (Samsung Health Watch Design) में डिजाइन और इंटीग्रेट करने का संपूर्ण खाका (Blueprint) प्रदान करती है।

---

## 📂 1. Tio-hub वियर ओएस पैकेज संरचना (Wear OS Package Structure)

सैमसंग हेल्थ वॉच ऐप के आर्किटेक्चर के अनुरूप, `:wear` मॉड्यूल (`apps/wear`) के पैकेज डायरेक्टरी ट्री को इस प्रकार व्यवस्थित किया जाना चाहिए:

```text
G:\Tio-hub\apps\wear\src\main\java\com\tnyx\wear\
│
├── base/                              # 1. बेस लेयर
│   ├── di/                            # Hilt Dependency Injection
│   └── ui/                            # Wear Base Activity & Lifecycle
│
├── feature/                           # 2. स्क्रीन और फीचर्स (UI Screens)
│   ├── home/                          # लॉन्चर डैशबोर्ड (रीसाइक्लर लिस्ट)
│   ├── workout/                       # लाइव वर्कआउट (DuringWorkout, Controls)
│   ├── steps/                         # कदम काउंटर और लक्ष्य सेटिंग
│   ├── water/                         # पानी पीने के कप्स चुनने का व्हील
│   └── nutrition/                     # कैलोरी/भोजन विवरण स्क्रीन
│
├── tile/                              # 3. घड़ी के होम विजेट्स (Tiles Service)
│   ├── DailyActivityTileService.kt
│   └── WaterQuickAddTileService.kt
│
├── complications/                     # 4. डायल डेटा विजेट्स (Complications Source)
│   ├── StepsComplicationProvider.kt
│   └── HRComplicationProvider.kt
│
├── sensor/                            # 5. रॉ सेंसर मैनेजमेंट
│   ├── PedometerSensorReader.kt
│   └── HeartRateSensorReader.kt
│
└── device/                            # 6. फोन सिंकिंग और नेटवर्किंग
    ├── sync/                          # DataClient & MessageClient Sync Engine
    └── listener/                      # WearableListenerService (फोन से डेटा प्राप्त करना)
```

---

## 🎨 2. सैमसंग स्टाइल थीम और रीउज़ेबल कंपोनेंट्स (Design System Setup)

सैमसंग हेल्थ वियर थीम को `:core` (या साझा UI मॉड्यूल) में निम्नानुसार सेटअप किया जाना चाहिए:

### A. Color.kt (AMOLED थीम कलर्स)
```kotlin
package com.tnyx.core.theme

import androidx.compose.ui.graphics.Color

val BackgroundBlack = Color(0xFF000000)
val CardBackground = Color(0xFF1C1C1E)   // सैमसंग स्टाइल डार्क ग्रे कार्ड बैकग्राउंड
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF8E8E93)

// ट्रैकर्स ब्रांड कलर्स (Accents)
val ColorSteps = Color(0xFF00C853)         // कदम: हरा
val ColorWater = Color(0xFF29B6F6)         // पानी: हल्का नीला
val ColorHeartRate = Color(0xFFFF5252)     // धड़कन: लाल
val ColorSleep = Color(0xFF5E35B1)         // नींद: जामुनी
```

### B. Reusable Composable: HealthCard
सैमसंग के होम कार्ड्स के समान, कॉम्पैक्ट और गोल कोनों वाला रीउज़ेबल विजेट:
```kotlin
@Composable
fun HealthCard(
    icon: Painter,
    title: String,
    valueText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = icon, contentDescription = title, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.title1, color = TextWhite)
            Text(text = valueText, style = MaterialTheme.typography.body1, color = TextGray)
        }
    }
}
```

---

## 📲 3. फोन और वॉच ऐप के बीच सिंकिंग (Phone-Watch Data Sync)

स्मार्टवॉच ऐप (`:wear`) को मुख्य फोन ऐप (`:app`) के साथ जोड़ने के लिए **Google Play Services Wearable APIs** का उपयोग किया जाएगा। इसके 3 प्रमुख स्तंभ हैं:

1. **`DataClient` (डेटा सिंक)**:
   - **कब उपयोग करें**: जब फोन और वॉच के बीच डेटा सिंक रखना हो (जैसे कैलोरी और पानी के आंकड़े)।
   - **विशेषता**: यह ऑटोमैटिक रूप से बैकग्राउंड में फाइलों या डेटा-मैप्स को सिंक्रोनाइज़ करता है, भले ही ऐप बंद हो।
2. **`MessageClient` (रैपिड इवेंट्स)**:
   - **कब उपयोग करें**: ट्रिगर्स और कमांड्स भेजने के लिए (जैसे वॉच से "Start Workout" दबाने पर फोन पर वर्कआउट शुरू होना)।
3. **`CapabilityClient` (खोज/डिस्कवरी)**:
   - **कब उपयोग करें**: यह जांचने के लिए कि क्या फोन में कम्पेनियन ऐप इंस्टॉल्ड है।

### 🛠️ डेटा सिंकिंग के लिए उदाहरण सर्विस (WearableListenerService)
वॉच और फोन दोनों में निम्नलिखित सर्विस सेटअप की जाएगी जो बैकग्राउंड में सिंक इवेंट्स को सुनती है:

```kotlin
package com.tnyx.wear.device.listener

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompanionDataListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == com.google.android.gms.wearable.DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/health_summary") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val calories = dataMap.getInt("calories")
                    val waterCups = dataMap.getInt("water")
                    
                    // लोकल रिपोजिटरी/Preferences में सिंक डेटा सेव करें
                    serviceScope.launch {
                        // Repository.saveSyncedData(calories, waterCups)
                    }
                }
            }
        }
    }
}
```

---

## 🛠️ एकीकरण योजना (Integration Roadmap)
1. **थीम माइग्रेशन**: `:core` मॉड्यूल में ऊपर दिए गए `Color.kt` और रीउज़ेबल कार्ड्स को सेव करें।
2. **पैकेज आर्किटेक्चर**: `:wear` में सुझाई गई डायरेक्टरी के अनुसार पैकेजेस बनाएं।
3. **ब्लूटूथ सिंक**: फोन और वॉच दोनों में `CompanionDataListenerService` रजिस्टर करें और `AndroidManifest.xml` में सर्विस बाइंडिंग कॉन्फ़िगर करें।
