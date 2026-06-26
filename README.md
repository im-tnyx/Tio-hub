# Tio-hub

Tio - AI-powered health, fitness, and nutrition companion app.

## 📱 Project Structure

```
Tio-hub/
├── apps/           # Mobile and Watch applications
│   ├── app/        # Android mobile app
│   ├── wear/       # Wear OS watch app  
│   ├── core/       # Design system & UI components
│   ├── features/   # Feature modules (workout, nutrition, onboarding, auth)
│   └── shared/     # Pure Kotlin domain & repositories
└── .env            # Environment configuration (not committed)
```

---

## 📚 Documentation

Complete technical documentation is available in the `apps/docs/` directory:

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](apps/docs/ARCHITECTURE.md) | Core theme system, component library, feature folder patterns |
| [NAVIGATION_GUIDE.md](apps/docs/NAVIGATION_GUIDE.md) | Type-safe navigation, nested graphs, UI chrome policy |
| [ONBOARDING_FLOW_DETAILED.md](apps/docs/ONBOARDING_FLOW_DETAILED.md) | Complete onboarding runtime flow reference |
| [TNYX_MODULAR_ONBOARDING.md](apps/docs/TNYX_MODULAR_ONBOARDING.md) | Modular onboarding implementation guide |
| [SUPABASE_INCREMENTAL_SETUP_PLAN.md](apps/docs/SUPABASE_INCREMENTAL_SETUP_PLAN.md) | Step-by-step Supabase tables, RLS, seed, and app integration plan |
| [TESTING_GUIDE.md](apps/docs/TESTING_GUIDE.md) | Testing strategy and guidelines |
| [WEAR_OS_PLAN.md](apps/docs/WEAR_OS_PLAN.md) | Wear OS feature plan — Workout + Nutrition watch app |
| [WEAR_OS_PROGRESS.md](apps/docs/WEAR_OS_PROGRESS.md) | Wear OS development progress tracking |

---

## 🏗️ Architecture

- **Clean Architecture** with `:shared` (pure Kotlin) and `:app` modules
- **MVI Pattern**: Route → ViewModel → Contract (UiState/Action/Effect) → Screen
- **Type-safe Navigation**: Using `@Serializable` routes (Navigation 2.8.5)
- **Design System**: TnyxTheme with comprehensive token system
- **Modular Structure**: Feature modules for scalability

---

## 🚀 Getting Started

1. Open project in Android Studio
2. Sync Gradle dependencies
3. Configure `.env` file (copy from `.env.example` if available)
4. Build and run on device/emulator

---

## 📖 Learn More

For detailed architecture, navigation patterns, and implementation guides, refer to the documentation links above.

---

**Last Updated:** 2026-06-26
