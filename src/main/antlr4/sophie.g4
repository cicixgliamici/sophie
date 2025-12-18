grammar sophie;

@header {
  package parser;
}

/* ====================================================================
   ======================= LEXER (token rules) =========================
   ==================================================================== */

/*
  Whitespace & comments are skipped, so they never reach the parser.
*/
WS       : [ \t\r\n]+           -> skip ;
COMMENT  : '//' ~[\r\n]*        -> skip ;

/*
  NUMBER
*/
NUMBER : [0-9]+ ('.' [0-9]+)? ;  // preferred (no trailing dot)
// Matches integer or decimal numbers, e.g. 42, 3.14


/*
  CURRENCY is defined as a token so that 'EUR', 'USD', 'GBP', 'BTC' are reserved
  words and never collide with IDENTIFIER. This is intentional: 'BTC' as a currency
  is not an IDENTIFIER token; it is a CURRENCY token. To allow BTC to be used as a
  *symbol* (e.g., "OF BTC" or "BTC.close"), the parser introduces a `symbol` rule
  that accepts either IDENTIFIER or CURRENCY (see the parser section below).
*/
CURRENCY : 'EUR' | 'USD' | 'GBP' | 'BTC' ;

/*
  Keywords come BEFORE IDENTIFIER so they are not swallowed by IDENTIFIER.
  This ensures 'BUY' is token BUY, not IDENTIFIER("BUY"). Same for SELL, IF, etc.
*/
BUY        : 'BUY' ;
SELL       : 'SELL' ;
OF         : 'OF' ;
IF         : 'IF' ;
PORTFOLIO  : 'PORTFOLIO' ;
QTY        : 'QTY' ;

/*
  IDENTIFIER comes after keywords (very important). This prevents keywords from
  being lexed as generic identifiers.
*/
IDENTIFIER : [A-Za-z_][A-Za-z0-9_]*  ;

/* Punctuation */
SEMICOLON  : ';' ;   // Statement separator
LPAR       : '(' ;
RPAR       : ')' ;
COMMA      : ',' ;   // Argument separator
DOT        : '.' ;   // Dot for field access (e.g., BTC.volume)

/* Operators */
GT   : '>' ;
LT   : '<' ;
EQ   : '=' ;
NEQ  : '!=' ;
AND  : '&&' ;
OR   : '||' ;
PLUS : '+' ;
MINUS: '-' ;
MUL  : '*' ;
DIV  : '/' ;


/* ====================================================================
   ======================= PARSER (grammar rules) ======================
   ==================================================================== */

/*
  Entry point: one or more statements, separated optionally by ';',
  then end of file.
*/
program
  : (statement SEMICOLON?)+ EOF
  ;

/*
  The DSL currently supports:
   - trade commands (BUY/SELL ...)
   - portfolio assignment (PORTFOLIO = ...)
*/
statement
  : trade_cmd
  | portfolio_cmd
  ;

/*
  CHANGE #1: symbol instead of IDENTIFIER
  ---------------------------------------
  Previously: ... OF IDENTIFIER ...
  Now: ... OF symbol ...
  Rationale: 'BTC' is tokenized as CURRENCY, not IDENTIFIER. With `symbol`, we
  allow both IDENTIFIER and CURRENCY to represent a tradable instrument.
*/
trade_cmd
  : (BUY | SELL) consideration OF symbol (IF condition)?
  ;

/*
  Portfolio definition is a sum of `value`s.
*/
portfolio_cmd
  : PORTFOLIO EQ allocation_list
  ;

/* Sum of values, e.g., "500 USD + 0.1 BTC". */
allocation_list
  : allocation (PLUS allocation)*
  ;

allocation
  : value OF symbol
  ;
/*
  A numeric value tied to a currency, e.g., "1500 EUR", "0.5 BTC".
*/
value            : NUMBER CURRENCY ;
/* A quantity literal tagged explicitly with QTY. */
consideration    : value | quantity ;
quantity         : QTY NUMBER ;
/*
  Boolean expressions with precedence:
    OR (lowest) < AND < comparison (highest)
*/
condition
  : disjunction
  ;

