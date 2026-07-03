# 📱 Hevy App — Complete Reusable Components & Widgets Reference

> **Source:** `G:\Tio-hub\design\references\Hevy\Hevy` (Decompiled APK — React Native Core + Native Android Kotlin/Compose Extensions)
> **Framework:** React Native (Hybrid UI Layer) + Android Jetpack Glance (Native Home Screen Widgets) + Native Java bridges (SQLite, AlarmManager, Notifications)

---

## 🏠 Section 1: Android Home-Screen Widgets (10 Widgets)

हीवी (Hevy) के Android Home-screen Widgets को **Jetpack Glance (Compose-like native UI framework for widgets)** का उपयोग करके कोटलिन में लिखा गया है।
सभी विजेट्स `WidgetData.java` से डेटा फेच करते हैं। जब भी यूजर ऐप में कोई वर्कआउट सेव या अपडेट करता है, JS लेयर `WidgetModule` को कॉल करके XML/JSON सिंक कराती है और विजेट री-रेंडर होते हैं।

---

### Widget 1: Streak Widget
**Class Path:** `com.hevy.widgets.streak.StreakWidget`

**क्या है:**
यूज़र की लगातार ट्रेनिंग डेज़ की गिनती (Streak) को होम स्क्रीन पर एक बड़े, बोल्ड नंबर के रूप में दिखाने वाला विजेट।

**Visual Structure:**
- Left Side (Vertical Column):
  - `TextTitleMedium`: Streak की संख्या (उदा. "15"), सफ़ेद रंग में।
  - `TextBodySmall`: "weeks" या "days" का लेबल।
- Right Side:
  - `streak_icon` (`R.drawable.streak_icon`): ३२dp साइज़ का फ़्लेम (आग) का आइकॉन, जो सेकेंडरी थीम कलर (`GlanceTheme.colors.secondary`) से टिंटेड रहता है।
- Layout Constraint: full size row aligned to bottom, rounded container padding 12dp.

**कहाँ Use होता है:** Android Home Screen पर।

**क्यों Use होता है:**
यह एक कोर गेमीफिकेशन एलिमेंट है। यूज़र को बिना ऐप खोले लगातार वर्कआउट करने के लिए प्रेरित (motivation) करता है।

**कैसे काम करता है:**
`StreakWidgetReceiver` विजेट को रजिस्टर करता है। `WidgetStore` से `streakLength` (integer) निकाला जाता है। कोटलिन में `ComponentsKt.TextTitleMedium(String.valueOf(streakLength))` कॉल होकर विजेट का UI ड्रा होता है।

---

### Widget 2: Calendar Widget
**Class Path:** `com.hevy.widgets.calendar.CalendarWidget`

**क्या है:**
साल भर की वर्कआउट कंसिस्टेंसी को दर्शाने वाला GitHub-style Activity Heatmap।

**Visual Structure:**
- Header: `CalendarWidget.Header` जिसमें करेंट मंथ का नाम और लोगो रहता है।
- Grid Area: `ComponentsKt.Calendar` द्वारा रेंडर किया गया ग्रिड, जिसमें पिछले कुछ महीनों के दिन छोटे डिब्बों (`DayContainer`) के रूप में दिखते हैं।
- Day Box: `DayContainer` विजेट:
  - अगर वर्कआउट हुआ है (`hasWorkoutDataInDay`): सॉलिड व्हाइट कलर (`Color.White`) का डिब्बा।
  - अगर आज का दिन है (`isToday`): प्राइमरी थीम कलर (`GlanceTheme.colors.primary`) का बॉर्डर या डिब्बा।
  - डिफ़ॉल्ट: ऑन-बैकग्राउंड म्यूटेड कलर।

**कहाँ Use होता है:** Android Home Screen पर (Medium या Large साइज में)।

**क्यों Use होता है:**
यूज़र को अपने पूरे महीने/साल का वर्कआउट पैटर्न एक नज़र में देखने की सुविधा देता है। खाली दिन वर्कआउट करने की याद दिलाते हैं।

