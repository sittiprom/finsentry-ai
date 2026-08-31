# Read-only demo mode

Lets you deploy publicly on AWS without exposing your OpenAI key to unlimited
use by strangers. Dashboard, Cases, and Case Detail stay fully browsable —
only *new* investigations (which cost real money per LLM call) are blocked.

## 1. Add the files

| File | Action |
|---|---|
| `ConfigController.java` | New file → `controller/` |
| `ADD_TO_InvestigationController.txt` | **Not a file to copy directly** — open your existing `InvestigationController.java` and manually add the field + the `if (demoMode)` check at the top of your `POST /api/investigations` method, per the instructions inside. |

## 2. Local development — leave it off

Don't set anything. `app.demo-mode` defaults to `false` via the `:false` in
`@Value("${app.demo-mode:false}")`, so nothing changes locally.

## 3. On AWS — set ONE environment variable

```
APP_DEMO_MODE=true
```

Spring Boot automatically maps this to the `app.demo-mode` property (relaxed
binding: `APP_DEMO_MODE` → `app.demo-mode`) — no extra config file needed.

Where to set it depends on what you're deploying to:
- **Elastic Beanstalk**: Environment → Configuration → Software → Environment properties
- **ECS/Fargate**: Task definition → container → environment variables
- **App Runner**: Service configuration → environment variables
- **EC2**: your systemd service file or startup script

Your actual `OPENAI_API_KEY` should be set the same way — as an environment
variable in AWS's config, never committed to the repo.

## 4. What happens when demo mode is on

- `GET /api/investigations`, `GET /api/investigations/{caseId}` — work normally,
  fully public, no cost (pure DB reads, no LLM calls).
- `POST /api/investigations` — returns `403 Forbidden` with a clear message
  instead of running the agent. The frontend (already updated) checks
  `GET /api/config` on load and disables the Investigate form proactively,
  so visitors see an explanation rather than a confusing error.

## 5. Before deploying at all

Set a hard monthly spending cap on your OpenAI account regardless of demo
mode — it's your safety net if anything's ever misconfigured.
