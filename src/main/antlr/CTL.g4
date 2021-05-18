grammar CTL;

start : temporalBinary? EOF;

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
  | '(' temporalBinary ')'
  ;

temporalQuantifier
  : Path 'X' '(' temporalBinary ')'
  | Path 'G' '(' temporalBinary ')'
  | Path 'F' '(' temporalBinary ')'
  | Path '(' temporalBinary 'U' temporalBinary ')'
  | predicate
  ;

temporalUnary
  : temporalQuantifier
  | '!' temporalQuantifier
  ;

temporalBinary
  : temporalUnary
  | temporalUnary '&' temporalBinary
  | temporalUnary '|' temporalBinary
  ;

/* Literals */
Path: 'E' ' '? | 'A' ' '?;
BoolLiteral : 'true' | 'false';
Bowtie: '<' | '<=' | '=' | '!=' | '>=' | '>';
Identifier              : (Letter | '_') (AlphaNum | '_')*;

/* Lexer rules */
NewLine                 : ('\r' '\n' | '\n') -> skip;
Whitespace              : [ \t]+ -> skip;
IntLiteral              : Number;
Number                  : Digit+;

fragment Letter         : [a-zA-Z];
fragment AlphaNum       : Digit | Letter;
fragment EscapeSeq      : '\\' ['"?abfnrtv\\];
fragment SChar          : ~["\\\r\n]
                        | EscapeSeq
                        | '\\\n'
                        | '\\\r\n'
                        ;
fragment Digit          : [0-9];
