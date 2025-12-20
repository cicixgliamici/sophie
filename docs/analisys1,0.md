# Sophie Codebase Report (MVP 0 Readiness)

## 1) Executive summary: MVP 0 readiness

### Key reasons

- The core pipeline is implemented and documented (**parse → AST → evaluate → lower → execute**).  
  Reference: `docs/language_overview.txt`
- The CLI and TUI are wired to that pipeline and support execution plus persistence.  
  References:  
  - `src/main/scala/cli/SophieCli.scala`  
  - `src/main/scala/frontend/SophieTui.scala`  
  - `docs/storage_and_persistence.md`
- Current limitations are clearly documented (single portfolio command, limited indicators, runtime errors for missing data, no order types beyond market BUY/SELL).  
  Reference: `docs/language_overview.txt`

### MVP 0 fit

- ✅ Good for demos, interactive exploration, and local simulations.  
- ❌ Not yet ready for real-world trading or production reliability.

---

## 2) What the system can do today

### DSL capabilities (current language features)

- `BUY` / `SELL` commands with currency amounts or explicit quantity (`QTY`).
- Optional `IF` conditions with boolean logic and comparisons.
- `PORTFOLIO` allocation block.
- Operands include:
  - `PRICE(symbol)`
  - series fields like `BTC.volume`
  - indicators (`MAVG` / `EMA` / `STDDEV` / `RSI`)
  - arithmetic expressions

References:
- `docs/language_overview.txt`
- `src/main/antlr4/sophie.g4`

### Runtime capabilities

- Parse and transform to AST  
  - `src/main/scala/frontend/SophieParserFacade.scala`  
  - `src/main/scala/ast/SophieAstBuilder.scala`  
  - `src/main/scala/ast/AST.scala`
- Evaluate AST to a high-level `ExecutionPlan`  
  - `src/main/scala/engine/Evalutator.scala`
- Lower execution plan to serializable instructions (IR)  
  - `src/main/scala/engine/IR.scala`
- Execute instructions, update portfolio, append ledger  
  - `src/main/scala/engine/Executor.scala`  
  - `src/main/scala/engine/Ledger.scala`  
  - `src/main/scala/engine/PortfolioStore.scala`
- CLI for batch execution and optional instruction printout  
  - `src/main/scala/cli/SophieCli.scala`
- TUI for interactive sessions, with `simulateSession` for non-interactive tests  
  - `src/main/scala/frontend/SophieTui.scala`  
  - `src/main/scala/frontend/CommandHandler.scala`

---

## 3) Architecture overview

The repository follows a classic layered compiler/runtime pipeline.

### Parsing (ANTLR)

- Grammar in `sophie.g4`, generated parser/lexer into Java.
- `SophieParserFacade` + `SophieAstBuilder` convert parse tree → typed AST.

References:
- `src/main/antlr4/sophie.g4`
- `src/main/scala/frontend/SophieParserFacade.scala`
- `src/main/scala/ast/SophieAstBuilder.scala`

### AST layer

- Typed AST with `case class` + `sealed trait` nodes.  
  Reference: `src/main/scala/ast/AST.scala`

### Evaluation

- `engine.Evaluator` interprets AST against `MarketData` and creates an `ExecutionPlan`.  
  References:
  - `src/main/scala/engine/Evalutator.scala`
  - `src/main/scala/engine/MarketData.scala`

### Lowering (IR)

- `engine.Lowering` turns plans into concrete, serializable instructions.  
  Reference: `src/main/scala/engine/IR.scala`

### Execution & persistence

- `engine.Executor` applies instructions; `Ledger` and `PortfolioStore` persist results.  
  References:
  - `src/main/scala/engine/Executor.scala`
  - `src/main/scala/engine/Ledger.scala`
  - `src/main/scala/engine/PortfolioStore.scala`

### Frontends

- CLI (batch) and TUI (interactive) are thin adapters over the pipeline.  
  References:
  - `src/main/scala/cli/SophieCli.scala`
  - `src/main/scala/frontend/SophieTui.scala`
  - `docs/language_overview.txt`

This matches the documented flow: **Parse → Evaluate → Lower → Execute** (`docs/language_overview.txt`).

---

## 4) Functional vs. Imperative vs. OOP

### Executive Summary (Functional Orientation)

