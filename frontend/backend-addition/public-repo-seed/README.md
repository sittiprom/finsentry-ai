# Public-repo seed data (no CSV needed)

Replaces the CSV-based `DataInitializer` with one that generates a small,
self-contained dataset directly in Java on startup — so anyone who clones
your repo can run it immediately with just Postgres, no 400MB file to
download or ship in Git.

## What changes

| File | Action | Destination |
|---|---|---|
| `DataInitializer.java` | **Replace** your existing CSV-based version | `init/` |
| `SyntheticDataInitializer.java` | **New file** | `init/` |

`TableInitializer` (creates the `transactions` table, `@Order(1)`) stays
exactly as it is — these two new/replaced files run after it, in order:

```
@Order(1)  TableInitializer         — creates transactions table
@Order(2)  DataInitializer          — generates ~5,000 synthetic transactions
@Order(3)  SyntheticDataInitializer — creates + generates customers/devices/merchants/login_history
```

## What you can remove from application.properties

```properties
app.init.csv-path=classpath:data/data.csv
app.init.table-name=transactions
```
No longer used — delete these two lines. You can also delete
`src/main/resources/data/data.csv` from the repo entirely if it's there.

## Key details

- **Fixed random seed (42)** in both files — the generated dataset is
  identical every time, on every machine. Anyone who clones your repo and
  runs it gets the exact same customers, transactions, and fraud cases you
  tested with — useful for demo consistency and for your README's
  screenshots to always match what a reader sees themselves.
- **~5,000 transactions, ~4% fraud (~200 cases)** — deliberately higher than
  real PaySim's ~0.13%, so a small, fast-to-generate dataset still gives
  meaningful coverage for demos and an evaluation harness.
- **Fraud pattern matches the real thing**: TRANSFER/CASH_OUT only, full
  balance drain, paired with a brand-new untrusted device and an unusual
  country in login_history — same signal design as your original
  PaySim-based dataset, just generated instead of sampled.
- Runs in a few seconds on startup (a few thousand JDBC round-trips) —
  fine for a one-time seed, not something you'd want running on every
  deploy of a real production app, but appropriate here since it's guarded
  by the same "skip if already populated" check as your other initializers.

## After adding these files

Point your app at a **fresh, empty** Postgres database (local or Railway),
start it, and check the logs — you should see:
```
Table 'transactions' is empty. Generating 5000 synthetic transactions...
Generated 5000 transactions (XXX fraud) into 'transactions'.
Generating customers, devices, merchants, and login history...
Generated XXXX customers, merchants: XXX, login history rows: 5000.
```
No manual SQL, no CSV, no separate seed script to run.
