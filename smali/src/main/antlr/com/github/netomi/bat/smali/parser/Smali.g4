grammar Smali;

/*
 * Parser Rules
 */

smaliclass : classline superclassline sourceline? EOF;

classline      : '.class' classmodifier* CLASSTYPE ;
superclassline : '.super' CLASSTYPE ;

classmodifier : PUBLIC | PRIVATE | PROTECTED | ABSTRACT | FINAL | ANNOTATION | INTERFACE ;

sourceline    : '.source' '"' FILENAME '"' ;

/*
 * Lexer Rules
 */

// modifiers
PUBLIC    :  'public' ;
PROTECTED  : 'protected' ;
PRIVATE    : 'private' ;
FINAL      : 'final' ;
ABSTRACT   : 'abstract' ;
INTERFACE  : 'interface' ;
ANNOTATION : 'annotation';

fragment DIGIT : [0-9] ;
NUMBER         : DIGIT+ ([.,] DIGIT+)? ;

COMMENT : '#' ~[\r\n]* -> skip ;

fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;

WORD                : (LOWERCASE | UPPERCASE | '_' )+ ;

fragment EXTENSION : (LOWERCASE | UPPERCASE | DIGIT)+ ;

FILENAME : (LOWERCASE | UPPERCASE)+ '.' EXTENSION ;

CLASSTYPE           : 'L' (WORD | DIGIT | '/')+ ';' ;

WS                  : (' ' | '\t') -> skip;
NEWLINE             : ('\r'? '\n' | '\r')+ -> skip;

