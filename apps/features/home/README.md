# Home Feature

Owns the main dashboard UI for `:features:home`.

Home owns the main dashboard composition and does not own Workout, Nutrition,
Progress, Coach, Health, or other domain business logic.

The module is wired in `settings.gradle.kts` and consumed by `:app`.
Detailed actions remain inside their owning feature modules.

Feature-owned visual assets are separated by purpose:

- `src/main/res-icons/drawable`: vector and icon drawables.
- `src/main/res-images/drawable`: raster images and illustrations.

Both resource roots are exposed through the Home module's generated `R` class.