The codebase is predominantly **functional in the core** (AST, evaluation, lowering, pure transformations) and **imperative at the boundaries** (CLI/TUI I/O, persistence, execution).

Functional practices include:
- immutability via `val`
- algebraic data types (`sealed trait` + `case class`)
- pattern matching
- higher-order functions
- tail recursion
- explicit state threading (stateless design)

Imperative elements appear in file I/O, console I/O, and a few `var` uses mainly in integration/test helpers.

Sources:
- `src/main/scala/ast/AST.scala`
- `src/main/scala/engine/Evalutator.scala`
- `src/main/scala/engine/IR.scala`
- `src/main/scala/frontend/PortfolioManager.scala`
- `src/main/scala/frontend/SophieTui.scala`
- `src/main/scala/cli/SophieCli.scala`
- `src/main/scala/engine/Executor.scala`

---

### Quantitative Indicators (Functional vs Imperative)

### 2.1 `val` vs `var`

- Total `val` occurrences: **626**
- Total `var` occurrences: **5** (some are in comments or tests)

Commands used:
- `rg -o "\\bval\\b" src/main/scala src/test/scala | wc -l`
- `rg -o "\\bvar\\b" src/main/scala src/test/scala | wc -l`
- `rg -n "\\bvar\\b" src/main/scala src/test/scala`

Notes on `var` usage (actual locations):
- `src/test/scala/testhelpers/ProcessRunner.scala` uses `var line` for reading a process stream.
- `src/main/scala/integration/RunIntegrationRunner.scala` uses `var totalFailures` and `var failuresLocal` for counting failures.
- `src/main/scala/frontend/PortfolioManager.scala` mentions `var` only in a comment describing an alternative imperative design.

Interpretation: The `val`/`var` ratio is extremely skewed toward immutability. Almost all real `var` usage is confined to tests or integration runners.

### 2.2 Tail recursion and explicit loops

- `@tailrec` occurrences: **2**  
  Both in the TUI REPL logic.  
  Command: `rg -n "@tailrec" src/main/scala src/test/scala`  
  Source: `src/main/scala/frontend/SophieTui.scala`

- `while` loops: **2 occurrences**  
  One is in a test helper, one is a comment.  
  Commands:
  - `rg -o "\\bwhile\\b" src/main/scala src/test/scala | wc -l`
  - `rg -n "\\bwhile\\b" src/main/scala src/test/scala`  
  Sources:
  - `src/test/scala/testhelpers/ProcessRunner.scala`
  - `src/main/scala/ast/SophieAstBuilder.scala`

Interpretation: Tail recursion is used for the TUI REPL loops, while explicit `while` loops appear only in a test helper (imperative edge). In core logic, iteration is primarily done via functional combinators.

---

### Functional Programming Features Present

### 3.1 Immutable data modeling (Algebraic Data Types)

The AST uses `sealed trait` + `case class`, enabling immutability, pattern matching, and exhaustive checking.  
Source: `src/main/scala/ast/AST.scala`

### 3.2 Pure transformations & stateless core

- Evaluator transforms **AST → ExecutionPlan** without side effects.  
  Source: `src/main/scala/engine/Evalutator.scala`
- Lowering converts **plan → instructions** with pure data transforms.  
  Source: `src/main/scala/engine/IR.scala`
- PortfolioManager is explicitly designed as a stateless, pure transformation layer; state is passed in/out rather than mutated internally.  
  Source: `src/main/scala/frontend/PortfolioManager.scala`

### 3.3 Higher-order functions & functional combinators

Widespread use of `.map`, `.foldLeft`, `.collect`, `.filter`, `.flatMap`, `.getOrElse`, etc.

Examples:
- Execution plan evaluation uses `.collect` and `.collectFirst`.  
  Source: `src/main/scala/engine/Evalutator.scala`
- IR lowering uses `foldRight` to accumulate results.  
  Source: `src/main/scala/engine/IR.scala`
- Portfolio transformations use `foldLeft`.  
  Source: `src/main/scala/frontend/PortfolioManager.scala`

### 3.4 Tail recursion

TUI loop is tail-recursive, annotated with `@tailrec`.  
Source: `src/main/scala/frontend/SophieTui.scala`

### 3.5 Explicit effect boundaries

Effects (I/O, state persistence) are separated from pure logic:
- Parsing and evaluation are pure.
- Execution + persistence in `Executor`, `Ledger`, and `PortfolioStore` are impure by design.

