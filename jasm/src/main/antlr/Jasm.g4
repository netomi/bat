grammar Jasm;

fragment
INT_NENT:
    ('+'|'-')?
    (
        '0'
      | ('1'..'9') ('0'..'9')*
      | '0' ('0'..'7')+
      | ('0x'|'0X') HEX_DIGIT+
    );

fragment
FLOAT_NENT
    : (('+'|'-')?( ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
    | '.' ('0'..'9')+ EXPONENT?
    | ('0'..'9')+ EXPONENT)| ( ('+'|'-') F_INFINITY) )
    ;

fragment
F_NAN: ('N'|'n') ('A'|'a') ('N'|'n');

COMMENT
    : ('//' ~('\n'|'\r')* '\r'? '\n'
    | '#' ~('\n'|'\r')* '\r'? '\n'
    | '/*' .*? '*/' ) -> skip
    ;

WS: [ \t\r\n]+ -> skip;

fragment
EXPONENT: ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
HEX_DIGIT: ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    : '\\' ('b'|'t'|'n'|'f'|'r'|'\''|'"'|'\\')
    | UNICODE_ESC
    | OCTAL_ESC
    ;

fragment
OCTAL_ESC
    : '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    | '\\' ('0'..'7') ('0'..'7')
    | '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    : '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

fragment
FRAGMENT_PRIMITIVE_TYPE: 'B' | 'Z' | 'S' | 'C' | 'I' | 'F' | 'J' | 'D' ;

fragment
FRAGMENT_FULL_CLASS_NAME: FRAGMENT_SIMPLE_NAME ('/' FRAGMENT_SIMPLE_NAME)* ;

fragment
FRAGMENT_OBJECT_TYPE: 'L' FRAGMENT_FULL_CLASS_NAME ';' ;

fragment
FRAGMENT_ARRAY_TYPE: ('[')+ (FRAGMENT_PRIMITIVE_TYPE | FRAGMENT_OBJECT_TYPE);

fragment
FRAGMENT_SIMPLE_NAME_CHAR
    : 'A' .. 'Z'
    | 'a' .. 'z'
    | '0' .. '9'
    //| ' '                    // since DEX version 040
    | '$'
    | '-'
    | '_'
    //| '\u00a0'               // since DEX version 040
    | '\u00a1' .. '\u1fff'
    //| '\u2000' .. '\u200a'   // since DEX version 040
    | '\u2010' .. '\u2027'
    //| '\u202f' 	           // since DEX version 040
    | '\u2030' .. '\ud7ff'
    | '\ue000' .. '\uffef'
    //| '\u10000' .. '\u10ffff' // not supported by antlr
    ;

fragment
FRAGMENT_SIMPLE_NAME: FRAGMENT_SIMPLE_NAME_CHAR+ ;

fragment
FRAGMENT_MEMBER_NAME: FRAGMENT_SIMPLE_NAME | '<' FRAGMENT_SIMPLE_NAME '>' ;

fragment
FRAGMENT_ID: (ESC_SEQ| ~('\\'|'\r'|'\n'|'\t'|' '|':'|'-'|'='|','|'{'|'}'|'('|')'|'+'|'"'|'\''|'#'|'/'|'.'|';'|'@'))+;

fragment
FRAGMENT_METHOD_PROTO: '(' (FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE|FRAGMENT_PRIMITIVE_TYPE)* ')' ('V' | FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE|FRAGMENT_PRIMITIVE_TYPE) ;

fragment
FRAGMENT_FIELD_PART: FRAGMENT_MEMBER_NAME ':' (FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE|FRAGMENT_PRIMITIVE_TYPE) ;

METHOD_FULL  : (FRAGMENT_OBJECT_TYPE | FRAGMENT_ARRAY_TYPE) '->' FRAGMENT_MEMBER_NAME FRAGMENT_METHOD_PROTO;
METHOD_PART  : FRAGMENT_MEMBER_NAME FRAGMENT_METHOD_PROTO;
METHOD_PROTO : FRAGMENT_METHOD_PROTO;

FIELD_FULL : FRAGMENT_FULL_CLASS_NAME '->' FRAGMENT_FIELD_PART;
FIELD_PART : FRAGMENT_FIELD_PART;
ENUM_FULL  : FRAGMENT_OBJECT_TYPE '->' FRAGMENT_MEMBER_NAME;

FLOAT_INFINITY  : F_INFINITY ('f'|'F');
DOUBLE_INFINITY : F_INFINITY ('d'|'D')?;

F_INFINITY : ('I'|'i') ('N'|'n') ('F'|'f') ('I'|'i') ('N'|'n') ('I'|'i') ('T'|'t') ('Y'|'y') ;
FLOAT_NAN  : F_NAN ('f'|'F');
DOUBLE_NAN : F_NAN ('d'|'D')?;

