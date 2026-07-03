# 🏋️ Lyfta App — Complete Reusable Components & Widgets Reference

> **Source:** `G:\Tio-hub\design\references\lyfta\Lyfta-1` (Decompiled APK — Native Android, Kotlin + Java + XML Views)
> **Framework:** Native Android — XML Views + ViewBinding + RecyclerView Adapters (React Native नहीं)

---

## 🏠 Section 1: Android Home-Screen Widgets (4 Widgets)

Lyfta के Home-screen Widgets `LyftaWidget.java (extends AppWidgetProvider)` class से बने हैं।
User `AddWidgetFragment` → Settings से इन्हें Home Screen पर pin कर सकता है।

---

### Widget 1: Streak Widget
**Layout File:** `lyfta_widget_layout.xml`

**क्या है:**
Lyfta का सबसे simple widget। User कितने consecutive हफ्तों से workout कर रहा है — यह बड़े number के रूप में दिखाता है।

**Visual Structure:**
- Top-left: "Streak" label text (12sp, white)
- Top-right: Lyfta logo (38×14dp, white tint)
- Bottom-left: Streak number (16sp bold, white) + "weeks" label (12sp, white) — vertical stack
- Bottom-right: Orange flame icon `ic_streak_empty` (28dp, `#ff9600` tint)
- Background: Black rounded card (`shape_22dp`)

**कहाँ Use होता है:** Android Home Screen (user manually add करता है)

**क्यों Use होता है:**
User को App खोले बिना motivational streak reminder देने के लिए। Habit formation का core gamification element।

**कैसे काम करता है:**
`LyftaWidget.onUpdate()` → `setTextViewText(R.id.textViewContent, streakValue)` → Widget refresh। `widgetLayout` container पर click → App open।

---

### Widget 2: Weekly Snapshot Widget
**Layout File:** `lyfta_widget_weekly_snapshot.xml`

**क्या है:**
इस हफ्ते की workout summary — Workouts count, Total Duration, Total Volume — एक बड़े widget में। नीचे "Start Workout" button भी।

**Visual Structure:**
- Top-left: Lyfta logo (38×14dp)
- Center: 3 vertical columns (horizontal LinearLayout):
  - Column 1: `tv_workouts_count` (24dp bold) + "Workouts" label + progress pill (`iv_workouts_progress_icon` + `tv_workouts_progress`)
  - Column 2: `tv_duration_total` (24dp bold, "0h") + "Duration" label + progress pill
  - Column 3: `tv_volume_this_week` (24dp bold, "0kg") + "Volume" label + progress pill
- Progress pill: Black rounded background + up/down arrow icon + % change text (12sp grey)
- Bottom: White "Start Workout" button (52dp height, full width), black text 16sp

**कहाँ Use होता है:** Android Home Screen

**क्यों Use होता है:**
Weekly progress at-a-glance। User बिना app खोले जान सकता है कितना किया है इस हफ्ते। "Start Workout" CTA से directly workout शुरू होती है।

**कैसे काम करता है:**
`LyftaWidget.onUpdate()` → weekly DB query → `remoteViews.setTextViewText()` for each metric → `setOnClickPendingIntent(btn_start_workout)` → HomeActivity launch।

---

### Widget 3: Monthly Calendar Widget
**Layout File:** `lyfta_widget_month_calendar.xml`

**क्या है:**
पूरे महीने का calendar grid। जिन दिनों workout किया उन्हें visually highlight करता है।

**Visual Structure:**
- Top: `tvMonthYear` (bold 16sp, white) — current month + year
- Grid: `GridLayout` (7 columns × 7 rows) — पहली row: day headers (Mon-Sun, 11sp grey), बाकी rows: date boxes
- Left section: calendar grid
- Right section: streak stats (month के साथ)
- Background: Black rounded card

**कहाँ Use होता है:** Android Home Screen (medium/large size widget)

**क्यों Use होता है:**
Consistency tracking। User physically देखता है कितने दिन active रहा — "don't break the chain" motivation।