**कैसे काम करता है:**
`workoutDayStats` मैप से तारीखों की मैपिंग उठाई जाती है। ग्रिड के हर सेल को `DayContainer(label, hasWorkoutDataInDay, isToday)` कंपोनेंट में पास करके रेंडर किया जाता है। सेल पर क्लिक करने पर यूज़र को हीवी ऐप के कैलेंडर टैब (`deeplinking path: "calendar"`) पर रीडायरेक्ट किया जाता है।

---

### Widget 3: Calendar Stats Widget
**Class Path:** `com.hevy.widgets.calendarstats.CalendarStatsWidget`

**क्या है:**
Calendar Widget (Heatmap) और साप्ताहिक वर्कआउट आंकड़ों (weekly stats) का एक कंबाइंड विजेट, जो ज़्यादा जानकारी देता है।

**Visual Structure:**
- Left Side: Monthly active Calendar grid.
- Right Side (Stats Panel):
  - इस हफ्ते के वर्कआउट्स की संख्या (उदा. "3 Workouts")।
  - साप्ताहिक वॉल्यूम (Volume) और कुल समय (Duration)।

**कहाँ Use होता है:** Android Home Screen पर (केवल Large/Horizontal साइज में)।

**क्यों Use होता है:**
यह उन यूज़र्स के लिए है जो एक ही विजेट में कंसिस्टेंसी (Calendar) और वॉल्यूम प्रोग्रेस (Weekly Stats) दोनों देखना चाहते हैं।

**कैसे काम करता है:**
`workoutDayStats` और `weekdayIndex` का कॉम्बिनेशन यूज़ करता है। कोटलिन फ़ाइल कैलेंडर कंपोनेंट और स्टेट्स लेआउट को एक `Row` में कंबाइन करती है।

---

### Widget 4: Chart Widget
**Class Path:** `com.hevy.widgets.chart.ChartWidget`

**क्या है:**
यूज़र द्वारा सेलेक्टेड किसी स्पेसिफिक एक्सरसाइज के प्रोग्रेस ट्रेंड (Volume, Reps, या Estimated 1RM) को होम स्क्रीन पर बार या लाइन चार्ट के रूप में दिखाना।

**Visual Structure:**
- Header: एक्सरसाइज का नाम (उदा. "Bench Press (Barbell)") और प्रोग्रेस प्रीडिक्टेड वॉल्यूम।
- Chart View: ग्लैंस कंपैटिबल बार ग्रैफिक्स या कैनवस ड्राइंग, जिसमें पिछले ६ या १२ सेशन्स का डेटा रिफ्लेक्ट होता है।
- Configuration Icon: टॉप-राइट कॉर्नर में एडिट/सेटिंग गियर आइकॉन।

**कहाँ Use होता है:** Android Home Screen पर।

**क्यों Use होता है:**
अपनी सबसे पसंदीदा या प्राइमरी एक्सरसाइज (उदा. Squats, Deadlift) की प्रोग्रेस को सीधे मॉनिटर करने के लिए।

**कैसे काम करता है:**
यह प्रोग्रेसिव रूप से `ChartWidgetConfig` क्लास का उपयोग करता है। यूज़र होम स्क्रीन पर विजेट ऐड करते समय `ChartWidgetConfigurationActivity` के ज़रिए एक्सरसाइज और मेट्रिक (उदा. 1RM) चुनता है। कोटलिन का ड्राइंग हेल्पर सिलेक्टेड डेटा को चार्ट ग्रैफ़िक्स में कन्वर्ट करता है।

---

### Widget 5: Day Routine Widget
**Class Path:** `com.hevy.widgets.dayroutine.DayRoutineWidget`

**क्या है:**
यूज़र के साप्ताहिक शेड्यूल के अनुसार आज के दिन के लिए असाइन्ड रुटीन (Routine) का क्विक शॉर्टकट।

**Visual Structure:**
- Routine Title: आज का रुटीन नेम (उदा. "Push Day A")।
- Info: कुल कितनी एक्सरसाइजेज़ हैं (उदा. "6 Exercises")।
- CTA: "Start Workout" का बड़ा प्ले बटन।

