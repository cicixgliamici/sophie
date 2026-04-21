# Sophie

Sophie is a JVM-based domain-specific language (DSL) for expressing simple trading rules and portfolio allocations in a concise, human-readable format.

The project combines:
- a custom DSL grammar (ANTLR),
- an execution pipeline (`parse -> evaluate -> lower -> execute`),
- a CLI for batch runs,
- and a TUI for interactive sessions.

It is currently suitable for demos, local simulations, and educational use.

---

## Why this project matters

Sophie is not just a toy parser: it is a compact language-engineering project designed to demonstrate how a small DSL can be built as a structured software artifact.

It is especially useful to review if you are interested in:
- **DSL design and syntax engineering**
- **ANTLR-based parsing pipelines**
- **typed AST construction**
- **interpreter/evaluator architecture**
- **separating pure core logic from execution-side effects**

In short, the project is meant to show both **programming language design** and **software architecture discipline** in a single repository.

---

## What you can express in the DSL

Sophie programs are made of ordered statements.

### Trade statements
- `BUY` / `SELL` by notional value (amount + currency), for example:
  - `BUY 1500 EUR OF MSFT`
- `BUY` / `SELL` by explicit quantity:
  - `SELL QTY 0.5 OF BTC`
- Optional `IF` conditions for guarded execution.

### Portfolio statement
- `PORTFOLIO` target allocations such as:
  - `6000 EUR OF VWCE + 2000 USD OF AAPL + 0.1 BTC OF BTC`

### Conditions and expressions
- Boolean logic: `AND`, `OR`
- Comparisons: `>`, `<`, `=`, `!=`
- Arithmetic: `+`, `-`, `*`, `/`
- Price lookup: `PRICE(SYMBOL)`
- Series fields: `BTC.close`, `BTC.volume`, etc.
- Indicators: `MAVG`, `EMA`, `STDDEV`, `RSI`

---

## Example

### Sample Sophie program

```text
BUY 1500 EUR OF MSFT IF PRICE(MSFT) < 420
SELL QTY 0.25 OF BTC IF RSI(BTC, 14) > 70
PORTFOLIO 6000 EUR OF VWCE + 2000 USD OF AAPL + 0.1 BTC OF BTC
````

### What happens conceptually

1. The source program is parsed with the ANTLR grammar.
2. The parse tree is converted into a typed AST.
3. The evaluator checks conditions against market data.
4. A high-level execution plan is produced.
5. The plan is lowered into executable instructions.
6. Instructions can then be executed and persisted to portfolio / ledger files.

This gives the repository a clear pipeline from **language input** to **concrete execution behavior**.

---

## Quick reviewer guide

If you want to evaluate the repository quickly, the best path is:

1. Read this README for the project goal and architecture.
2. Open `src/main/antlr4/sophie.g4` to inspect the DSL grammar.
3. Inspect `src/main/scala/ast` for the typed AST model.
4. Inspect `src/main/scala/engine` for evaluation, lowering, execution, and persistence.
5. Check `src/test/scala` to see how behavior is validated.
6. Optionally run the CLI or TUI with the demo market data.

---

## What this project demonstrates

* DSL design and syntax engineering
* parser-driven architecture with ANTLR
* typed AST construction
* evaluation and execution pipeline design
* separation between core logic and side effects

---

## Architecture at a glance

Sophie is organized in layered components:

1. **Parsing**: ANTLR grammar + generated lexer/parser convert source code into a parse tree.
2. **AST build**: parse tree is transformed into a typed AST.
3. **Evaluation**: AST is evaluated against market data to produce an `ExecutionPlan`.
4. **Lowering**: the plan is converted to concrete JSON-ready instructions (IR).
5. **Execution**: instructions are applied to portfolio state and persisted to ledger.
6. **Frontends**: CLI and TUI reuse the same core pipeline.

This separation keeps core logic mostly pure and side effects concentrated in execution and persistence layers.

---

## Repository structure (main areas)

* `src/main/antlr4/sophie.g4`: DSL grammar
* `src/main/scala/ast`: AST model and AST builder
* `src/main/scala/engine`: evaluator, lowering/IR, executor, ledger, portfolio store
* `src/main/scala/frontend`: parser facade, TUI, command handling, market-data codecs
* `src/main/scala/cli`: CLI entrypoint and argument handling
* `src/test/scala`: unit and integration tests
* `docs/`: architecture, language, persistence, and command notes

---

## Tech stack

* **Scala**: 3.3.6
* **Build tool**: sbt
* **Parser generator**: ANTLR 4 (via `sbt-antlr4` plugin)
* **Testing**: ScalaTest
* **JSON**: uPickle

---

## Getting started

### Prerequisites

* JDK 17+ recommended
* sbt installed

### Build

```bash
sbt compile
```

### Run tests

```bash
sbt test
```

---

## Running Sophie

### Interactive TUI (default main)

```bash
sbt run
```

or explicitly:

```bash
sbt "runMain TuiMain"
```

### CLI mode

```bash
sbt "runMain cli.SophieCli --file examples/cli_buy_sell.sophie --run --ledger ledger.ndjson --portfolio portfolio.json"
```

Useful CLI options:

* `--file <path>`: input `.sophie` program
* `--md <path>`: market data JSON (if omitted, demo data is used)
* `--print-instructions`: print lowered IR JSON
* `--run`: execute instructions and persist outputs
* `--ledger <path>`: ledger NDJSON path (default: `ledger.ndjson`)
* `--portfolio <path>`: portfolio JSON path (default: `portfolio.json`)
* `--receipt-file <path>`: append textual execution receipts
* `--reset-portfolio`: reset portfolio file without interactive prompt

---

## Persistence and file formats

* **Portfolio**: JSON (positions map + cash)
* **Ledger**: NDJSON (one JSON event per executed instruction)
* **Market data**: JSON (`prices`, `series`, `indicatorOverrides`)

Example demo market data is available at:

* `src/main/resources/md_demo.json`

---

## Current limitations

* Only the first `PORTFOLIO` block is honored.
* Missing market data and division by zero are runtime errors.
* Indicator support is currently limited to `MAVG`, `EMA`, `STDDEV`, and `RSI`.
* Only market-like `BUY`/`SELL` instructions are modeled (no advanced order types).

---

## Typical use cases

* Demonstrating DSL design and ANTLR-based parsing
* Running simple what-if simulations with synthetic market data
* Teaching layered interpreter/compiler architecture
* Rapid prototyping of rule-based trade plans

---

## Documentation

Additional project documentation is available in `docs/`, including:

* language overview and architecture notes
* DSL/ANTLR rationale
* TUI command behavior and execution pipeline
* storage and persistence behavior
* developer quick commands
