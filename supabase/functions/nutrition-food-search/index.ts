import "jsr:@supabase/functions-js/edge-runtime.d.ts";

type Nutriments = Record<string, number | string | null | undefined>;

type OpenFoodFactsProduct = {
  code?: string;
  product_name?: string;
  brands?: string[] | string;
  nutriments?: Nutriments;
};

type OpenFoodFactsProductResponse = {
  status?: number;
  product?: OpenFoodFactsProduct;
};

type EdamamFood = {
  foodId?: string;
  label?: string;
  brand?: string;
  nutrients?: Record<string, number | null | undefined>;
};

type EdamamFoodMatch = {
  food?: EdamamFood;
};

type EdamamParserResponse = {
  parsed?: EdamamFoodMatch[];
  hints?: EdamamFoodMatch[];
};

type EdamamBarcodeResult = {
  configured: boolean;
  item: ReturnType<typeof normalizeEdamamFood>;
};

type FatSecretServing = {
  serving_id?: string;
  serving_description?: string;
  is_default?: string | number;
  calories?: string | number;
  carbohydrate?: string | number;
  protein?: string | number;
  fat?: string | number;
  saturated_fat?: string | number;
  trans_fat?: string | number;
  fiber?: string | number;
  sugar?: string | number;
};

type FatSecretFood = {
  food_id?: string;
  food_name?: string;
  brand_name?: string;
  servings?: {
    serving?: FatSecretServing | FatSecretServing[];
  };
};

type FatSecretBarcodeResponse = {
  food?: FatSecretFood;
  error?: {
    code?: string | number;
  };
};

type FatSecretBarcodeResult = {
  configured: boolean;
  item: ReturnType<typeof normalizeFatSecretFood>;
};

const JSON_HEADERS = { "Content-Type": "application/json" };
const SEARCH_ENDPOINT = "https://search.openfoodfacts.org/search";
const PRODUCT_ENDPOINT = "https://world.openfoodfacts.org/api/v2/product";
const FATSECRET_TOKEN_ENDPOINT = "https://oauth.fatsecret.com/connect/token";
const FATSECRET_BARCODE_ENDPOINT =
  "https://platform.fatsecret.com/rest/food/barcode/find-by-id/v2";
const EDAMAM_PARSER_ENDPOINT = "https://api.edamam.com/api/food-database/v2/parser";
const USER_AGENT = "Tio-hub/1.0 (https://github.com/im-tnyx/Tio-hub)";
const SEARCH_RESULT_LIMIT = 15;
const INDIA_COUNTRY_FILTER = 'countries_tags:"en:india"';

Deno.serve(async (request: Request) => {
  if (request.method !== "POST") {
    return json({ error: "Method not allowed." }, 405);
  }

  if (!authenticatedSubject(request)) {
    return json({ error: "Authentication required." }, 401);
  }

  let query = "";
  let barcode = "";
  try {
    const body = await request.json();
    query = typeof body?.query === "string" ? body.query.trim() : "";
    barcode = typeof body?.barcode === "string" ? body.barcode.trim() : "";
  } catch {
    return json({ error: "Invalid JSON body." }, 400);
  }

  if (barcode && !/^\d{8,14}$/.test(barcode)) {
    return json({ error: "Barcode must contain 8 to 14 digits." }, 400);
  }

  if (!barcode && (query.length < 2 || query.length > 80)) {
    return json({ error: "Query must contain 2 to 80 characters." }, 400);
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 15_000);

  try {
    if (barcode) {
      let openFoodFactsFailed = false;
      let product: OpenFoodFactsProduct | null = null;
      try {
        product = await getProductByBarcode(barcode, controller.signal);
      } catch {
        openFoodFactsFailed = true;
      }
      const openFoodFactsItem = product ? normalizeProduct(product) : null;
      if (openFoodFactsItem) {
        return json({
          items: [openFoodFactsItem],
          source: "open_food_facts",
          region: "GLOBAL",
          lookup: "barcode",
        });
      }

      const fatSecretResult = await getFatSecretProductByBarcode(
        barcode,
        controller.signal,
      );
      if (fatSecretResult.item) {
        return json({
          items: [fatSecretResult.item],
          source: "fatsecret",
          region: "IN",
          lookup: "barcode",
        });
      }

      const edamamResult = await getEdamamProductByBarcode(barcode, controller.signal);
      if (edamamResult.item) {
        return json({
          items: [edamamResult.item],
          source: "edamam",
          region: "IN",
          lookup: "barcode",
        });
      }
      if (
        openFoodFactsFailed &&
        !fatSecretResult.configured &&
        !edamamResult.configured
      ) {
        throw new Error("No barcode provider is available.");
      }
      return json({
        items: [],
        source: "none",
        region: "IN",
        lookup: "barcode",
      });
    }

    const safeQuery = escapeLuceneQuery(query);
    const regionalProducts = await searchProducts(
      `${INDIA_COUNTRY_FILTER} ${safeQuery}`,
      controller.signal,
    );
    const globalProducts = regionalProducts.length < SEARCH_RESULT_LIMIT
      ? await searchProducts(safeQuery, controller.signal)
      : [];
    const products = uniqueProducts([...regionalProducts, ...globalProducts])
      .slice(0, SEARCH_RESULT_LIMIT);
    const items = products
      .map(normalizeProduct)
      .filter((item: ReturnType<typeof normalizeProduct>) => item !== null);

    return json({ items, source: "open_food_facts", region: "IN" });
  } catch (error) {
    const timedOut = error instanceof DOMException && error.name === "AbortError";
    return json(
      { error: timedOut ? "Food provider timed out." : "Food provider is unavailable." },
      timedOut ? 504 : 502,
    );
  } finally {
    clearTimeout(timeout);
  }
});