**कैसे काम करता है:**
`LyftaWidget.onUpdate()` → workout dates fetch → हर date के लिए GridLayout cell highlight/unhighlight → `setViewVisibility` या background tint से।

---

### Widget 4: Calendar Only Widget
**Layout File:** `lyfta_widget_month_calendar_only.xml`

**क्या है:**
Widget 3 का simplified, compact version। सिर्फ calendar grid — बिना side stats के। छोटी widget size के लिए।

**Visual Structure:** Widget 3 जैसा लेकिन सिर्फ calendar grid portion, कोई extra stats column नहीं।

**कहाँ Use होता है:** Android Home Screen (small/compact widget size)

**क्यों Use होता है:**
कम screen space में भी monthly consistency track करने के लिए।

**कैसे काम करता है:** Widget 3 जैसा ही — same `LyftaWidget` class, different layout।

---

## 🧩 Section 2: In-App Reusable Components

---

### Component 1: WorkoutExpandItem (Exercise Logger Row)
**Layout File:** `workout_expand_item.xml` (17,243 bytes — सबसे complex component)

**क्या है:**
Active Workout और History में एक exercise का complete interactive card। इसमें exercise header, notes field, rest timer, sets table header, और sets RecyclerView सब एक साथ हैं।

**Visual Structure (top to bottom):**
1. **Exercise Header (`cl_exercise_name`):**
   - `fl_image_container` (43×64dp CardView, corner 6dp): Exercise thumbnail image (`img_exercise`) या initials fallback (`tv_initials`, colorPrimary background, 16sp)
   - `superset_indicator`: 4dp vertical colored bar (left edge) — superset group का visual connector
   - `tv_exercise_name`: Exercise name text (16sp Medium, 2 lines max)
   - `exercise_option_menu`: 3-dot menu icon (48dp, right side) — rename/delete/swap options
   - `img_question`: Small question mark icon (bottom of image) — exercise info link
2. **Notes Section:**
   - `fl_pinned_notes`: Pinned note field (border rounded 12dp) — `et_pinned_notes` (EditText, multiline) + `img_pinned` (pin icon, right side)
   - `et_exercise_note`: Regular exercise note (EditText, multiline, 14sp)
3. **Rest Timer Row (`ll_rest_timer`):**
   - `img_rest_timer`: Timer icon (20dp, colorPrimary)
   - `tv_rest_timer_value`: Timer value text (default: "Off", 14sp colorPrimary) — tap करने पर timer picker खुलता है
4. **Sets Table Header (`linear_header`, 48dp height):**
   - `tv_set_title`: "Set" column (38dp width, autoSize 1-14sp)
   - `tv_previous_title`: "Previous" column (weight 1.4) — last session का value ghosted
   - `ll_weight_title`: Weight column (weight 1.1) — dumbbell icon + kg/lbs label
   - `tv_reps_title`: Reps column (weight 1.1)
5. **Sets RecyclerView (`rv_workout_sets`):** `SetsInfoRow` components की list
6. **"Add Set" Button (`ml_add_set`):** MaterialButton, full width, 42dp height

**कहाँ Use होता है:**
- Active Workout Logger (live workout के दौरान, editable mode)
- Workout History Detail Screen (readonly mode)
- Routine Editor (template बनाते वक्त)

**क्यों Use होता है:**
एक exercise के सारे logging elements — thumbnail, name, notes, rest timer, sets — एक self-contained, scrollable unit में। RecyclerView में repeat होता है (एक workout में multiple exercises)।

**कैसे काम करता है:**
`RecyclerView` adapter → `workout_expand_item` inflate → exercise data bind (`tv_exercise_name`, `img_exercise`) → `rv_workout_sets` nested RecyclerView → `SetsInfoRow` items bind। "Add Set" button click → new row append to sets list।

---

### Component 2: SetsInfoRow (Individual Set Row)
**Layout File:** `sets_info_variant_b.xml` (7,290 bytes)

**क्या है:**
एक exercise के अंदर एक single set की data row। Weight और Reps दिखाता/edit करता है। PR (Personal Record) होने पर medal badges भी।

