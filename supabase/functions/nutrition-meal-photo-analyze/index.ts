import "jsr:@supabase/functions-js/edge-runtime.d.ts";

type JsonRecord = Record<string, unknown>;

const JSON_HEADERS = { "Content-Type": "application/json" };
const TOKEN_ENDPOINT = "https://oauth.fatsecret.com/connect/token";
const RECOGNITION_V2_ENDPOINT = "https://platform.fatsecret.com/rest/image-recognition/v2";
const RECOGNITION_V1_ENDPOINT = "https://platform.fatsecret.com/rest/image-recognition/v1";
const MAX_IMAGE_BASE64_LENGTH = 999_982;

class OAuth2CredentialsRejected extends Error {}

Deno.serve(async (request: Request) => {
  if (request.method !== "POST") {
    return json({ error: "Method not allowed." }, 405);
  }
  if (!authenticatedSubject(request)) {
    return json({ error: "Authentication required." }, 401);
  }

  let imageBase64 = "";
  let mimeType = "";
  try {
    const body = await request.json();
    imageBase64 = typeof body?.imageBase64 === "string" ? body.imageBase64 : "";
    mimeType = typeof body?.mimeType === "string" ? body.mimeType : "";
  } catch {
    return json({ error: "Invalid JSON body." }, 400);
  }
  if (!imageBase64 || imageBase64.length > MAX_IMAGE_BASE64_LENGTH) {
    return json({ error: "Meal photo is missing or too large." }, 400);
  }
  if (mimeType !== "image/jpeg") {
    return json({ error: "Meal photo analysis requires JPEG input." }, 400);
  }

  const clientId = Deno.env.get("FATSECRET_CLIENT_ID");
  const clientSecret = Deno.env.get("FATSECRET_CLIENT_SECRET");
  if (!clientId || !clientSecret) {
    const missingSecrets = [
      !clientId ? "FATSECRET_CLIENT_ID" : null,
      !clientSecret ? "FATSECRET_CLIENT_SECRET" : null,
    ].filter(isPresent);
    return json({
      error: `Meal photo analysis is missing ${missingSecrets.join(" and ")}.`,
    }, 503);
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 20_000);
  try {
    let providerResponse = await requestRecognition(
      clientId,
      clientSecret,
      imageBase64,
      RECOGNITION_V2_ENDPOINT,
      controller.signal,
    );
    let providerCode = providerResponse.ok ? 0 : await readProviderErrorCode(providerResponse);
    if (providerCode === 211) {
      providerResponse = await requestRecognition(
        clientId,
        clientSecret,
        imageBase64,
        RECOGNITION_V1_ENDPOINT,
        controller.signal,
      );
      providerCode = providerResponse.ok ? 0 : await readProviderErrorCode(providerResponse);
    }
    if (!providerResponse.ok) {
      if (providerCode === 14 || providerCode === 23) {
        return json({ error: "FatSecret Image Recognition access is not enabled." }, 502);
      }
      if ([5, 8, 13].includes(providerCode)) {
        return json({ error: "FatSecret credentials were rejected." }, 502);
      }
      if (providerCode === 211) {
        return json({ error: "No food was detected in this photo." }, 422);
      }
      return json({ error: "Meal photo provider is unavailable." }, 502);
    }

    const payload = await providerResponse.json() as JsonRecord;
    const responses = asArray(payload.food_response);
    const items = responses.map(normalizeFoodResponse).filter(isPresent);
    if (items.length === 0) {
      return json({ error: "No food was detected in this photo." }, 422);
    }
    return json({
      suggestedName: items.map((item) => item.name).slice(0, 2).join(" and "),
      items,
      source: "fatsecret_image_recognition",
    });
  } catch (error) {
    const timedOut = error instanceof DOMException && error.name === "AbortError";
    return json(
      {
        error: timedOut
          ? "Meal photo analysis timed out."
          : "Meal photo analysis failed.",
      },
      timedOut ? 504 : 502,
    );
  } finally {
    clearTimeout(timeout);
  }
});