**कहाँ Use होता है:** Android Home Screen पर।

**क्यों Use होता है:**
जिम पहुँचते ही ऐप को मैन्युअली खोलकर रुटीन खोजने का झंझट खत्म करने के लिए। सिर्फ होम स्क्रीन से ही वर्कआउट सेशन चालू हो जाता है।

**कैसे काम करता है:**
`weekdayIndex` के आधार पर आज की रुटीन आइडेंटिफाई होती है। प्ले बटन दबाने पर `Deeplinking` मॉड्यूल को ट्रिगर किया जाता है जो रुटीन आईडी के साथ हीवी के 'Active Logger' को सीधे खोल देता है।

---

### Widget 6: Last Routines Widget
**Class Path:** `com.hevy.widgets.lastroutines.LastRoutinesWidget`

**क्या है:**
यूज़र की सबसे हाल ही में इस्तेमाल की गई (Recently Used) या पसंदीदा (Favorite) २ या ३ रुटीन्स की लिस्ट।

**Visual Structure:**
- Vertical List layout:
  - प्रत्येक रो में रुटीन का नाम।
  - नंबर ऑफ सेट्स/एक्सरसाइजेज़ की समरी।
  - राइट साइड में क्विक स्टार्ट प्ले आइकॉन।

**कहाँ Use होता है:** Android Home Screen पर (Medium size में)।

**क्यों Use होता है:**
मल्टीपल कस्टमाइज्ड रुटीन्स फॉलो करने वाले यूज़र्स के लिए प्रोग्रेस शॉर्टकट।

**कैसे काम करता है:**
`latestRoutines` लिस्ट (जो कि `SimpleRoutine.java` से मैप होती है) से डेटा रीड करके रो रेंडर होती है। हर रो का प्ले बटन सीधे उस रुटीन के वर्कआउट को ट्रिगर करता है।

---

### Widget 7: Last Workouts Widget
**Class Path:** `com.hevy.widgets.lastworkouts.LastWorkoutsWidget`

**क्या है:**
पिछले वर्कआउट्स का संक्षिप्त इतिहास (History Overview) दिखाने वाला विजेट।

**Visual Structure:**
- वर्कआउट का नाम (उदा. "Upper Body Strength")।
- वर्कआउट करने की तारीख (उदा. "Yesterday" या "Oct 15")।
- मुख्य मेट्रिक्स: Total Lifted Weight, Total Sets, Workout Time।

**कहाँ Use होता है:** Android Home Screen पर।

**क्यों Use होता है:**
यूज़र को अपने हालिया अचीवमेंट को होम स्क्रीन पर देखने और ट्रैक करने की सहूलियत देता है।

**कैसे काम करता है:**
`WidgetData` में मौजूद `workoutDayStats` के आखिरी २-३ आइटम्स के एग्रीगेट्स को रेंडर किया जाता है।

---

### Widget 8: Quick Access Widget
**Class Path:** `com.hevy.widgets.quickaccess.QuickAccessWidget`

**क्या है:**
अक्सर इस्तेमाल होने वाले फीचर्स जैसे "Start Empty Workout", "Search Exercises", या फोल्डर्स का एक ग्रिड-आधारित क्विक एक्सेस पैड।

**Visual Structure:**
- 2×2 या 3×2 ग्रिड ऑफ आइकॉन शॉर्टकट्स।
- प्रत्येक बटन का अपना एक राउंडेड बैकग्राउंड और हीवी स्टाइल आइकॉन (उदा. डंबल आइकॉन, प्लस आइकॉन) होता है।

**कहाँ Use होता है:** Android Home Screen पर।

**क्यों Use होता है:**
ऐप के मुख्य फ़ीचर्स को वन-क्लिक में एक्सेस करने के लिए।

**कैसे काम करता है:**
`QuickAccessWidgetConfig` के ऑप्शन्स को रीड करके ग्रिड तैयार होता है। हर ग्रिड आइटम पर हीवी का स्पेसिफिक डीपलिंक (जैसे `hevy://start-empty` या `hevy://search`) बाइंड रहता है।