**Visual Structure:**
- `tv_set_no`: Set number (17sp bold, 32dp wide, left) — "1", "2", "3"...
- `tv_weight`: Weight value (16sp) — "80 kg" या "176 lbs"
- **PR Badges (HorizontalScrollView में, initially GONE):**
  - `layout_1rm_record` + `rm_medal` (18dp icon) + "1RM" text: 1 Rep Max record
  - `layout_weight_record` + `weight_medal` (18dp icon) + "Weight" text: Max weight record
  - `layout_volume_record` + `volume_medal` (18dp icon) + "Volume" text: Max volume record
  - `layout_reps_record` + `reps_medal` (18dp icon) + "Reps" text: Max reps record
- Min height: 52dp

**कहाँ Use होता है:**
- `WorkoutExpandItem` के अंदर `rv_workout_sets` में (nested RecyclerView)
- Workout History/Detail Screen (readonly)
- Workout Summary Screen

**क्यों Use होता है:**
हर set independently track होता है। User weight और reps edit करता है। PR break होने पर automatic badge दिखता है।

**कैसे काम करता है:**
Set data bind → weight/reps display → app backend PR check करता है → अगर नया record है तो `layout_1rm_record.visibility = VISIBLE` → medal icon show।

---

### Component 3: WorkoutTemplateItem (Routine Card)
**Layout File:** `workout_template_item_layout.xml` (6,252 bytes)

**क्या है:**
एक saved workout routine/template का list item। Name, exercise count, last performed time, और start/options buttons दिखाता है।

**Visual Structure:**
- `rl_image_container` (56×56dp FrameLayout): Routine cover image (`img_workout`) या `tv_workout_initials` (text fallback, 20sp)
- `tv_workout_name`: Routine name (16sp Medium, 2 lines max)
- `tv_exercise_count`: Exercise count (14sp, supportText color) — "5 Exercises"
- `ll_performed_ago`: Clock icon (14dp) + `tvPerformedAgo` text (14sp) — "Last performed 3 days ago"
- `img_perform_workout` (48dp): Circle border + play icon (`ic_play_filled`, 18dp) — directly workout start
- `img_options` (42dp): 3-dot vertical menu — Edit/Duplicate/Delete/Share

**कहाँ Use होता है:**
- My Routines/Templates list screen
- `item_workout_unified.xml` में `include` होकर (Home Feed "Today's Workout" card में)
- Explore Templates tab

**क्यों Use होता है:**
User की saved routines को consistently एक तरह से दिखाने के लिए — हर जगह same card layout। Play button से instant workout start।

**कैसे काम करता है:**
Adapter → routine data bind → play icon click → `HomeActivity.startWorkout(routineId)` → Active Workout screen। 3-dot menu → BottomSheet options।

---

### Component 4: WorkoutUnifiedItem (Home Feed Workout Card)
**Layout File:** `item_workout_unified.xml` (18,317 bytes)

**क्या है:**
Home Feed का main "actionable" card। दो parts हैं — "Today's Workout" card (scheduled routine) और "Mission" card (AI/program-based goal)।

**Visual Structure:**
1. **Today's Workout CardView (`cl_todays_workout`, initially GONE):**
   - "Today's Workout" title (20sp bold)
   - `included_workout`: `workout_template_item_layout` included — routine का preview
   - `btn_start`: MaterialButton "Start" (48dp, Lyfta filled primary style)
2. **Mission Card (`mission_card_container`, dark green `#20663c`):**
   - Mission title + description
   - Complete/Start action

**कहाँ Use होता है:** Home Feed Screen — feed के top में pinned

**क्यों Use होता है:**
User को daily workout reminder और one-tap start। Scheduled routine automatically populate होती है।

**कैसे काम करता है:**
ViewModel → today's scheduled routine check → `cl_todays_workout.visibility = VISIBLE` → `WorkoutTemplateItem` data bind → "Start" → workout session।

---

