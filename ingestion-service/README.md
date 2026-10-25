# IngestionServiceApp

## Overview

Parses and cleans `hubs-global.csv`, a messy legacy export of hubs, sorting centers, and regional districts data, and is the
first stop in the LogisticsConnect pipeline. Independent Maven module, no parent pom.

Part of the [LogisticsConnect](../README.md) project.

## Known data issues

`hubs-global.csv` is deliberately messy — cleaning it is the point of this service. Look
out for (and handle) at least:

- **Inconsistent casing** in IDs, names, and status/category values (`Active` /
  `active` / `ACTIVE`)
- **Padding** — leading/trailing spaces, and the occasional double space, inside
  fields
- **Duplicate records** for the same real-world entity, written with a different ID
  casing/format and/or slightly different field values
- **Inconsistent date formats** (`YYYY-MM-DD`, `MM/DD/YYYY`, `DD-MM-YYYY`, one- and
  two-digit months/days) and outright invalid dates
- **Missing / placeholder values** — blank fields, `N/A`, `n/a`, `TBD`, `unknown`,
  `-`, `NaN`
- **Invalid or non-numeric values** in numeric columns (negative counts, spelled-out
  numbers, unrealistic values)
- **Inconsistent boolean/flag representations** (`Y`/`N`, `yes`/`no`, `1`/`0`,
  `true`/`FALSE`)
- **Naming/spelling variants** for the same thing (e.g. regional spelling
  differences, synonyms)

## Worked example

Two raw rows from `hubs-global.csv`:

```
hub_id, Province ,sorting_center,active
H-502 ,gauteng,Pretoria North,0
H-505,Western Cape ,Cape Town  Port,TRUE
```

A reasonable cleaned shape for those same two rows:

| hub_id | province | sorting_center | active |
|---|---|---|---|
| H-502 | Gauteng | Pretoria North | false |
| H-505 | Western Cape | Cape Town Port | true |

That covers padding (`H-502 ` → `H-502`), casing (`gauteng` → `Gauteng`), a
collapsed double space (`Cape Town  Port` → `Cape Town Port`), and boolean
normalization (`0`/`TRUE` → `false`/`true`). Column names, exact casing
convention, and boolean representation are up to you — just be consistent.

This doesn't cover deduplication: rows like `H-500`, `H-504`, `H-510`, and `H-515`
all describe "Johannesburg Central" in "Gauteng" under different hub IDs, with
conflicting `active` values between them. How you resolve that (which one wins,
how you detect they're duplicates in the first place) is part of the exercise —
there's no single correct answer, but be ready to explain your reasoning.

## Project structure

```
ingestion-service/
├── pom.xml
└── src/main/
    ├── java/co/wethinkcode/logisticsconnect/IngestionServiceApp.java
    └── resources/hubs-global.csv
```

## Build

```
mvn package
```

## Run

```
java -jar target/ingestion-service.jar
```

Listens on port `7050`. Currently just exposes `/health` — the actual CSV
parsing/cleaning logic is a TODO.

## What it does now

`GET /hubs` returns the cleaned hubs as JSON, and `GET /health` returns `OK`.
Raw rows go through these steps in order:

1. **Clean each field.** IDs are trimmed and upper-cased. Names are trimmed,
   have double spaces collapsed and are title-cased. Provinces are matched to
   the nine real provinces, ignoring case, spaces and hyphens, so
   `Kwa-Zulu Natal` and `KwaZulu Natal` both become `KwaZulu-Natal`. The
   active flag accepts `Y/yes/true/1` and `N/no/false/0`.
2. **Unknown stays unknown.** Placeholders (`unknown`, `N/A`) and unrecognised
   values become `null`. They are never guessed.
3. **Fill a blank province** only when every other row with the same sorting
   center agrees on one province.
4. **Merge duplicates.** Rows with the same sorting center and province are one
   hub. The lowest hub ID is canonical and the others are kept as `aliases`,
   so a lookup by an old ID can still be resolved by other services.
5. **Resolve the active flag by majority vote.** On a tie, the lowest ID's value
   wins.

18 raw rows become 10 hubs.

## Decisions and trade-offs

- **Aliases instead of deleting duplicates.** Other services will look hubs up
  by ID. Keeping the old IDs means none of them break.
- **Majority vote.** One conflicting row among several shouldn't decide.
- **Tie-break by lowest ID.** Arbitrary, but deterministic and tested.
- **Not guessing.** A blank province stays null if the sorting center appears
  in more than one province.
- **There are no date columns** in this CSV, so no date handling was needed.

## Tests

    mvn test

## Test

No automated tests yet. Manually verify it's up:

```
curl http://localhost:7050/health   # -> OK
```

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/logisticsconnect/`, and run `mvn test`.