async function requestRecognition(
  clientId: string,
  clientSecret: string,
  imageBase64: string,
  endpoint: string,
  signal: AbortSignal,
): Promise<Response> {
  const body = JSON.stringify({
    image_b64: imageBase64,
    include_food_data: true,
    region: "IN",
    language: "en",
  });
  try {
    const accessToken = await requestAccessToken(clientId, clientSecret, signal);
    return await fetch(endpoint, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body,
      signal,
    });
  } catch (error) {
    if (!(error instanceof OAuth2CredentialsRejected)) throw error;
    return await fetch(endpoint, {
      method: "POST",
      headers: {
        Authorization: await oauth1AuthorizationHeader(clientId, clientSecret, endpoint),
        "Content-Type": "application/json",
      },
      body,
      signal,
    });
  }
}

async function requestAccessToken(
  clientId: string,
  clientSecret: string,
  signal: AbortSignal,
): Promise<string> {
  const credentials = btoa(`${clientId}:${clientSecret}`);
  const response = await fetch(TOKEN_ENDPOINT, {
    method: "POST",
    headers: {
      Authorization: `Basic ${credentials}`,
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: new URLSearchParams({
      grant_type: "client_credentials",
      scope: "image-recognition",
    }),
    signal,
  });
  if (!response.ok) {
    await response.body?.cancel();
    throw new OAuth2CredentialsRejected();
  }
  const payload = await response.json();
  if (typeof payload?.access_token !== "string") {
    throw new Error("FatSecret access token is missing.");
  }
  return payload.access_token;
}

async function oauth1AuthorizationHeader(
  consumerKey: string,
  consumerSecret: string,
  endpoint: string,
): Promise<string> {
  const oauthParameters: Record<string, string> = {
    oauth_consumer_key: consumerKey,
    oauth_nonce: crypto.randomUUID().replaceAll("-", ""),
    oauth_signature_method: "HMAC-SHA1",
    oauth_timestamp: Math.floor(Date.now() / 1000).toString(),
    oauth_version: "1.0",
  };
  const normalizedParameters = Object.entries(oauthParameters)
    .sort(([left], [right]) => left.localeCompare(right))
    .map(([key, value]) => `${percentEncode(key)}=${percentEncode(value)}`)
    .join("&");
  const signatureBase = [
    "POST",
    percentEncode(endpoint),
    percentEncode(normalizedParameters),
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

async function readProviderErrorCode(response: Response): Promise<number> {
  try {
    const payload = await response.json();
    const error = asRecord(payload?.error);
    const parsed = Number(error.code);
    return Number.isFinite(parsed) ? parsed : 0;
  } catch {
    return 0;
  }
}

function normalizeFoodResponse(value: JsonRecord) {
  const eaten = asRecord(value.eaten);
  const nutrients = asRecord(eaten.total_nutritional_content);
  const serving = asRecord(value.suggested_serving);
  const name = stringValue(value.food_entry_name);
  if (!name) return null;
  return {
    id: crypto.randomUUID(),
    name,
    calories: Math.round(nonNegative(nutrients.calories)),
    protein: nonNegative(nutrients.protein),
    quantity: 1,
    unit: stringValue(serving.serving_description) ||
      stringValue(eaten.singular_description) || "serving",
    carbs: nonNegative(nutrients.carbohydrate),
    fats: nonNegative(nutrients.fat),
    fiber: nonNegative(nutrients.fiber),
    sugar: nonNegative(nutrients.sugar),
    saturatedFat: nonNegative(nutrients.saturated_fat),
  };
}

function asArray(value: unknown): JsonRecord[] {
  if (Array.isArray(value)) return value.filter(isRecord);
  return isRecord(value) ? [value] : [];
}

function asRecord(value: unknown): JsonRecord {
  return isRecord(value) ? value : {};
}

function isRecord(value: unknown): value is JsonRecord {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isPresent<T>(value: T | null): value is T {
  return value !== null;
}

function stringValue(value: unknown): string {
  return typeof value === "string" ? value.trim() : "";
}

function nonNegative(value: unknown): number {
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
    return payload?.role === "authenticated" && payload?.is_anonymous !== true &&
        typeof payload?.sub === "string"
      ? payload.sub
      : null;
  } catch {
    return null;
  }
}

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), { status, headers: JSON_HEADERS });
}