BASE_FLOAT  :	(('0'..'9')+ | FLOAT_NENT) ('f'|'F');
BASE_DOUBLE : FLOAT_NENT ('d'|'D')? | ('0'..'9')+ ('d'|'D') ;

CHAR    : '\'' ( ESC_SEQ | ~('\\'|'\'') ) '\'';
LONG    : INT_NENT ('L'|'l');
SHORT   : INT_NENT ('S'|'s');
BYTE    : INT_NENT ('T'|'t');
INT     : INT_NENT;
BOOLEAN : 'true'|'false';
STRING  : '"' ( ESC_SEQ | ~('\\'|'"') )* '"';

OBJECT_TYPE    : FRAGMENT_OBJECT_TYPE;
ARRAY_TYPE     : FRAGMENT_ARRAY_TYPE;
PRIMITIVE_TYPE : FRAGMENT_PRIMITIVE_TYPE;
NULL: 'null' ;

ACC
    : 'public'
    | 'private'
    | 'protected'
    | 'static'
    | 'final'
    | 'super'
    | 'synchronized'
    | 'open'
    | 'transitive'
    | 'volatile'
    | 'bridge'
    | 'static_phase'
    | 'varargs'
    | 'native'
    | 'transient'
    | 'interface'
    | 'abstract'
    | 'strict'
    | 'synthetic'
    | 'annotation'
    | 'enum'
    | 'module'
    | 'mandated'
    ;

ANN_VISIBLE
    : 'build'
    | 'runtime'
    ;

DENUM         : '.enum';
DPARAM        : '.param';
DRESTARTLOCAL : '.restart local';

ID: FRAGMENT_ID;

CLASS_NAME : FRAGMENT_FULL_CLASS_NAME;

cFiles : cFile+ EOF;
cFile  : '.class' sAccList className=CLASS_NAME
         (sBytecode|sSuper|sInterface|sMethod|sField|sAttribute)*
         '.end class'?
       ;

sAccList: ACC*;

sAttribute
    : sSource
    | sSignature
    | sAnnotation
    | sAnnotationDefault
    ;

sBytecode  : '.bytecode' version=STRING;
sSource    : '.source' src=STRING;
sSuper	   : '.super' name=CLASS_NAME;
sInterface : '.implements' name=CLASS_NAME;
sMethod
	: '.method' sAccList methodObj=(METHOD_FULL|METHOD_PART)
        ( sAttribute
        | sInstruction
        | sDirective )*
	 '.end method';

sField
    : '.field' sAccList fieldObj=FIELD_PART ('=' sBaseValue)?
	  (sAttribute* '.end field')?
	;

sSignature: '.signature' sig=STRING;

sAnnotation
	: '.annotation' visibility=ANN_VISIBLE type=OBJECT_TYPE
	  ((sAnnotationKeyName '=' sAnnotationValue)* '.end annotation')?
	;

sAnnotationDefault
    : '.annotationdefault' value=sBaseValue;

sSubannotation
	: '.subannotation' type=OBJECT_TYPE (sAnnotationKeyName '=' sAnnotationValue )* '.end subannotation' ;

sAnnotationKeyName: name=ID ;

sAnnotationValue
	: sSubannotation
	| sBaseValue
	| sArrayValue
	;

sBaseValue
	: STRING
	| BOOLEAN
	| BYTE
	| SHORT
	| CHAR
	| INT
	| LONG
	| BASE_FLOAT
	| FLOAT_INFINITY
	| FLOAT_NAN
	| BASE_DOUBLE
	| DOUBLE_INFINITY
	| DOUBLE_NAN
	| METHOD_FULL
	| METHOD_PROTO
	| OBJECT_TYPE
	| ARRAY_TYPE
	| PRIMITIVE_TYPE
	| NULL
	| DENUM ENUM_FULL
	;

sArrayValue: '{' sAnnotationValue? (',' sAnnotationValue)* '}';

sInstruction
    : fReturn
    | fField
    ;

sDirective
    : fStack
    | fLocals
    | fLine
    | fStartlocal
    | fEndlocal
    ;

fStack     : '.stack'  maxStack=INT;
fLocals    : '.locals' maxLocals=INT;
fLine      : '.line'   line=INT;
fStartlocal: '.local'  variable=INT ',' name=STRING (':' descriptor=(PRIMITIVE_TYPE | OBJECT_TYPE | ARRAY_TYPE))? (',' signature=STRING)? ;
fEndlocal  : '.end local' variable=INT;

fReturn: op=
    ( 'areturn'
    | 'dreturn'
    | 'freturn'
    | 'ireturn'
    | 'lreturn'
    | 'return' )
    ;

fField: op=
    ( 'getfield'
    | 'getstatic'
    | 'putfield'
    | 'putstatic' ) fld=FIELD_FULL
    ;
