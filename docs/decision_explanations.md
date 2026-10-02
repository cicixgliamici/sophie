# Decision explanations

Sophie records why each trade condition is true or false as it evaluates the
program. The explanation is a snapshot of that evaluation, including the values
actually read. Printing or exporting it does not consult market data again.

## Try the example

```bash
sbt "runMain cli.SophieCli --file examples/explain_decisions.sophie --md examples/explain-market-data.json --explain --explain-json tmp/decisions.json"
```

These flags inspect the program without executing it. Add `--run` and explicit
account funding if execution is wanted. The example has four trades: two selected,
one rejected after evaluating both sides of an OR, and one rejected by an AND
short-circuit. The fourth trade demonstrates an OR short-circuit.

An excerpt from the first decision:

```text
RSI(MSFT, 14) < 30 => true
  RSI(MSFT, 14) = 28 [indicator override]
  30 = 30
```

A skipped branch in the third decision:

```text
PRICE(UNKNOWN) > 0 => NOT EVALUATED (AND short-circuit: left condition is false)
```

`UNKNOWN` has no quote in this example. Explaining the skipped branch does not
attempt a lookup or invent a value for it.

In the TUI, evaluate a program, then use:

```text
:explain
:explain 1
```

Trade numbers start at one and follow the order of trade statements. Portfolio
declarations do not occupy trade numbers. Explaining leaves the current account,
last plan and any active paste buffer unchanged. Changing quotes after evaluation
does not change the stored explanation; evaluate again to get a new decision.

## Trace model

`TradeDecision.explanation` contains a `ConditionTrace`:

- `expression`: a canonical expression that preserves grouping. This is rendered
  from the AST, rather than an exact copy of the original whitespace or comments.
- `result`: `Some(true)` or `Some(false)` for evaluated conditions; `None` for a
  skipped condition.
- `operands`: evaluated values and their provenance, with child operands for
  arithmetic expressions.
- `children`: evaluated logical structure, including an explicit skipped right
  branch when AND or OR short-circuits. Parentheses retain their child condition.
- `skippedReason`: why the branch was not evaluated. A skipped subtree is recorded
  as one canonical expression, with no operand values or evaluated children.

Operand sources distinguish literals, market prices, the latest series value,
indicator overrides, indicators computed from close data, and arithmetic results.
A trade without IF has an explicit unconditional true trace. Truthy expressions
use the AST's existing comparison against zero.

`DecisionReport.from(plan)` produces a versioned report (`schemaVersion = 1`)
with every trade, including trades whose conditions are false. `conditionMet`
describes condition selection; it does not promise an order will be filled.
Cash, holdings, currency and execution-price checks still run when applying a plan.

JSON uses the project's uPickle codecs. Decimal trace values are strings to retain
precision. Optional values use uPickle's option representation: a singleton array
for `Some`, an empty array for `None`. For example, an evaluated result is
`"result": [false]`, while a skipped result is `"result": []`.

## Persistence and compatibility

Lowering copies the recorded explanation into each executable instruction.
The executor then copies it into the ledger event. This preserves the original
condition explanation even when the program or quotes subsequently change.
Skipped trades appear in the decision report, not in the execution ledger.

Older instructions and events without an explanation remain readable. Manually
constructed decisions may also omit the field; the printer reports that no
explanation was recorded instead of generating one later.

Runtime evaluation errors retain the existing failure behavior: an evaluated
missing quote or division by zero raises an error, and no complete decision report
is produced. Short-circuiting avoids runtime reads only; static validation still
checks the whole program before evaluation.
