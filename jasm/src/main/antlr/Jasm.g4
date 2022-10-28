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
FRAGMENT_METHOD_DESCRIPTOR: '(' (FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE|FRAGMENT_PRIMITIVE_TYPE)* ')' ('V' | FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE|FRAGMENT_PRIMITIVE_TYPE) ;

fragment
FRAGMENT_FIELD_PART: FRAGMENT_MEMBER_NAME ':' (FRAGMENT_OBJECT_TYPE|FRAGMENT_ARRAY_TYPE|FRAGMENT_PRIMITIVE_TYPE) ;

METHOD_FULL  : (FRAGMENT_FULL_CLASS_NAME|ARRAY_TYPE) '->' FRAGMENT_MEMBER_NAME FRAGMENT_METHOD_DESCRIPTOR;
METHOD_PART  : FRAGMENT_MEMBER_NAME FRAGMENT_METHOD_DESCRIPTOR;
METHOD_DESCRIPTOR : FRAGMENT_METHOD_DESCRIPTOR;

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
    | sEnclosingMethod
    | sDeprecated
    | sInnerClass
    | sThrows
    ;

sBytecode  : '.bytecode' version=STRING;
sSource    : '.source' src=STRING;
sSuper	   : '.super' name=CLASS_NAME;
sInterface : '.implements' name=CLASS_NAME;
sMethod
	: '.method' sAccList methodObj=METHOD_PART
        ( sAttribute
        | sParameter
        | sInstruction )*
	 '.end method' ;

sField
    : '.field' sAccList fieldObj=FIELD_PART ('=' sBaseValue)?
	  (sAttribute* '.end field')?
	;

sSignature: '.signature' sig=STRING ;

sDeprecated: '.deprecated' ;

sAnnotation
	: '.annotation' visibility=ANN_VISIBLE type=OBJECT_TYPE
	  ((sAnnotationKeyName '=' sAnnotationValue)* '.end annotation')?
	;

sAnnotationDefault
    : '.annotationdefault' value=sBaseValue ;

sEnclosingMethod
    : '.enclosingmethod' methodObj=(CLASS_NAME | METHOD_FULL) ;

sThrows
    : '.throws' className=CLASS_NAME ;

sInnerClass
    : '.innerclass' sAccList innerClass=CLASS_NAME ('as' name=ID)? ('in' outerClass=CLASS_NAME)? ;

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
	| METHOD_DESCRIPTOR
	| CLASS_NAME
	| OBJECT_TYPE
	| ARRAY_TYPE
	| PRIMITIVE_TYPE
	| NULL
	| DENUM ENUM_FULL
	;

sArrayValue: '{' sAnnotationValue? (',' sAnnotationValue)* '}' ;

sParameter: param=DPARAM parameterIndex=INT (',' name=STRING)? (sAnnotation* '.end param')? ;

sInstruction
    : sLabel
    | fCatch
    | fCatchall
    | fReturnInstructions
    | fFieldInstructions
    | fImplicitVariableInstructions
    | fExplicitVariableInstructions
    | fMethodInstructions
    | fInterfaceMethodInstructions
    | fImplicitLiteralInstructions
    | fExplicitLiteralInstruction
    | fClassInstructions
    | fStackInstructions
    | fLiteralConstantInstructions
    | fWideLiteralConstantInstructions
    | fExceptionInstructions
    | fNullReferenceInstructions
    | fBranchInstructions
    | fArrayInstructions
    | fPrimitiveArrayInstructions
    | fArrayClassInstructions
    | fMultiArrayClassInstruction
    | fLookupSwitch
    | fTableSwitch
    | fArithmeticInstructions
    | fConversionInstructions
    | fLiteralVariableInstructions
    | fMonitorInstructions
    | fCompareInstructions
    | fInvokeDynamicInstructions
    | fLine
    | fStartlocal
    | fEndlocal
    | fMaxStack
    | fMaxLocals
    ;

fMaxStack  : '.maxstack'  maxStack=INT;
fMaxLocals : '.maxlocals' maxLocals=INT;
fLine      : '.line' line=INT;
fStartlocal: '.local' variable=INT ',' name=STRING (':' descriptor=(PRIMITIVE_TYPE | OBJECT_TYPE | ARRAY_TYPE))? (',' signature=STRING)? ;
fEndlocal  : '.end local' variable=INT;
sLabel     : ':' label=ID;
fCatch     : '.catch' type=CLASS_NAME '{' start=sLabel '..' end=sLabel  '}' handle=sLabel ;
fCatchall  : '.catchall' '{' start=sLabel '..' end=sLabel  '}' handle=sLabel ;

fReturnInstructions: op=
    ( 'areturn'
    | 'dreturn'
    | 'freturn'
    | 'ireturn'
    | 'lreturn'
    | 'return' )
    ;

fFieldInstructions: op=
    ( 'getfield'
    | 'getstatic'
    | 'putfield'
    | 'putstatic' ) fld=FIELD_FULL
    ;

fImplicitVariableInstructions: op=
    ( 'aload_0'
    | 'aload_1'
    | 'aload_2'
    | 'aload_3'
    | 'astore_0'
    | 'astore_1'
    | 'astore_2'
    | 'astore_3'
    | 'dload_0'
    | 'dload_1'
    | 'dload_2'
    | 'dload_3'
    | 'dstore_0'
    | 'dstore_1'
    | 'dstore_2'
    | 'dstore_3'
    | 'fload_0'
    | 'fload_1'
    | 'fload_2'
    | 'fload_3'
    | 'fstore_0'
    | 'fstore_1'
    | 'fstore_2'
    | 'fstore_3'
    | 'iload_0'
    | 'iload_1'
    | 'iload_2'
    | 'iload_3'
    | 'istore_0'
    | 'istore_1'
    | 'istore_2'
    | 'istore_3'
    | 'lload_0'
    | 'lload_1'
    | 'lload_2'
    | 'lload_3'
    | 'lstore_0'
    | 'lstore_1'
    | 'lstore_2'
    | 'lstore_3')
    ;