---

### Widget 9: Rest Widget (Live Countdown Timer)
**Class Path:** `com.hevy.widgets.rest.RestWidget`

**क्या है:**
वर्कआउट करते समय सेट्स के बीच में रेस्ट टाइमर को होम स्क्रीन पर लाइव दिखाना।

**Visual Structure:**
- Circular progress bar (Glance circular loader या custom ring)।
- बीच में बड़ा टाइम काउंटडाउन (उदा. "01:45")।
- नीचे रेस्ट स्किपिंग या एक्सटेंड बटन (+15s)।

**कहाँ Use होता है:** Active workout के दौरान होम स्क्रीन पर (वर्कआउट बंद होते ही हाइड हो जाता है)।

**क्यों Use होता है:**
यूज़र अगर सेट्स के बीच में दूसरा ऐप (उदा. Spotify, Instagram) चला रहा है, तो भी वह होम स्क्रीन पर बिना हीवी खोले अपना रेस्ट टाइम पूरा होते देख सकता है।

**कैसे काम करता है:**
`TimerNotificationModule` के लाइव ब्रॉडकास्ट और नोटिफिकेशन सर्विस के साथ सिंक रहता है। हर सेकंड टाइमर अपडेट होने पर विजेट सर्विस से टिक-इवेंट रिसीव करके री-ड्रॉ होता है।

---

### Widget 10: Weekly Stats Widget
**Class Path:** `com.hevy.widgets.weeklystats.WeeklyStatsWidget`

**क्या है:**
इस हफ्ते के वर्कआउट स्टेट्स (Volume, Sets, Reps या Time) की तुलना पिछले हफ्ते से करके दिखाने वाला प्रोग्रेस विजेट।

**Visual Structure:**
- Header: विजेट का नाम (उदा. "Weekly Volume" या "Weekly Sets") + Hevy Logo.
- Main Panel:
  - "This Week": बड़ा प्रोग्रेस नंबर (उदा. "14,520 kg" या "45 Sets") सफ़ेद रंग में।
  - "Last Week": पिछला नंबर (उदा. "12,200 kg" या "38 Sets") म्यूटेड ग्रे रंग में (`GlanceTheme.colors.onSurfaceVariant`)।
- Alignment: Bottom-aligned vertically stacked data column.

**कहाँ Use होता है:** Android Home Screen पर।

**क्यों Use होता है:**
साप्ताहिक वॉल्यूम ओवरलोड (Progressive Overload) को मॉनिटर करने के लिए।

**कैसे काम करता है:**
`Utils.getDayRangeSum()` मेथड `workoutDayStats` से डेटा फ़िल्टर करती है:
1. करेंट वीक के दिनों (weekdayIndex के आधार पर) के वॉल्यूम/सेट्स को सम (sum) किया जाता है → `This Week`.
2. पिछले हफ्ते के सेम दिनों का डेटा फ़िल्टर किया जाता है → `Last Week`.
3. `formatValue()` के ज़रिए यूनिट (kg/lbs) के साथ वैल्यू फॉर्मेट करके रेंडर होती है।

---

## 🧩 Section 2: In-App Shared UI Components (Kotlin Glance)

ये कंपोनेंट `ComponentsKt.java` में मौजूद हैं और सभी होम-स्क्रीन विजेट्स के लेआउट को यूनिफ़ॉर्म डिज़ाइन देने के लिए इस्तेमाल होते हैं।

---

### Component 1: Typography System (Text Components)
ये हीवी विजेट्स के कस्टमाइज्ड टेक्स्ट एलिमेंट्स हैं जो सही फॉन्ट-फैमिली, साइज और वेट को डिफाइन करते हैं।

