# FinSentry AI — Frontend

React + Vite + MUI console for the FinSentry AI investigation backend.
Sidebar layout, four pages: Dashboard, Investigate, Cases, Case Detail.

## 1. Backend: add CORS config (required)

The Vite dev server runs on `localhost:5173`; your Spring Boot backend runs on `localhost:8080`.
Without CORS configured, the browser blocks every request. Copy `backend-addition/WebConfig.java`
into your backend at:
```
src/main/java/com/finsentry/finsentry_ai/config/WebConfig.java
```
Restart the backend after adding it.

## 2. Install and run the frontend

```bash
npm install
npm run dev
```
Opens at `http://localhost:5173`.

## 3. API contract — what your backend needs to return

This is the important part. The pages below assume these exact JSON shapes.
The same contract is documented in `src/api/client.js`.

### `GET /api/investigations` → array, used by Dashboard + Cases

```json
[
  {
    "caseId": 11,
    "transactionId": 1237396,
    "status": "COMPLETED",
    "riskLevel": "MEDIUM",
    "riskScore": 40,
    "recommendation": "REVIEW",
    "createdAt": "2026-08-30T02:00:00"
  }
]
```
`riskLevel` / `riskScore` / `recommendation` should be `null` for cases that
haven't finished (`PENDING` / `IN_PROGRESS` / `FAILED`) — Dashboard and Cases
both handle that gracefully.

### `GET /api/investigations/{caseId}` → single object, used by Case Detail

```json
{
  "caseId": 11,
  "transactionId": 1237396,
  "status": "COMPLETED",
  "riskLevel": "MEDIUM",
  "riskScore": 40,
  "summary": "...",
  "findings": ["...", "..."],
  "policyMatches": ["POLICY-DEVICE-002"],
  "recommendation": "REVIEW",
  "riskIndicators": {
    "newDevice": true,
    "unusualCountry": true,
    "amountAnomalyMultiplier": 0,
    "balanceDrained": false,
    "rapidTransactions": false
  }
}
```
`riskIndicators` is **optional** — it's what powers the "Key risk indicators"
chip row on the case detail page. If you don't include it, that section just
doesn't render; nothing breaks. It maps directly to your existing
`RiskIndicators` record from `RiskTools` — easiest way to fill it in is to
also persist those fields on `InvestigationReportEntity`, or recompute them
from the case's transaction when this endpoint is called.

### `POST /api/investigations` `{ transactionId }` → same shape as the detail endpoint

The Investigate page calls this, then redirects straight to `/cases/{caseId}`
using the `caseId` from the response — so this endpoint's response needs to
include `caseId` at minimum for the redirect to work.

## Pages

- **Dashboard** (`/`) — stat cards (open / high risk / pending review / completed),
  a risk distribution bar, and a recent-cases table. All computed client-side
  from `GET /api/investigations` — no separate stats endpoint needed.
- **Investigate** (`/investigate`) — transaction ID input + a checklist of what
  gets checked. On success, redirects to the new case's detail page.
- **Cases** (`/cases`) — full case list with search (case/transaction ID) and
  risk/status filters, computed client-side.
- **Case Detail** (`/cases/:caseId`) — the full report: risk score + badge,
  summary, optional indicator chips, evidence ledger, policy matches, and the
  recommendation banner.

## Design notes

- Sidebar layout (not top tabs) — navy sidebar, off-white workspace, one teal
  accent. Space Grotesk headings, Inter body, IBM Plex Mono for all transaction
  data (IDs, amounts, scores).
- Risk score renders as a horizontal zone-banded meter, not a circular gauge.
- Findings render as a numbered evidence ledger (F01, F02, ...).
- The recommendation banner never uses the word "fraud" — states a next step
  (no action / review / escalate), matching the backend's constraint that the
  system never declares a customer fraudulent.
- Status badges (PENDING/IN_PROGRESS/COMPLETED/FAILED) use neutral/accent
  colors, deliberately separate from the risk-level color scale (LOW/MEDIUM/HIGH)
  so the two concepts never visually blur together.

## Extending

- `src/api/client.js` — all backend calls + the full API contract live here.
- `src/theme.js` — all color/type tokens (`tokens` export) — reuse these
  rather than hardcoding hex values in components.