async function searchProducts(
  query: string,
  signal: AbortSignal,
): Promise<OpenFoodFactsProduct[]> {
  const response = await fetch(SEARCH_ENDPOINT, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "User-Agent": USER_AGENT,
    },
    body: JSON.stringify({
      q: query,
      page: 1,
      page_size: SEARCH_RESULT_LIMIT,
      langs: ["en"],
      fields: ["code", "product_name", "brands", "nutriments"],
    }),
    signal,
  });
  if (!response.ok) throw new Error("Food provider request failed.");
  const payload = await response.json();
  return Array.isArray(payload?.hits) ? payload.hits : [];
}

async function getProductByBarcode(
  barcode: string,
  signal: AbortSignal,
): Promise<OpenFoodFactsProduct | null> {
  const fields = "code,product_name,brands,nutriments";
  const response = await fetch(
    `${PRODUCT_ENDPOINT}/${encodeURIComponent(barcode)}.json?fields=${fields}`,
    {
      headers: { "User-Agent": USER_AGENT },
      signal,
    },
  );
  if (response.status === 404) return null;
  if (!response.ok) throw new Error("Food provider request failed.");

  const payload = await response.json() as OpenFoodFactsProductResponse;
  return payload.status === 1 && payload.product ? payload.product : null;
}