| Component Name | Size Level | Font Weight | Implementation & Behavior |
| :--- | :--- | :--- | :--- |
| `TextBodyUltraSmall` | Extra Small (11sp) | Medium (W500) | `ComponentsKt.TextBodyUltraSmall` - यह कैलेंडर के डेट लेबल्स या छोटे नंबर्स को ड्रा करता है। |
| `TextBodySmall` | Small (12sp) | Regular | `ComponentsKt.TextBodySmall` - सेकेंडरी लेबल्स जैसे "last week", "weeks ago" के लिए उपयोग होता है। |
| `TextBodyMedium` | Medium (14sp) | Regular | `ComponentsKt.TextBodyMedium` - विजेट के अंदर पैराग्राफ या डिटेल्स टेक्स्ट के लिए। |
| `TextTitleSmall` | Small Title (14sp) | Semi-Bold (W600) | `ComponentsKt.TextTitleSmall` - सेक्शन हेडिंग्स के लिए। |
| `TextTitleMedium` | Medium Title (16sp) | Semi-Bold (W600) | `ComponentsKt.TextTitleMedium` - एक्सरसाइज और रुटीन्स के नाम रेंडर करने के लिए। |
| `TextTitleBig` | Large Title (20sp) | Bold (W700) | `ComponentsKt.TextTitleBig` - बड़े आंकड़ों और मेट्रिक्स काउंटर्स के लिए। |
| `TextHeadlineSmall` | XL (24sp) | Bold (W700) | `ComponentsKt.TextHeadlineSmall` - वॉल्यूम काउंट्स (उदा. 12,000 kg) के लिए। |
| `TextHeadlineMedium` | XXL (32sp) | Bold (W800) | `ComponentsKt.TextHeadlineMedium` - बड़े स्ट्रिक काउंटर्स या हाइलाइटेड विजेट वैल्यूज़ के लिए। |

**कहाँ Use होते हैं:** सभी 10 Android Widgets में।

**क्यों Use होते हैं:**
ताकि अलग-अलग एंड्रॉयड वर्जन और स्क्रीन डेंसिटी पर फॉन्ट साइज और लाइन-हाइट बिखरे नहीं। ग्लैंस कंपोज़ में कस्टमाइज़्ड रैपर प्रदान करना।

**कैसे काम करते हैं:**
ये सभी हीवी के कोर कस्टम थीम (`Themes.java` और `HevyThemes.java`) के कलर्स और स्टाइल्स को कंज़्यूम करके ग्लैंस के `Text` विजेट को कॉल करते हैं।

---

### Component 2: DayContainer (Calendar Cell)
**Class File:** `ComponentsKt.java` (Line 289)

**क्या है:**
GitHub स्टाइल हीटमैप ग्रिड में एक दिन का प्रतिनिधित्व करने वाला चौकोर बॉक्स (Square Cell)।

**Visual Structure:**
- Label: तारीख का नंबर (11sp, `TextBodyUltraSmall`)।
- Background Color Behavior:
  - `z == true` (वर्कआउट डेटा मौजूद है): सफ़ेद रंग (`Color.White`) का बैकग्राउंड।
  - `z2 == true` (आज का दिन है): हीवी का ब्रांड थीम प्राइमरी कलर (`GlanceTheme.colors.primary` - ब्लू)।
  - डिफ़ॉल्ट: डार्क या ऑन-बैकग्राउंड थीम कलर।

**कहाँ Use होता है:** `CalendarWidget` और `CalendarStatsWidget` के ग्रिड लेआउट के अंदर।

**क्यों Use होता है:**
सालाना या मासिक एक्सरसाइज फ्रीक्वेंसी को ग्राफिकली मैप करने के लिए।

**कैसे काम करता है:**
`z` (hasWorkoutDataInDay) और `z2` (isToday) बूलियन फ़्लैग्स को चेक करता है। तदनुसार `ColorProvider` का बैकग्राउंड कलर सेट करके सेल को रेंडर करता है।

---

### Component 3: Calendar Grid Component
**Class File:** `ComponentsKt.java` (Line 344)

**क्या है:**
सातों दिनों (Mon-Sun) के हेडर और तारीखों के डिब्बों (`DayContainer`) को मिलाकर तैयार किया गया पूरा हीटमैप ग्रिड।

