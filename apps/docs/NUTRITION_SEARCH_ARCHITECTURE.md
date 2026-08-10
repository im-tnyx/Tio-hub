# TNYX Android: Multi-Tier Nutrition Search & Logging Architecture

Document Status: Partial runtime implementation; remaining tiers planned
Last Verified: 2026-08-09
Owner: Android Engineering / Product

---

## 🏛️ Multi-Tier Provider Hierarchy

The TNYX Nutrition platform employs a resilient, 4-tier food search, barcode scanning, and natural language logging strategy to ensure optimal coverage for Indian and global dietary items:

```text
┌─────────────────────────────────────────────────────────────────┐
│                    User Input / Trigger                         │
└─────────────────────────────────────────────────────────────────┘
        │                        │                        │
        ▼                        ▼                        ▼
[ Text / Category ]      [ Voice / Text ]       [ Barcode Scan ]
        │                        │                        │
        ▼                        ▼                        ▼
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│  TIER 1 (Primary)│    │  TIER 2 (Smart)  │    │  TIER 3 (Barcode)│
│  FatSecret India │    │  Edamam NLP      │    │ Open Food Facts  │
└──────────────────┘    └──────────────────┘    └──────────────────┘
        │                                                 │
        ▼ (fallback if item not found)                    ▼ (fallback)
┌──────────────────┐                            ┌──────────────────┐
│  TIER 4 (Raw DB) │                            │  TIER 4 (Raw DB) │
│  USDA FoodData   │                            │  USDA FoodData   │
└──────────────────┘                            └──────────────────┘
```

---

## 📋 Tier Breakdown & Specification

### Tier 1: Primary Search → **FatSecret India API**
- **Role:** Primary provider for Indian packaged goods, restaurant dishes, and regional items (Roti, Dal, Paneer, Biryani, Dosa, Idli, etc.).
- **Protocol:** OAuth 2.0 Client Credentials → REST API (`foods.search` / `food.get.v2`).
- **Use Case:** Direct search bar queries and category browse in `MealSearchScreen`.

### Tier 2: Smart Text & Voice Logging → **Edamam Natural Language Processing (NLP) API**
- **Role:** Parses conversational / freeform text and voice inputs (e.g. *"2 rotis with 1 bowl dal tadka and 100g curd for lunch"*).
- **Protocol:** REST API (`/api/nutrition-details` / `/api/food-database/v2/parser`).
- **Use Case:** Mic / Voice sub-action and natural text quick-log prompt.

### Tier 3: Barcode Fallback → **Open Food Facts API**
- **Role:** Instant lookup of packaged foods by scanning EAN / UPC barcodes via camera.
- **Protocol:** Open REST API (`https://world.openfoodfacts.org/api/v2/product/{barcode}.json`).
- **Use Case:** Camera sub-action barcode scanner.

### Tier 4: Raw Food & Ingredient Fallback → **USDA FoodData Central API**
- **Role:** Fallback for raw ingredients, whole fruits, vegetables, and basic single-item foods when primary search has no match.
- **Protocol:** REST API (`https://api.nal.usda.gov/fdc/v1/foods/search`).
- **Use Case:** Search fallback when Tier 1 returns zero results.

---

## 🛠️ Clean Architecture Implementation Boundaries

```text
apps/features/nutrition/
├── domain/
│   ├── repository/
│   │   └── FoodSearchRepository.kt      # Domain contract for multi-tier search
│   └── models/
│       ├── FoodSearchResult.kt          # Normalized food item domain model
│       └── ParsedMealTextResult.kt      # Smart NLP parsed meal model
└── data/ (app level wiring)
    ├── api/
    │   ├── FatSecretApiService.kt        # Tier 1 Client
    │   ├── EdamamNlpApiService.kt        # Tier 2 Client
    │   ├── OpenFoodFactsApiService.kt   # Tier 3 Client
    │   └── UsdaFoodDataApiService.kt    # Tier 4 Client
    └── repository/
        └── MultiTierFoodSearchRepositoryImpl.kt  # Fallback & orchestration logic
```

---

## 🔒 Security & Key Management
- Provider API keys, OAuth client secrets, and app secrets MUST remain in a
  trusted backend or Supabase Edge Function. They must never be shipped through
  Android `BuildConfig`, resources, encrypted local properties, or client source.
- Android will call a Tio-owned authenticated search gateway. The gateway owns
  provider selection, rate limiting, response normalization, caching, and
  credential rotation.
- The current runtime implements manual meal logging, text search, same-screen
  ML Kit barcode capture, and an authenticated `nutrition-food-search` Supabase
  Edge Function.
- Android debounces Search Meals query changes before calling
  `FoodSearchRepository`; cancellation prevents stale requests from replacing
  newer results.
- The live first slice normalizes Open Food Facts Search-a-licious results into
  `MealItem` drafts. It requests India-tagged products first and fills sparse
  regional results from a deduplicated global fallback. Checked-in function
  source also normalizes exact Open Food Facts product-by-code responses, then
  tries FatSecret Barcode v2 with `region=IN`, and finally can fall back to
  Edamam's UPC parser when server-side Edamam credentials are configured. None
  of these exact branches is present in active remote version 13. FatSecret
  barcode access and storable-data terms plus Edamam attribution and
  plan-specific caching rights must be verified before release. FatSecret text
  search, voice, USDA fallback, caching infrastructure, and complete
  multi-provider orchestration remain planned.
- Provider credentials are not present in the repository or Android client.
