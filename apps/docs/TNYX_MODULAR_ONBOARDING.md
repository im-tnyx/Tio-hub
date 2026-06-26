# TNYX Modular Onboarding: Full Runtime Specification

यह दस्तावेज़ TNYX के ऑनबोर्डिंग इंजन की पूरी गहराई को परिभाषित करता है। यह पुराने `ONBOARDING_FLOW_DETAILED.md` के हर लॉजिक गेट, कोडिंग बारीकियाँ (Technical Glue), और स्क्रीन मैपिंग को नए मॉड्यूलर आर्किटेक्चर में समाहित करता है।

---

## 🏗️ 1. The Core Engine Logic

ऑनबोर्डिंग कोई साधारण नेविगेशन नहीं है, बल्कि एक **Dynamic Flow Engine** है।

### A. Flow Decision Maker (`FlowEngine.kt`)
यह तय करता है कि यूजर को कौन सा रास्ता दिखाना है। इसके इनपुट्स हैं:
- `entryPath`: (GET_STARTED / SKIP / SIGN_IN)
- `gymAccess`: (HOME / COMMERCIAL) -> यह WORKOUT के स्टेप्स बदलता है।
- `authStatus`: (LOGGED_IN / GUEST) -> यह DATA के बाद AUTH गेट लगाता है।
- `mobileVerified`: (TRUE / FALSE) -> यह MOBILE सेक्शन को इन्सर्ट करता है।

---

## 🗺️ 2. Detailed Section & Step Mapping

यहाँ 30+ स्क्रीन्स का पूरा विवरण है जो `SectionRenderer` द्वारा संभाला जाएगा:

### **P1: INTRO SECTION** (Module: `:features:onboarding`)
*Total Steps: 5*
1. **Splash/Logo**: `SplashActivity` से `MainActivity` तक का हैंडऑफ।
2. **Welcome Hero**: Get Started / Sign In / Skip ऑप्शंस।
3. **Education Slide 1**: Nutrition Tracking value prop.
4. **Education Slide 2**: Workout Coaching value prop.
5. **Education Slide 3**: AI Insights value prop.

### **P2: DATA SECTION (Bio-Metrics)** (Module: `:features:onboarding`)
*Total Steps: 9*
1. **Name** | 2. **Gender** | 3. **Primary Goal** | 4. **DOB/Age** | 5. **Height** | 6. **Current Weight** | 7. **Target Weight** | 8. **Activity Level** | 9. **Health Conditions**.

### **P3: MOBILE SECTION (Identity)** (Module: `:features:auth`)
*Total Steps: 1*
1. **Mobile Verification**: OTP आधारित वेरिफिकेशन। `FlowEngine` इसे `DATA` के बाद या `SKIP` पाथ में शुरू में इन्सर्ट करता है।

### **P4: WORKOUT INTRO** (Module: `:features:workout`)
*Total Steps: 1*
1. **Workout Opt-in**: "Yes" -> P5 पर जाओ, "No" -> P6 (TARGETS) पर जंप करो।

### **P5: WORKOUT SECTION** (Module: `:features:workout`)
*Total Steps: 8 or 9 (Dynamic)*
1. **Gym Access** | 2. **Equipment** (सिर्फ 'Home' एक्सेस पर) | 3. **Experience** | 4. **Focus Areas** | 5. **Training Days** | 6. **Duration** | 7. **Workout Split** | 8. **Injuries** | 9. **Special Event**.

### **P6: TARGETS SECTION** (Module: `:features:nutrition`)
*Total Steps: 6*
1. **Bridge/Processing** | 2. **Steps Target** | 3. **Sleep Target** | 4. **Water Target** | 5. **Goal Pace** | 6. **Nutrition Summary**.

### **P7: SOURCE SECTION** (Module: `:features:onboarding`)
*Total Steps: 2*
1. **Referral Source** | 2. **Finalizing**: "Finish" बटन ट्रिगर।

---

## 🔘 3. Interactive Component Logic

### Next Button Logic (`NextStepUseCase`)
- **Step < MaxSteps**: अगले स्टेप पर जाओ।
- **Step == MaxSteps**: `OnboardingStateMachine` से अगला `Section` पूछो।
- **Auth Gate**: यदि `DATA` सेक्शन खत्म हुआ और यूजर `GUEST` है, तो `ResumeManager` में स्टेट सेव करो और `Route.AUTH_UI` पर भेजो।

