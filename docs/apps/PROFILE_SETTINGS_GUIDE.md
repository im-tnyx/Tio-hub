# 👤 Profile & Settings Architectural Guide

This document defines the redesign of the Profile and Settings features into a modular, launcher-based architecture.

## 1. Vision: The "Fitness Hub"
The Profile feature is no longer a simple settings page. It acts as a **User Hub** and **Feature Launcher**. It provides a high-level summary of the user's state and quick shortcuts to domain-specific configurations.

### Core Principles
- **Dumb Profile**: The Profile feature should not own business logic for Nutrition, Workout, or Health. It only displays summaries and provides navigation.
- **Feature Ownership**: Each domain (Nutrition, Workout, Health, Progress) owns its own settings and targets.
- **Modular Settings**: Global app configurations are centralized in a dedicated `settings` feature.

---

## 2. Profile Structure (The Launcher)

The Profile screen follows this hierarchy:

| Component | Responsibility |
| :--- | :--- |
| **Profile Card** | Avatar, Name, Current Subscription Plan, and BMI summary. |
| **Activity Hub** | **Statistics** (Current Resources UI layout/shape) |
| | **Calendar** (Current Rewards UI layout/shape) |
| **Journey Card** | Visual representation of the active goal progress (e.g., Weight Gain/Loss). |
| **Progress Photos** | Visual shortcut to the latest progress photos. |
| **Quick Actions** | **Nutrition Targets** → (Shortcut to Nutrition Settings) |
| | **Workout Settings** → (Shortcut to Workout Settings) |
| | **Graph Settings** → (Shortcut to Workout Graph Config) |
| | **Wear OS Settings** → (Shortcut to Watch App Configuration) |
| **Secondary Links** | **Resources** |
| | **Rewards** |
| **Health Connections** | **Manage Connections** Card (Single large card at the bottom). |
| **Settings (⚙)** | Link to Global App Configuration (In Header). |

---

## 3. Settings Hierarchy (The App Config)

The `lib/src/features/settings` feature handles non-domain app configurations:

### A. Personal Information
- User Identity: Name, Birthday, Gender.
- Body Metrics: Height, Weight.
- Profile Goals: Primary Goal, Activity Level.

### B. App Preferences
- **Theme**: Light, Dark, System.
- **Language**: Localization settings.
- **Units**: Metric/Imperial.
- **Sound**: Effects, Volume.
- **Experience**: Glass Animation, Dynamic Calories (UI toggle).

### C. Notifications
- **Status**: Global toggle.
- **Schedule**: Wake Time, Sleep Time.
- **Frequency**: Reminder intervals for Nutrition, Workout, Hydration, Recovery, and Routine.

### D. Subscription & Billing
- **Plan**: Current status (Free/Premium).
- **Management**: Upgrade, Restore Purchase, Billing History.

### E. Help & About
- **Support**: FAQ, Contact, Feedback, Bug Reporting.
- **Legal**: Version, Privacy Policy, Terms of Service, Open Source Licenses.

---

## 4. Domain Settings Ownership

To keep the architecture clean, domain-specific settings are kept within their respective features:

### 🍎 Nutrition (`features/nutrition`)
- **Settings**: Nutrition Targets (Calories/Macros), Water Goal, Glass Size, Meals, Recipes.

### 🏋 Workout (`features/workout`)
- **Settings**: Workout Preferences, **Graph Settings**, Rest Timer, RPE settings, Plate Calculator.

### ❤️ Health (`features/health`)
- **Connections**: Samsung Health, Google Health Connect, Apple Health, Garmin, Fitbit, etc.

---

## 5. Implementation Roadmap (Reference Only)

1.  **Documentation First**: (Completed) This guide serves as the source of truth.
2.  **Navigation Update**: Define routes in `AppRouter` according to the hierarchy above.
3.  **Shell Creation**: Create `features/settings` and `features/health` folder structures.
4.  **Feature Migration**: Move existing targets/logic from Profile into their respective feature ViewModels.
5.  **UI Redesign**: Apply the "Fitness Hub" layout to `ProfileScreen`.

---

> [!IMPORTANT]
> **Constraint**: Do not modify existing business logic or UI until the documentation is approved and the migration plan is finalized. Use this guide as the target state for all future PRs related to Profile or Settings.
