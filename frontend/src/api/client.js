import axios from 'axios'

const client = axios.create({
  baseURL: 'http://finsentry-ai.com:8080',
  headers: { 'Content-Type': 'application/json' },
})

// ---------------------------------------------------------------------
// API CONTRACT — what the backend needs to return for each endpoint.
// Keep this in sync with whatever InvestigationController ends up
// returning; the pages below assume these exact shapes.
// ---------------------------------------------------------------------
//
// GET /api/investigations  →  InvestigationSummary[]
//   {
//     "caseId": 11,
//     "transactionId": 1237396,
//     "status": "COMPLETED",        // PENDING | IN_PROGRESS | COMPLETED | FAILED
//     "riskLevel": "MEDIUM",        // null until status is COMPLETED
//     "riskScore": 40,              // null until status is COMPLETED
//     "recommendation": "REVIEW",   // null until status is COMPLETED
//     "createdAt": "2026-08-30T02:00:00"
//   }
//
// GET /api/investigations/{caseId}  →  InvestigationDetail
//   {
//     "caseId": 11,
//     "transactionId": 1237396,
//     "status": "COMPLETED",
//     "riskLevel": "MEDIUM",
//     "riskScore": 40,
//     "summary": "...",
//     "findings": ["...", "..."],
//     "policyMatches": ["POLICY-DEVICE-002"],
//     "recommendation": "REVIEW",
//     "riskIndicators": {                 // OPTIONAL — enables the indicator
//       "newDevice": true,                // chips row on the case detail page.
//       "unusualCountry": true,           // If omitted, that row is hidden
//       "amountAnomalyMultiplier": 0,     // rather than shown broken.
//       "balanceDrained": false,
//       "rapidTransactions": false
//     }
//   }
//
// POST /api/investigations  { transactionId }  →  same shape as
//   GET /api/investigations/{caseId} (the just-created case).
// ---------------------------------------------------------------------

export async function investigateTransaction(transactionId) {
  const { data } = await client.post('/api/investigations', {
    transactionId: Number(transactionId),
  })
  return data
}

export async function getInvestigations() {
  const { data } = await client.get('/api/investigations')
  return data
}

export async function getInvestigation(caseId) {
  const { data } = await client.get(`/api/investigations/${caseId}`)
  return data
}

// GET /api/config — { demoMode: boolean }. Used to disable the Investigate
// form proactively on a public read-only deployment, rather than letting
// visitors submit and hit a 403.
export async function getConfig() {
  const { data } = await client.get('/api/config')
  return data
}

// POST /api/copilot/ask — { question } → CopilotResponse
//   { answer, responseType: "TEXT"|"TABLE"|"REPORT", table, report }
// See CopilotDrawer.jsx for how each responseType renders.
export async function askCopilot(question, caseId) {
  const { data } = await client.post('/api/copilot/ask', { question, caseId: caseId ?? null })
  return data
}

export default client
