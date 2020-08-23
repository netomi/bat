grammar Smali;

/*
 * Parser Rules
 */

smaliclass : classline superclassline sourceline? interfaceline* memberdef* EOF ;

classline      : '.class' classmodifier* CLASSTYPE ;
superclassline : '.super' CLASSTYPE ;
interfaceline  : '.implements' CLASSTYPE ;

memberdef : fielddef | methoddef ;

fielddef : '.field' fieldmodifier* MEMBERNAME ':' fieldtype ;

methoddef : '.method' methodmodifier* MEMBERNAME DESCRIPTOR ;

classmodifier :
    PUBLIC     |
    PRIVATE    |
    PROTECTED  |
    ABSTRACT   |
    FINAL      |
    ANNOTATION |
    INTERFACE ;

fieldmodifier :
    PUBLIC     |
    PRIVATE    |
    PROTECTED  |
    FINAL      |
    STATIC ;

methodmodifier :
    PUBLIC     |
    PRIVATE    |
    PROTECTED  |
    FINAL      |
    STATIC ;

sourceline : '.source' '"' FILENAME '"' ;

fieldtype : CLASSTYPE | PRIMITIVETYPE | ('[' fieldtype) ;

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
ANNOTATION : 'annotation' ;
STATIC     : 'static' ;

// primitive types
PRIMITIVETYPE :
    'B' |
    'S' |
    'Z' |
    'I' |
    'J' |
    'F' |
    'D' |
    'V' ;

fragment DIGIT : [0-9] ;
NUMBER         : DIGIT+ ([.,] DIGIT+)? ;

COMMENT : '#' ~[\r\n]* -> skip ;

fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;

MEMBERNAME : (LOWERCASE | UPPERCASE)+ ;

WORD                : (LOWERCASE | UPPERCASE | '_' )+ ;

fragment EXTENSION : (LOWERCASE | UPPERCASE | DIGIT)+ ;

FILENAME   : (WORD)+ '.' EXTENSION ;

DESCRIPTOR : (WORD | '(' | ')' | ';')+ ;

CLASSTYPE : 'L' (WORD | DIGIT | '/')+ ';' ;

WS                  : (' ' | '\t') -> skip;
NEWLINE             : ('\r'? '\n' | '\r')+ -> skip;

