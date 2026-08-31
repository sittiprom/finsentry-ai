# Investigator Copilot backend

Powers the chat drawer opened from the avatar in the top-right header. One
new endpoint, reusing your existing tools + RAG rather than building
anything new from scratch.

## Endpoint

```
POST /api/copilot/ask   { "question": "..." }
```

## Response contract

```json
{
  "answer": "Here are the 4 most recent transactions for C132693135.",
  "responseType": "TABLE",
  "table": {
    "columns": ["Transaction ID", "Type", "Amount", "Step"],
    "rows": [
      ["3854275", "CASH_OUT", "273171.01", "517"],
      ["4786795", "TRANSFER", "9960382.40", "203"]
    ]
  },
  "report": null
}
```

`responseType` is one of `TEXT`, `TABLE`, `REPORT`. Exactly one of
`table`/`report` should be non-null depending on which — the frontend
switches on `responseType` to decide how to render, so keep the unused
one `null` rather than omitting it.

**`REPORT`** (single-entity lookup, e.g. "who is customer X"):
```json
{
  "answer": "Customer profile for C132693135.",
  "responseType": "REPORT",
  "table": null,
  "report": {
    "title": "Customer C132693135",
    "fields": [
      { "label": "Home country", "value": "DE" },
      { "label": "Risk level", "value": "LOW" },
      { "label": "Account created", "value": "2022-04-11" }
    ]
  }
}
```

**`TEXT`** (policy/general questions — no structured data to show):
```json
{
  "answer": "Per POLICY-TRANSACTION-001, transfers exceeding $200,000 combined with a login from a previously unseen device within 10 minutes must be escalated for manual review.",
  "responseType": "TEXT",
  "table": null,
  "report": null
}
```

## Suggested implementation shape

This is the same structured-output pattern as `InvestigationAgent` — a
`ChatClient.prompt()` call with your existing tools attached, forced into
a `CopilotResponse` record via `.entity(CopilotResponse.class)`:

```java
public record CopilotResponse(
    String answer,
    ResponseType responseType,
    TableData table,
    ReportData report
) {
    public enum ResponseType { TEXT, TABLE, REPORT }
    public record TableData(List<String> columns, List<List<String>> rows) {}
    public record ReportData(String title, List<Field> fields) {
        public record Field(String label, String value) {}
    }
}
```

```java
@PostMapping("/api/copilot/ask")
public CopilotResponse ask(@RequestBody CopilotRequest request) {
    return chatClient.prompt()
        .system("""
            Answer using the available tools and retrieved policy context.
            Choose responseType TABLE when the answer is a list of records
            (e.g. transactions), REPORT when it's a single entity's profile
            (e.g. a customer), and TEXT for anything else, especially
            policy questions. Never invent data not returned by a tool.
            """)
        .user(request.question())
        .tools(transactionTools, customerTools, loginTools, riskTools)
        .call()
        .entity(CopilotResponse.class);
}
```

## Worth applying the same guardrails you already built elsewhere

- **Demo mode**: decide whether copilot calls should also be blocked when
  `APP_DEMO_MODE=true` — they cost the same per-call as an investigation.
  If you want it available read-only, consider restricting it to
  `TEXT`/policy-only questions in demo mode, since those don't reveal any
  customer-specific data.
- **`isFraud` boundary**: the copilot uses the same tools as
  `InvestigationAgent`, so it already can't see `isFraud`/`isFlaggedFraud`
  — no extra work needed there, just worth confirming when you test it.
- **No `ReportValidator` equivalent yet** — unlike investigations, nothing
  currently double-checks copilot answers against policy IDs for
  hallucination. Worth a lightweight version if you have time: reject or
  flag any policy ID cited in the answer text that isn't in your known set.
