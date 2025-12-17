// Generated from C:/Progetti/Sophie/src/main/antlr4/sophie.g4 by ANTLR 4.13.2

  package parser;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class sophieParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, WS=11, COMMENT=12, NUMBER=13, CURRENCY=14, BUY=15, SELL=16, OF=17, 
		IF=18, PORTFOLIO=19, IDENTIFIER=20, SEMICOLON=21, LPAR=22, RPAR=23, COMMA=24, 
		DOT=25, GT=26, LT=27, EQ=28, NEQ=29, AND=30, OR=31, PLUS=32, MINUS=33, 
		MUL=34, DIV=35;
	public static final int
		RULE_program = 0, RULE_statement = 1, RULE_trade_cmd = 2, RULE_portfolio_cmd = 3, 
		RULE_allocation_list = 4, RULE_allocation = 5, RULE_value = 6, RULE_condition = 7, 
		RULE_disjunction = 8, RULE_conjunction = 9, RULE_comparison = 10, RULE_expr = 11, 
		RULE_term = 12, RULE_primary = 13, RULE_series_operation = 14, RULE_series_field = 15, 
		RULE_agg_func = 16, RULE_price_expr = 17, RULE_symbol = 18;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "statement", "trade_cmd", "portfolio_cmd", "allocation_list", 
			"allocation", "value", "condition", "disjunction", "conjunction", "comparison", 
			"expr", "term", "primary", "series_operation", "series_field", "agg_func", 
			"price_expr", "symbol"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'open'", "'high'", "'low'", "'close'", "'volume'", "'MAVG'", "'EMA'", 
			"'RSI'", "'STDDEV'", "'PRICE'", null, null, null, null, "'BUY'", "'SELL'", 
			"'OF'", "'IF'", "'PORTFOLIO'", null, "';'", "'('", "')'", "','", "'.'", 
			"'>'", "'<'", "'='", "'!='", "'&&'", "'||'", "'+'", "'-'", "'*'", "'/'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, "WS", 
			"COMMENT", "NUMBER", "CURRENCY", "BUY", "SELL", "OF", "IF", "PORTFOLIO", 
			"IDENTIFIER", "SEMICOLON", "LPAR", "RPAR", "COMMA", "DOT", "GT", "LT", 
			"EQ", "NEQ", "AND", "OR", "PLUS", "MINUS", "MUL", "DIV"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "sophie.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public sophieParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(sophieParser.EOF, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public List<TerminalNode> SEMICOLON() { return getTokens(sophieParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(sophieParser.SEMICOLON, i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitProgram(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(42); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(38);
				statement();
				setState(40);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SEMICOLON) {
					{
					setState(39);
					match(SEMICOLON);
					}
				}

				}
				}
				setState(44); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 622592L) != 0) );
			setState(46);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public Trade_cmdContext trade_cmd() {
			return getRuleContext(Trade_cmdContext.class,0);
		}
		public Portfolio_cmdContext portfolio_cmd() {
			return getRuleContext(Portfolio_cmdContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			setState(50);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BUY:
			case SELL:
				enterOuterAlt(_localctx, 1);
				{
				setState(48);
				trade_cmd();
				}
				break;
			case PORTFOLIO:
				enterOuterAlt(_localctx, 2);
				{
				setState(49);
				portfolio_cmd();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Trade_cmdContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode OF() { return getToken(sophieParser.OF, 0); }
		public SymbolContext symbol() {
			return getRuleContext(SymbolContext.class,0);
		}
		public TerminalNode BUY() { return getToken(sophieParser.BUY, 0); }
		public TerminalNode SELL() { return getToken(sophieParser.SELL, 0); }
		public TerminalNode IF() { return getToken(sophieParser.IF, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public Trade_cmdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trade_cmd; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterTrade_cmd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitTrade_cmd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitTrade_cmd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Trade_cmdContext trade_cmd() throws RecognitionException {
		Trade_cmdContext _localctx = new Trade_cmdContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_trade_cmd);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(52);
			_la = _input.LA(1);
			if ( !(_la==BUY || _la==SELL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(53);
			value();
			setState(54);
			match(OF);
			setState(55);
			symbol();
			setState(58);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IF) {
				{
				setState(56);
				match(IF);
				setState(57);
				condition();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Portfolio_cmdContext extends ParserRuleContext {
		public TerminalNode PORTFOLIO() { return getToken(sophieParser.PORTFOLIO, 0); }
		public TerminalNode EQ() { return getToken(sophieParser.EQ, 0); }
		public Allocation_listContext allocation_list() {
			return getRuleContext(Allocation_listContext.class,0);
		}
		public Portfolio_cmdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_portfolio_cmd; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterPortfolio_cmd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitPortfolio_cmd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitPortfolio_cmd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Portfolio_cmdContext portfolio_cmd() throws RecognitionException {
		Portfolio_cmdContext _localctx = new Portfolio_cmdContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_portfolio_cmd);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			match(PORTFOLIO);
			setState(61);
			match(EQ);
			setState(62);
			allocation_list();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Allocation_listContext extends ParserRuleContext {
		public List<AllocationContext> allocation() {
			return getRuleContexts(AllocationContext.class);
		}
		public AllocationContext allocation(int i) {
			return getRuleContext(AllocationContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(sophieParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(sophieParser.PLUS, i);
		}
		public Allocation_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_allocation_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterAllocation_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitAllocation_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitAllocation_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Allocation_listContext allocation_list() throws RecognitionException {
		Allocation_listContext _localctx = new Allocation_listContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_allocation_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			allocation();
			setState(69);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS) {
				{
				{
				setState(65);
				match(PLUS);
				setState(66);
				allocation();
				}
				}
				setState(71);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AllocationContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode OF() { return getToken(sophieParser.OF, 0); }
		public SymbolContext symbol() {
			return getRuleContext(SymbolContext.class,0);
		}
		public AllocationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_allocation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterAllocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitAllocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitAllocation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AllocationContext allocation() throws RecognitionException {
		AllocationContext _localctx = new AllocationContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_allocation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72);
			value();
			setState(73);
			match(OF);
			setState(74);
			symbol();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValueContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(sophieParser.NUMBER, 0); }
		public TerminalNode CURRENCY() { return getToken(sophieParser.CURRENCY, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_value);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			match(NUMBER);
			setState(77);
			match(CURRENCY);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConditionContext extends ParserRuleContext {
		public DisjunctionContext disjunction() {
			return getRuleContext(DisjunctionContext.class,0);
		}
		public ConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitCondition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitCondition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConditionContext condition() throws RecognitionException {
		ConditionContext _localctx = new ConditionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			disjunction();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DisjunctionContext extends ParserRuleContext {
		public List<ConjunctionContext> conjunction() {
			return getRuleContexts(ConjunctionContext.class);
		}
		public ConjunctionContext conjunction(int i) {
			return getRuleContext(ConjunctionContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(sophieParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(sophieParser.OR, i);
		}
		public DisjunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_disjunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterDisjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitDisjunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitDisjunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DisjunctionContext disjunction() throws RecognitionException {
		DisjunctionContext _localctx = new DisjunctionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_disjunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81);
			conjunction();
			setState(86);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(82);
				match(OR);
				setState(83);
				conjunction();
				}
				}
				setState(88);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConjunctionContext extends ParserRuleContext {
		public ComparisonContext comparison() {
			return getRuleContext(ComparisonContext.class,0);
		}
		public TerminalNode LPAR() { return getToken(sophieParser.LPAR, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(sophieParser.RPAR, 0); }
		public TerminalNode AND() { return getToken(sophieParser.AND, 0); }
		public ConjunctionContext conjunction() {
			return getRuleContext(ConjunctionContext.class,0);
		}
		public ConjunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conjunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterConjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitConjunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitConjunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConjunctionContext conjunction() throws RecognitionException {
		ConjunctionContext _localctx = new ConjunctionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_conjunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(89);
				comparison();
				}
				break;
			case 2:
				{
				setState(90);
				match(LPAR);
				setState(91);
				condition();
				setState(92);
				match(RPAR);
				}
				break;
			}
			setState(98);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(96);
				match(AND);
				setState(97);
				conjunction();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode GT() { return getToken(sophieParser.GT, 0); }
		public TerminalNode LT() { return getToken(sophieParser.LT, 0); }
		public TerminalNode EQ() { return getToken(sophieParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(sophieParser.NEQ, 0); }
		public ComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterComparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitComparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitComparison(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonContext comparison() throws RecognitionException {
		ComparisonContext _localctx = new ComparisonContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_comparison);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			expr();
			setState(103);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1006632960L) != 0)) {
				{
				setState(101);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1006632960L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(102);
				expr();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprContext extends ParserRuleContext {
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(sophieParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(sophieParser.PLUS, i);
		}
		public List<TerminalNode> MINUS() { return getTokens(sophieParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(sophieParser.MINUS, i);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_expr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			term();
			setState(110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS || _la==MINUS) {
				{
				{
				setState(106);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(107);
				term();
				}
				}
				setState(112);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TermContext extends ParserRuleContext {
		public List<PrimaryContext> primary() {
			return getRuleContexts(PrimaryContext.class);
		}
		public PrimaryContext primary(int i) {
			return getRuleContext(PrimaryContext.class,i);
		}
		public List<TerminalNode> MUL() { return getTokens(sophieParser.MUL); }
		public TerminalNode MUL(int i) {
			return getToken(sophieParser.MUL, i);
		}
		public List<TerminalNode> DIV() { return getTokens(sophieParser.DIV); }
		public TerminalNode DIV(int i) {
			return getToken(sophieParser.DIV, i);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			primary();
			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MUL || _la==DIV) {
				{
				{
				setState(114);
				_la = _input.LA(1);
				if ( !(_la==MUL || _la==DIV) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(115);
				primary();
				}
				}
				setState(120);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(sophieParser.NUMBER, 0); }
		public Price_exprContext price_expr() {
			return getRuleContext(Price_exprContext.class,0);
		}
		public Series_operationContext series_operation() {
			return getRuleContext(Series_operationContext.class,0);
		}
		public TerminalNode LPAR() { return getToken(sophieParser.LPAR, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(sophieParser.RPAR, 0); }
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitPrimary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitPrimary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_primary);
		try {
			setState(128);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NUMBER:
				enterOuterAlt(_localctx, 1);
				{
				setState(121);
				match(NUMBER);
				}
				break;
			case T__9:
				enterOuterAlt(_localctx, 2);
				{
				setState(122);
				price_expr();
				}
				break;
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case CURRENCY:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 3);
				{
				setState(123);
				series_operation();
				}
				break;
			case LPAR:
				enterOuterAlt(_localctx, 4);
				{
				setState(124);
				match(LPAR);
				setState(125);
				expr();
				setState(126);
				match(RPAR);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Series_operationContext extends ParserRuleContext {
		public SymbolContext symbol() {
			return getRuleContext(SymbolContext.class,0);
		}
		public TerminalNode DOT() { return getToken(sophieParser.DOT, 0); }
		public Series_fieldContext series_field() {
			return getRuleContext(Series_fieldContext.class,0);
		}
		public Agg_funcContext agg_func() {
			return getRuleContext(Agg_funcContext.class,0);
		}
		public TerminalNode LPAR() { return getToken(sophieParser.LPAR, 0); }
		public TerminalNode COMMA() { return getToken(sophieParser.COMMA, 0); }
		public TerminalNode NUMBER() { return getToken(sophieParser.NUMBER, 0); }
		public TerminalNode RPAR() { return getToken(sophieParser.RPAR, 0); }
		public Series_operationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_series_operation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterSeries_operation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitSeries_operation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitSeries_operation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Series_operationContext series_operation() throws RecognitionException {
		Series_operationContext _localctx = new Series_operationContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_series_operation);
		try {
			setState(141);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CURRENCY:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(130);
				symbol();
				setState(131);
				match(DOT);
				setState(132);
				series_field();
				}
				break;
			case T__5:
			case T__6:
			case T__7:
			case T__8:
				enterOuterAlt(_localctx, 2);
				{
				setState(134);
				agg_func();
				setState(135);
				match(LPAR);
				setState(136);
				symbol();
				setState(137);
				match(COMMA);
				setState(138);
				match(NUMBER);
				setState(139);
				match(RPAR);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Series_fieldContext extends ParserRuleContext {
		public Series_fieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_series_field; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterSeries_field(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitSeries_field(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitSeries_field(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Series_fieldContext series_field() throws RecognitionException {
		Series_fieldContext _localctx = new Series_fieldContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_series_field);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 62L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Agg_funcContext extends ParserRuleContext {
		public Agg_funcContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_agg_func; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterAgg_func(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitAgg_func(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitAgg_func(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Agg_funcContext agg_func() throws RecognitionException {
		Agg_funcContext _localctx = new Agg_funcContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_agg_func);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(145);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 960L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Price_exprContext extends ParserRuleContext {
		public TerminalNode LPAR() { return getToken(sophieParser.LPAR, 0); }
		public SymbolContext symbol() {
			return getRuleContext(SymbolContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(sophieParser.RPAR, 0); }
		public Price_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_price_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterPrice_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitPrice_expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitPrice_expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Price_exprContext price_expr() throws RecognitionException {
		Price_exprContext _localctx = new Price_exprContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_price_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			match(T__9);
			setState(148);
			match(LPAR);
			setState(149);
			symbol();
			setState(150);
			match(RPAR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SymbolContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(sophieParser.IDENTIFIER, 0); }
		public TerminalNode CURRENCY() { return getToken(sophieParser.CURRENCY, 0); }
		public SymbolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_symbol; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterSymbol(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitSymbol(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitSymbol(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SymbolContext symbol() throws RecognitionException {
		SymbolContext _localctx = new SymbolContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_symbol);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(152);
			_la = _input.LA(1);
			if ( !(_la==CURRENCY || _la==IDENTIFIER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001#\u009b\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0001\u0000\u0001\u0000\u0003\u0000)\b\u0000\u0004\u0000+\b\u0000\u000b"+
		"\u0000\f\u0000,\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0003"+
		"\u00013\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0003\u0002;\b\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004D\b"+
		"\u0004\n\u0004\f\u0004G\t\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001"+
		"\b\u0001\b\u0001\b\u0005\bU\b\b\n\b\f\bX\t\b\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0003\t_\b\t\u0001\t\u0001\t\u0003\tc\b\t\u0001\n\u0001\n\u0001"+
		"\n\u0003\nh\b\n\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000bm\b\u000b"+
		"\n\u000b\f\u000bp\t\u000b\u0001\f\u0001\f\u0001\f\u0005\fu\b\f\n\f\f\f"+
		"x\t\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r"+
		"\u0081\b\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0003\u000e\u008e\b\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0000\u0000\u0013\u0000\u0002\u0004\u0006\b\n"+
		"\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$\u0000\u0007"+
		"\u0001\u0000\u000f\u0010\u0001\u0000\u001a\u001d\u0001\u0000 !\u0001\u0000"+
		"\"#\u0001\u0000\u0001\u0005\u0001\u0000\u0006\t\u0002\u0000\u000e\u000e"+
		"\u0014\u0014\u0096\u0000*\u0001\u0000\u0000\u0000\u00022\u0001\u0000\u0000"+
		"\u0000\u00044\u0001\u0000\u0000\u0000\u0006<\u0001\u0000\u0000\u0000\b"+
		"@\u0001\u0000\u0000\u0000\nH\u0001\u0000\u0000\u0000\fL\u0001\u0000\u0000"+
		"\u0000\u000eO\u0001\u0000\u0000\u0000\u0010Q\u0001\u0000\u0000\u0000\u0012"+
		"^\u0001\u0000\u0000\u0000\u0014d\u0001\u0000\u0000\u0000\u0016i\u0001"+
		"\u0000\u0000\u0000\u0018q\u0001\u0000\u0000\u0000\u001a\u0080\u0001\u0000"+
		"\u0000\u0000\u001c\u008d\u0001\u0000\u0000\u0000\u001e\u008f\u0001\u0000"+
		"\u0000\u0000 \u0091\u0001\u0000\u0000\u0000\"\u0093\u0001\u0000\u0000"+
		"\u0000$\u0098\u0001\u0000\u0000\u0000&(\u0003\u0002\u0001\u0000\')\u0005"+
		"\u0015\u0000\u0000(\'\u0001\u0000\u0000\u0000()\u0001\u0000\u0000\u0000"+
		")+\u0001\u0000\u0000\u0000*&\u0001\u0000\u0000\u0000+,\u0001\u0000\u0000"+
		"\u0000,*\u0001\u0000\u0000\u0000,-\u0001\u0000\u0000\u0000-.\u0001\u0000"+
		"\u0000\u0000./\u0005\u0000\u0000\u0001/\u0001\u0001\u0000\u0000\u0000"+
		"03\u0003\u0004\u0002\u000013\u0003\u0006\u0003\u000020\u0001\u0000\u0000"+
		"\u000021\u0001\u0000\u0000\u00003\u0003\u0001\u0000\u0000\u000045\u0007"+
		"\u0000\u0000\u000056\u0003\f\u0006\u000067\u0005\u0011\u0000\u00007:\u0003"+
		"$\u0012\u000089\u0005\u0012\u0000\u00009;\u0003\u000e\u0007\u0000:8\u0001"+
		"\u0000\u0000\u0000:;\u0001\u0000\u0000\u0000;\u0005\u0001\u0000\u0000"+
		"\u0000<=\u0005\u0013\u0000\u0000=>\u0005\u001c\u0000\u0000>?\u0003\b\u0004"+
		"\u0000?\u0007\u0001\u0000\u0000\u0000@E\u0003\n\u0005\u0000AB\u0005 \u0000"+
		"\u0000BD\u0003\n\u0005\u0000CA\u0001\u0000\u0000\u0000DG\u0001\u0000\u0000"+
		"\u0000EC\u0001\u0000\u0000\u0000EF\u0001\u0000\u0000\u0000F\t\u0001\u0000"+
		"\u0000\u0000GE\u0001\u0000\u0000\u0000HI\u0003\f\u0006\u0000IJ\u0005\u0011"+
		"\u0000\u0000JK\u0003$\u0012\u0000K\u000b\u0001\u0000\u0000\u0000LM\u0005"+
		"\r\u0000\u0000MN\u0005\u000e\u0000\u0000N\r\u0001\u0000\u0000\u0000OP"+
		"\u0003\u0010\b\u0000P\u000f\u0001\u0000\u0000\u0000QV\u0003\u0012\t\u0000"+
		"RS\u0005\u001f\u0000\u0000SU\u0003\u0012\t\u0000TR\u0001\u0000\u0000\u0000"+
		"UX\u0001\u0000\u0000\u0000VT\u0001\u0000\u0000\u0000VW\u0001\u0000\u0000"+
		"\u0000W\u0011\u0001\u0000\u0000\u0000XV\u0001\u0000\u0000\u0000Y_\u0003"+
		"\u0014\n\u0000Z[\u0005\u0016\u0000\u0000[\\\u0003\u000e\u0007\u0000\\"+
		"]\u0005\u0017\u0000\u0000]_\u0001\u0000\u0000\u0000^Y\u0001\u0000\u0000"+
		"\u0000^Z\u0001\u0000\u0000\u0000_b\u0001\u0000\u0000\u0000`a\u0005\u001e"+
		"\u0000\u0000ac\u0003\u0012\t\u0000b`\u0001\u0000\u0000\u0000bc\u0001\u0000"+
		"\u0000\u0000c\u0013\u0001\u0000\u0000\u0000dg\u0003\u0016\u000b\u0000"+
		"ef\u0007\u0001\u0000\u0000fh\u0003\u0016\u000b\u0000ge\u0001\u0000\u0000"+
		"\u0000gh\u0001\u0000\u0000\u0000h\u0015\u0001\u0000\u0000\u0000in\u0003"+
		"\u0018\f\u0000jk\u0007\u0002\u0000\u0000km\u0003\u0018\f\u0000lj\u0001"+
		"\u0000\u0000\u0000mp\u0001\u0000\u0000\u0000nl\u0001\u0000\u0000\u0000"+
		"no\u0001\u0000\u0000\u0000o\u0017\u0001\u0000\u0000\u0000pn\u0001\u0000"+
		"\u0000\u0000qv\u0003\u001a\r\u0000rs\u0007\u0003\u0000\u0000su\u0003\u001a"+
		"\r\u0000tr\u0001\u0000\u0000\u0000ux\u0001\u0000\u0000\u0000vt\u0001\u0000"+
		"\u0000\u0000vw\u0001\u0000\u0000\u0000w\u0019\u0001\u0000\u0000\u0000"+
		"xv\u0001\u0000\u0000\u0000y\u0081\u0005\r\u0000\u0000z\u0081\u0003\"\u0011"+
		"\u0000{\u0081\u0003\u001c\u000e\u0000|}\u0005\u0016\u0000\u0000}~\u0003"+
		"\u0016\u000b\u0000~\u007f\u0005\u0017\u0000\u0000\u007f\u0081\u0001\u0000"+
		"\u0000\u0000\u0080y\u0001\u0000\u0000\u0000\u0080z\u0001\u0000\u0000\u0000"+
		"\u0080{\u0001\u0000\u0000\u0000\u0080|\u0001\u0000\u0000\u0000\u0081\u001b"+
		"\u0001\u0000\u0000\u0000\u0082\u0083\u0003$\u0012\u0000\u0083\u0084\u0005"+
		"\u0019\u0000\u0000\u0084\u0085\u0003\u001e\u000f\u0000\u0085\u008e\u0001"+
		"\u0000\u0000\u0000\u0086\u0087\u0003 \u0010\u0000\u0087\u0088\u0005\u0016"+
		"\u0000\u0000\u0088\u0089\u0003$\u0012\u0000\u0089\u008a\u0005\u0018\u0000"+
		"\u0000\u008a\u008b\u0005\r\u0000\u0000\u008b\u008c\u0005\u0017\u0000\u0000"+
		"\u008c\u008e\u0001\u0000\u0000\u0000\u008d\u0082\u0001\u0000\u0000\u0000"+
		"\u008d\u0086\u0001\u0000\u0000\u0000\u008e\u001d\u0001\u0000\u0000\u0000"+
		"\u008f\u0090\u0007\u0004\u0000\u0000\u0090\u001f\u0001\u0000\u0000\u0000"+
		"\u0091\u0092\u0007\u0005\u0000\u0000\u0092!\u0001\u0000\u0000\u0000\u0093"+
		"\u0094\u0005\n\u0000\u0000\u0094\u0095\u0005\u0016\u0000\u0000\u0095\u0096"+
		"\u0003$\u0012\u0000\u0096\u0097\u0005\u0017\u0000\u0000\u0097#\u0001\u0000"+
		"\u0000\u0000\u0098\u0099\u0007\u0006\u0000\u0000\u0099%\u0001\u0000\u0000"+
		"\u0000\r(,2:EV^bgnv\u0080\u008d";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}