Sources:
- `src/main/scala/engine/Executor.scala`
- `src/main/scala/engine/Ledger.scala`
- `src/main/scala/engine/PortfolioStore.scala`

---

### Where Functional Programming Is Missing or Weaker

### 4.1 Lack of a dedicated validation/effect system

Errors for missing data or division by zero are thrown at runtime (exceptions), rather than being modeled with `Either`, `Validated`, or an effect type (`IO`).  
Source: `src/main/scala/engine/Evalutator.scala`

### 4.2 Imperative boundaries are still significant

CLI and TUI are imperative, by necessity:
- interactive prompts
- filesystem reads/writes
- console printing

Sources:
- `src/main/scala/cli/SophieCli.scala`
- `src/main/scala/frontend/SophieTui.scala`

Execution is impure and mutates external state (ledger + portfolio files).  
Sources:
- `src/main/scala/engine/Executor.scala`
- `src/main/scala/engine/Ledger.scala`
- `src/main/scala/engine/PortfolioStore.scala`

### 4.3 Some mutation in test and integration harness

Test helpers and integration runners use `var` for loop state and counters.  
Sources:
- `src/test/scala/testhelpers/ProcessRunner.scala`
- `src/main/scala/integration/RunIntegrationRunner.scala`

---

### Statelessness — Where It’s Strong

The project explicitly documents and implements stateless transformations:

- `PortfolioManager` accepts state and returns updated state; it does not hold internal mutable state.  
  Source: `src/main/scala/frontend/PortfolioManager.scala`
- `Evaluator`, `Lowering`, and AST components are pure with explicit inputs/outputs.  
  Sources:
  - `src/main/scala/engine/Evalutator.scala`
  - `src/main/scala/engine/IR.scala`
  - `src/main/scala/ast/AST.scala`

---

## 5) Tests implemented & coverage

### Test coverage types (not percentage)

There is a substantial test suite across parser, AST, evaluator, lowering, executor, CLI, TUI, and utilities. Examples:

**Parser & AST**
- `src/test/scala/parser/SyntaxErrorTest.scala`
- `src/test/scala/parser/SophieParserSmokeTest.scala`
- `src/test/scala/ast/SophieAstBuilderTest.scala`

**Evaluator & indicators**
- `src/test/scala/engine/EvaluatorTest.scala`
- `src/test/scala/engine/IndicatorsSpec.scala`

**Lowering & execution**
- `src/test/scala/engine/IRLoweringSpec.scala`
- `src/test/scala/engine/ExecutorSpec.scala`

**CLI/TUI integration**
- `src/test/scala/cli/SophieCliIntegrationSpec.scala`
- `src/test/scala/frontend/SophieTuiIntegrationSpec.scala`

### Coverage percentage

There is no explicit coverage tool configured (e.g., `scoverage`) in the build, so coverage percentage is unknown.  
Source: `build.sbt`

---

## 6) MVP 0 gaps & risks (what’s missing)

From the code and docs, key MVP 0 limitations are:

- Validation & error handling gaps: missing market data or division by zero are runtime errors; no structured validation layer.  
  Sources: `docs/language_overview.txt`, `src/main/scala/engine/Evalutator.scala`
- Single portfolio block limitation: only the first portfolio is honored.  
  Sources: `docs/language_overview.txt`, `src/main/scala/engine/Evalutator.scala`
- Limited order types: only BUY/SELL market-style instructions.  
  Sources: `docs/language_overview.txt`, `src/main/scala/engine/IR.scala`
- Indicator set limited to `MAVG` / `EMA` / `STDDEV` / `RSI`.  
  Sources: `docs/language_overview.txt`, `src/main/scala/engine/Evalutator.scala`
- No real market data or broker integration; current design uses file-based inputs and in-memory data.  
  Sources: `src/main/scala/engine/MarketData.scala`, `docs/storage_and_persistence.md`

---

## 7) Overall assessment (MVP 0)

### ✅ Yes for a local, simulated MVP 0

You can parse a DSL program, evaluate it, preview a plan, lower to IR, and execute it with ledger/portfolio persistence via CLI or TUI.  
Sources:
- `docs/language_overview.txt`
- `src/main/scala/cli/SophieCli.scala`
- `src/main/scala/frontend/SophieTui.scala`

### ❌ Not yet for production or “MVP” with real trading integration

