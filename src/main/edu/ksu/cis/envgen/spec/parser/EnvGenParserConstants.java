/* Generated By:JavaCC: Do not edit this line. EnvGenParserConstants.java */
package edu.ksu.cis.envgen.spec.parser;

public interface EnvGenParserConstants {

  int EOF = 0;
  int SINGLE_LINE_COMMENT = 9;
  int FORMAL_COMMENT = 11;
  int MULTI_LINE_COMMENT = 12;
  int ENVIRONMENT = 14;
  int SETUP = 15;
  int CLEANUP = 16;
  int PROPERTIES = 17;
  int REASSUMPTIONS = 18;
  int LTLASSUMPTIONS = 19;
  int STUBASSUMPTIONS = 20;
  int DRIVERASSUMPTIONS = 21;
  int GLOBALLY = 22;
  int EVENTUALLY = 23;
  int NEXT = 24;
  int UNTIL = 25;
  int V = 26;
  int TRUE = 27;
  int FALSE = 28;
  int ABSTRACT = 29;
  int BOOLEAN = 30;
  int BREAK = 31;
  int BYTE = 32;
  int CASE = 33;
  int CATCH = 34;
  int CHAR = 35;
  int CLASS = 36;
  int CONST = 37;
  int CONTINUE = 38;
  int _DEFAULT = 39;
  int DO = 40;
  int DOUBLE = 41;
  int ELSE = 42;
  int EXTENDS = 43;
  int FINAL = 44;
  int FINALLY = 45;
  int FLOAT = 46;
  int FOR = 47;
  int GOTO = 48;
  int IF = 49;
  int IMPLEMENTS = 50;
  int IMPORT = 51;
  int INSTANCEOF = 52;
  int INT = 53;
  int INTERFACE = 54;
  int LONG = 55;
  int NATIVE = 56;
  int NEW = 57;
  int NULL = 58;
  int PACKAGE = 59;
  int PRIVATE = 60;
  int PROTECTED = 61;
  int PUBLIC = 62;
  int RETURN = 63;
  int SHORT = 64;
  int STATIC = 65;
  int SUPER = 66;
  int SWITCH = 67;
  int SYNCHRONIZED = 68;
  int THIS = 69;
  int THROW = 70;
  int THROWS = 71;
  int TRANSIENT = 72;
  int TRY = 73;
  int VOID = 74;
  int VOLATILE = 75;
  int WHILE = 76;
  int OR = 77;
  int MANY = 78;
  int MANY1 = 79;
  int MANYW = 80;
  int MAYBE = 81;
  int NOT = 82;
  int TIMES = 83;
  int INTEGER_LITERAL = 84;
  int DECIMAL_LITERAL = 85;
  int HEX_LITERAL = 86;
  int OCTAL_LITERAL = 87;
  int FLOATING_POINT_LITERAL = 88;
  int EXPONENT = 89;
  int CHARACTER_LITERAL = 90;
  int STRING_LITERAL = 91;
  int IDENTIFIER = 92;
  int LETTER = 93;
  int DIGIT = 94;
  int LPAREN = 95;
  int RPAREN = 96;
  int LBRACE = 97;
  int RBRACE = 98;
  int LBRACKET = 99;
  int RBRACKET = 100;
  int SEMICOLON = 101;
  int COMMA = 102;
  int DOT = 103;
  int ASSIGN = 104;
  int GT = 105;
  int LT = 106;
  int BANG = 107;
  int TILDE = 108;
  int HOOK = 109;
  int COLON = 110;
  int EQ = 111;
  int LE = 112;
  int GE = 113;
  int NE = 114;
  int SC_OR = 115;
  int SC_AND = 116;
  int INCR = 117;
  int DECR = 118;
  int PLUS = 119;
  int MINUS = 120;
  int STAR = 121;
  int SLASH = 122;
  int BIT_AND = 123;
  int BIT_OR = 124;
  int XOR = 125;
  int REM = 126;
  int IMPLICATION = 127;
  int EQUIVALENCE = 128;

  int DEFAULT = 0;
  int IN_SINGLE_LINE_COMMENT = 1;
  int IN_FORMAL_COMMENT = 2;
  int IN_MULTI_LINE_COMMENT = 3;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\f\"",
    "\"//\"",
    "<token of kind 7>",
    "\"/*\"",
    "<SINGLE_LINE_COMMENT>",
    "<token of kind 10>",
    "\"*/\"",
    "\"*/\"",
    "<token of kind 13>",
    "\"environment\"",
    "\"setup\"",
    "\"cleanup\"",
    "\"properties\"",
    "\"re\"",
    "\"ltl\"",
    "\"stub-assumptions\"",
    "\"driver-assumptions\"",
    "\"[]\"",
    "\"<>\"",
    "\"X\"",
    "\"U\"",
    "\"V\"",
    "\"true\"",
    "\"false\"",
    "\"abstract\"",
    "\"boolean\"",
    "\"break\"",
    "\"byte\"",
    "\"case\"",
    "\"catch\"",
    "\"char\"",
    "\"class\"",
    "\"const\"",
    "\"continue\"",
    "\"default\"",
    "\"do\"",
    "\"double\"",
    "\"else\"",
    "\"extends\"",
    "\"final\"",
    "\"finally\"",
    "\"float\"",
    "\"for\"",
    "\"goto\"",
    "\"if\"",
    "\"implements\"",
    "\"import\"",
    "\"instanceof\"",
    "\"int\"",
    "\"interface\"",
    "\"long\"",
    "\"native\"",
    "\"new\"",
    "\"null\"",
    "\"package\"",
    "\"private\"",
    "\"protected\"",
    "\"public\"",
    "\"return\"",
    "\"short\"",
    "\"static\"",
    "\"super\"",
    "\"switch\"",
    "\"synchronized\"",
    "\"this\"",
    "\"throw\"",
    "\"throws\"",
    "\"transient\"",
    "\"try\"",
    "\"void\"",
    "\"volatile\"",
    "\"while\"",
    "\"OR\"",
    "\"MANY\"",
    "\"MANY1\"",
    "\"MANYW\"",
    "\"MAYBE\"",
    "\"NOT\"",
    "\"TIMES\"",
    "<INTEGER_LITERAL>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<OCTAL_LITERAL>",
    "<FLOATING_POINT_LITERAL>",
    "<EXPONENT>",
    "<CHARACTER_LITERAL>",
    "<STRING_LITERAL>",
    "<IDENTIFIER>",
    "<LETTER>",
    "<DIGIT>",
    "\"(\"",
    "\")\"",
    "\"{\"",
    "\"}\"",
    "\"[\"",
    "\"]\"",
    "\";\"",
    "\",\"",
    "\".\"",
    "\"=\"",
    "\">\"",
    "\"<\"",
    "\"!\"",
    "\"~\"",
    "\"?\"",
    "\":\"",
    "\"==\"",
    "\"<=\"",
    "\">=\"",
    "\"!=\"",
    "\"||\"",
    "\"&&\"",
    "\"++\"",
    "\"--\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"&\"",
    "\"|\"",
    "\"^\"",
    "\"%\"",
    "\"-->\"",
    "\"<==>\"",
    "\"#\"",
    "\"*=\"",
    "\"/=\"",
    "\"%=\"",
    "\"+=\"",
    "\"-=\"",
    "\"<<=\"",
    "\">>=\"",
    "\">>>=\"",
    "\"&=\"",
    "\"^=\"",
    "\"|=\"",
    "\"<<\"",
    "\">>\"",
    "\">>>\"",
  };

}
