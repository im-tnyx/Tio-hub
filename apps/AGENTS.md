# TNYX AI Agent Protocol & Source of Truth

यह दस्तावेज़ TNYX प्रोजेक्ट पर काम करने वाले किसी भी AI Agent के लिए "Constitutional Rules" है। इन नियमों का पालन करना अनिवार्य है।

---

## 1. Core Architecture (Source: ARCHITECTURE.md)
- **Clean Architecture:** हमेशा `:shared` (domain) और `:app` (presentation/data) के बीच स्पष्ट अलगाव रखें।
- **Dumb UI Pattern:** Screens केवल `UiState` रेंडर करेंगी और `Action` एमिट करेंगी। ViewModel/Contract ही बिजनेस लॉजिक का मालिक है।
- **Folder Ownership:** `com.tnyx.core` (design system/shell) और `com.tnyx.features` (business features) के बीच डिपेंडेंसी के नियमों का कड़ाई से पालन करें।

## 2. Design System & Tokens
- **No Hardcoding:** किसी भी फाइल में raw DP, Color, या Alpha values (जैसे `0.65f`) का उपयोग न करें। हमेशा `TnyxTheme` का उपयोग करें।
- **Token Update Chain:** यदि नया Component Token जोड़ा जाता है, तो इन 3 फाइलों को एक साथ अपडेट करना अनिवार्य है:
    1. `TnyxComponentTokens.kt` (Data class)
    2. `LocalTnyxComponentTokens.kt` (CompositionLocal default)
    3. `TnyxThemeProvider.kt` (Actual value mapping)
- **Modifier Order:** Modifier का क्रम Project की Visual Integrity के लिए महत्वपूर्ण है। `padding` और `background/clip` के क्रम को यूजर के ओरिजिनल कोड के अनुसार ही रखें।

## 3. Navigation (Source: NAVIGATION_GUIDE.md)
- **Type-Safety:** केवल `@Serializable` रूट्स का उपयोग करें।
- **Detection Logic:** Navigation 2.8.5+ के लिए हमेशा `it.hasRoute(Class::class)` जैसे Explicit Class checks का उपयोग करें ताकि Nested Graphs में डिटेक्शन फेल न हो।
- **Ownership:** `MainScreen` केवल टैब स्विचिंग हैंडल करेगा; नेविगेशन की आंतरिक वायरिंग फीचर के अपने `NavGraph` में होगी।

## 4. User Logic Sovereignty (The "Masterpiece" Rule)
- **Non-Interference:** यूजर द्वारा बनाए गए जटिल लॉजिक (जैसे `TnyxShell` का Scroll-Hide, Animation, या Custom Drawing) को "Masterpiece" मानें।
- **Debug Protocol:** बग फिक्स करते समय यूजर के कस्टम फीचर्स को डिलीट या सिम्प्लीफाई न करें। समस्या के "Integration" को ठीक करें, न कि "Feature" को।

## 5. Interaction Protocol
- **Audit & Plan:** किसी भी फाइल को एडिट करने से पहले प्रोजेक्ट का Audit करें और यूजर को स्पष्ट Plan बताएं।
- **Language:** बातचीत हमेशा **हिन्दी (Hindi)** में होगी, लेकिन कोड, कमेंट्स और टेक्निकल टर्म्स **English** में रहेंगे।
- **Tone:** प्रोफेशनल, डायरेक्ट और मददगार (Like a Principal Architect).

---

*Last Updated: 2024-12-30*
*Maintainer: TNYX Lead Architect*
