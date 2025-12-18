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
		IF=18, PORTFOLIO=19, QTY=20, IDENTIFIER=21, SEMICOLON=22, LPAR=23, RPAR=24, 
		COMMA=25, DOT=26, GT=27, LT=28, EQ=29, NEQ=30, AND=31, OR=32, PLUS=33, 
		MINUS=34, MUL=35, DIV=36;
	public static final int
		RULE_program = 0, RULE_statement = 1, RULE_trade_cmd = 2, RULE_portfolio_cmd = 3, 
		RULE_allocation_list = 4, RULE_allocation = 5, RULE_value = 6, RULE_consideration = 7, 
		RULE_quantity = 8, RULE_condition = 9, RULE_disjunction = 10, RULE_conjunction = 11, 
		RULE_comparison = 12, RULE_expr = 13, RULE_term = 14, RULE_primary = 15, 
		RULE_series_operation = 16, RULE_series_field = 17, RULE_agg_func = 18, 
		RULE_price_expr = 19, RULE_symbol = 20;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "statement", "trade_cmd", "portfolio_cmd", "allocation_list", 
			"allocation", "value", "consideration", "quantity", "condition", "disjunction", 
			"conjunction", "comparison", "expr", "term", "primary", "series_operation", 
			"series_field", "agg_func", "price_expr", "symbol"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'open'", "'high'", "'low'", "'close'", "'volume'", "'MAVG'", "'EMA'", 
			"'RSI'", "'STDDEV'", "'PRICE'", null, null, null, null, "'BUY'", "'SELL'", 
			"'OF'", "'IF'", "'PORTFOLIO'", "'QTY'", null, "';'", "'('", "')'", "','", 
			"'.'", "'>'", "'<'", "'='", "'!='", "'&&'", "'||'", "'+'", "'-'", "'*'", 
			"'/'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, "WS", 
			"COMMENT", "NUMBER", "CURRENCY", "BUY", "SELL", "OF", "IF", "PORTFOLIO", 
			"QTY", "IDENTIFIER", "SEMICOLON", "LPAR", "RPAR", "COMMA", "DOT", "GT", 
			"LT", "EQ", "NEQ", "AND", "OR", "PLUS", "MINUS", "MUL", "DIV"
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
			setState(46); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(42);
				statement();
				setState(44);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SEMICOLON) {
					{
					setState(43);
					match(SEMICOLON);
					}
				}

				}
				}
				setState(48); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 622592L) != 0) );
			setState(50);
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
			setState(54);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BUY:
			case SELL:
				enterOuterAlt(_localctx, 1);
				{
				setState(52);
				trade_cmd();
				}
				break;
			case PORTFOLIO:
				enterOuterAlt(_localctx, 2);
				{
				setState(53);
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
		public ConsiderationContext consideration() {
			return getRuleContext(ConsiderationContext.class,0);
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
			setState(56);
			_la = _input.LA(1);
			if ( !(_la==BUY || _la==SELL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(57);
			consideration();
			setState(58);
			match(OF);
			setState(59);
			symbol();
			setState(62);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IF) {
				{
				setState(60);
				match(IF);
				setState(61);
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
			setState(64);
			match(PORTFOLIO);
			setState(65);
			match(EQ);
			setState(66);
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
			setState(68);
			allocation();
			setState(73);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS) {
				{
				{
				setState(69);
				match(PLUS);
				setState(70);
				allocation();
				}
				}
				setState(75);
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
			setState(76);
			value();
			setState(77);
			match(OF);
			setState(78);
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
			setState(80);
			match(NUMBER);
			setState(81);
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
	public static class ConsiderationContext extends ParserRuleContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public QuantityContext quantity() {
			return getRuleContext(QuantityContext.class,0);
		}
		public ConsiderationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_consideration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterConsideration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitConsideration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitConsideration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConsiderationContext consideration() throws RecognitionException {
		ConsiderationContext _localctx = new ConsiderationContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_consideration);
		try {
			setState(85);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NUMBER:
				enterOuterAlt(_localctx, 1);
				{
				setState(83);
				value();
				}
				break;
			case QTY:
				enterOuterAlt(_localctx, 2);
				{
				setState(84);
				quantity();
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
	public static class QuantityContext extends ParserRuleContext {
		public TerminalNode QTY() { return getToken(sophieParser.QTY, 0); }
		public TerminalNode NUMBER() { return getToken(sophieParser.NUMBER, 0); }
		public QuantityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quantity; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).enterQuantity(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof sophieListener ) ((sophieListener)listener).exitQuantity(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof sophieVisitor ) return ((sophieVisitor<? extends T>)visitor).visitQuantity(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QuantityContext quantity() throws RecognitionException {
		QuantityContext _localctx = new QuantityContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_quantity);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			match(QTY);
			setState(88);
			match(NUMBER);
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
		enterRule(_localctx, 18, RULE_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
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
		enterRule(_localctx, 20, RULE_disjunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(92);
			conjunction();
			setState(97);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(93);
				match(OR);
				setState(94);
				conjunction();
				}
				}
				setState(99);
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
		enterRule(_localctx, 22, RULE_conjunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(100);
				comparison();
				}
				break;
			case 2:
				{
				setState(101);
				match(LPAR);
				setState(102);
				condition();
				setState(103);
				match(RPAR);
				}
				break;
			}
			setState(109);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AND) {
				{
				setState(107);
				match(AND);
				setState(108);
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
		enterRule(_localctx, 24, RULE_comparison);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			expr();
			setState(114);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2013265920L) != 0)) {
				{
				setState(112);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 2013265920L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(113);
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
		enterRule(_localctx, 26, RULE_expr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			term();
			setState(121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PLUS || _la==MINUS) {
				{
				{
				setState(117);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(118);
				term();
				}
				}
				setState(123);
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
		enterRule(_localctx, 28, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
			primary();
			setState(129);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==MUL || _la==DIV) {
				{
				{
				setState(125);
				_la = _input.LA(1);
				if ( !(_la==MUL || _la==DIV) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(126);
				primary();
				}
				}
				setState(131);
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
		enterRule(_localctx, 30, RULE_primary);
		try {
			setState(139);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NUMBER:
				enterOuterAlt(_localctx, 1);
				{
				setState(132);
				match(NUMBER);
				}
				break;
			case T__9:
				enterOuterAlt(_localctx, 2);
				{
				setState(133);
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
				setState(134);
				series_operation();
				}
				break;
			case LPAR:
				enterOuterAlt(_localctx, 4);
				{
				setState(135);
				match(LPAR);
				setState(136);
				expr();
				setState(137);
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
		enterRule(_localctx, 32, RULE_series_operation);
		try {
			setState(152);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CURRENCY:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(141);
				symbol();
				setState(142);
				match(DOT);
				setState(143);
				series_field();
				}
				break;
			case T__5:
			case T__6:
			case T__7:
			case T__8:
				enterOuterAlt(_localctx, 2);
				{
				setState(145);
				agg_func();
				setState(146);
				match(LPAR);
				setState(147);
				symbol();
				setState(148);
				match(COMMA);
				setState(149);
				match(NUMBER);
				setState(150);
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
		enterRule(_localctx, 34, RULE_series_field);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
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
		enterRule(_localctx, 36, RULE_agg_func);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
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
		enterRule(_localctx, 38, RULE_price_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			match(T__9);
			setState(159);
			match(LPAR);
			setState(160);
			symbol();
			setState(161);
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
		enterRule(_localctx, 40, RULE_symbol);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
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
		"\u0004\u0001$\u00a6\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0001\u0000\u0001\u0000"+
		"\u0003\u0000-\b\u0000\u0004\u0000/\b\u0000\u000b\u0000\f\u00000\u0001"+
		"\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0003\u00017\b\u0001\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003"+
		"\u0002?\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0005\u0004H\b\u0004\n\u0004\f\u0004K\t"+
		"\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0003\u0007V\b\u0007\u0001"+
		"\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0005\n`\b"+
		"\n\n\n\f\nc\t\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0003\u000bj\b\u000b\u0001\u000b\u0001\u000b\u0003\u000bn\b\u000b"+
		"\u0001\f\u0001\f\u0001\f\u0003\fs\b\f\u0001\r\u0001\r\u0001\r\u0005\r"+
		"x\b\r\n\r\f\r{\t\r\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e\u0080"+
		"\b\u000e\n\u000e\f\u000e\u0083\t\u000e\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u008c\b\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010"+
		"\u0099\b\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0000\u0000\u0015\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010"+
		"\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(\u0000\u0007\u0001\u0000"+
		"\u000f\u0010\u0001\u0000\u001b\u001e\u0001\u0000!\"\u0001\u0000#$\u0001"+
		"\u0000\u0001\u0005\u0001\u0000\u0006\t\u0002\u0000\u000e\u000e\u0015\u0015"+
		"\u00a0\u0000.\u0001\u0000\u0000\u0000\u00026\u0001\u0000\u0000\u0000\u0004"+
		"8\u0001\u0000\u0000\u0000\u0006@\u0001\u0000\u0000\u0000\bD\u0001\u0000"+
		"\u0000\u0000\nL\u0001\u0000\u0000\u0000\fP\u0001\u0000\u0000\u0000\u000e"+
		"U\u0001\u0000\u0000\u0000\u0010W\u0001\u0000\u0000\u0000\u0012Z\u0001"+
		"\u0000\u0000\u0000\u0014\\\u0001\u0000\u0000\u0000\u0016i\u0001\u0000"+
		"\u0000\u0000\u0018o\u0001\u0000\u0000\u0000\u001at\u0001\u0000\u0000\u0000"+
		"\u001c|\u0001\u0000\u0000\u0000\u001e\u008b\u0001\u0000\u0000\u0000 \u0098"+
		"\u0001\u0000\u0000\u0000\"\u009a\u0001\u0000\u0000\u0000$\u009c\u0001"+
		"\u0000\u0000\u0000&\u009e\u0001\u0000\u0000\u0000(\u00a3\u0001\u0000\u0000"+
		"\u0000*,\u0003\u0002\u0001\u0000+-\u0005\u0016\u0000\u0000,+\u0001\u0000"+
		"\u0000\u0000,-\u0001\u0000\u0000\u0000-/\u0001\u0000\u0000\u0000.*\u0001"+
		"\u0000\u0000\u0000/0\u0001\u0000\u0000\u00000.\u0001\u0000\u0000\u0000"+
		"01\u0001\u0000\u0000\u000012\u0001\u0000\u0000\u000023\u0005\u0000\u0000"+
		"\u00013\u0001\u0001\u0000\u0000\u000047\u0003\u0004\u0002\u000057\u0003"+
		"\u0006\u0003\u000064\u0001\u0000\u0000\u000065\u0001\u0000\u0000\u0000"+
		"7\u0003\u0001\u0000\u0000\u000089\u0007\u0000\u0000\u00009:\u0003\u000e"+
		"\u0007\u0000:;\u0005\u0011\u0000\u0000;>\u0003(\u0014\u0000<=\u0005\u0012"+
		"\u0000\u0000=?\u0003\u0012\t\u0000><\u0001\u0000\u0000\u0000>?\u0001\u0000"+
		"\u0000\u0000?\u0005\u0001\u0000\u0000\u0000@A\u0005\u0013\u0000\u0000"+
		"AB\u0005\u001d\u0000\u0000BC\u0003\b\u0004\u0000C\u0007\u0001\u0000\u0000"+
		"\u0000DI\u0003\n\u0005\u0000EF\u0005!\u0000\u0000FH\u0003\n\u0005\u0000"+
		"GE\u0001\u0000\u0000\u0000HK\u0001\u0000\u0000\u0000IG\u0001\u0000\u0000"+
		"\u0000IJ\u0001\u0000\u0000\u0000J\t\u0001\u0000\u0000\u0000KI\u0001\u0000"+
		"\u0000\u0000LM\u0003\f\u0006\u0000MN\u0005\u0011\u0000\u0000NO\u0003("+
		"\u0014\u0000O\u000b\u0001\u0000\u0000\u0000PQ\u0005\r\u0000\u0000QR\u0005"+
		"\u000e\u0000\u0000R\r\u0001\u0000\u0000\u0000SV\u0003\f\u0006\u0000TV"+
		"\u0003\u0010\b\u0000US\u0001\u0000\u0000\u0000UT\u0001\u0000\u0000\u0000"+
		"V\u000f\u0001\u0000\u0000\u0000WX\u0005\u0014\u0000\u0000XY\u0005\r\u0000"+
		"\u0000Y\u0011\u0001\u0000\u0000\u0000Z[\u0003\u0014\n\u0000[\u0013\u0001"+
		"\u0000\u0000\u0000\\a\u0003\u0016\u000b\u0000]^\u0005 \u0000\u0000^`\u0003"+
		"\u0016\u000b\u0000_]\u0001\u0000\u0000\u0000`c\u0001\u0000\u0000\u0000"+
		"a_\u0001\u0000\u0000\u0000ab\u0001\u0000\u0000\u0000b\u0015\u0001\u0000"+
		"\u0000\u0000ca\u0001\u0000\u0000\u0000dj\u0003\u0018\f\u0000ef\u0005\u0017"+
		"\u0000\u0000fg\u0003\u0012\t\u0000gh\u0005\u0018\u0000\u0000hj\u0001\u0000"+
		"\u0000\u0000id\u0001\u0000\u0000\u0000ie\u0001\u0000\u0000\u0000jm\u0001"+
		"\u0000\u0000\u0000kl\u0005\u001f\u0000\u0000ln\u0003\u0016\u000b\u0000"+
		"mk\u0001\u0000\u0000\u0000mn\u0001\u0000\u0000\u0000n\u0017\u0001\u0000"+
		"\u0000\u0000or\u0003\u001a\r\u0000pq\u0007\u0001\u0000\u0000qs\u0003\u001a"+
		"\r\u0000rp\u0001\u0000\u0000\u0000rs\u0001\u0000\u0000\u0000s\u0019\u0001"+
		"\u0000\u0000\u0000ty\u0003\u001c\u000e\u0000uv\u0007\u0002\u0000\u0000"+
		"vx\u0003\u001c\u000e\u0000wu\u0001\u0000\u0000\u0000x{\u0001\u0000\u0000"+
		"\u0000yw\u0001\u0000\u0000\u0000yz\u0001\u0000\u0000\u0000z\u001b\u0001"+
		"\u0000\u0000\u0000{y\u0001\u0000\u0000\u0000|\u0081\u0003\u001e\u000f"+
		"\u0000}~\u0007\u0003\u0000\u0000~\u0080\u0003\u001e\u000f\u0000\u007f"+
		"}\u0001\u0000\u0000\u0000\u0080\u0083\u0001\u0000\u0000\u0000\u0081\u007f"+
		"\u0001\u0000\u0000\u0000\u0081\u0082\u0001\u0000\u0000\u0000\u0082\u001d"+
		"\u0001\u0000\u0000\u0000\u0083\u0081\u0001\u0000\u0000\u0000\u0084\u008c"+
		"\u0005\r\u0000\u0000\u0085\u008c\u0003&\u0013\u0000\u0086\u008c\u0003"+
		" \u0010\u0000\u0087\u0088\u0005\u0017\u0000\u0000\u0088\u0089\u0003\u001a"+
		"\r\u0000\u0089\u008a\u0005\u0018\u0000\u0000\u008a\u008c\u0001\u0000\u0000"+
		"\u0000\u008b\u0084\u0001\u0000\u0000\u0000\u008b\u0085\u0001\u0000\u0000"+
		"\u0000\u008b\u0086\u0001\u0000\u0000\u0000\u008b\u0087\u0001\u0000\u0000"+
		"\u0000\u008c\u001f\u0001\u0000\u0000\u0000\u008d\u008e\u0003(\u0014\u0000"+
		"\u008e\u008f\u0005\u001a\u0000\u0000\u008f\u0090\u0003\"\u0011\u0000\u0090"+
		"\u0099\u0001\u0000\u0000\u0000\u0091\u0092\u0003$\u0012\u0000\u0092\u0093"+
		"\u0005\u0017\u0000\u0000\u0093\u0094\u0003(\u0014\u0000\u0094\u0095\u0005"+
		"\u0019\u0000\u0000\u0095\u0096\u0005\r\u0000\u0000\u0096\u0097\u0005\u0018"+
		"\u0000\u0000\u0097\u0099\u0001\u0000\u0000\u0000\u0098\u008d\u0001\u0000"+
		"\u0000\u0000\u0098\u0091\u0001\u0000\u0000\u0000\u0099!\u0001\u0000\u0000"+
		"\u0000\u009a\u009b\u0007\u0004\u0000\u0000\u009b#\u0001\u0000\u0000\u0000"+
		"\u009c\u009d\u0007\u0005\u0000\u0000\u009d%\u0001\u0000\u0000\u0000\u009e"+
		"\u009f\u0005\n\u0000\u0000\u009f\u00a0\u0005\u0017\u0000\u0000\u00a0\u00a1"+
		"\u0003(\u0014\u0000\u00a1\u00a2\u0005\u0018\u0000\u0000\u00a2\'\u0001"+
		"\u0000\u0000\u0000\u00a3\u00a4\u0007\u0006\u0000\u0000\u00a4)\u0001\u0000"+
		"\u0000\u0000\u000e,06>IUaimry\u0081\u008b\u0098";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}