### Component 5: MuscleMapItem (Body Muscle Diagram)
**Layout File:** `item_muscle_map.xml` → includes `body_detailed.xml`

**क्या है:**
Human body का 2D front+back diagram जो primary और secondary muscles को अलग-अलग colors से highlight करता है।

**Visual Structure:**
- `muscle_detailed` ID (300×320dp): `body_detailed` layout include
- `body_detailed` में front/back body silhouette + individual muscle overlay ImageViews
- Primary muscles: एक color (typically red/orange)
- Secondary muscles: different color (typically blue/grey)

**कहाँ Use होता है:**
- Exercise Detail Screen (About tab) — exercise किस muscle को target करती है
- Workout Summary Screen — पूरे workout में किन muscles पर काम हुआ
- Home Feed Workout Card — carousel के अंदर muscle visualization
- Analysis Screen (muscle recovery section)

**क्यों Use होता है:**
Visual learners के लिए text से बेहतर। User instantly समझता है कौन सी exercise कहाँ काम करती है।

**कैसे काम करता है:**
Exercise data → primary/secondary muscle IDs list → हर muscle के लिए corresponding ImageView को `visibility = VISIBLE` + color tint apply → body diagram पर overlay।

---

### Component 6: WorkoutExpandItemBig (Full Exercise Detail View)
**Layout File:** `workout_expand_item_big.xml` (15,324 bytes)

**क्या है:**
`WorkoutExpandItem` का larger/detailed version। Workout history में किसी specific exercise को fully expand करके देखने के लिए।

**कहाँ Use होता है:** Workout History Detail Screen (past workout review)

**क्यों Use होता है:**
Active logging में compact view चाहिए, लेकिन history review में full detail (सभी sets का complete data) दिखाना होता है।

**कैसे काम करता है:** Same structure जैसा `WorkoutExpandItem` लेकिन read-only mode, ज्यादा spacing, और full stats visible।

---

### Component 7: SupersetItem (Superset Exercise Selector)
**Layout File:** `superset_item_layout.xml` (3,762 bytes, fixed height 62dp)

**क्या है:**
Superset create करते समय exercises choose करने का list item। Superset में दो या ज्यादा exercises को एक group में link करते हैं।

**Visual Structure:**
- `superset_indicator` (4dp vertical bar, left edge): Superset group का visual indicator — groups linked exercises
- `fl_image_container` (38×38dp CardView): Exercise thumbnail या initials
- `tv_initials`: Text fallback (16sp, colorPrimary background)
- `selected_superset_indicator`: Check icon (24dp, right side) — selected state में visible
- `img_exercise`: Exercise image

**कहाँ Use होता है:**
- `SupersetOptionsAdapter` — superset create करने का bottom sheet में
- Routine Editor (superset grouping)

**क्यों Use होता है:**
User multiple exercises को एक superset में group कर सकता है — rest कम, efficiency ज्यादा। Visual indicator से clear होता है कौन से exercises linked हैं।

**कैसे काम करता है:**
BottomSheet open → `SupersetOptionsAdapter` → `superset_item_layout` inflate → user selects exercises → `selected_superset_indicator` show → confirm → exercises link।

---

### Component 8: StrengthStandardItem (Strength Level Indicator)
**Layout File:** `strenght_standard_item.xml` (4,515 bytes)

**क्या है:**
एक exercise में user की strength level — Beginner/Novice/Intermediate/Advanced/Elite — और उसकी weight range star rating के साथ।

**Visual Structure:**
- `tv_level`: Level name text (16sp Medium, white) — e.g., "Intermediate"
- Star icons (`ic_star_one` to `ic_star_five`, each 20dp, `ic_star_filled`): Level के अनुसार filled stars (gold color `colorRatingStars`)
- Weight range: Level पर कितना weight lift करना चाहिए

**कहाँ Use होता है:**
- Exercise Detail Screen (Strength Standards tab)
- Leaderboard Screen

**क्यों Use होता है:**
User को goal clarity देता है — "मुझे इस exercise में कहाँ होना चाहिए?" Gamification + goal setting।