**Visual Structure:**
- ७ कॉलम का हॉरिजॉन्टल लेआउट।
- टॉप रो: हफ़्ते के दिनों के नाम (M, T, W, T, F, S, S)।
- बॉटम रोज़: पिछले ५ हफ्तों या महीनों की तारीखों का ग्रिड।

**कहाँ Use होता है:** `CalendarWidget` और `CalendarStatsWidget` के कोर बॉडी में।

**क्यों Use होता है:**
पूरी एक्टिविटी को कंबाइन करके रेंडर करने की ज़िम्मेदारी इस कंपोनेंट की होती है।

**कैसे काम करता है:**
यह `workoutDayStats` मैप से हर डेट की एंट्री उठाता है। लूप चलाकर सातों दिनों के लिए `DayContainer` सेल्स को रेंडर करके ग्रिड को पूरा करता है।

---

## 📲 Section 3: Native Bridges & Modules (JS to Kotlin)

हीवी (Hevy) ऐप मूल रूप से **React Native** पर बना है, इसलिए ऐप के कोर फ़ीचर्स (लॉगिंग, डेटाबेस) नेटिव bridges के ज़रिए चलते हैं।

---

### Bridge Module 1: WorkoutStorage (SQLite Bridge)
**Java Class:** `com.hevy.WorkoutStorageModule`

**क्या है:**
रिएक्ट नेटिव जावास्क्रिप्ट थ्रेड से वर्कआउट डेटा लेकर लोकल SQLite डेटाबेस में स्टोर, मॉडिफाई और फेच करने वाला डेटा एक्सेस ब्रिज।

**कहाँ Use होता है:**
- Active Workout Logger (वर्कआउट ख़त्म होने पर डेटा सेव करते समय)।
- Workout History (इतिहास लोड करते समय)।
- Settings (डेटा सिंक/एक्सपोर्ट)।

**क्यों Use होता है:**
रिएक्ट नेटिव का डिफ़ॉल्ट `AsyncStorage` भारी वर्कआउट डेटा के लिए स्लो है। हीवी बड़े JSON ऑब्जेक्ट्स को तेज़ SQLite क्वेरी से प्रोसेस करने के लिए इस नेटिव ब्रिज का इस्तेमाल करता है।

**कैसे काम करता है:**
1. JS से `@ReactMethod storeWorkouts(ReadableMap, Promise)` कॉल होता है।
2. JSON स्ट्रिंग को एक्सट्रैक्ट किया जाता है।
3. एक बैकग्राउंड थ्रेड (`new Thread(...)`) चालू होती है ताकि मेन UI थ्रेड ब्लॉक न हो।
4. SQLite डेटाबेस (`workouts.sqlite`) में `ContentValues` के ज़रिए डेटा सिंक किया जाता है।
5. थ्रेड सेफ ऑपरेशन्स के लिए `synchronized(this)` ब्लॉक का यूज़ होता है।

---

### Bridge Module 2: TimerNotification (Rest Timer Notification & Service)
**Java Class:** `com.hevy.TimerNotificationModule`

**क्या है:**
वर्कआउट के सेट्स के बीच रेस्ट टाइमर को सिस्टम ट्रे में एक इंटरएक्टिव नोटिफिकेशन (Foreground Service) के रूप में मैनेज करने वाला मॉड्यूल।

**Visual Structure (Notification Panel):**
- Progress counter: "01:30" (लाइव बदलता टाइमर)।
- Action buttons:
  - "Skip Timer" (टाइमर खत्म करने के लिए)।
  - "Complete Set" (सीधे अगला सेट डन मार्क करने के लिए)।
  - "+15s" (रेस्ट टाइम बढ़ाने के लिए)।

**कहाँ Use होता है:**
Active workout के दौरान, सेट्स लॉग करने के बाद जब बैकग्राउंड रेस्ट टाइमर एक्टिव होता है।

**क्यों Use होता है:**
यह बहुत महत्वपूर्ण यूटिलिटी है। अगर यूज़र स्क्रीन बंद भी कर दे या दूसरा ऐप चलाए, तो भी एंड्रॉयड सिस्टम ओएस (OS) बैकग्राउंड थ्रेड में टाइमर को जिंदा रखता है और एग्जैक्ट अलार्म ट्रिगर करता है।