### Back Button Logic (`PreviousStepUseCase`)
- **System Back**: `BackHandler` के जरिए कैप्चर किया जाएगा।
- **Logic**: पिछले स्टेप या पिछले सेक्शन के आखिरी स्टेप पर वापसी।

---

## 💾 4. Persistence & Migration (Persistence Layers)

ऑनबोर्डिंग में दो अलग-अलग स्टोरेज सिस्टम काम करते हैं:

1. **Route Progress (`ResumeManager.kt`)**:
   - **Storage**: `SharedPreferences` (`onboarding` नाम से)।
   - **Role**: यह याद रखता है कि यूजर किस सेक्शन और स्टेप पर था। `RootNavHost` ऐप स्टार्ट पर इसी से तय करता है कि यूजर को कहाँ भेजना है।
   
2. **Form Draft (`OnboardingStorage.kt`)**:
   - **Storage**: Android `DataStore`.
   - **Role**: यूजर द्वारा भरा गया एक्चुअल डेटा (Name, Weight, etc.) लोकल में सेव करता है ताकि लॉगिन से पहले डेटा सुरक्षित रहे।
   - **Migration**: ऑथेंटिकेशन सफल होने के बाद `migrateLocalDraft()` के जरिए यह डेटा बैकएंड पर सिंक किया जाता है।

---

## ⚙️ 5. Infrastructure & "The Glue" (Technical Infrastructure)

### A. Auth Success Resume Flow
सफल लॉगिन (Google/Phone/Email) के बाद `navigateAfterStatus()` कॉल होता है:
- यदि प्रोफाइल अपूर्ण है -> `Section.DATA`
- यदि टार्गेट्स अपूर्ण हैं -> `Section.TARGETS`
- यदि पूरी तरह कम्प्लीट है -> `Graph.MAIN`

### B. Language Management (`LanguageManager.kt`)
- `currentLanguage` के आधार पर `TranslationKeys` से लेबल उठाए जाते हैं।
- ऑनबोर्डिंग के दौरान भाषा बदलने पर `StepValidator` के बटन टेक्स्ट (`CONTINUE`, `NEXT`, `FINISH`) तुरंत अपडेट होते हैं।

### C. Analytics Tracking (`AnalyticsTracker.kt`)
- `ScreenView`, `NextClicked`, `BackClicked`, और `OnboardingCompleted` इवेंट्स Firebase पर ट्रैक किए जाते हैं।

### D. Device Identity (`DeviceHelper.kt`)
- बैकएंड कॉल के समय `deviceId` और `deviceFingerprint` भेजे जाते हैं ताकि फ्रॉड और डुप्लीकेट प्रोफाइल रोकी जा सकें।

---

## 🎨 6. Rendering & Personalization

### SectionRenderer Strategy
`:app` मॉड्यूल का सेंट्रलाइज्ड रेंडरर मॉड्यूलर स्क्रीन्स को होस्ट करता है।

### Gender-based Visual Personalization (Logic)
- यदि यूजर `DATA` में `Male` चुनता है, तो `WORKOUT` सेक्शन की इमेजेस (Focus Areas) पुरुष एथलीट की दिखेंगी।
- यदि `Female` चुनता है, तो महिला एथलीट की इमेजेस।
- **Assets Rule**: `photo_male_chest` vs `photo_female_chest` (Gender-aware image resolver)।

---

## 🚀 7. Finalization Logic (`SETTING_UP_PROCESSING`)

1. **Simulation**: फाइनल स्टेप के बाद `SettingUpScreen` पर 1 से 85% तक प्रोग्रेस सिम्युलेट होती है।
2. **Backend Finalize**: `POST api/v1/onboarding/finalize` कॉल होता है।
3. **Success**: 100% प्रोग्रेस -> `Route.CONGRATS` -> `Graph.MAIN`।
4. **Failure**: टोस्ट एरर और यूजर को ऑनबोर्डिंग के आखिरी वैलिड स्टेप पर वापस भेजना।

---
*Status: Full Technical & Modular Specification Documented*
*Maintainer: TNYX Lead Architect*
*Last Updated: June 25, 2026*
