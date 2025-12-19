package ast

/**
  * SophieAstBuilder
  * ----------------
  * This object acts as a bridge between the ANTLR-generated parse tree (from sophieParser)
  * and the hand-crafted AST (Abstract Syntax Tree) used by the rest of the application.
  *
  * Purpose:
  *   - Converts the parse tree nodes (which closely follow the grammar) into a more convenient,
  *     semantically meaningful AST representation.
  *   - Decouples the rest of the software from the details of the parser and grammar.
  *   - Centralizes the logic for interpreting grammar constructs and mapping them to domain objects.
  *
  * Structure:
  *   - Provides a set of 'fromX' methods, each converting a specific parse tree context into an AST node.
  *   - Handles all grammar constructs: statements, expressions, conditions, arithmetic, etc.
  *   - Contains helper methods for extracting tokens and handling operator precedence.
  *
  * Why a separate layer?
  *   - The parse tree is tightly coupled to the grammar and not always convenient for further processing.
  *   - The AST is designed for semantic analysis, transformations, and code generation.
  *   - This separation improves maintainability and testability.
  */

import parser.{sophieLexer, sophieParser}
// Import only the required contexts to avoid name clashes with token constants (e.g., NEQ, EQ).
import parser.sophieParser.{
  ProgramContext,
  StatementContext,
  Trade_cmdContext,
  Portfolio_cmdContext,
  Allocation_listContext,
  AllocationContext,
  ConditionContext,
  DisjunctionContext,
  ConjunctionContext,
  ComparisonContext,
  ExprContext,
  TermContext,
  PrimaryContext,
  Price_exprContext,
  Series_operationContext
}

import org.antlr.v4.runtime.tree.TerminalNode
import scala.jdk.CollectionConverters._

// Explicit aliasing: use AST operators, not ANTLR token constants.
import ast.{GT => CmpGT, LT => CmpLT, EQ => CmpEQ, NEQ => CmpNEQ}

object SophieAstBuilder {
  // ---------- Entry ----------
  /**
    * Converts the root ProgramContext into an AST Program node.
    * Maps each statement in the parse tree to an AST statement.
    */
  def fromProgram(ctx: ProgramContext): Program = {
    val statements = ctx.statement().asScala.toList.map(fromStatement)
    Program(statements)
  }

  /**
    * Converts a StatementContext into the corresponding AST Statement.
    * Dispatches to the appropriate handler based on the statement type.
    */
  private def fromStatement(ctx: StatementContext): Statement = {
    Option(ctx.trade_cmd()).map(fromTrade)
      .orElse(Option(ctx.portfolio_cmd()).map(fromPortfolio))
      .getOrElse(throw new IllegalArgumentException("Unknown statement type"))
  }

  // ---------- Trade ----------
  /**
    * Converts a Trade_cmdContext into a TradeCmd AST node.
    * Handles BUY/SELL actions, value, symbol, and the required condition.
    */
  private def fromTrade(ctx: Trade_cmdContext): TradeCmd = {
    val action = Option(ctx.BUY()).map(_ => Buy)
      .orElse(Option(ctx.SELL()).map(_ => Sell))
      .getOrElse(throw new IllegalArgumentException("Missing BUY/SELL"))

    // The grammar now wraps value/quantity into a `consideration` rule. Use that.
    // Explanation (educational): the parser does not expose a single `value`
    // token on the `trade_cmd` anymore — instead the grammar introduces a
    // `consideration` non-terminal which can be either a `value` (NUMBER+CURRENCY)
    // or a `quantity` (QTY NUMBER). This gives the language flexibility.
    //
    // We convert both shapes into the same AST representation (`Value`) so the
    // downstream logic only needs to deal with a single concept: a numeric
    // amount and a currency. When `quantity` is provided (QTY N), we treat the
    // currency as the symbol being traded (so that PortfolioManager.computeQuantity
    // can recognize it as a direct quantity and avoid market-data conversion).
    //
    // Functional vs Imperative note:
    //  - Functional: we explicitly pattern-match on the parse tree and build
    //    a pure AST node. No side effects, no mutable state. The transformation
    //    is deterministic and easy to test: given the same parse tree we get the
    //    same `TradeCmd` object.
    //  - Imperative: one might parse tokens while mutating a shared structure or
    //    using indexes into token arrays; error handling and branching tend to
    //    interleave with parsing logic, making unit testing harder.
    val cons = ctx.consideration()
    val sym = symbolText(ctx.symbol())

    val v = Option(cons.value()).map(fromValue)
      .orElse(Option(cons.quantity()).map { q =>
        // Quantity is specified as `QTY <NUMBER>`; treat it as a Value with currency == symbol
        val num = firstTokenOf(q, sophieParser.NUMBER)
        Value(BigDecimal(num), sym)
      })
      .getOrElse(throw new IllegalArgumentException("Missing consideration (value or quantity)"))

    val cond = Option(ctx.condition()).map(fromCondition).getOrElse(AlwaysTrue)

    TradeCmd(action = action, value = v, symbol = sym, condition = cond)
  }

