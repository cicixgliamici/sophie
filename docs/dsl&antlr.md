# Why a DSL: simplicity, specificity, and value for non-programmer users

## 1) Why a DSL (Domain-Specific Language)

A DSL is a language designed for a **specific purpose**. Unlike general-purpose
languages, a DSL:

* **Reduces noise**: it removes concepts that are not needed in the domain.
* **Improves readability**: the concepts expressed are those of the problem,
  not those of the machine.
* **Reduces errors**: fewer constructs mean fewer chances for wrong or ambiguous
  combinations.
* **Facilitates communication**: a shared language makes it clearer *what you
  want to do* without getting into technical implementation details.

In our case, Sophie’s syntax is designed around **trading decisions**,
indicators, and portfolio management—i.e., the concepts users expect to
manipulate.

---

## 2) Why keep it simple and specific

An effective DSL does **not** try to become a full programming language.
Simplicity is an advantage:

* **Ease of learning**: only a few rules to remember.
* **Less ambiguity**: a more linear syntax ⇒ clearer interpretation.
* **Stronger validations**: a domain-specific DSL enables targeted checks
  (e.g., “does this operation make sense in the domain?”).
* **Controlled evolution**: new features can be added without breaking the
  user’s mental model.

Specificity also helps define **useful constraints**: for example, Sophie has no
loops or unnecessary imperative structures, because the domain is centered on
*rules* and *execution plans*, not generic algorithms.

---

## 3) Usefulness for technical users who are not CS experts

Many “technical” users (finance, operations, analytics) are not professional
programmers, but they are **domain experts**. A DSL helps them because it:

* **Lets them express ideas using the domain language**
  (e.g., “BUY 1500 EUR OF MSFT IF RSI(MSFT, 14) < 30”).
* **Reduces the need to know frameworks or complex languages**.
* **Provides transparency**: behavior is readable as a rule,
  not as a program.
* **Encourages collaboration**: an analyst can read and understand the rule
  without knowing implementation details.

In practice, a DSL “lowers the barrier to entry” without losing precision.

---

## 4) What ANTLR does

ANTLR is a parser generator that starts from a **formal grammar**
(a `.g4` file) and produces:

1. **Lexer**: recognizes tokens (keywords, numbers, symbols).
2. **Parser**: uses those tokens to build a syntax tree that conforms to the
   grammar.

Practical advantages:

* The grammar is **declarative and readable**.
* The generated parser is **reliable** and **well-tested**.
* We can evolve the language by modifying a single definition (the grammar)
  without rewriting parsing logic “by hand”.

ANTLR therefore turns a formal language definition into concrete code to read
and validate programs.

### 4.1) What happens when you run ANTLR (high-level)

At build time, the ANTLR tool reads your `.g4` grammar and performs (roughly)
these steps:

* **Grammar analysis**: it checks the grammar for inconsistencies and builds an
  internal automaton representation of the rules.
* **Code generation**: it emits source files for the chosen *target language*
  (e.g., Java), typically including:

  * `*Lexer` (tokenizer)
  * `*Parser` (recognizes rule structure)
  * `*Listener` + `*BaseListener` (callbacks)
  * `*Visitor` + `*BaseVisitor` (tree traversal returning values)
* **Runtime dependency**: the generated code relies on the ANTLR runtime
  library (e.g., `org.antlr.v4.runtime.*`) for shared infrastructure such as
  `Token`, `TokenStream`, error handling, and parse-tree node types.

At run time, the pipeline is conceptually:

1. **Char stream → Lexer → Tokens**

   * Lexer rules define regular-language patterns (similar to regexes) that
     recognize keywords, identifiers, numbers, operators, etc.
   * Tokens are produced as a stream. Whitespace/comments are often sent to a
     hidden channel or skipped, so they do not affect parsing.

2. **Tokens → Parser → Parse tree**

   * Parser rules define how tokens can be combined into valid constructs.
   * The parser consumes the token stream and builds a **parse tree** made of
     `ParserRuleContext` nodes that mirror the grammar structure.

3. **Parse tree → Listener/Visitor → Your AST / semantic model**

   * A *listener* is event-driven (enter/exit rule callbacks).
   * A *visitor* is more functional: you explicitly return values from each
     visit method (often convenient for building an AST).

### 4.2) Parsing strategy: why ANTLR is efficient in practice

ANTLR 4 uses an adaptive prediction strategy (often described as *LL(*)*) that
can handle many real-world grammars without manual lookahead tuning. In simple
terms, it tries to decide *which alternative to take* by looking ahead in the
token stream as needed, caching decisions to make repeated parses faster.

This is why you typically get a robust parser without having to implement your
own parsing algorithms.

### 4.3) Why ANTLR generates Java code (and what “target” means)

ANTLR can generate parsers for multiple target languages (Java is the default
and most mature target). It generates Java code when the grammar is compiled
with **Java as the selected target**.

Reasons a project may choose Java output include:

* **JVM ecosystem**: easy integration with Java/Scala/Kotlin projects.
* **Strong runtime support**: ANTLR’s Java runtime is widely used and well
  supported.
* **Tooling**: build plugins (Gradle/Maven), IDE support, debugging, and
  dependency management are straightforward.

If you wanted a different target, you would compile the grammar with another
language target (via CLI flags or build plugins) and include the corresponding
runtime library.

---

## 5) Why we build an AST on top of the generated parser/lexer

The parser generated by ANTLR produces a **parse tree**, which mirrors the
grammar but is not ideal for the rest of the system.

From an engineering point of view, the parse tree is *syntax-oriented*: it is
excellent for representing what the input looked like according to the grammar,
but it often contains structure that is irrelevant once you move to evaluation
and execution.

An **AST (Abstract Syntax Tree)** is instead *meaning-oriented*: it keeps only
what your compiler/interpreter actually needs.
An **AST (Abstract Syntax
Tree)**:

* **Removes unnecessary syntactic details** (parentheses, separator tokens,
  etc.).
* **Represents meaning**, not just grammatical structure.
* **Simplifies later phases**: evaluation, optimization, lowering.
* **Makes the code more stable**: we can change the grammar without rewriting
  all evaluation logic, because the AST remains our internal “contract”.

In short: **ANTLR gives us structure**, the AST gives us **semantics**.

---

## 6) In summary

* A DSL is useful because it speaks the domain language and reduces noise.
* Simplicity is a strategic choice: it improves understanding and reliability.
* Non-programmer technical users can use a DSL easily.
* ANTLR automates lexing and parsing in a robust way.
* The AST isolates semantics and makes the system extensible and maintainable.
