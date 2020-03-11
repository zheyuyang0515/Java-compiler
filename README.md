# Java Compiler
## Stage 1:  implement a Java solution that will tokenize a string.
### Lexical Analysis

InputCharacter  -> any 7-bit ASCII character.

LineTerminator -> LF | CR | CR LF

	LF is the ASCII character also known as “newline”. The Java character literal is ‘\n’.
	CR is the ASII character also known as “return”, The Java character literal is ‘\r’.
	CR immediately followed by LF counts as one line terminator, not two. 

Input -> (WhisteSpace | Comment | Token)*

WhiteSpace  -> SP | HT | FF | LineTerminator

	SP is the ASCII character also known as “space”. The Java char literal is ‘ ‘.
	HT is the ASCII character also known as “horizontal tab”. The Java char literal is ‘\t’. 
	FF is the ASCII character also known as “form feed”. The Java char literal is ‘\f’. 

Comment ->  %{    (   (% NOT({) ) | NOT(%) )* %+}

	Comments will be identified and discarded [you do not need to keep them as a token].

Token  -> Identifier | Keyword | Literal | Separator | Operator

Identifier  -> IdentifierChar but not a keyword or a Boolean literal

IdentifierChar  -> IdentifierStart IdentifierPart*

IdentifierStart  -> UnderScoreStart IdentifierStart |A..Z IdentifierPart | a..z IdentifierPart

UnderScoreStart   -> _ UnderScoreStart | _

IdentifierPart -> A..Z IdentifierPart | a..z IdentifierPart | Digit IdentifierPart | _ IdentifierPart | 

Literal  -> IntegerLiteral | FloatingPointLiteral | BooleanLiteral | StringLiteral | CharLiteral

IntegerLiteral -> 0 | NonZeroDigit Digit*

FloatingPointLiteral ->  IntegerLiteral . Digit Digit*

StringLiteral -> " ASCII* "

CharLiteral -> ' ASCII '

ASCII ->  ASCII_CHAR | 

NoneZeroDigit ->  1 .. 9

Digit -> NonZeroDigit | 0

BooleanLiteral -> true | false

Separators -> ( | ) | [ | ] | ; | , | { | }

Operators ->  < | > | <= | >= | - | + | * | / | % | ! | ** | == | =

Keywords -> print | int | float | boolean | char | string | sleep | if


•If an illegal character is encountered, your scanner should throw a LexicalException. The message should contain useful information about the error. The contents of the message will not be graded, but you will appreciate it later if it is descriptive.

•If a numeric literal is provided that is out of the range of the Java equivalent of that type, then your scanner should throw a Lexical exception. The contents of the error message will not be graded, but you will appreciate it later if it is descriptive. 

•Use the provided PLPScanner.java and PLPScannerTest.java as starting points. The scanner will be part of all the subsequent assignments. Errors may cause failures in subsequent assignments. A careful job now, including a complete test suite developed now will help you later.

------------
### Valid [Parse-able] Cases

boolean a;

int b, x, y;

char  c;

double d, t;

string e;


a = true;

B = 10;

c = ‘a’;

d = 23.2;

e = “Hello, World!”;


a = 1+2;

d = 2.12 – 1;

a==3;

a  = 1 + 2 *4.5;

t = (1+2) * 4.5;

t = (((4-2)*5.6)/3)+2;

t = 4 - 2 * 5.6 / 3;


int score = 100;


if ( a==100 ){

  print (“Value of a is 100”);
  
}

if ( score > 100 ) {

  print( a );
  
  print(B);
  
  print(score);
  
}
