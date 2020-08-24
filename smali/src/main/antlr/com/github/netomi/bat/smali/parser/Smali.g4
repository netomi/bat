grammar Smali;

/*
 * Parser Rules
 */

smaliclass : classline superclassline sourceline? interfaceline* memberdef* EOF ;

classline      : CLASS_START classmodifier* ClassType ;
superclassline : SUPER_START ClassType ;
interfaceline  : INTERFACE_START ClassType ;

memberdef : fielddef | methoddef ;

fielddef : FIELD_START fieldmodifier* MemberName ':' type ;

methoddef : METHOD_START methodmodifier* MemberName descriptor ;

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

type :
    ClassType      |
    primitive_type |
    array_type ;

array_type : '[' type ;

// primitive types
primitive_type :
    'B' |
    'S' |
    'Z' |
    'I' |
    'J' |
    'F' |
    'D' |
    'V' ;

descriptor : (WORD | '(' | ')' | ';')+ ;

/*
 * Lexer Rules
 */

// smali keywords
CLASS_START     : '.class' ;
SUPER_START     : '.super' ;
INTERFACE_START : '.implements' ;
FIELD_START     : '.field' ;
METHOD_START    : '.method' ;

// modifiers
PUBLIC                : 'public' ;
PRIVATE               : 'private' ;
PROTECTED             : 'protected' ;
STATIC                : 'static' ;
FINAL                 : 'final' ;
SYNCHRONIZED          : 'synchronized' ;
VOLATILE              : 'volatile' ;
BRIDGE                : 'bridge' ;
TRANSIENT             : 'transient' ;
VARARGS               : 'varargs' ;
NATIVE                : 'native' ;
INTERFACE             : 'interface' ;
ABSTRACT              : 'abstract' ;
STRICT                : 'strict' ;
SYNTHETIC             : 'synthetic' ;
ANNOTATION            : 'annotation' ;
ENUM                  : 'enum' ;
CONSTRUCTOR           : 'constructor' ;
DECLARED_SYNCHRONIZED : 'declared-synchronized' ;

fragment SimpleName : SimpleNameChar+ ;

fragment SimpleNameChar :
	'A' .. 'Z'             |
    'a' .. 'z'             |
    '0' .. '9'             |
    //' '                  | // since DEX version 040
    '$'                    |
    '-'                    |
    '_'                    |
    //'\u00a0'             | // since DEX version 040
    '\u00a1' .. '\u1fff'   |
    //'\u2000' .. '\u200a' | // since DEX version 040
    '\u2010' .. '\u2027'   |
    //'\u202f' 	           | // since DEX version 040
    '\u2030' .. '\ud7ff'   |
    '\ue000' .. '\uffef'
    //'\u10000' .. '\u10ffff' // not supported by antlr
    ;

MemberName    : SimpleName | '<' SimpleName '>' ;
FullClassName : SimpleName ('/' SimpleName)* ;
ClassType     : 'L' FullClassName ';' ;

fragment DIGIT : [0-9] ;
NUMBER         : DIGIT+ ([.,] DIGIT+)? ;

fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;
fragment CHARACTER  : LOWERCASE | UPPERCASE ;
WORD : (CHARACTER | '_' )+ ;

fragment EXTENSION : (CHARACTER | DIGIT)+ ;
FILENAME : (WORD)+ '.' EXTENSION ;

COMMENT : '#' ~[\r\n]* -> skip ;
WS : [ \t\r\n] -> skip;
