/**
* Initial code for the Parser
* Name: Zheyu Yang
* Assignment number: Project #3
* Date Due: October 05, 2018
*/
package cop5556fa18;

import java.util.ArrayList;
import java.util.List;

import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.Declaration;
import cop5556fa18.PLPAST.Expression;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionConditional;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.LHS;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.Statement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;

public class PLPParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}
	
	PLPScanner scanner;
	Token t;
	//PLPSymbolTable sT;
	PLPParser(PLPScanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
		//sT = new PLPSymbolTable();
	}
	
	public Program parse() throws SyntaxException {
		Program p = null;
		p = program();
		matchEOF();
		return p;
	}
	
	/*
	 * Program -> Identifier Block
	 */
	public Program program() throws SyntaxException {
		Program p = null;
		String name = String.copyValueOf(scanner.chars, t.pos, t.length);
		Block b = null;
		Token tp = t;
		match(Kind.IDENTIFIER);
		b = block();
		p = new Program(tp, name, b);
		return p;
	}
	
	/*
	 * Block ->  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float, Kind.KW_char, Kind.KW_string /* Complete this */ };
	Kind[] firstStatement = { Kind.KW_if, Kind.IDENTIFIER, Kind.KW_sleep, Kind.KW_print, Kind.KW_while/* Complete this */  };

	public Block block() throws SyntaxException {
		Block b = null;
		Statement s = null;
		Declaration d = null;
		List<PLPASTNode> lplp = new ArrayList<PLPASTNode>();
		Token tb = t;
		//sT.enterScope();
		match(Kind.LBRACE);
		while (checkKind(firstDec) | checkKind(firstStatement)) {
	    if (checkKind(firstDec)) {
			d = declaration();
			lplp.add(d);
		} else if (checkKind(firstStatement)) {
			s = statement();
			lplp.add(s);
		}
		match(Kind.SEMI);
		}
		match(Kind.RBRACE);
		b = new Block(tb, lplp);
		//sT.closeScope();
		return b;
	}
	/*
	 * Declaration
	 */
	
	public Declaration declaration() throws SyntaxException{
		Declaration d = null;
		Expression e = null;
		Token td = t;
		List<String> name = new ArrayList<String>();
		//throw new UnsupportedOperationException();
		type();
		name.add(String.copyValueOf(scanner.chars, t.pos, t.length));
		match(Kind.IDENTIFIER);
		while(checkKind(Kind.OP_ASSIGN) | checkKind(Kind.COMMA)) {
			if(checkKind(Kind.OP_ASSIGN)) {
				match(Kind.OP_ASSIGN);
				e = expression();
				//d = new VariableDeclaration(td, td.kind, name.get(0), e);
			} else if(checkKind(Kind.COMMA)) {
				match(Kind.COMMA);
				name.add(String.copyValueOf(scanner.chars, t.pos, t.length));
				match(Kind.IDENTIFIER);
				//d = new VariableListDeclaration(td, td.kind, name);
			}
		}
		if(name.size() == 1) {
			d = new VariableDeclaration(td, td.kind, name.get(0), e);
			//sT.add(name.get(0), sT.current_scope, td.kind);
		} else {
			d = new VariableListDeclaration(td, td.kind, name);
			/*for(int i = 0; i < name.size(); i++) {
				sT.add(name.get(i), sT.current_scope, td.kind);
			}	*/
		}
		return d;
	}
	/*
	 * Type
	 */
	public void type() throws SyntaxException {
		if(checkKind(Kind.KW_int)) {
			match(Kind.KW_int);
		} else if(checkKind(Kind.KW_boolean)){
			match(Kind.KW_boolean);
		} else if(checkKind(Kind.KW_float)){
			match(Kind.KW_float);
		} else if(checkKind(Kind.KW_char)){
			match(Kind.KW_char);
		} else if(checkKind(Kind.KW_string)){
			match(Kind.KW_string);
		} else {
			error(t);
		}
	}
	public Statement statement() throws SyntaxException {
		Statement s = null;
		while(checkKind(Kind.KW_if) | checkKind(Kind.IDENTIFIER) | checkKind(Kind.KW_sleep) | checkKind(Kind.KW_print) | checkKind(Kind.KW_while)) {
			if(checkKind(Kind.KW_if)) {
				s = ifStatement();
			} else if(checkKind(Kind.IDENTIFIER)) {
				s = assignmentStatement();
			} else if(checkKind(Kind.KW_sleep)) {
				s = sleepStatement();
			} else if(checkKind(Kind.KW_print)) {
				s = printStatement();
			} else if(checkKind(Kind.KW_while)) {
				s = whileStatement();
			}
		}
		return s;
	}
	
	//TODO Complete all other productions
	/*
	 * IfStatement
	 */
	public IfStatement ifStatement() throws SyntaxException {
		IfStatement i = null;
		Expression e = null;
		Block b = null;
		Token ti = t;
		match(Kind.KW_if);
		match(Kind.LPAREN);
		e = expression();
		match(Kind.RPAREN);
		b = block();
		i = new IfStatement(ti, e, b);
		return i;
	}
	/*
	 * AssignmentStatement
	 */
	public AssignmentStatement assignmentStatement() throws SyntaxException {
		AssignmentStatement a = null;
		Expression e = null;
		VariableListDeclaration d = null;
		LHS l = null;
		Token ta = t;
		String identifier =  String.copyValueOf(scanner.chars, t.pos, t.length);
		//d = (VariableListDeclaration)sT.lookup(identifier, ta);
		//if(d != null) {
			//l = new LHS(ta, identifier, d.type, d);
		//}	
		l = new LHS(ta, identifier);
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		e = expression();
		a = new AssignmentStatement(ta, l, e);
		return a;
	}
	/*
	 * SleepStatement
	 */
	public SleepStatement sleepStatement() throws SyntaxException {
		SleepStatement s = null;
		Expression e = null;
		Token ts = t;
		match(Kind.KW_sleep);
		e = expression();
		s = new SleepStatement(ts, e);
		return s;
	}
	/*
	 * PrintStatement
	 */
	public PrintStatement printStatement() throws SyntaxException {
		Expression e = null;
		PrintStatement p = null;
		Token tp = t;
		match(Kind.KW_print);
		e = expression();
		p = new PrintStatement(tp, e);
		return p;
	}
	/*
	 * WhileStatement
	 */
	public WhileStatement whileStatement() throws SyntaxException {
		WhileStatement w = null;
		Expression e = null;
		Block b = null;
		Token tw = t;
		match(Kind.KW_while);
		match(Kind.LPAREN);
		e = expression();
		match(Kind.RPAREN);
		b = block();
		w = new WhileStatement(tw, e, b);
		return w;
	}
	/*
	 * Expression
	 */
	Kind[] firstexpr = { Kind.OP_EXCLAMATION, Kind.OP_PLUS, Kind.OP_MINUS, Kind.INTEGER_LITERAL, Kind.BOOLEAN_LITERAL, Kind.FLOAT_LITERAL, Kind.CHAR_LITERAL, Kind.STRING_LITERAL, Kind.LPAREN, Kind.IDENTIFIER, Kind.KW_sin, Kind.KW_cos, Kind.KW_atan, Kind.KW_abs, Kind.KW_log, Kind.KW_int, Kind.KW_float};	
	public Expression expression() throws SyntaxException {
		Expression e1 = null;
		Expression e2 = null;
		Expression e3 = null;
		Token te = t;
		if(checkKind(firstexpr)) {
			e1 = orExpression();
			if(checkKind(Kind.OP_QUESTION)) {
				Token op = t;
				match(Kind.OP_QUESTION);
				e2 = expression();
				match(Kind.OP_COLON);
				e3 = expression();
				e1 = new ExpressionConditional(te, e1, e2, e3);
			}
		} else {
			error(t);
		}
		return e1;
	}
	/*
	 * OrExpression
	 */
	public Expression orExpression() throws SyntaxException {
		Expression e1 = null;
		Expression e2 = null;
		Token to = t;
		e1 = andExpression();
		while(checkKind(Kind.OP_OR)) {
			Token op = t;
			match(Kind.OP_OR);
			e2 = andExpression();
			e1 = new ExpressionBinary(to, e1, op.kind, e2);
		}
		return e1;
	}
	/*
	 * AndExpression
	 */
	public Expression andExpression() throws SyntaxException {
		Expression e1 = null;
		Expression e2 = null;
		Token ta = t;
		e1 = eqExpression();
		while(checkKind(Kind.OP_AND)) {
			Token op = t;
			match(Kind.OP_AND);
			e2 = eqExpression();
			e1 = new ExpressionBinary(ta, e1, op.kind, e2);
		}
		return e1;
	}
	/*
	 * EqExpression
	 */
	//Kind[] followRelExpr = { Kind.OP_EQ, Kind.OP_NEQ };
	public Expression eqExpression() throws SyntaxException {
		Expression e1 = null;
		Expression e2 = null;
		Token te = t;
		e1 = relExpression();
		while(checkKind(Kind.OP_EQ) | checkKind(Kind.OP_NEQ)) {
			Token op = t;
			if(checkKind(Kind.OP_EQ)) {
				match(Kind.OP_EQ);
			} else if(checkKind(Kind.OP_NEQ)) {
				match(Kind.OP_NEQ);
			}
			e2 = relExpression();
			e1 = new ExpressionBinary(te, e1, op.kind, e2);
		}
		return e1;
	}
	/*
	 * RelExpression
	 */
	public Expression relExpression() throws SyntaxException {
		Expression e1 = null;
		Expression e2 = null;
		Token tr = t;
		e1 = addExpression();
		while(checkKind(Kind.OP_GT) | checkKind(Kind.OP_LT) | checkKind(Kind.OP_GE) | checkKind(Kind.OP_LE)) {
			Token op = t;
			if(checkKind(Kind.OP_GT)) {
				match(Kind.OP_GT);
			} else if(checkKind(Kind.OP_LT)) {
				match(Kind.OP_LT);
			} else if(checkKind(Kind.OP_GE)) {
				match(Kind.OP_GE);
			} else if(checkKind(Kind.OP_LE)) {
				match(Kind.OP_LE);
			}
			e2 = addExpression();
			e1 = new ExpressionBinary(tr, e1, op.kind, e2);
		}
		return e1;
	}
	/*
	 * AddExpression
	 */
	public Expression addExpression() throws SyntaxException {
		Expression e1 = null;
		Expression e2 = null;
		Token ta = t;
		e1 = multExpression();
		while(checkKind(Kind.OP_PLUS) | checkKind(Kind.OP_MINUS)) {
			Token op = t;
			if(checkKind(Kind.OP_PLUS)) {
				match(Kind.OP_PLUS);
			} else if(checkKind(Kind.OP_MINUS)) {
				match(Kind.OP_MINUS);
			}
			e2 = multExpression();
			e1 = new ExpressionBinary(ta, e1, op.kind, e2);
		}
		return e1;
	}
	/*
	 * MultExpression
	 */
	public Expression multExpression() throws SyntaxException {
		Expression e1 = null;
		Expression e2 = null;
		Token tm = t;
		e1 = powerExpression();
		while(checkKind(Kind.OP_TIMES) | checkKind(Kind.OP_DIV) |checkKind(Kind.OP_MOD)) {
			Token op = t;
			if(checkKind(Kind.OP_TIMES)) {
				match(Kind.OP_TIMES);
			} else if(checkKind(Kind.OP_DIV)) {
				match(Kind.OP_DIV);
			} else if(checkKind(Kind.OP_MOD)) {
				match(Kind.OP_MOD);
			} 
			e2 = powerExpression();
			e1 = new ExpressionBinary(tm, e1, op.kind, e2);
		}	
		return e1;
	}
	/*
	 * PowerExpression
	 */
	public Expression powerExpression() throws SyntaxException {
		Expression e1 = null;
		Expression e2 = null;
		Token tp = t;
		e1 = unaryExpression();
		while(checkKind(Kind.OP_POWER)) {
			Token op = t;
			match(Kind.OP_POWER);
			e2 = powerExpression();
			e1 = new ExpressionBinary(tp, e1, op.kind, e2);
		}	
		return e1;
	}
	/*
	 * UnaryExpression 
	 */
	public Expression unaryExpression() throws SyntaxException {
		Expression e1 = null;
		Token tp = t;
		if(checkKind(Kind.OP_EXCLAMATION) | checkKind(Kind.OP_PLUS) | checkKind(Kind.OP_MINUS)) {
			Token op = t;
			if(checkKind(Kind.OP_EXCLAMATION)) {
				match(Kind.OP_EXCLAMATION);
			} else if(checkKind(Kind.OP_PLUS)) {
				match(Kind.OP_PLUS);
			} else if(checkKind(Kind.OP_MINUS)) {
				match(Kind.OP_MINUS);
			}
			e1 = unaryExpression();
			e1 = new ExpressionUnary(tp, op.kind, e1);
		} else {
			e1 = primary();
		}
		return e1;
	}
	/*
	 * Primary
	 */
	Kind[] firstPrimary = { Kind.INTEGER_LITERAL, Kind.BOOLEAN_LITERAL, Kind.FLOAT_LITERAL, Kind.CHAR_LITERAL, Kind.STRING_LITERAL, Kind.IDENTIFIER, Kind.LPAREN, Kind.KW_sin, Kind.KW_cos, Kind.KW_atan, Kind.KW_abs, Kind.KW_log, Kind.KW_int, Kind.KW_float};
	Kind[] firstFunction = { Kind.KW_sin, Kind.KW_cos, Kind.KW_atan, Kind.KW_abs, Kind.KW_log, Kind.KW_int, Kind.KW_float };
	public Expression primary() throws SyntaxException {
		Expression e = null;
		Token tp = t;
		if(checkKind(firstPrimary)) {
			if(checkKind(firstFunction)) {
				e = function();
			} else if(checkKind(Kind.LPAREN)) {
				match(Kind.LPAREN);
				e = expression();
				match(Kind.RPAREN);
			} else {
				if(checkKind(Kind.INTEGER_LITERAL)) {
					e = new ExpressionIntegerLiteral(tp, Integer.parseInt(String.copyValueOf(scanner.chars, t.pos, t.length)));
					match(Kind.INTEGER_LITERAL);				
				} else if(checkKind(Kind.BOOLEAN_LITERAL)) {
					e = new ExpressionBooleanLiteral(tp, Boolean.parseBoolean(String.copyValueOf(scanner.chars, t.pos, t.length)));
					match(Kind.BOOLEAN_LITERAL);
				} else if(checkKind(Kind.FLOAT_LITERAL)) {
					e = new ExpressionFloatLiteral(tp, Float.parseFloat(String.copyValueOf(scanner.chars, t.pos, t.length)));
					match(Kind.FLOAT_LITERAL);
				} else if(checkKind(Kind.CHAR_LITERAL)) {
					e = new ExpressionCharLiteral(tp, String.copyValueOf(scanner.chars, t.pos, t.length).charAt(1));
					match(Kind.CHAR_LITERAL);
				} else if(checkKind(Kind.STRING_LITERAL)) {
					e = new ExpressionStringLiteral(tp, String.copyValueOf(scanner.chars, (t.pos + 1), (t.length - 2)));
					match(Kind.STRING_LITERAL);
				} else if(checkKind(Kind.IDENTIFIER)) {
					e = new ExpressionIdentifier(tp, String.copyValueOf(scanner.chars, t.pos, t.length));
					match(Kind.IDENTIFIER);
				} 
			}
		} else {
			error(t);
		}
		return e;
	}
	/*
	 * Function
	 */
	public Expression function() throws SyntaxException {
		Expression e = null;
		Token tf = t;
		functionName();
		match(Kind.LPAREN);
		e = expression();
		e = new FunctionWithArg(tf, tf.kind, e);
		match(Kind.RPAREN);
		return e;
	}
	/*
	 * FunctionName
	 */
	public void functionName() throws SyntaxException {
		if(checkKind(Kind.KW_sin)) {
			match(Kind.KW_sin);
		} else if(checkKind(Kind.KW_cos)) {
			match(Kind.KW_cos);
		} else if(checkKind(Kind.KW_atan)) {
			match(Kind.KW_atan);
		} else if(checkKind(Kind.KW_abs)) {
			match(Kind.KW_abs);
		} else if(checkKind(Kind.KW_log)) {
			match(Kind.KW_log);
		} else if(checkKind(Kind.KW_int)) {
			match(Kind.KW_int);
		} else if(checkKind(Kind.KW_float)) {
			match(Kind.KW_float);
		} else { 
			error(t);
		}
	}
	protected boolean checkKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean checkKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}
	
	/**
	 * @param kind
	 * @return 
	 * @return
	 * @throws SyntaxException
	 */
	private void match(Kind kind) throws SyntaxException {
		if (kind == Kind.EOF) {
			System.out.println("End of file"); //return t;
		}
		else if (checkKind(kind)) {
			t = scanner.nextToken();
		}
		else {
			//TODO  give a better error message!
			//throw new SyntaxException(t," Syntax Error");
			error(t);
		}
	}
	private Token matchEOF() throws SyntaxException {
		if (checkKind(Kind.EOF)) {
			return t;
		} else {
			error(t);
			return t;
		}
	}
	/**
	 * @param t, kind
	 * @return
	 * @throws SyntaxException
	 */
	private void error(Token t) throws SyntaxException {
		String message = "line " + (scanner.line(t.pos) + 1) + ", position " + (t.pos + 1) + ", " + String.copyValueOf(scanner.chars, t.pos, t.length) +" : " + t.kind + " Syntax Error!";
		throw new SyntaxException(t, message);
	}
}
