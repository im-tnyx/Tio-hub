# Ownership Rules

Use `apps/docs/PROFILE_SETTINGS_GUIDE.md` as the canonical ownership reference.

## Profile

Profile is not a business domain.

Profile is a Fitness Hub + Account Launcher.

Profile may show:

- User identity summary
- Current plan summary
- Journey card as launcher only
- Progress Photos card as launcher only
- Quick Actions
- Rewards shortcut
- Resources shortcut
- Settings shortcut

Profile must not own another feature's business logic.

## Progress

Progress owns:

- Journey
- Progress Photos
- Measurements
- Weight
- Achievements
- Progress Analytics

Profile only launches these screens.

## Nutrition

Nutrition owns:

- Nutrition Targets
- Calories
- Macros
- Water Goal
- Glass Size

Nutrition does not own Steps or Sleep.

## Workout

Workout owns:

- Workout Settings
- Rest Timer
- Plate Calculator
- Graph Settings
- RPE
- Previous Workout Values

## Health

Health is a future module.

Health owns:

- Health Connections
- Samsung Health
- Health Connect
- Garmin
- Fitbit
- Apple Health

Profile may provide a shortcut only.

## Recovery

Recovery is a future module.

Recovery owns:

- Sleep
- HRV
- Readiness
- Recovery Score

Sleep must not be placed inside Nutrition.

## Subscription

Subscription business logic belongs to Billing / Entitlement.

Settings is the primary UI entry:

- Settings -> Subscription

Profile may show a current plan shortcut.

Do not assign subscription ownership to `AuthViewModel` or generic global state.

## Personal Information

Personal Information belongs to the Profile domain.

Onboarding, Profile, and Settings must use the same repository as the single source of truth.

## Canonical References

- [Profile / Settings Guide](../../../apps/docs/PROFILE_SETTINGS_GUIDE.md)
- [Android App Progress](../../../apps/docs/ANDROID_APP_PROGRESS.md)
