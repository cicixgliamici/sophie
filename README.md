# Sophie

Sophie is a JVM-based domain-specific language (DSL) for expressing simple trading rules and portfolio allocations in a concise, human-readable format.

The project combines:
- a custom DSL grammar (ANTLR),
- an execution pipeline (`parse -> evaluate -> lower -> execute`),
- a CLI for batch runs,
- and a TUI for interactive sessions.

It is currently suitable for demos, local simulations, and educational use.

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

## Architecture at a glance

Sophie is organized in layered components:

1. **Parsing**: ANTLR grammar + generated lexer/parser convert source code into a parse tree.
2. **AST build**: parse tree is transformed into a typed AST.
3. **Evaluation**: AST is evaluated against market data to produce an `ExecutionPlan`.
4. **Lowering**: the plan is converted to concrete JSON-ready instructions (IR).
5. **Execution**: instructions are applied to portfolio state and persisted to ledger.
6. **Frontends**: CLI and TUI reuse the same core pipeline.

This separation keeps core logic mostly pure and side effects concentrated in execution and persistence layers.

## Repository structure (main areas)

- `src/main/antlr4/sophie.g4`: DSL grammar
- `src/main/scala/ast`: AST model and AST builder
- `src/main/scala/engine`: evaluator, lowering/IR, executor, ledger, portfolio store
- `src/main/scala/frontend`: parser facade, TUI, command handling, market-data codecs
- `src/main/scala/cli`: CLI entrypoint and argument handling
- `src/test/scala`: unit and integration tests
- `docs/`: architecture, language, persistence, and command notes

## Tech stack

- **Scala**: 3.3.6
- **Build tool**: sbt
- **Parser generator**: ANTLR 4 (via `sbt-antlr4` plugin)
- **Testing**: ScalaTest
- **JSON**: uPickle

## Getting started

### Prerequisites
- JDK 17+ recommended
- sbt installed

### Build
```bash
sbt compile
```

### Run tests
```bash
sbt test
```

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
- `--file <path>`: input `.sophie` program
- `--md <path>`: market data JSON (if omitted, demo data is used)
- `--print-instructions`: print lowered IR JSON
- `--run`: execute instructions and persist outputs
- `--ledger <path>`: ledger NDJSON path (default: `ledger.ndjson`)
- `--portfolio <path>`: portfolio JSON path (default: `portfolio.json`)
- `--receipt-file <path>`: append textual execution receipts
- `--reset-portfolio`: reset portfolio file without interactive prompt

## Persistence and file formats

- **Portfolio**: JSON (positions map + cash)
- **Ledger**: NDJSON (one JSON event per executed instruction)
- **Market data**: JSON (`prices`, `series`, `indicatorOverrides`)

Example demo market data is available at:
- `src/main/resources/md_demo.json`

## Current limitations

- Only the first `PORTFOLIO` block is honored.
- Missing market data and division by zero are runtime errors.
- Indicator support is currently limited to `MAVG`, `EMA`, `STDDEV`, and `RSI`.
- Only market-like `BUY`/`SELL` instructions are modeled (no advanced order types).

## Typical use cases

- Demonstrating DSL design and ANTLR-based parsing
- Running simple what-if simulations with synthetic market data
- Teaching layered interpreter/compiler architecture
- Rapid prototyping of rule-based trade plans

## Documentation

Additional project documentation is available in `docs/`, including:
- language overview and architecture notes,
- DSL/ANTLR rationale,
- TUI command behavior and execution pipeline,
- storage and persistence behavior,
- developer quick commands.
