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

### Mandatory Core UI Reuse Gate

Before creating or materially changing any Compose screen:

1. Inspect `core/theme/TnyxTheme.kt`, `core/theme/tokens/`, and `core/ui/components/`.
2. Record which existing theme tokens and components the screen will reuse.
3. Use `TnyxTheme.colors`, `dimens`, `insets`, `elevation`, `typography`, `textStyles`, `motion`, `shapes`, `gradients`, `shadows`, and `components` instead of feature-local visual constants when an applicable token exists.
4. Prefer existing components such as `TnyxPrimaryButton`, `TnyxSecondaryButton`, `TnyxGhostButton`, `TnyxCard`, `TnyxTextField`, `TnyxScreenHeader`, `TnyxDynamicHeader`, `TnyxTabSwitcher`, and `TnyxModalBottomSheet` before creating an equivalent composable.
5. Do not copy or fork a core component into a feature merely to customize it. Extend a generic core API when the behavior remains feature-agnostic; otherwise wrap the core component in a feature-owned widget.
6. Keep a widget feature-local when it contains domain language, feature state, or feature-specific behavior. Promote it to `core` only after real cross-feature reuse is demonstrated.
7. If a new component token is required, update `TnyxComponentTokens`, `LocalTnyxComponentTokens`, and `TnyxThemeProvider` together and validate every affected caller.

A screen review is incomplete until it confirms that no existing core component or token was unnecessarily duplicated.

### Reference UI Adaptation Gate

When a task includes a reference file, screenshot, image, mockup, external app, decompiled screen, or URL:

1. Use the reference only to understand the requested content, information hierarchy, states, flow, and interaction intent.
2. Treat the checked-in Tio/TNYX design system as the only visual authority. Implement the result with `TnyxTheme` and existing `Tnyx*` components even when the reference looks different.
3. Do not pixel-copy or import the reference's colors, typography, spacing, shapes, shadows, icons, illustrations, branding, animations, or component styling.
4. Map each relevant reference element to an existing project token or component before implementation. If no equivalent exists, follow the core-versus-feature ownership rules instead of creating a feature-local visual imitation.
5. Do not add a reference image or asset to runtime source unless the user explicitly requests that exact asset and its provenance, licence, and product ownership are cleared.
6. When reference styling conflicts with the current project, preserve the current project UI and report the intentional difference.

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

*Last Updated: 2026-08-02*
*Maintainer: TNYX Lead Architect*