disjunction
  : conjunction (OR conjunction)*
  ;
// Logical OR chain of conjunctions

conjunction
  : (comparison | LPAR condition RPAR) (AND conjunction)?
  ;
// Logical AND chain of comparisons or parenthesized conditions

/*
  A comparison is either:
    - a single operand (treated as "truthy" test), or
    - operand <op> operand
*/
comparison
  : expr ((GT | LT | EQ | NEQ) expr)?
  ;

// Espressioni aritmetiche
expr
  : term ((PLUS | MINUS) term)*
  ;
// Arithmetic expression with addition/subtraction

term
  : primary ((MUL | DIV) primary)*
  ;
// Arithmetic term with multiplication/division

// Foglie per l'aritmetica
primary
  : NUMBER
  | price_expr
  | series_operation
  | LPAR expr RPAR        // parentesi aritmetiche
  ;
/*
  CHANGE #2: symbol in series/PRICE and NUMBER in agg_func
  --------------------------------------------------------
  - Use `symbol` (IDENTIFIER | CURRENCY) for tickers everywhere (AAPL, BTC, ...).
  - Use NUMBER for the "window" argument (e.g., RSI(MSFT, 14)).
    Because the lexer emits NUMBER for both "14" and "14.0", using NUMBER here
    avoids a mismatch (previously it expected INTEGER and failed on "14" which
    was being lexed as NUMBER).

operand
  : series_operation
  | price_expr
  | NUMBER
  | LPAR condition RPAR
  ;
*/

/*
  Historical series operations and aggregated indicators.
  Examples:
    BTC.volume
    RSI(MSFT, 14)
    MAVG(AAPL, 50)
*/
series_operation
    : symbol DOT series_field  // e.g., AAPL.close or BTC.volume
    | agg_func LPAR symbol COMMA NUMBER RPAR     // e.g., RSI(MSFT, 14)
    ;
// Time series field access or aggregation function call

/* Available fields from a historical time series. */
series_field
  : 'open' | 'high' | 'low' | 'close' | 'volume'
  ;

/* Set of supported aggregation / TA functions. */
agg_func
  : 'MAVG' | 'EMA' | 'RSI' | 'STDDEV'
  ;

/*
  Current market price of a symbol, e.g., PRICE(GOOG) or PRICE(BTC).
  NOTE: symbol (not IDENTIFIER) so currencies work as tickers too.
*/
price_expr
  : 'PRICE' LPAR symbol RPAR
  ;
// Gets the current market price of a symbol

/*
  NEW helper rule:
  ----------------
  A `symbol` is either a generic IDENTIFIER (e.g., AAPL, MSFT, VWCE_ETF)
  or one of the currency tickers (CURRENCY token: EUR, USD, GBP, BTC).
  This unifies how we refer to tradable instruments across the grammar.
*/
symbol
  : IDENTIFIER
  | CURRENCY;
// A tradable instrument: either a generic identifier or a currency


/* ====================================================================
   ======================= Usage examples ==============================
   ====================================================================

   BUY strategy: buy MSFT when oversold (RSI<30) and the 50-day moving
   average exceeds the current price.

   BUY 1500 EUR OF MSFT
     IF RSI(MSFT, 14) < 30
     && MAVG(MSFT, 50) > PRICE(MSFT)

   SELL strategy: sell 0.5 BTC when high volume and volatility are high.

   SELL 0.5 BTC OF BTC
     IF BTC.volume > 1000000
     && STDDEV(BTC, 20) > PRICE(BTC)

  Explicit quantity (bypassing price conversion):
     SELL QTY 5 OF AAPL

   Portfolio definition:

   PORTFOLIO =
     6000 EUR OF VWCE
     + 2000 USD OF AAPL
     + 0.1 BTC OF BTC

   Notes on the design choices above:
   - Keywords before IDENTIFIER: prevents keywords from being parsed as IDs.
   - NUMBER before INTEGER: "14" will be token NUMBER; parser uses NUMBER
     in function windows to accept both 14 and 14.0.
   - `symbol` rule: lets currency tickers act as instruments wherever an
     identifier was previously required (e.g., "OF BTC", "BTC.close").
