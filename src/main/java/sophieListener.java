// Generated from C:/Progetti/Sophie/src/main/antlr4/sophie.g4 by ANTLR 4.13.2

  package parser;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link sophieParser}.
 */
public interface sophieListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link sophieParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(sophieParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(sophieParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(sophieParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(sophieParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#trade_cmd}.
	 * @param ctx the parse tree
	 */
	void enterTrade_cmd(sophieParser.Trade_cmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#trade_cmd}.
	 * @param ctx the parse tree
	 */
	void exitTrade_cmd(sophieParser.Trade_cmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#portfolio_cmd}.
	 * @param ctx the parse tree
	 */
	void enterPortfolio_cmd(sophieParser.Portfolio_cmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#portfolio_cmd}.
	 * @param ctx the parse tree
	 */
	void exitPortfolio_cmd(sophieParser.Portfolio_cmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#allocation_list}.
	 * @param ctx the parse tree
	 */
	void enterAllocation_list(sophieParser.Allocation_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#allocation_list}.
	 * @param ctx the parse tree
	 */
	void exitAllocation_list(sophieParser.Allocation_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#allocation}.
	 * @param ctx the parse tree
	 */
	void enterAllocation(sophieParser.AllocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#allocation}.
	 * @param ctx the parse tree
	 */
	void exitAllocation(sophieParser.AllocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(sophieParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(sophieParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#consideration}.
	 * @param ctx the parse tree
	 */
	void enterConsideration(sophieParser.ConsiderationContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#consideration}.
	 * @param ctx the parse tree
	 */
	void exitConsideration(sophieParser.ConsiderationContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#quantity}.
	 * @param ctx the parse tree
	 */
	void enterQuantity(sophieParser.QuantityContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#quantity}.
	 * @param ctx the parse tree
	 */
	void exitQuantity(sophieParser.QuantityContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(sophieParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(sophieParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#disjunction}.
	 * @param ctx the parse tree
	 */
	void enterDisjunction(sophieParser.DisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#disjunction}.
	 * @param ctx the parse tree
	 */
	void exitDisjunction(sophieParser.DisjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#conjunction}.
	 * @param ctx the parse tree
	 */
	void enterConjunction(sophieParser.ConjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#conjunction}.
	 * @param ctx the parse tree
	 */
	void exitConjunction(sophieParser.ConjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(sophieParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(sophieParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(sophieParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(sophieParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(sophieParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(sophieParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(sophieParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(sophieParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#series_operation}.
	 * @param ctx the parse tree
	 */
	void enterSeries_operation(sophieParser.Series_operationContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#series_operation}.
	 * @param ctx the parse tree
	 */
	void exitSeries_operation(sophieParser.Series_operationContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#series_field}.
	 * @param ctx the parse tree
	 */
	void enterSeries_field(sophieParser.Series_fieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#series_field}.
	 * @param ctx the parse tree
	 */
	void exitSeries_field(sophieParser.Series_fieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#agg_func}.
	 * @param ctx the parse tree
	 */
	void enterAgg_func(sophieParser.Agg_funcContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#agg_func}.
	 * @param ctx the parse tree
	 */
	void exitAgg_func(sophieParser.Agg_funcContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#price_expr}.
	 * @param ctx the parse tree
	 */
	void enterPrice_expr(sophieParser.Price_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#price_expr}.
	 * @param ctx the parse tree
	 */
	void exitPrice_expr(sophieParser.Price_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link sophieParser#symbol}.
	 * @param ctx the parse tree
	 */
	void enterSymbol(sophieParser.SymbolContext ctx);
	/**
	 * Exit a parse tree produced by {@link sophieParser#symbol}.
	 * @param ctx the parse tree
	 */
	void exitSymbol(sophieParser.SymbolContext ctx);
}