async function getFatSecretProductByBarcode(
  barcode: string,
  signal: AbortSignal,
): Promise<FatSecretBarcodeResult> {
  const clientId = Deno.env.get("FATSECRET_CLIENT_ID");
  const clientSecret = Deno.env.get("FATSECRET_CLIENT_SECRET");
  if (!clientId || !clientSecret) {
    return { configured: false, item: null };
  }

  const url = new URL(FATSECRET_BARCODE_ENDPOINT);
  url.searchParams.set("barcode", barcode.padStart(13, "0"));
  url.searchParams.set("region", "IN");
  url.searchParams.set("language", "en");
  url.searchParams.set("format", "json");

  try {
    let response: Response;
    try {
      const accessToken = await requestFatSecretAccessToken(clientId, clientSecret, signal);
      response = await fetch(url, {
        headers: {
          Accept: "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        signal,
      });
    } catch (error) {
      if (!(error instanceof FatSecretOAuth2Rejected)) throw error;
      response = await fetch(url, {
        headers: {
          Accept: "application/json",
          Authorization: await fatSecretOAuth1AuthorizationHeader(
            clientId,
            clientSecret,
            url,
          ),
        },
        signal,
      });
    }

    const payload = await response.json() as FatSecretBarcodeResponse;
    const providerCode = Number(payload.error?.code ?? 0);
    if (providerCode === 211) return { configured: true, item: null };
    if (!response.ok || payload.error) {
      // Missing barcode scope or a provider outage should not block Edamam fallback.
      return { configured: true, item: null };
    }
    return {
      configured: true,
      item: payload.food ? normalizeFatSecretFood(payload.food) : null,
    };
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") throw error;
    return { configured: true, item: null };
  }
}

class FatSecretOAuth2Rejected extends Error {}

async function requestFatSecretAccessToken(
  clientId: string,
  clientSecret: string,
  signal: AbortSignal,
): Promise<string> {
  const response = await fetch(FATSECRET_TOKEN_ENDPOINT, {
    method: "POST",
    headers: {
      Authorization: `Basic ${btoa(`${clientId}:${clientSecret}`)}`,
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: new URLSearchParams({
      grant_type: "client_credentials",
      scope: "barcode",
    }),
    signal,
  });
  if (!response.ok) {
    await response.body?.cancel();
    throw new FatSecretOAuth2Rejected();
  }
  const payload = await response.json();
  if (typeof payload?.access_token !== "string") {
    throw new FatSecretOAuth2Rejected();
  }
  return payload.access_token;
}

async function fatSecretOAuth1AuthorizationHeader(
  consumerKey: string,
  consumerSecret: string,
  url: URL,
): Promise<string> {
  const oauthParameters: Record<string, string> = {
    oauth_consumer_key: consumerKey,
    oauth_nonce: crypto.randomUUID().replaceAll("-", ""),
    oauth_signature_method: "HMAC-SHA1",
    oauth_timestamp: Math.floor(Date.now() / 1000).toString(),
    oauth_version: "1.0",
  };
  const signatureParameters = [
    ...Object.entries(oauthParameters),
    ...url.searchParams.entries(),
  ]
    .map(([key, value]) => [percentEncode(key), percentEncode(value)] as const)
    .sort(([leftKey, leftValue], [rightKey, rightValue]) =>
      leftKey.localeCompare(rightKey) || leftValue.localeCompare(rightValue)
    )
    .map(([key, value]) => `${key}=${value}`)
    .join("&");
  const signatureBase = [
    "GET",
    percentEncode(`${url.origin}${url.pathname}`),
    percentEncode(signatureParameters),
  ].join("&");
  const signingKey = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(`${percentEncode(consumerSecret)}&`),
    { name: "HMAC", hash: "SHA-1" },
    false,
    ["sign"],
  );
  const signatureBytes = await crypto.subtle.sign(
    "HMAC",
    signingKey,
    new TextEncoder().encode(signatureBase),
  );
  const signature = btoa(
    Array.from(new Uint8Array(signatureBytes), (byte) => String.fromCharCode(byte)).join(""),
  );
  return `OAuth ${Object.entries({ ...oauthParameters, oauth_signature: signature })
    .sort(([left], [right]) => left.localeCompare(right))
    .map(([key, value]) => `${percentEncode(key)}="${percentEncode(value)}"`)
    .join(", ")}`;
}

function percentEncode(value: string): string {
  return encodeURIComponent(value).replace(
    /[!'()*]/g,
    (character) => `%${character.charCodeAt(0).toString(16).toUpperCase()}`,
  );
}

async function getEdamamProductByBarcode(
  barcode: string,
  signal: AbortSignal,
): Promise<EdamamBarcodeResult> {
  const appId = Deno.env.get("EDAMAM_APP_ID");
  const appKey = Deno.env.get("EDAMAM_APP_KEY");
  if (!appId || !appKey) {
    return { configured: false, item: null };
  }

  const url = new URL(EDAMAM_PARSER_ENDPOINT);
  url.searchParams.set("app_id", appId);
  url.searchParams.set("app_key", appKey);
  url.searchParams.set("upc", barcode);
  url.searchParams.set("nutrition-type", "logging");
  const response = await fetch(url, {
    headers: { Accept: "application/json" },
    signal,
  });
  if (response.status === 404) return { configured: true, item: null };
  if (!response.ok) throw new Error("Edamam barcode request failed.");

  const payload = await response.json() as EdamamParserResponse;
  const match = payload.parsed?.[0] ?? payload.hints?.[0];
  return {
    configured: true,
    item: match?.food ? normalizeEdamamFood(match.food) : null,
  };
}

function uniqueProducts(products: OpenFoodFactsProduct[]): OpenFoodFactsProduct[] {
  const seen = new Set<string>();
  return products.filter((product) => {
    const code = product.code?.trim();
    if (!code || seen.has(code)) return false;
    seen.add(code);
    return true;
  });
}

function escapeLuceneQuery(value: string): string {
  return value.replace(/([+\-=&|><!(){}\[\]^"~*?:\\/])/g, "\\$1");
}

function normalizeProduct(product: OpenFoodFactsProduct) {
  const code = product.code?.trim();
  const productName = product.product_name?.trim();
  if (!code || !productName) return null;

  const brand = Array.isArray(product.brands)
    ? product.brands[0]?.trim()
    : product.brands?.split(",")[0]?.trim();
  const nutrients = product.nutriments ?? {};
  return {
    id: crypto.randomUUID(),
    name: brand ? `${productName} - ${brand}` : productName,
    calories: Math.round(nonNegative(nutrients["energy-kcal_100g"])),
    protein: nonNegative(nutrients.proteins_100g),
    quantity: 1,
    unit: "100 g",
    carbs: nonNegative(nutrients.carbohydrates_100g),
    fats: nonNegative(nutrients.fat_100g),
    fiber: nonNegative(nutrients.fiber_100g),
    sugar: nonNegative(nutrients.sugars_100g),
    transFat: nonNegative(nutrients["trans-fat_100g"]),
    saturatedFat: nonNegative(nutrients["saturated-fat_100g"]),
  };
}

function normalizeEdamamFood(food: EdamamFood) {
  const foodId = food.foodId?.trim();
  const label = food.label?.trim();
  if (!foodId || !label) return null;

  const brand = food.brand?.trim();
  const nutrients = food.nutrients ?? {};
  return {
    id: crypto.randomUUID(),
    name: brand && !label.toLowerCase().includes(brand.toLowerCase())
      ? `${label} - ${brand}`
      : label,
    calories: Math.round(nonNegative(nutrients.ENERC_KCAL)),
    protein: nonNegative(nutrients.PROCNT),
    quantity: 1,
    unit: "100 g",
    carbs: nonNegative(nutrients.CHOCDF),
    fats: nonNegative(nutrients.FAT),
    fiber: nonNegative(nutrients.FIBTG),
    sugar: nonNegative(nutrients.SUGAR),
    transFat: nonNegative(nutrients.FATRN),
    saturatedFat: nonNegative(nutrients.FASAT),
  };
}

function normalizeFatSecretFood(food: FatSecretFood) {
  const foodId = food.food_id?.trim();
  const foodName = food.food_name?.trim();
  if (!foodId || !foodName) return null;

  const rawServings = food.servings?.serving;
  const servings = Array.isArray(rawServings)
    ? rawServings
    : rawServings
    ? [rawServings]
    : [];
  const serving = servings.find((candidate) => Number(candidate.is_default) === 1) ??
    servings[0];
  if (!serving?.serving_id) return null;

  const brand = food.brand_name?.trim();
  return {
    id: crypto.randomUUID(),
    name: brand && !foodName.toLowerCase().includes(brand.toLowerCase())
      ? `${foodName} - ${brand}`
      : foodName,
    calories: Math.round(nonNegative(serving.calories)),
    protein: nonNegative(serving.protein),
    quantity: 1,
    unit: serving.serving_description?.trim() || "serving",
    carbs: nonNegative(serving.carbohydrate),
    fats: nonNegative(serving.fat),
    fiber: nonNegative(serving.fiber),
    sugar: nonNegative(serving.sugar),
    transFat: nonNegative(serving.trans_fat),
    saturatedFat: nonNegative(serving.saturated_fat),
  };
}

function nonNegative(value: number | string | null | undefined): number {
  const parsed = typeof value === "number" ? value : Number(value);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0;
}

function authenticatedSubject(request: Request): string | null {
  const authorization = request.headers.get("Authorization");
  const token = authorization?.startsWith("Bearer ")
    ? authorization.slice("Bearer ".length)
    : null;
  if (!token) return null;

  try {
    const encodedPayload = token.split(".")[1];
    if (!encodedPayload) return null;
    const normalized = encodedPayload.replaceAll("-", "+").replaceAll("_", "/");
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
    const payload = JSON.parse(atob(padded));
    return payload?.role === "authenticated" &&
        payload?.is_anonymous !== true &&
        typeof payload?.sub === "string"
      ? payload.sub
      : null;
  } catch {
    return null;
  }
}

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: JSON_HEADERS,
  });
}
