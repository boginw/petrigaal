grammar ATL;

start : temporalQuantifier? EOF;

expressionPrimary
  : IntLiteral
  | EnabledActions
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
  | PlayerQuantifier '@' '(' temporalQuantifier ')'
  | PlayerQuantifier '#' '(' temporalQuantifier ')'
  | PlayerQuantifier '(' temporalQuantifier 'U' temporalQuantifier ')'
  ;

/* Literals */
BoolLiteral : 'true' | 'false';
Bowtie: '<' | '<=' | '=' | '!=' | '>=' | '>';
EnabledActions: 'd1' | 'd2';
PlayerQuantifier: '{' Player '}';
Identifier              : (Letter | '_') (AlphaNum | '_')*;

/* Lexer rules */
NewLine                 : ('\r' '\n' | '\n') -> skip;
Whitespace              : [ \t]+ -> skip;
IntLiteral              : Number;
Number                  : Digit+;

fragment Player         : '1' | '2';
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