Sophie — quick dev & test guide
===============================

This repository contains a small DSL and runtime for trading-like programs.
The project is structured to keep core logic pure (parser → AST → evaluator → lowering)
and isolate side-effects (file I/O, TUI) to a thin adapter layer.

Quick commands
--------------
- Compile:

```bash
sbt compile
```

- Run the full test suite:

```bash
sbt test
```

- Run a single test suite:

```bash
sbt "testOnly frontend.SophieTuiIntegrationSpec"
```

- Run the interactive TUI locally (manual):

```bash
sbt "runMain TuiMain"
# or from the REPL
sbt console
scala> frontend.SophieTui.run()
```

- Run the CLI on a `.sophie` file (example):

```bash
sbt "runMain cli.SophieCli --file examples/buy_ok.sophie --run --ledger data/ledger.ndjson --portfolio data/portfolio.json"
```

Notes about testing
-------------------
- Most core behaviour is pure and unit-tested via the test suite.
- Use `SophieTui.simulateSession` to programmatically exercise TUI commands
  (no interactive stdin required) — this is what the tests already do.
- For integration tests that write the ledger/portfolio the project uses a
  temporary `data/` folder that tests create and clean up; be mindful of
  existing files in `data/` when running tests locally.

Design notes (short)
--------------------
- Pure core: parsing, AST transformation, evaluation and lowering are pure.
- I/O adapters: ledger, portfolio store, and receipt printer perform side-effects
  and are implemented in dedicated modules to keep the core testable.

If you want, I can also:
- Add a short developer doc explaining where to add new tests for TUI/CLI.
- Convert I/O adapters to traits (Ledger, PortfolioStore) to make mocking
  easier during tests.

