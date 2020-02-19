/**
* Initial code for the Typechecker
* Name: Zheyu Yang
* Assignment number: Project #5
* Date Due: November 05, 2018
*/
package cop5556fa18;


import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPTypes.Type;
import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
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
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;

public class PLPTypeChecker implements PLPASTVisitor {
	PLPTypeChecker() {
	}
	PLPSymbolTable sT = new PLPSymbolTable();
	//PLPSymbolTable sT = new PLPSymbolTable();
	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}
	private Type inferTypeExprBinary(Type type0, Type type1, Kind op) {
		Type exprBinaryType = null;
		if(type0 == PLPTypes.getType(Kind.KW_int) && type1 == PLPTypes.getType(Kind.KW_int) && ((op == Kind.OP_PLUS) || (op == Kind.OP_MINUS) || (op == Kind.OP_TIMES) || (op == Kind.OP_DIV) || (op == Kind.OP_MOD) || (op == Kind.OP_POWER) || (op == Kind.OP_AND) || (op == Kind.OP_OR))) {
			exprBinaryType = Type.INTEGER;
		} else if(type0 == PLPTypes.getType(Kind.KW_float) && type1 == PLPTypes.getType(Kind.KW_float) && ((op == Kind.OP_PLUS) || (op == Kind.OP_MINUS) || (op == Kind.OP_TIMES) || (op == Kind.OP_DIV) || (op == Kind.OP_POWER))) {
			exprBinaryType = Type.FLOAT;
		} else if(type0 == PLPTypes.getType(Kind.KW_float) && type1 == PLPTypes.getType(Kind.KW_int) && ((op == Kind.OP_PLUS) || (op == Kind.OP_MINUS) || (op == Kind.OP_TIMES) || (op == Kind.OP_DIV) || (op == Kind.OP_POWER))) {
			exprBinaryType = Type.FLOAT;
		} else if(type0 == PLPTypes.getType(Kind.KW_int) && type1 == PLPTypes.getType(Kind.KW_float) && ((op == Kind.OP_PLUS) || (op == Kind.OP_MINUS) || (op == Kind.OP_TIMES) || (op == Kind.OP_DIV) || (op == Kind.OP_POWER))) {
			exprBinaryType = Type.FLOAT;
		} else if(type0 == PLPTypes.getType(Kind.KW_string) && type1 == PLPTypes.getType(Kind.KW_string) && op == Kind.OP_PLUS) {
			exprBinaryType = Type.STRING;
		} else if(type0 == PLPTypes.getType(Kind.KW_boolean) && type1 == PLPTypes.getType(Kind.KW_boolean) && ((op == Kind.OP_AND) || (op == Kind.OP_OR))) {
			exprBinaryType = Type.BOOLEAN;
		} else if(type0 ==PLPTypes.getType( Kind.KW_int) && type1 == PLPTypes.getType(Kind.KW_int) && ((op == Kind.OP_EQ) || (op == Kind.OP_NEQ) || (op == Kind.OP_GT) || (op == Kind.OP_GE) || (op == Kind.OP_LT) || (op == Kind.OP_LE))) {
			exprBinaryType = Type.BOOLEAN;
		} else if(type0 == PLPTypes.getType(Kind.KW_float) && type1 == PLPTypes.getType(Kind.KW_float) && ((op == Kind.OP_EQ) || (op == Kind.OP_NEQ) || (op == Kind.OP_GT) || (op == Kind.OP_GE) || (op == Kind.OP_LT) || (op == Kind.OP_LE))) {
			exprBinaryType = Type.BOOLEAN;
		} else if(type0 == PLPTypes.getType(Kind.KW_boolean) && type1 == PLPTypes.getType(Kind.KW_boolean) && ((op == Kind.OP_EQ) || (op == Kind.OP_NEQ) || (op == Kind.OP_GT) || (op == Kind.OP_GE) || (op == Kind.OP_LT) || (op == Kind.OP_LE))) {
			exprBinaryType =Type.BOOLEAN;
		}     
		return exprBinaryType;
	}
	private Type inferTypeFuncWithArg(Kind funcName, Type type) {
		Type FuncWithArgType = null;
		if(type == PLPTypes.getType(Kind.KW_int) && funcName == Kind.KW_abs) {
			FuncWithArgType = Type.INTEGER;
		} else if(type == PLPTypes.getType(Kind.KW_float) && ((funcName == Kind.KW_abs) || (funcName == Kind.KW_sin) || (funcName == Kind.KW_cos) || (funcName == Kind.KW_atan) || (funcName == Kind.KW_log))) {
			FuncWithArgType = Type.FLOAT;
		} else if(type == PLPTypes.getType(Kind.KW_int) && funcName == Kind.KW_float) {
			FuncWithArgType = Type.FLOAT;
		} else if(type == PLPTypes.getType(Kind.KW_float) && funcName == Kind.KW_float) {
			FuncWithArgType = Type.FLOAT;
		} else if(type == PLPTypes.getType(Kind.KW_float) && funcName == Kind.KW_int) {
			FuncWithArgType = Type.INTEGER;
		} else if(type == PLPTypes.getType(Kind.KW_int) && funcName == Kind.KW_int) {
			FuncWithArgType = Type.INTEGER;
		}
		return FuncWithArgType;
	}
	private void error(Token t, String message) throws SemanticException {
		throw new SemanticException(t, message);
	}
	// Name is only used for naming the output file. 
		// Visit the child block to type check program.
		@Override
		public Object visitProgram(Program program, Object arg) throws Exception {
			//System.out.println("program");
			program.block.visit(this, arg);
			return null;
		}
		
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		sT.enterScope();
		for (int i = 0; i < block.declarationsAndStatements.size(); i++) {				
			block.declarationsAndStatements.get(i).visit(this, arg);
		}	
		sT.closeScope();
		return null;
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("VariDec");
		Type type = PLPTypes.getType(declaration.type);
		int scopeNum = sT.checkScope(declaration.name);
		if(scopeNum != -1 && scopeNum >= sT.current_scope) {
			error(declaration.firstToken, (declaration.name + " Variable Declare repeatly"));
		}
		if(declaration.expression == null) {
			sT.add(declaration, sT.current_scope);
			//System.out.println("add: " + declaration.name +" scope: "+sT.current_scope);
		} else{
			declaration.expression.visit(this, arg);
			Type exprType = declaration.expression.type;
			if(type != exprType) {
				error(declaration.firstToken, (declaration.firstToken + " Type mismatch: cannot convert from " + type + " to " + exprType));
			} else {
				sT.add(declaration, sT.current_scope);
				//System.out.println("add: " + declaration.name +" scope: "+sT.current_scope);
			}
		}
		return null;	
	}
	
	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		VariableDeclaration declaration0 = null;
		int scopeNum;
		for(int i = 0; i < declaration.names.size(); i++){
			scopeNum = sT.checkScope(declaration.names.get(i));
			if(scopeNum != -1 && scopeNum >= sT.current_scope)  {
				error(declaration.firstToken, (declaration.names.get(i) + " Variable Declare repeatly"));
			} else {
				declaration0 = new VariableDeclaration(declaration.firstToken, declaration.type, declaration.names.get(i), null);
				//System.out.println(declaration.names.get(i)+" "+declaration.type+" "+sT.current_scope);
				sT.add(declaration0, sT.current_scope);
				//System.out.println("add: " + declaration0.name +" scope: "+sT.current_scope);
			}
			
		}
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("bool");
		expressionBooleanLiteral.type = PLPTypes.getType(Kind.KW_boolean);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		//System.out.println("Binary");
		expressionBinary.leftExpression.visit(this, arg);
		Type type0 = expressionBinary.leftExpression.type;
		expressionBinary.rightExpression.visit(this, arg);
		Type type1 = expressionBinary.rightExpression.type;
		Kind op = expressionBinary.op;
		Type exprBinaryType = inferTypeExprBinary(type0, type1, op);
		if(exprBinaryType == null) {
			error(expressionBinary.firstToken, (expressionBinary.firstToken + " Type error: " + type0 + " " + op + " " + type1));
		}
		expressionBinary.type = exprBinaryType;
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		expressionConditional.condition.visit(this, arg);
		Type type0 = expressionConditional.condition.type;
		expressionConditional.trueExpression.visit(this, arg);
		Type type1 = expressionConditional.trueExpression.type;
		expressionConditional.falseExpression.visit(this, arg);
		Type type2 = expressionConditional.falseExpression.type;
		if(type0 != Type.BOOLEAN) {
			error(expressionConditional.firstToken, (expressionConditional.firstToken + " Type error: should be BOOLEAN, but " + type0));
		}
		if(type1 != type2) {
			error(expressionConditional.firstToken, (expressionConditional.firstToken + " Type mismatch:" + type1 + " and " + type2));
		}
		expressionConditional.type = type1;
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionFloatLiteral.type = PLPTypes.getType(Kind.KW_float);
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		Kind funcName = FunctionWithArg.functionName;
		FunctionWithArg.expression.visit(this, arg);
		Type type = FunctionWithArg.expression.type;
		Type FuncWithArgType = inferTypeFuncWithArg(funcName, type);
		if(FuncWithArgType == null) {
			error(FunctionWithArg.firstToken, (FunctionWithArg.firstToken + " Type error"));
		}
		FunctionWithArg.type = FuncWithArgType;
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		//System.out.println("Ident");
		VariableDeclaration dec = null;
		//System.out.println(expressionIdent.name+" search");
		dec = (VariableDeclaration)sT.lookup(expressionIdent.name);
		if(dec == null) {
			error(expressionIdent.firstToken, (expressionIdent.firstToken + "Variable not found"));
		}
		expressionIdent.type = PLPTypes.getType(dec.type);
		expressionIdent.declaration = dec;
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionIntegerLiteral.type = PLPTypes.getType(Kind.KW_int);
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionStringLiteral.type = PLPTypes.getType(Kind.KW_string);
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		expressionCharLiteral.type = PLPTypes.getType(Kind.KW_char);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		//System.out.println("assign");
		statementAssign.expression.visit(this, arg);
		Type exprType = statementAssign.expression.type;
		statementAssign.lhs.visit(this, arg);
		Type lhsType = statementAssign.lhs.type;
		//System.out.println("exprtype:" + exprType +", lhstype" + lhsType);
		if(lhsType != exprType) {
			error(statementAssign.firstToken, (statementAssign.firstToken + " Type mismatch: " + lhsType + " and " + exprType));
		}
		return null; 
		
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		ifStatement.condition.visit(this, arg);
		Type type = ifStatement.condition.type;
		if(type != Type.BOOLEAN) {
			error(ifStatement.firstToken, (ifStatement.firstToken + " Type error: should be BOOLEAN, but " + type));
		}
		ifStatement.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("while");
		whileStatement.condition.visit(this, arg);
		Type type = whileStatement.condition.type;
		if(type != Type.BOOLEAN) {
			error(whileStatement.firstToken, (whileStatement.firstToken + " Type error: should be BOOLEAN, but " + type));
		}
		whileStatement.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		printStatement.expression.visit(this, arg);
		Type type = printStatement.expression.type;
		if(type != Type.BOOLEAN && type != Type.INTEGER && type != Type.FLOAT && type != Type.CHAR && type != Type.STRING) {
			error(printStatement.firstToken, (printStatement.firstToken + " Type error: type could not match, " + type));
		}
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		sleepStatement.time.visit(this, arg);
		Type type = sleepStatement.time.type;
		if(type != Type.INTEGER) {
			error(sleepStatement.firstToken, (sleepStatement.firstToken + " Type error: should be Integer, but " + type));
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		expressionUnary.expression.visit(this, arg);
		Type exprType0 = expressionUnary.expression.type;
		if(expressionUnary.op == Kind.OP_EXCLAMATION && (exprType0 != Type.INTEGER && exprType0 != Type.BOOLEAN)) {
			error(expressionUnary.firstToken, (expressionUnary.firstToken + " Type error: " + expressionUnary.op +" and "+ exprType0));
		} else if((expressionUnary.op == Kind.OP_MINUS || expressionUnary.op == Kind.OP_PLUS) && (exprType0 != Type.INTEGER && exprType0 != Type.FLOAT)) {
			error(expressionUnary.firstToken, (expressionUnary.firstToken + " Type error: " + expressionUnary.op +" and "+ exprType0));
		}
		expressionUnary.type = exprType0;
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		VariableDeclaration vd = null;
		vd = (VariableDeclaration)sT.lookup(lhs.identifier);
		if(vd == null) {
			error(lhs.firstToken, (lhs.firstToken + " "+ lhs.identifier +": declaration of variable not found"));
		}
		lhs.type = PLPTypes.getType(vd.type);
		lhs.declaration = vd;
		return null;
	}

}
