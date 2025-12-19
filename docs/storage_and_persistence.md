Storage and Persistence (Sophie)

This document describes where the Sophie application stores files, which formats are used, and how to reset or control persistence from both the CLI and the TUI.

1) File locations (defaults)

- CLI defaults (when using `cli.SophieCli`):
  - Portfolio file: `portfolio.json` (in the current working directory)
  - Ledger file: `ledger.ndjson` (in the current working directory)
  - These defaults can be overridden with the CLI flags `--portfolio <path>` and `--ledger <path>`.

- TUI defaults (when using the interactive TUI `TuiMain` / `SophieTui`):
  - The IR executor uses the `data/` directory by default:
    - Portfolio file for IR execution: `data/portfolio.json`
    - Ledger file for IR execution: `data/ledger.ndjson`
  - Other TUI commands (`:pf save`, `:pf load`, `:save md`, `:save prog`) always require explicit file paths you provide.

2) File formats

- Portfolio files:
  - Format: JSON
  - Shape: the code uses `frontend.PortfolioJson.PortfolioJ` which serializes as a JSON object with at least a `positions` map and an optional `cash` value.
  - Example shape (pretty-printed):
    {
      "positions": {
        "MSFT": "3.125"
      },
      "cash": "0"
    }
  - Values for quantities and cash are serialized as strings that represent decimal numbers (BigDecimal in Scala). The project reads/writes this format via upickle and the `PortfolioJ` codec.

- Ledger files:
  - Format: NDJSON (newline-delimited JSON)
  - Each line is one JSON object representing a single executed event (trade). Typical fields include:
    - `ts`: timestamp (milliseconds since epoch)
    - `action`: `BUY` or `SELL`
    - `symbol`: asset symbol (e.g. `AAPL`)
    - `qty`: quantity traded (string or numeric representation)
    - `price`: execution price
    - `notional`: notional amount in currency
    - `source`: source id (e.g. `cli` or `ir:<file>`)
    - `note`: human-friendly note describing the trade
  - NDJSON is convenient to append to (the executor appends one JSON object per executed instruction).

- Market data files (used by CLI `--md` and TUI `:load md`):
  - Format: JSON
  - Keys used by the bundled demo format include `prices`, `series`, and `indicatorOverrides`.
  - Example demo file is `src/main/resources/md_demo.json`.

3) Reset / clean state options

- CLI
  - Interactive prompt: when running with `--run`, if the CLI detects that the portfolio file already exists at the chosen path, it will prompt the user:
    "Portfolio file <path> esiste. Vuoi eliminarlo e iniziare da zero? [y/N]:"
    - Reply `y` or `yes` to delete/reset the file before execution.
    - Any other reply will keep the existing portfolio file and proceed.
  - Non-interactive force reset: pass the flag `--reset-portfolio` to the CLI invocation to reset the portfolio file without asking.
    - Example:
      sbt "runMain cli.SophieCli --file data/examples/strategy_example.sophie --run --portfolio data/portfolio_test.json --md data/md_for_strategy.json --reset-portfolio"
  - The reset writes a minimal empty `PortfolioJ` JSON (empty `positions` map and `cash = 0`) to the chosen portfolio path.

- TUI
  - In-memory reset: use `:pf new` to reset the in-memory portfolio state in the TUI session. This affects the simulated portfolio used by TUI commands like `:pf show` and `:pf apply`.
  - Save / overwrite to disk: after resetting (or editing) you can use `:pf save <path.json>` to write the current in-memory portfolio to a file. If you provide an existing path, it will be overwritten.
  - Load from disk: use `:pf load <path.json>` to load a portfolio file into the TUI session.
- IR execution path: when executing IR instructions with `:exec ir <file.json>` the TUI will by default create or update files under `data/` (e.g. `data/portfolio.json` and `data/ledger.ndjson`). Changing these paths currently requires a code change (there are no TUI flags to override them).

4) Recommended workflows for testing (clear reproducible state)

- Automated / non-interactive tests (recommended):
  - Use the CLI with explicit paths and `--reset-portfolio` to ensure the portfolio file is reinitialized for each run. Example:
    sbt "runMain cli.SophieCli --file data/examples/buy_example.sophie --run --ledger data/ledger_test.ndjson --portfolio data/portfolio_test.json --md data/md_for_strategy.json --reset-portfolio"
  - This guarantees the portfolio file starts empty and the ledger file you provide will be appended with this run's trades.

- Manual / interactive testing with TUI:
  - Start TUI: `sbt "runMain TuiMain"` (or run the packaged application).
  - Within TUI:
    - Use `:pf new` to clear in-memory portfolio.
    - Use `:run prog <file.sophie>` to evaluate a program (this only prints a plan by default).
    - Use `:pf save data/portfolio.json` to persist the in-memory portfolio (overwrites existing file).
    - Use `:exec ir data/instructions.json` to execute IR instructions and have the TUI update `data/portfolio.json` and append `data/ledger.ndjson` by default.

5) Notes and caveats

- Working directory matters: the CLI uses relative paths from the process working directory. If you run sbt from the project root, default `portfolio.json` and `ledger.ndjson` will be created in the project root unless you pass explicit paths.
- TUI uses the `data/` subdirectory only for IR execution. If you want a single canonical place for test artifacts, prefer providing explicit `--ledger`/`--portfolio` paths in the CLI or explicit paths in TUI save/load commands.
- The JSON formats are stable but rely on the project's upickle codecs. If you modify `frontend.PortfolioJson.PortfolioJ` or codec behavior, the on-disk format may change accordingly.

6) Quick reference (commands)

- CLI examples:
  - Print plan and instructions only:
    sbt "runMain cli.SophieCli --file data/examples/buy_example.sophie --print-instructions --md data/md_for_strategy.json"
  - Execute and reset portfolio before running:
    sbt "runMain cli.SophieCli --file data/examples/strategy_example.sophie --run --ledger data/ledger_test.ndjson --portfolio data/portfolio_test.json --md data/md_for_strategy.json --reset-portfolio"

- TUI examples (inside TUI session):
  - Reset in-memory portfolio: `:pf new`
  - Save current in-memory portfolio to file: `:pf save data/portfolio.json`
  - Load portfolio from file: `:pf load data/portfolio.json`
  - Run a program from disk: `:run prog data/examples/buy_example.sophie`
  - Compile last plan to IR: `:compile ir data/out_instructions.json`
  - Execute IR (updates `data/portfolio.json` and appends `data/ledger.ndjson` by default): `:exec ir data/out_instructions.json`
