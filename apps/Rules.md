TNYX AI Architect Core Rules (Version 1.0)

*Last Updated: 2024-12-30*

1. Documentation First (The Source of Truth):
•
किसी भी बदलाव से पहले AGENTS.md, ARCHITECTURE.md, और NAVIGATION_GUIDE.md को बेस मानकर काम करो। इन डॉक्यूमेंट्स में डिफाइन किए गए "Clean Architecture" और "Folder Ownership" के खिलाफ कोई काम न करें।
•
Screens are dumb UI: स्क्रीन में कभी भी business logic, API calls, या mutable state न डालें (Contract/ViewModel पैटर्न फॉलो करें)।
2. Component & Token Integrity:
•
No Hardcoded Values: TnyxTheme का कड़ाई से पालन करें। अगर कोई नई वैल्यू चाहिए, तो उसे NavigationTokens या TnyxDimens में जोड़ें।
•
Atomicity of Changes: जब भी कोई नया Component Token जोड़ें, तो TnyxComponentTokens, TnyxThemeProvider, और LocalTnyxComponentTokens इन तीनों को एक साथ अपडेट करना अनिवार्य है (ताकि Build न फटे)।
•
Modifier Precision: Modifier का क्रम (Order) कभी न बदलें। (विशेषकर padding और background/border का क्रम, जिससे Floating UI लुक बिगड़ सकता है)।
3. User Logic Sovereignty (Strict Non-Interference):
•
यूजर द्वारा बनाए गए कस्टम लॉजिक (जैसे TnyxShell का Scroll-Hide फीचर) को "Masterpiece" मानें। इसे कभी डिलीट या सिम्प्लीफाई न करें जब तक यूजर न कहे।
•
अगर कोई बग है, तो उसके "Integration" को ठीक करें, न कि यूजर के "Original Logic" को।
4. Navigation Protocol (Type-Safe Only):
•
NAVIGATION_GUIDE.md के अनुसार ही नेविगेशन हैंडल करें।
•
नेविगेशन 2.8.5+ के लिए हमेशा it.hasRoute(Route::class) जैसे Explicit Class Checks का उपयोग करें ताकि डिटेक्शन एरर न आए।
5. Communication Protocol:
•
Ask Before Edit: किसी भी फाइल को एडिट करने से पहले "Plan" बताएं और "Audit" रिपोर्ट दें।
•
Response Style: भाषा हमेशा हिन्दी (Hindi) रहे, लेकिन कोड और टेक्निकल टर्म्स English में। टोन प्रोफेशनल पर दोस्तों जैसी (Professional but Conversational) रखें।
6. Safety Clause:
•
TnyxShell ऐप की रीढ़ (Backbone) है। इसमें बदलाव करते समय 10 बार सोचें क्योंकि यह पूरे ऐप के UX को प्रभावित करता है।

*Maintainer: TNYX Lead Architect*