**कैसे काम करता है:**
User's best lift fetch → strength standard table lookup → level assign → corresponding stars highlight → weight range display।

---

### Component 9: UserItemLayout (User List Row)
**Layout File:** `users_item_layout.xml` (7,594 bytes, min height 74dp)

**क्या है:**
किसी user को list में दिखाने का standard row — profile photo, name, bio, और follow button।

**Visual Structure:**
- `img_profile` (58×58dp): User profile picture (circular)
- `tv_username` (14sp Medium): Username + `img_premium` (14dp crown icon — premium badge)
- `tv_bio` (12sp, 80% opacity): User bio text
- `rl_follow_container`: Follow/Unfollow button

**कहाँ Use होता है:**
- User Search Screen (users ढूंढना)
- Followers/Following List
- Social Feed (post commenters/likers)

**क्यों Use होता है:**
Consistent user representation। हर जगह same format → user identity instantly recognizable।

**कैसे काम करता है:**
`FollowFollowingAdapter` या search → `users_item_layout` inflate → profile data bind → follow button click → API call → button state toggle।

---

### Component 10: FeedSnapshotItem (Weekly Summary Feed Card)
**Layout File:** `item_feed_snapshot.xml` (9,947 bytes)

**क्या है:**
Home Feed में बीच-बीच में automatically insert होने वाला "Your Weekly Snapshot" card। इस हफ्ते का Duration, Volume, Workouts count दिखाता है।

**Visual Structure:**
- Header: "Your Weekly Snapshot" (16sp Bold) + "See More" link (right, primary color, 12sp)
- Stats:
  - `tv_duration_total` (big number) + `tv_duration` label
  - Volume total + label
  - Workouts count + label
- Background: subtle card

**कहाँ Use होता है:** Home Feed Screen — feed list में weekly milestone पर inject होता है

**क्यों Use होता है:**
Feed scroll करते हुए weekly summary से user engage रहता है। "See More" → Analysis Screen।

**कैसे काम करता है:**
Feed adapter → weekly milestone detect (every 7 days) → `item_feed_snapshot` inject → stats populate।

---

### Component 11: FriendStreakItem (Friend Streak Row)
**Layout File:** `item_friend_streak.xml` (2,508 bytes)

**क्या है:**
Friend की current workout streak दिखाने वाला list item। Social motivation के लिए।

**Visual Structure:**
- `img_profile` (48×48dp): Friend's profile photo (circular)
- `tvName` (16sp Medium): Friend का display name
- Streak number + flame indicator

