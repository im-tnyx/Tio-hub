# TNYX Android: Multi-Tier Nutrition Search & Logging Architecture

Document Status: Canonical Nutrition API Architecture Reference
Last Verified: 2026-07-31
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
- API Keys, OAuth Secret Tokens, and App IDs MUST be supplied via environment variables (`buildConfigField` or encrypted local properties) and NEVER hardcoded in client source files.
