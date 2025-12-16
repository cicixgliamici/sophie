/**
 * Abstract Syntax Tree (AST) definitions for the Sophie language.
 *
 * The AST is an intermediate representation of parsed code, designed to decouple
 * the grammar (parser) from the rest of the software. This layer allows the parser
 * to convert raw syntax into a structured, strongly-typed tree of objects, which
 * can then be analyzed, transformed, or executed by other components of the system.
 *
 * Structure:
 * - The root node is `Program`, containing a list of `Statement`s.
 * - Statements represent top-level commands, such as trades or portfolio assignments.
 * - Expressions, conditions, and operands are modeled as case classes and sealed traits,
 *   capturing the semantics of the language in a type-safe way.
 *
 * Why use an AST layer?
 * - It separates parsing concerns from business logic, making the codebase more modular.
 * - It enables easier testing, analysis, and transformation of code.
 * - It provides a clear contract between parsing and interpretation/execution.
 */
package ast

// Base trait for all AST nodes
sealed trait AST

// Program
final case class Program(statements: List[Statement]) extends AST

// Statements
sealed trait Statement extends AST
final case class TradeCmd(
                           action: TradeAction,
                           value: Value,
                           symbol: String,
                           condition: Condition
                         ) extends Statement

final case class PortfolioCmd(
                               allocations: List[Allocation]
                             ) extends Statement

// Actions
sealed trait TradeAction
case object Buy  extends TradeAction
case object Sell extends TradeAction

// Money
final case class Value(amount: BigDecimal, currency: String)
final case class Allocation(value: Value, symbol: String)

// Conditions
sealed trait Condition extends AST
final case class Comparison(left: Operand, op: CompOp, right: Operand) extends Condition
final case class And(left: Condition, right: Condition) extends Condition
final case class Or(left: Condition, right: Condition) extends Condition
final case class Parens(cond: Condition) extends Condition

// Operands
sealed trait Operand extends AST
final case class SeriesOperation(symbol: String, field: String) extends Operand
final case class AggFunc(name: String, symbol: String, period: BigDecimal) extends Operand
final case class Price(symbol: String) extends Operand
final case class NumberLiteral(value: BigDecimal) extends Operand

// Comparison ops
sealed trait CompOp
case object GT  extends CompOp
case object LT  extends CompOp
case object EQ  extends CompOp
case object NEQ extends CompOp

// Arithmetic ops
sealed trait ArithOp
case object Add extends ArithOp
case object Sub extends ArithOp
case object Mul extends ArithOp
case object Div extends ArithOp

// Arithmetic exp
final case class Binary(op: ArithOp, left: Operand, right: Operand) extends Operand

/**
 * Scala keywords and concepts used in this AST:
 *
 * - `sealed`: When a trait or class is marked as `sealed`, all its subclasses must be defined in the same file.
 *   This enables the compiler to know all possible subtypes, which is useful for exhaustive pattern matching.
 *
 * - `trait`: A trait is similar to an interface in other languages. It defines a set of methods and fields that
 *   can be mixed into classes. Traits can be extended by classes or other traits.
 *
 * - `final case class`: A `case class` is a special class in Scala that is immutable by default and supports
 *   pattern matching, structural equality, and convenient copy methods. Marking it as `final` prevents further
 *   subclassing, ensuring the class hierarchy remains closed and predictable.
 */
