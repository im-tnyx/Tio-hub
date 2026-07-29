# Workout Feature

`features:workout` owns Android Workout presentation, feature orchestration,
typed navigation, and feature-specific resources.

## Workflow Structure

```text
presentation/
├── home/
├── library/
│   ├── exercises/
│   ├── favorites/
│   ├── folders/
│   ├── filters/
│   └── createexercise/
├── routines/
│   ├── builder/
│   └── templates/
├── activesession/
├── history/
├── calendar/
├── settings/
└── shared/
```

These directories are ownership placeholders. Add `Route`, `Screen`,
`ViewModel`, `UiState`, and `Action` files only when that workflow is
implemented. Existing presentation files remain in place until they are moved
as part of a tested vertical slice.

## Boundaries

- `apps/shared` owns reusable Workout contracts, reducer logic, and repository
  interfaces shared by Phone and Wear.
- `apps/app` owns Android composition, Room entities, DAO, database, and
  repository implementations.
- `apps/wear` owns Wear-specific runtime behavior.
- `features:workout` must not expose Room, network, or mutable business logic
  directly from Compose screens.
- `WorkoutNavGraph` owns internal Workout destinations. The app shell only
  selects the top-level Workout or Library destination.

## Resources

- `src/main/res-icons/drawable` owns Workout vector icons.
- `src/main/res-images/drawable` owns approved Workout images.
- `src/main/res_chips` contains the existing body-part chip resources.
- `src/main/res` contains standard Android resources and compatibility aliases.

Do not ship reference-derived assets without provenance and licence clearance.
