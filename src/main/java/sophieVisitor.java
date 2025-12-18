// Generated from C:/Progetti/Sophie/src/main/antlr4/sophie.g4 by ANTLR 4.13.2

  package parser;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link sophieParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface sophieVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link sophieParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(sophieParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(sophieParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#trade_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrade_cmd(sophieParser.Trade_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#portfolio_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPortfolio_cmd(sophieParser.Portfolio_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#allocation_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllocation_list(sophieParser.Allocation_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#allocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllocation(sophieParser.AllocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(sophieParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#consideration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConsideration(sophieParser.ConsiderationContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#quantity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuantity(sophieParser.QuantityContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition(sophieParser.ConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#disjunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDisjunction(sophieParser.DisjunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#conjunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConjunction(sophieParser.ConjunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison(sophieParser.ComparisonContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(sophieParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(sophieParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(sophieParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#series_operation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeries_operation(sophieParser.Series_operationContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#series_field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeries_field(sophieParser.Series_fieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#agg_func}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAgg_func(sophieParser.Agg_funcContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#price_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrice_expr(sophieParser.Price_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link sophieParser#symbol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSymbol(sophieParser.SymbolContext ctx);
}