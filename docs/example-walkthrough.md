# Example Walkthrough — Sophie

This document gives a compact end-to-end walkthrough of a small Sophie program, showing how a source file moves through the repository pipeline from parsing to execution-oriented output.

---

## Example program

```text
BUY 1500 EUR OF MSFT IF PRICE(MSFT) < 420
SELL QTY 0.25 OF BTC IF RSI(BTC, 14) > 70
PORTFOLIO 6000 EUR OF VWCE + 2000 USD OF AAPL + 0.1 BTC OF BTC
````

This program contains three useful elements for understanding the language:

* a conditional buy order,
* a conditional sell order,
* and a portfolio-allocation target.

---

## Step 1 — Parsing

The source file is first processed by the ANTLR grammar.

At this stage, the repository turns the textual DSL into a parse tree that captures the syntactic structure of:

* commands such as `BUY`, `SELL`, and `PORTFOLIO`
* symbols such as `MSFT`, `BTC`, and `VWCE`
* conditions such as `PRICE(MSFT) < 420`
* indicator expressions such as `RSI(BTC, 14) > 70`

This stage answers the question:

> “Is the program syntactically valid Sophie code?”

---

## Step 2 — AST construction

The parse tree is then transformed into a typed AST.

This is an important architectural step because it moves the program from a grammar-shaped representation to a domain-shaped representation.

Conceptually, the example becomes something like:

* a `Buy` command with amount, currency, symbol, and condition
* a `Sell` command with explicit quantity and condition
* a `Portfolio` statement containing multiple allocation targets

The AST stage is what makes the later evaluator logic clean and explicit.

---

## Step 3 — Evaluation against market data

The evaluator runs the AST against market data.

Suppose the relevant market inputs are:

* `PRICE(MSFT) = 410`
* `RSI(BTC, 14) = 74`

Then:

* `PRICE(MSFT) < 420` is **true**
* `RSI(BTC, 14) > 70` is **true**

So both conditional trade statements are eligible to produce actions.

The portfolio statement is interpreted as a target allocation request rather than a guard-based trade.

---

## Step 4 — Execution plan

Once conditions are evaluated, the repository can produce a higher-level execution plan.

Conceptually, that plan may contain instructions such as:

* buy 1500 EUR of MSFT
* sell 0.25 BTC of BTC
* target a portfolio containing:

  * 6000 EUR of VWCE
  * 2000 USD of AAPL
  * 0.1 BTC of BTC

This stage is useful because it separates:

* **what the program means**
  from
* **how the repository later chooses to encode or persist that meaning**

---

## Step 5 — Lowering

The execution plan is then lowered into a more concrete instruction-oriented representation.

This step makes the output easier to serialize, inspect, and persist.

In other words, lowering bridges the gap between:

* a language-level intent
  and
* an execution-ready artifact

This is one of the clearest places where Sophie shows compiler/interpreter-style design structure.

---

## Step 6 — Execution and persistence

If the run mode requests execution, the lowered instructions can then be applied to persistent state such as:

* a portfolio file
* a ledger file
* optional textual receipts

This is where the repository’s architecture keeps side effects separate from the earlier logic-heavy phases.

That separation is important because it makes the project easier to:

* reason about
* test
* and extend

---

## What this example demonstrates

This single example is useful because it shows, in a compact way:

* DSL syntax design
* conditional command semantics
* indicator-based evaluation
* typed AST modeling
* multi-stage pipeline architecture
* separation between evaluation and execution

---

## Recommended reviewer path

If you want to connect this walkthrough to the codebase:

1. Read the example program in `examples/overview-example.sophie`
2. Inspect the grammar in `src/main/antlr4/sophie.g4`
3. Inspect the AST model in `src/main/scala/ast`
4. Inspect the evaluator and execution pipeline in `src/main/scala/engine`
5. Run the CLI or tests to compare the conceptual flow with the implementation