  // ---------- Portfolio ----------
  /**
    * Converts a Portfolio_cmdContext into a PortfolioCmd AST node.
    * Expects an allocation list and maps each allocation.
    */
  private def fromPortfolio(ctx: Portfolio_cmdContext): PortfolioCmd = {
    Option(ctx.allocation_list()).map { list =>
      val allocs = list.allocation().asScala.toList.map(fromAllocation)
      PortfolioCmd(allocs)
    }.getOrElse(throw new IllegalArgumentException("PORTFOLIO must have an allocation list"))
  }

  /**
    * Converts an AllocationContext into an Allocation AST node.
    */
  private def fromAllocation(ctx: AllocationContext): Allocation = {
    val v = fromValue(ctx.value())
    val sym = symbolText(ctx.symbol())
    Allocation(v, sym)
  }

  // ---------- Leaf: Value & Symbol ----------
  /**
    * Converts a ValueContext into a Value AST node.
    * Extracts the number and currency tokens.
    */
  private def fromValue(ctx: sophieParser.ValueContext): Value = {
    val numberTok = firstTokenOf(ctx, sophieParser.NUMBER)
    val currTok   = firstTokenOf(ctx, sophieParser.CURRENCY)
    Value(BigDecimal(numberTok), currTok)
  }

  /**
    * Extracts the symbol text from a SymbolContext.
    */
  private def symbolText(ctx: sophieParser.SymbolContext): String = ctx.getText

  // ---------- Conditions ----------
  /**
    * Converts a ConditionContext into an AST Condition.
    * Handles logical OR (disjunction) at the top level.
    */
  private def fromCondition(ctx: ConditionContext): Condition = fromDisjunction(ctx.disjunction())

  /**
    * Handles disjunctions (OR chains) in conditions.
    */
  private def fromDisjunction(ctx: DisjunctionContext): Condition = {
    val parts = ctx.conjunction().asScala.toList.map(fromConjunction)
    reduceLeft(parts, Or.apply)
  }

  /**
    * Handles conjunctions (AND chains) and parenthesized conditions.
    */
  private def fromConjunction(ctx: ConjunctionContext): Condition = {
    val left: Condition = Option(ctx.comparison()).map(fromComparison)
      .orElse(Option(ctx.condition()).map(c => Parens(fromCondition(c))))
      .getOrElse(throw new IllegalArgumentException("Invalid conjunction"))

    if (ctx.AND() != null && ctx.conjunction() != null) And(left, fromConjunction(ctx.conjunction()))
    else left
  }

  /**
    * Converts a ComparisonContext into a Comparison AST node.
    * Handles both binary and unary (truthy) comparisons.
    */
  private def fromComparison(ctx: ComparisonContext): Condition = {
    val exprs = ctx.expr().asScala.toList
    exprs match {
      case List(single) => Comparison(fromExpr(single), CmpNEQ, NumberLiteral(0))
      case List(l, r) =>
        // Binary comparison: extract operator and operands
        val opTok = firstOpToken(ctx, Set(sophieParser.GT, sophieParser.LT, sophieParser.EQ, sophieParser.NEQ))
        val op: CompOp = opTok.getType match {
          case sophieParser.GT  => CmpGT
          case sophieParser.LT  => CmpLT
          case sophieParser.EQ  => CmpEQ
          case sophieParser.NEQ => CmpNEQ
          case other            => throw new IllegalArgumentException(s"Unexpected comparison op token: $other")
        }
        Comparison(fromExpr(l), op, fromExpr(r))
      case _ => throw new IllegalArgumentException("comparison must have one or two expr children")
    }
  }

  // ---------- Arithmetic ----------
  /**
    * Converts an ExprContext into an Operand AST node.
    * Handles addition and subtraction, respecting left associativity.
    */
  private def fromExpr(ctx: ExprContext): Operand = {
    val terms = ctx.term().asScala.toList.map(fromTerm)
    if (terms.isEmpty) throw new IllegalArgumentException("Empty expr")
    if (terms.size == 1) terms.head
    else {
      // Collects all '+' and '-' operators between terms
      val ops: List[ArithOp] =
        (1 until ctx.getChildCount by 2).toList.map(i => ctx.getChild(i).getText match {
          case "+" => Add
          case "-" => Sub
          case s   => throw new IllegalArgumentException(s"Unexpected +|- token: $s")
        })
      foldLeftArith(terms.head, ops.zip(terms.tail))
    }
  }