fExplicitVariableInstructions: op=
    ( 'aload'
    | 'astore'
    | 'dload'
    | 'dstore'
    | 'fload'
    | 'fstore'
    | 'iload'
    | 'istore'
    | 'lload'
    | 'lstore'
    | 'ret' ) variable=INT
    ;

fMethodInstructions: op=
    ( 'invokespecial'
    | 'invokestatic'
    | 'invokevirtual' ) method=METHOD_FULL
    ;

fInterfaceMethodInstructions: op='invokeinterface' method=METHOD_FULL;

fImplicitLiteralInstructions: op=
    ( 'dconst_0'
    | 'dconst_1'
    | 'fconst_0'
    | 'fconst_1'
    | 'fconst_2'
    | 'iconst_m1'
    | 'iconst_0'
    | 'iconst_1'
    | 'iconst_2'
    | 'iconst_3'
    | 'iconst_4'
    | 'iconst_5'
    | 'lconst_0'
    | 'lconst_1' )
    ;

fExplicitLiteralInstruction: op=
    ( 'bipush'
    | 'sipush' ) value=INT
    ;

fClassInstructions: op=
    ( 'new'
    | 'checkcast'
    | 'instanceof' ) className=(CLASS_NAME|ARRAY_TYPE)
    ;

fStackInstructions: op=
    ( 'dup'
    | 'dup_x1'
    | 'dup_x2'
    | 'dup2'
    | 'dup2_x1'
    | 'dup2_x2'
    | 'pop'
    | 'pop2'
    | 'swap' )
    ;

fLiteralConstantInstructions: op='ldc' value=sBaseValue;

fWideLiteralConstantInstructions: op=
    ( 'ldc_w'
    | 'ldc2_w' ) value=sBaseValue
    ;

fExceptionInstructions: op='athrow';

fNullReferenceInstructions: op='aconst_null';

fBranchInstructions: op=
    ( 'if_acmpeq'
    | 'if_acmpne'
    | 'if_icmpeq'
    | 'if_icmpne'
    | 'if_icmplt'
    | 'if_icmpge'
    | 'if_icmpgt'
    | 'if_icmple'
    | 'ifeq'
    | 'ifne'
    | 'iflt'
    | 'ifge'
    | 'ifgt'
    | 'ifle'
    | 'ifnonnull'
    | 'ifnull'
    | 'goto' ) label=sLabel
    ;

fArrayInstructions: op=
    ( 'aaload'
    | 'aastore'
    | 'baload'
    | 'bastore'
    | 'caload'
    | 'castore'
    | 'daload'
    | 'dastore'
    | 'faload'
    | 'fastore'
    | 'iaload'
    | 'iastore'
    | 'laload'
    | 'lastore'
    | 'saload'
    | 'sastore'
    | 'arraylength' )
    ;

fPrimitiveArrayInstructions: op='newarray' type=('boolean' | 'char' | 'float' | 'double' | 'byte' | 'short' | 'int' | 'long');

fArrayClassInstructions: op='anewarray' className=(CLASS_NAME | ARRAY_TYPE) ;

fMultiArrayClassInstruction: op='multianewarray' className=(CLASS_NAME | ARRAY_TYPE) ',' dimension=INT ;

sSwitchKey: key=(INT | 'default') ;

fLookupSwitch: op='lookupswitch' '{' (sSwitchKey '->' sLabel)* '}';

fTableSwitch: op='tableswitch' '{' (sSwitchKey '->' sLabel)* '}';

fArithmeticInstructions: op=
    ( 'dadd'
    | 'ddiv'
    | 'dmul'
    | 'dneg'
    | 'drem'
    | 'dsub'
    | 'fadd'
    | 'fdiv'
    | 'fmul'
    | 'fneg'
    | 'frem'
    | 'fsub'
    | 'iadd'
    | 'iand'
    | 'idiv'
    | 'imul'
    | 'ineg'
    | 'ior'
    | 'irem'
    | 'ishl'
    | 'ishr'
    | 'isub'
    | 'iushr'
    | 'ixor'
    | 'ladd'
    | 'land'
    | 'ldiv'
    | 'lmul'
    | 'lneg'
    | 'lor'
    | 'lrem'
    | 'lshl'
    | 'lshr'
    | 'lsub'
    | 'lushr'
    | 'lxor' )
    ;

fConversionInstructions: op=
    ( 'd2f'
    | 'd2i'
    | 'd2l'
    | 'f2d'
    | 'f2i'
    | 'f2l'
    | 'i2b'
    | 'i2c'
    | 'i2d'
    | 'i2f'
    | 'i2l'
    | 'i2s'
    | 'l2d'
    | 'l2f'
    | 'l2i' )
    ;

fLiteralVariableInstructions: op='iinc' variable=INT ',' value=INT ;

fMonitorInstructions: op=
    ( 'monitorenter'
    | 'monitorexit' )
    ;

fCompareInstructions: op=
    ( 'dcmpg'
    | 'dcmpl'
    | 'fcmpg'
    | 'fcmpl'
    | 'lcmp' )
    ;

fInvokeDynamicInstructions: op='invokedynamic' ID '@' method=METHOD_PART ;