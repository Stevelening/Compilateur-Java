lexer grammar DecaLexer;

options {
   language=Java;
   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and
   // variables will be placed.
   superClass = AbstractDecaLexer;
}

@members {
}

// Deca lexer rules.
EOL : '\n' {skip();};

ASM : 'asm';
CLASS : 'class';
EXTENDS : 'extends';
ELSE : 'else';
FALSE : 'false';
IF : 'if';
INSTANCEOF : 'instanceof';
NEW : 'new';
NULL : 'null';
READINT : 'readInt';
READFLOAT : 'readFloat';
PRINT : 'print';
PRINTLN : 'println';
PRINTLNX : 'printlnx';
PRINTX : 'printx';
PROTECTED : 'protected';
RETURN : 'return';
THIS : 'this';
TRUE : 'true';
WHILE : 'while';

fragment LETTER : 'a' .. 'z' | 'A' .. 'Z';
fragment DIGIT : '0' .. '9';
IDENT : (LETTER | '$' | '_')(LETTER | DIGIT | '$' | '_')*;

LT : '<';
GT : '>';
EQUALS : '=';
PLUS : '+';
MINUS : '-';
TIMES : '*';
SLASH : '/';
PERCENT : '%';
DOT : '.';
COMMA : ',';
OPARENT : '(';
CPARENT : ')';
OBRACE : '{';
CBRACE : '}';
EXCLAM : '!';
SEMI : ';';
EQEQ : '==';
NEQ : '!=';
GEQ : '>=';
LEQ : '<=';
AND : '&&';
OR : '||';

fragment POSITIVE_DIGIT : '1' .. '9';
INT : '0' | POSITIVE_DIGIT DIGIT*{
   try {
   int intValue = Integer.parseInt(getText());
   }
   catch (NumberFormatException e){
      throw new DecaRecognitionException(this, getInputStream());
   }
};

fragment NUM : DIGIT+;
fragment SIGN : PLUS | MINUS;
fragment EXP : ('E' | 'e') SIGN? NUM;
fragment DEC : NUM DOT NUM;
fragment FLOATDEC : (DEC | DEC EXP)('F' | 'f')?;
fragment DIGITHEX : DIGIT | 'A' .. 'F' | 'a' .. 'f';
fragment NUMHEX : DIGITHEX+;
fragment FLOATHEX : ('0x' | '0X') NUMHEX '.' NUMHEX ('P' | 'p') SIGN? NUM ('F' | 'f')?;
FLOAT : FLOATDEC | FLOATHEX {
   try {
      float floatValue = Float.parseFloat(getText());
      if (Float.isInfinite(floatValue) || Float.isNaN(floatValue)) {
         throw new RuntimeException("Littéral flottant trop grand ou trop petit: " + getText());
      }
   } 
   catch (NumberFormatException e) {
          throw new DecaRecognitionException(this, getInputStream());
   }
};

fragment STRING_CAR :~('"' | '\\' | '\n');
STRING : '"' (STRING_CAR | '\\"' | '\\\\')* '"';
MULTI_LINE_STRING : '"' (STRING_CAR | '\n' | '\\"' | '\\\\')* '"';

fragment FILENAME : (LETTER | DIGIT | DOT | MINUS | '_')+;
INCLUDE : '#include' (' ')* '"' FILENAME '"' {
   doInclude(getText());
   skip();
};

COMMENT : '/*' .*? '*/' { skip(); } ;
COMMENTMONO : '//' .*? (EOL | EOF) { skip(); } ;

SEPARATE : (' ' | '\t' | '\r') { skip(); } ;

DEFAULT : .{
   if (true){
      throw new DecaRecognitionException(this, getInputStream());
   }
};