**कैसे काम करता है:**
1. JS से `@ReactMethod startOrUpdateForegroundService(ReadableMap, ...)` कॉल होता है।
2. मॉड्यूल एंड्रॉयड `AlarmManager` को कॉल करके एग्जैक्ट टाइम सेट करता है (`alarmManager.setExactAndAllowWhileIdle(...)`)।
3. `TimerNotificationService` स्टार्ट होती है, जो नोटिफिकेशन को रियल-टाइम अपडेट देती है।
4. बैटरी ऑप्टिमाइजेशन और बैकग्राउंड लिमिट्स को बायपास करने के लिए यूज़र से `canScheduleExactAlarms` की परमिशन ली जाती है।

---

### Bridge Module 3: WidgetModule (Widget Syncer)
**Java Class:** `com.hevy.WidgetModule`

**क्या है:**
JS वर्कआउट लॉग्स और स्टेट्स अपडेट होने के बाद एंड्रॉयड होम स्क्रीन विजेट्स के लिए डेटा सिंक करने वाला ब्रिज।

**कहाँ Use होता है:**
वर्कआउट सेव होने पर, रुटीन एडिट होने पर, या स्ट्रिक प्रोग्रेस बदलने पर बैकग्राउंड में।

**क्यों Use होता है:**
रिएक्ट नेटिव डायरेक्ट विजेट ओएस (OS) लेयर से बात नहीं कर सकता। यह मॉड्यूल ब्रिज का काम करता है।

**कैसे काम करता है:**
JS `WidgetModule.update(widgetDataJSON)` कॉल करता है → यह मॉड्यूल `WidgetData` को लोकल स्टोरेज में सेव करता है → एंड्रॉयड `AppWidgetManager` को ब्रॉडकास्ट भेजकर सभी 10 विजेट्स को फोर्स री-रेंडर (`onUpdate`) कराता है।

---

### Bridge Module 4: ShareStories (Social Sharing Helper)
**Java Class:** `com.hevy.ShareStories`

**क्या है:**
वर्कआउट समरी का एक सुंदर सोशल मीडिया स्टोरी इमेज (Instagram/WhatsApp Story) जेनरेट करके इंस्टाग्राम या अन्य ऐप्स पर भेजने वाला ब्रिज।

**कहाँ Use होता है:** Workout Complete / Summary Screen पर।

**क्यों Use होता है:**
फिटनेस प्रोग्रेस शेयरिंग को आसान बनाने और ऑर्गेनिक ऐप मार्केटिंग के लिए।

**कैसे काम करता है:**
यह रिएक्ट नेटिव व्यूज़ का स्क्रीनशॉट कैप्चर करता है या कस्टमाइज्ड कैनवस इमेज बनाता है, उसे `ShareStoriesFileProvider` के ज़रिए एक सेफ URI जनरेट करके इंस्टाग्राम स्टोरी इंटेंट (`com.instagram.share.ADD_TO_STORY`) के साथ स्टार्ट करता है।

---

## 🗺️ Section 4: Screen-wise Component Mapping (JS + Native)

हीवी (Hevy) ऐप की सभी कोर स्क्रीन्स और उनमें नेटिव/हाइब्रिड कंपोनेंट्स की भूमिका:

---

### Screen 1: Active Workout Logger
*   **JS/React Native UI Components:**
    *   `WorkoutSetTable`: टेबल ग्रिड जिसमें यूजर वजन (Weight), रेप्स (Reps), और सेट टाइप (Normal/Warmup/Drop Set) बदलता है।
    *   `SetTypeBadge`: सेट नंबर के पास दिखने वाला छोटा बैज (W = Warmup, D = Drop Set, F = Failure)।
    *   `FloatingRestTimer`: स्क्रीन के नीचे तैरता हुआ छोटा रेस्ट टाइमर।
*   **Serving Widgets:**
    *   `RestWidget` (होम स्क्रीन पर लाइव रेस्ट सिंक दिखाता है)।