**कहाँ Use होता है:** Profile Screen (Friends' Streaks section) — `FriendStreakAdapter.java`

**क्यों Use होता है:**
Social competition। User देखता है कि friends कितना active हैं → motivation।

**कैसे काम करता है:**
Friends list API → `FriendStreakAdapter` → `item_friend_streak` inflate → each friend's streak value bind।

---

### Component 12: ViewTemplateItemExplore (Explore Template Card)
**Layout File:** `view_template_item_explore.xml` (4,670 bytes)

**क्या है:**
Explore Screen में workout templates को grid या list में दिखाने का card। `WorkoutTemplateItem` का explore-specific variant।

**Visual Structure:**
- `fl_image_container` (42×62dp CardView, corner 4dp): Slightly smaller — denser grid के लिए
- Exercise thumbnail + border overlay (`shape_border_6dp`, 12% alpha)
- Workout name, exercise count, estimated time

**कहाँ Use होता है:** Explore Screen → Templates/Collections tab

**क्यों Use होता है:**
Explore में routines browse करने के लिए compact card — grid layout में ज्यादा templates एक screen पर।

**कैसे काम करता है:**
Templates list fetch → `view_template_item_explore` inflate → tap → template detail screen।

---

### Component 13: OverviewSectionGraph (Progress Graph Section)
**Layout File:** `overview_section_graph.xml` (8,464 bytes)

**क्या है:**
Analysis Screen का main progress graph — Volume या Duration का trend line/bar chart, time range selector के साथ।

**Visual Structure:**
- `frameLayout5`: Tab selector (pill shape, `tab_indicator` background):
  - `tab_layout` (32dp height): 3 tabs — "3 Months" / "6 Months" / "Year to Date"
- `tv_graph_value` (24sp Roboto Bold): Current period total value — e.g., "12h 30m"
- `tv_graph_value_label`: Unit label next to value
- `tv_workout_date_range` (16sp, supportText): Date range text — "Jan 2024 – Mar 2024"
- Chart view (actual graph library component)

**कहाँ Use होता है:** Analysis/Overview Screen — top section

**क्यों Use होता है:**
Long-term progress visualization। User देख सकता है क्या improve हुआ।

**कैसे काम करता है:**
Tab select → date range update → DB query for period → graph data populate → chart redraw। Value TextView update करता है aggregate।

---

### Component 14: OverviewSectionStreakCalendar (Streak + Calendar Section)
**Layout File:** `overview_section_streak_calendar.xml` (12,095 bytes)

**क्या है:**
Analysis Screen में streak stats + monthly calendar combined section।

**Visual Structure:**
- `tv_month` (24sp Bold): Current month name
- `streak_stats_container` (horizontal): Two bordered boxes:
  - `ll_week_streak` (62dp height, border): Flame icon + `tv_week_streak` (20sp Bold) + `tv_week_streak_label`
  - `ll_streak_workouts` (62dp height, border): Total workouts in streak + label
- Monthly calendar grid (workout day highlights)

**कहाँ Use होता है:** Analysis/Overview Screen

**क्यों Use होता है:**
Streak motivation + calendar visualization एक section में।

**कैसे काम करता है:**
Current streak calculate → `tv_week_streak` bind → calendar month data load → workout days highlight।

---

### Component 15: WeighPlateView / PlateCalculator
**Layout File:** `weigh_plate_view.xml` + Class: `PlateCalculatorSettingsFragment.java` (33,109 bytes)

**क्या है:**
Barbell पर कौन-कौन से weight plates लगाने हैं यह visually calculate करने वाला utility widget।

**Visual Structure:**
- Barbell diagram
- Weight input field
- Plate breakdown: 25kg × 2, 10kg × 1 etc.
- `PlateAndBarsAdapter` — different plate types की list

**कहाँ Use होता है:**
- Settings → Plate Calculator
- Active Workout Logger (quick access)

**क्यों Use होता है:**
Gym में जल्दी plates calculate करना — mental math से बचाव।

**कैसे काम करता है:**
User inputs target weight → algorithm calculates optimal plate combination → `PlateAndBarsAdapter` → plate items display।

---

### Component 16: PerformedWorkoutItem (History Workout Card)
**Layout File:** `performed_workout_item_layout_variant_b.xml` (4,866 bytes)

**क्या है:**
Workout History में एक completed workout का summary card।

**Visual Structure:**
- Date + time
- Workout name
- Total duration, volume, exercises
- Muscle map thumbnail (small)

**कहाँ Use होता है:** Workout History Screen (list of past workouts)

**क्यों Use होता है:**
Past workouts को chronologically browse करने के लिए।

---

### Component 17: WorkoutNowItem (Active Feed Item)
**Layout File:** `workout_now_item_layout.xml` (4,207 bytes)

**क्या है:**
Home Feed में दिखने वाला special "Currently working out" या "Quick Start" card।

**कहाँ Use होता है:** Home Feed (active workout के दौरान या quick start के लिए)

**क्यों Use होता है:**
Social visibility — friends देख सकते हैं कि user workout कर रहा है। Quick re-join का option।

---

## 📱 Section 3: Shimmer / Loading Skeleton Components

ये components data load होने तक placeholder animation दिखाते हैं — UX smoothness के लिए।

| Component | Layout File | कहाँ Use | क्यों |
| :--- | :--- | :--- | :--- |
| `ShimmerFeedItem` | `shimmer_explore_workout_layout.xml` | Feed Screen (loading) | Feed data आने से पहले blank screen की बजाय skeleton animation |
| `ShimmerProfileItem` | `shimmer_fragment_profile.xml` | Profile Screen (loading) | Profile data fetch के दौरान |
| `ShimmerUserItem` | `shimmer_users_item_layout.xml` | Search Screen (loading) | User search results आने से पहले |
| `ShimmerLogWorkoutAI` | `shimmer_log_workout_ai.xml` | Log Workout (AI generating) | AI workout generation के दौरान |
| `ShimmerExploreCollection` | `shimmer_explore_collection_layout.xml` | Explore Screen (loading) | Collections load होने से पहले |
| `ShimmerNotificationItem` | `shimmer_notification_items_layout.xml` | Notifications (loading) | Notifications fetch के दौरान |

**कैसे काम करते हैं:**
Shimmer library → layout inflate → shimmer animation start → data आने पर → real layout swap → animation stop।

---

## 📊 Section 4: Analytics Deep-Dive Sections

### OverviewSectionVolumeCalendar
**Layout:** `overview_section_volume_calendar.xml` (8,453 bytes)
**क्या:** Volume को calendar heatmap format में — हर दिन का volume intensity से color।
**कहाँ:** Analysis Screen
**क्यों:** GitHub-style contribution graph — consistency + intensity दोनों एक view में।

### OverviewSectionRadarChart
**Layout:** `overview_section_radar_chart.xml` (4,153 bytes)
**क्या:** Muscle group balance को spider/radar chart में — Chest/Back/Legs/Shoulders/Arms का relative % training।
**कहाँ:** Analysis Screen
**क्यों:** User देख सकता है कोई muscle group neglect तो नहीं हो रहा।

### OverviewSectionRecords
**Layout:** `overview_section_records.xml` (3,367 bytes)
**क्या:** Exercise-wise Personal Records की chronological list।
**कहाँ:** Analysis Screen
**क्यों:** Achievement tracking — PR milestones।

### OverviewSectionWeekly
**Layout:** `overview_section_weekly.xml` (4,332 bytes)
**क्या:** इस हफ्ते के aggregate stats — sets, volume, duration।
**कहाँ:** Analysis Screen
**क्यों:** Short-term weekly view (graph section long-term के लिए है)।

---

## 🏗️ Section 5: Architecture — Component Flow

```
Home Feed Screen
├── WorkoutUnifiedItem (Today's Workout card)
│   └── WorkoutTemplateItem (included — routine preview)
├── FeedSnapshotItem (Weekly Snapshot)
└── WorkoutNowItem (Active workout indicator)

Active Workout Screen
├── WorkoutExpandItem (per exercise — RecyclerView)
│   ├── Exercise header (image/initials, name, 3-dot)
│   ├── Notes (pinned + regular)
│   ├── Rest Timer row
│   ├── Sets Table header (Set/Previous/Weight/Reps)
│   └── SetsInfoRow × N (nested RecyclerView)
│       └── PR medals (1RM/Weight/Volume/Reps)
└── SupersetItem (superset creation BottomSheet)

Exercise Detail Screen
├── MuscleMapItem (body_detailed)
├── StrengthStandardItem (level + stars)
└── StrengthRankingSheet (parameters BottomSheet)

Analysis/Overview Screen
├── OverviewSectionGraph (progress chart + tabs)
├── OverviewSectionStreakCalendar (streak + month)
├── OverviewSectionVolumeCalendar (heatmap)
├── OverviewSectionRadarChart (muscle balance)
├── OverviewSectionRecords (PR list)
└── OverviewSectionWeekly (week summary)

Home Screen Widgets (Android OS level)
├── Streak Widget — lyfta_widget_layout.xml
├── Weekly Snapshot Widget — lyfta_widget_weekly_snapshot.xml
├── Monthly Calendar Widget — lyfta_widget_month_calendar.xml
└── Calendar Only Widget — lyfta_widget_month_calendar_only.xml
```

---

*Source: `G:\Tio-hub\design\references\lyfta\Lyfta-1\app\src\main\` — July 2026*