Lacks richer validation, broader indicator library, order types, and live data/broker connectivity.  
Source: `docs/language_overview.txt`

---

# Sophie Language — Theoretical Analysis

## 1) Associativity (Right/Left)

Associativity is derived from the grammar structure in `src/main/antlr4/sophie.g4`.

### 1.1 Arithmetic expressions

- `expr : term ((PLUS | MINUS) term)*`  
  This form is **left-associative** for `+` and `-` because it parses as a sequence of `term` nodes combined from left to right.  
  Source: `src/main/antlr4/sophie.g4`

- `term : primary ((MUL | DIV) primary)*`  
  Similarly **left-associative** for `*` and `/`.  
  Source: `src/main/antlr4/sophie.g4`

### 1.2 Boolean logic

- `disjunction : conjunction (OR conjunction)*`  
  This is **left-associative** for `OR`, because it builds a flat chain from left to right.  
  Source: `src/main/antlr4/sophie.g4`

- `conjunction : (comparison | LPAR condition RPAR) (AND conjunction)?`  
  This is **right-associative** for `AND`, because recursion is on the right side.  
  Example parse: `A && B && C` becomes `A && (B && C)`.  
  Source: `src/main/antlr4/sophie.g4`

### 1.3 Comparisons

- `comparison : expr ((GT | LT | EQ | NEQ) expr)?`  
  Comparisons are **non-associative**: only one comparison operator is allowed per comparison node (you can’t chain `a < b < c` without parentheses).  
  Source: `src/main/antlr4/sophie.g4`

---

## 2) Language Characteristics (Theoretical Features)

### 2.1 Domain-specific, declarative DSL

The language is a trading-oriented DSL aimed at expressing trades and portfolio allocations rather than general computation.

It is declarative: users specify what trades/allocations they want, not how to execute them.  
Sources:
- `docs/language_overview.txt`
- `src/main/antlr4/sophie.g4`

### 2.2 Statement-based program structure

A program is a sequence of statements separated by semicolons:

- `program : (statement SEMICOLON?)+ EOF`

Statements are either trade commands or portfolio commands.  
Source: `src/main/antlr4/sophie.g4`

### 2.3 Strongly typed, structured syntax

Uses explicit keywords (`BUY`, `SELL`, `PORTFOLIO`, `IF`, `QTY`) and reserved currencies (`EUR`, `USD`, `GBP`, `BTC`).

Symbols are explicitly separated from currencies via a `symbol` rule.  
Source: `src/main/antlr4/sophie.g4`

### 2.4 Expression features

Supports arithmetic, comparisons, logical operators, and parentheses.

Operands include numeric literals, price lookup, time-series fields (`BTC.volume`), and indicators (`MAVG`, `EMA`, `STDDEV`, `RSI`).  
Sources:
- `src/main/antlr4/sophie.g4`
- `docs/language_overview.txt`

### 2.5 Truthiness, quantities, and market data semantics

- **Truthy comparisons:** a comparison with a single expression is interpreted as “expression != 0” in the AST builder. This means `IF RSI(MSFT,14)` is treated as `RSI(MSFT,14) != 0`.  
  Source: `src/main/scala/ast/SophieAstBuilder.scala`
- **Quantity vs. value:** `QTY n` represents an explicit quantity. The AST builder normalizes this to a `Value` where `currency == symbol`, so downstream logic can interpret it as a direct quantity and skip FX conversion.  
  Sources: `src/main/antlr4/sophie.g4`, `src/main/scala/ast/SophieAstBuilder.scala`
- **Indicator overrides:** market data supports explicit indicator overrides (useful for tests and demos), which are used before computing indicators from series.  
  Source: `src/main/scala/engine/MarketData.scala`

### 2.6 Functional evaluation model
The language is interpreted by transforming syntax into a typed AST and then evaluating it into a pure execution plan (no side effects during evaluation).  
Source: `docs/language_overview.txt`

### 2.7 Persistence formats (execution semantics)

While not part of the grammar, the language’s execution semantics are tied to file formats:
- **Portfolio** state is persisted as JSON (`positions` map, optional `cash`) using `PortfolioJ`.
- **Ledger** is persisted as NDJSON (one event per line).
This impacts how executions are replayed or audited.
Sources:
- `docs/storage_and_persistence.md`
- `src/main/scala/engine/Ledger.scala`