  /**
    * Converts a TermContext into an Operand AST node.
    * Handles multiplication and division, respecting left associativity.
    */
  private def fromTerm(ctx: TermContext): Operand = {
    val prims = ctx.primary().asScala.toList.map(fromPrimary)
    if (prims.isEmpty) throw new IllegalArgumentException("Empty term")
    if (prims.size == 1) prims.head
    else {
      // Collects all '*' and '/' operators between primaries
      val ops: List[ArithOp] =
        (1 until ctx.getChildCount by 2).toList.map(i => ctx.getChild(i).getText match {
          case "*" => Mul
          case "/" => Div
          case s   => throw new IllegalArgumentException(s"Unexpected *|/ token: $s")
        })
      foldLeftArith(prims.head, ops.zip(prims.tail))
    }
  }

  /**
    * Converts a PrimaryContext into an Operand AST node.
    * Handles price expressions, series operations, parenthesized expressions, and number literals.
    */
  private def fromPrimary(ctx: PrimaryContext): Operand = {
    Option(ctx.price_expr()).map(fromPrice)
      .orElse(Option(ctx.series_operation()).map(fromSeriesOp))
      .orElse(Option(ctx.expr()).map(fromExpr))
      .getOrElse {
        // Fallback: must be a number literal
        val t = firstTokenNode(ctx, sophieParser.NUMBER)
        NumberLiteral(BigDecimal(t.getText))
      }
  }

  // ---------- Series & PRICE ----------
  /**
    * Converts a Series_operationContext into an Operand AST node.
    * Handles both field access (e.g., symbol.field) and aggregation functions (e.g., SMA(symbol, n)).
    */
  private def fromSeriesOp(ctx: Series_operationContext): Operand = {
    Option(ctx.series_field()).map(_ => {
      val sym = symbolText(ctx.symbol())
      val field = ctx.series_field().getText
      SeriesOperation(sym, field)
    }).getOrElse {
      val name = ctx.agg_func().getText
      val sym  = symbolText(ctx.symbol())
      val per  = BigDecimal(firstTokenOf(ctx, sophieParser.NUMBER))
      AggFunc(name, sym, per)
    }
  }

  /**
    * Converts a Price_exprContext into a Price AST node.
    */
  private def fromPrice(ctx: Price_exprContext): Operand = Price(symbolText(ctx.symbol()))

  // ---------- Helpers ----------
  /**
    * Folds a list of (operator, operand) pairs into a left-associative binary tree.
    * Used for arithmetic expressions.
    */
  private def foldLeftArith(head: Operand, tail: List[(ArithOp, Operand)]): Operand = tail.foldLeft(head) { case (acc, (op, right)) => Binary(op, acc, right) }

  /**
    * Reduces a list of conditions using the provided constructor (e.g., Or, And).
    * Throws if the list is empty.
    */
  private def reduceLeft(parts: List[Condition], ctor: (Condition, Condition) => Condition): Condition = parts.reduceLeftOption(ctor).getOrElse(throw new IllegalArgumentException("Empty boolean chain"))

  /**
    * Finds the first TerminalNode of the given token type in the context's children.
    * Throws if not found.
    */
  private def firstTokenNode(ctx: org.antlr.v4.runtime.RuleContext, tokenType: Int): TerminalNode = {
    (0 until ctx.getChildCount).iterator
      .map(ctx.getChild)
      .collect { case t: TerminalNode if t.getSymbol.getType == tokenType => t }
      .to(LazyList)
      .headOption
      .getOrElse(throw new IllegalArgumentException(s"Token not found in context: $tokenType"))
  }

  /**
    * Returns the text of the first token of the given type in the context.
    */
  private def firstTokenOf(ctx: org.antlr.v4.runtime.RuleContext, tokenType: Int): String = firstTokenNode(ctx, tokenType).getText

  /**
    * Finds the first operator token (from the allowed set) in the context's children.
    * Used for extracting comparison operators.
    */
  private def firstOpToken(ctx: org.antlr.v4.runtime.RuleContext, allowed: Set[Int]) = {
    (0 until ctx.getChildCount).iterator
      .map(ctx.getChild)
      .collect { case t: TerminalNode if allowed.contains(t.getSymbol.getType) => t.getSymbol }
      .to(LazyList)
      .headOption
      .getOrElse(throw new IllegalArgumentException("Comparison operator not found"))
  }
}