*   **Serving Bridges:**
    *   `TimerNotificationModule` (नोटिफिकेशन अलार्म चलाता है)।
    *   `WorkoutStorageModule` (लाइव वर्कआउट स्टेट को डेटाबेस में ड्राफ्ट रखता है)।
    *   `SoundsModule` (टाइमर ख़त्म होने पर 'Beep' साउंड ट्रिगर करता है)।
    *   `WearOSConnectorModule` (स्मार्टवॉच के साथ हार्ट-रेट सिंक करता है)।

---

### Screen 2: Routine Editor & Creator
*   **JS/React Native UI Components:**
    *   `ExercisePicker`: सर्च बार, मसल ग्रुप फिल्टर चिप्स, और एक्सरसाइज की लिस्ट का कॉम्बो।
    *   `DragHandle`: सेट्स या एक्सरसाइज का आर्डर बदलने वाला ड्रैग-एंड-ड्रॉप लॉन्चर।
*   **Serving Widgets:**
    *   `DayRoutineWidget`, `LastRoutinesWidget`, `QuickAccessWidget` (यहाँ से यूज़र एडिट या सिलेक्ट करता है)।
*   **Serving Bridges:**
    *   `WorkoutStorageModule` (रुटीन टेम्पलेट्स को SQLite में सेव करता है)।

---

### Screen 3: Profile Overview & Analytics
*   **JS/React Native UI Components:**
    *   `ActivityHeatmap`: ऐप के अंदर प्रोफाइल स्क्रीन पर दिखने वाला हीटमैप (जो `CalendarWidget` का इन-ऐप वर्जन है)।
    *   `AnalyticsGraph`: पिछले ३ महीने या १ साल का वॉल्यूम और रेप्स प्रोग्रेस ग्रैफ़।
*   **Serving Widgets:**
    *   `CalendarWidget`, `CalendarStatsWidget`, `WeeklyStatsWidget`, `StreakWidget` (ये होम स्क्रीन पर इसी डेटा को सिंक करके दिखाते हैं)।
*   **Serving Bridges:**
    *   `WidgetModule` (डेटा बदलते ही होम स्क्रीन को अपडेट ट्रिगर करता है)।

---

### Screen 4: Workout Complete / Summary Screen
*   **JS/React Native UI Components:**
    *   `PRBanner`: पर्सनल रिकॉर्ड ब्रेक होने पर दिखने वाला गोल्ड कंफ़ेटी (confetti) बैनर।
    *   `WorkoutSummaryCard`: टाइम, वॉल्यूम, और सेट्स का ग्रिड कार्ड।
*   **Serving Widgets:**
    *   `LastWorkoutsWidget` (ऑटोमैटिकली नया वर्कआउट यहाँ समराइज़ हो जाता है)।
*   **Serving Bridges:**
    *   `ShareStories` (इंस्टाग्राम स्टोरी शेयरिंग लॉन्चर)।

---

## 🏗️ Section 5: Architecture — Component Data Flow

हीवी (Hevy) में कंपोनेंट और विजेट का डेटा फ्लो चार्ट:

```
[ Active Logger / Save Workout ] 
      │ (JS Layer)
      ▼
[ WorkoutStorageModule ] ──(Sync)──► [ SQLite: workouts.sqlite ]
      │
      │ (Triggers Update)
      ▼
[ WidgetModule ] ──(Serialize JSON)──► [ WidgetStore ]
      │
      │ (AppWidgetManager.notifyAppWidgetViewDataChanged)
      ▼
[ 10 Android Home-Screen Widgets ]
 ├── StreakWidget ──► (Streak length counter)
 ├── CalendarWidget ──► (Workout day frequency dots)
 ├── WeeklyStatsWidget ──► (This week vs Last week volumes)
 ├── ChartWidget ──► (Configure & draw exercise progress chart)
 └── RestWidget ◄──(Syncs with)──► [ TimerNotificationModule / Service ]
```

---

*Source: `G:\Tio-hub\design\references\Hevy\Hevy\app\src\main\` — July 2026*
