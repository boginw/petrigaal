grammar ATL;

start : temporalQuantifier? EOF;

expressionPrimary
  : IntLiteral
  | Identifier
  | '(' expressionAdditive ')'
  ;

expressionUnary
  : expressionPrimary
  | '-' expressionPrimary
  | '+' expressionPrimary
  ;

expressionMultiplicative
  : expressionUnary
  | expressionMultiplicative '*' expressionUnary
  ;

expressionAdditive
  : expressionMultiplicative
  | expressionAdditive '+' expressionMultiplicative
  | expressionAdditive '-' expressionMultiplicative
  ;

predicate
  : BoolLiteral
  | expressionAdditive Bowtie expressionAdditive
  | '(' temporalQuantifier ')'
  ;

temporalUnary
  : predicate
  | '!' temporalQuantifier
  ;

temporalBinary
  : temporalUnary
  | temporalUnary '&' temporalUnary
  | temporalUnary '|' temporalUnary
  ;

temporalQuantifier
  : temporalBinary
  | Path 'X' '(' temporalQuantifier ')'
  | Path 'G' '(' temporalQuantifier ')'
  | Path 'F' '(' temporalQuantifier ')'
  | Path '(' temporalQuantifier 'U' temporalQuantifier ')'
  ;

/* Literals */
Path: 'E' | 'A';
BoolLiteral : 'true' | 'false';
Bowtie: '<' | '<=' | '=' | '!=' | '>=' | '>';
Identifier              : (Letter | '_') (AlphaNum | '_')*;

/* Lexer rules */
NewLine                 : ('\r' '\n' | '\n') -> skip;
Whitespace              : [ \t]+ -> skip;
IntLiteral              : Number;
Number                  : Digit+;

fragment Letter         : [a-zA-Z];
fragment Word           : Letter+;
fragment AlphaNum       : Digit | Letter;
fragment AlphaNumSeq    : AlphaNum+;
fragment EscapeSeq      : '\\' ['"?abfnrtv\\];
fragment SChar          : ~["\\\r\n]
                        | EscapeSeq
                        | '\\\n'
                        | '\\\r\n'
                        ;
fragment SCharSeq       : SChar+;
fragment Digit          : [0-9];