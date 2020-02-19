package cop5556fa18;

import java.util.HashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa18.PLPScanner.Kind;
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
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;

public class PLPCodeGen implements PLPASTVisitor, Opcodes {
	HashMap<String, Integer> slotTable = new HashMap<String, Integer>();
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int v_slot = 1;
	
	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;  // visitor of filed
	

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	

	public PLPCodeGen(String sourceFileName, boolean dEVEL, boolean gRADE) {
		super();
		this.sourceFileName = sourceFileName;
		DEVEL = dEVEL;
		GRADE = gRADE;
	}
	public String convertType(Kind type) {
		switch(type) {
		case KW_int: return "I";
		case KW_float: return "F";
		case KW_boolean: return "Z";
		case KW_char: return "C";
		case KW_string: return "Ljava/lang/String;";
		default: return null;
		}
	}
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		for (PLPASTNode node : block.declarationsAndStatements) {
			node.visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); 
		// If the call to mv.visitMaxs(1, 1) crashes, it is sometimes helpful 
		// to temporarily run it without COMPUTE_FRAMES. You probably won't 
		// get a completely correct classfile, but you will be able to see the 
		// code that was generated.
		
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		PLPCodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		PLPCodeGenUtils.genLog(DEVEL, mv, "leaving main");
		
		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor, asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();			
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		//filedType = convertType(declaration.type);
		if(declaration.expression != null) {
			declaration.expression.visit(this, arg);
			switch(PLPTypes.getType(declaration.type)) {
			case INTEGER: {				
				mv.visitVarInsn(ISTORE, v_slot);
			}	
			break;
			case FLOAT: {			
				mv.visitVarInsn(FSTORE, v_slot);
			}
			break;
			case BOOLEAN: {
				mv.visitVarInsn(ISTORE, v_slot);
			}
			break;
			case CHAR: {
				mv.visitVarInsn(ISTORE, v_slot);
			}
			break;
			case STRING: {
				mv.visitVarInsn(ASTORE, v_slot);
			}
			break;
			default: break;
			}
			declaration.slotNum = v_slot;
			v_slot++;
		}
		return null;
	}
	
	

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		//System.out.println("BOOL");
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		switch(expressionBinary.op) {
		/* + */
		case OP_PLUS:{
			if(expressionBinary.leftExpression.type == Type.INTEGER) {
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(IADD);
				}
				else if(expressionBinary.rightExpression.type == Type.FLOAT) {
					mv.visitInsn(SWAP);
					mv.visitInsn(I2F);
					mv.visitInsn(SWAP);
					mv.visitInsn(FADD);
				}
			}
			else if(expressionBinary.leftExpression.type == Type.FLOAT) {
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(I2F);
					mv.visitInsn(FADD);
				}
				else if(expressionBinary.rightExpression.type == Type.FLOAT) {
					mv.visitInsn(FADD);
				}
			}
			else if(expressionBinary.leftExpression.type == Type.STRING) {
				if(expressionBinary.rightExpression.type == Type.STRING) {
					mv.visitInsn(SWAP); 
					mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
					mv.visitInsn(DUP);		
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V",false);
					mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;",false);			
					mv.visitInsn(SWAP);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;",false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;",false);
				}		
			}
		}
		break;
		/* - */
		case OP_MINUS:{
			if(expressionBinary.leftExpression.type == Type.INTEGER) {
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(ISUB);
				}
				else if(expressionBinary.rightExpression.type == Type.FLOAT) {
					mv.visitInsn(SWAP);
					mv.visitInsn(I2F);
					mv.visitInsn(SWAP);
					mv.visitInsn(FSUB);
				}
			}
			else if(expressionBinary.leftExpression.type == Type.FLOAT) {
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(I2F);
					mv.visitInsn(FSUB);
				}
				else if(expressionBinary.rightExpression.type == Type.FLOAT) {
					mv.visitInsn(FSUB);
				}
			}
		}
		break;
		/* * */
		case OP_TIMES:{
			if(expressionBinary.leftExpression.type == Type.INTEGER) {
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(Opcodes.IMUL);
				}
				else if(expressionBinary.rightExpression.type == Type.FLOAT) {
					mv.visitInsn(SWAP);
					mv.visitInsn(I2F);
					mv.visitInsn(SWAP);
					mv.visitInsn(FMUL);
				}
			}
			else if(expressionBinary.leftExpression.type == Type.FLOAT) {
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(I2F);
					mv.visitInsn(FMUL);
				}
				else if(expressionBinary.rightExpression.type == Type.FLOAT) {
					mv.visitInsn(FMUL);
				}
			}
		}
		break;
		/* / */
		case OP_DIV:{
			if(expressionBinary.leftExpression.type == Type.INTEGER) {
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(IDIV);
				}
				else if(expressionBinary.rightExpression.type == Type.FLOAT) {
					mv.visitInsn(SWAP);
					mv.visitInsn(I2F);
					mv.visitInsn(SWAP);
					mv.visitInsn(FDIV);
				}
			}
			else if(expressionBinary.leftExpression.type == Type.FLOAT) {
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(I2F);
					mv.visitInsn(FDIV);
				}
				else if(expressionBinary.rightExpression.type == Type.FLOAT) {
					mv.visitInsn(FDIV);
				}
			}
		}
		break;
		/* % */
		case OP_MOD:{
			if(expressionBinary.leftExpression.type == Type.INTEGER) {
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(IREM);
				}
			}
		}
		break;
		/* ** */
		case OP_POWER:{
			if(expressionBinary.leftExpression.type == Type.INTEGER) {
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(I2D);
					mv.visitVarInsn(DSTORE, v_slot);
					int left_slot = v_slot;
					v_slot++;
					mv.visitInsn(I2D);
					mv.visitVarInsn(DLOAD, left_slot);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2I);
				}
				else if(expressionBinary.rightExpression.type == Type.FLOAT) {
					mv.visitInsn(F2D);
					mv.visitVarInsn(DSTORE, v_slot);
					int left_slot = v_slot;
					v_slot++;
					mv.visitInsn(I2D);		
					mv.visitVarInsn(DLOAD, left_slot);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2F);
				}
			}
			else if(expressionBinary.leftExpression.type == Type.FLOAT) {
				if(expressionBinary.rightExpression.type == Type.INTEGER) {
					mv.visitInsn(I2D);
					mv.visitVarInsn(DSTORE, v_slot);
					int left_slot = v_slot;
					v_slot++;
					mv.visitInsn(F2D);		
					mv.visitVarInsn(DLOAD, left_slot);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2F);
				}
				else if(expressionBinary.rightExpression.type == Type.FLOAT) {
					mv.visitInsn(F2D);
					mv.visitVarInsn(DSTORE, v_slot);
					int left_slot = v_slot;
					v_slot++;
					mv.visitInsn(F2D);		
					mv.visitVarInsn(DLOAD, left_slot);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2F);
				}
			}
		}
		break;
		/* & */
		case OP_AND:{
			mv.visitInsn(IAND);
		}
		break;
		/* | */
		case OP_OR:{
			mv.visitInsn(IOR);
		}
		break;
		/* == */
		case OP_EQ:{
			if((expressionBinary.leftExpression.type == Type.INTEGER)||(expressionBinary.leftExpression.type == Type.BOOLEAN)) {
				System.out.println(expressionBinary.leftExpression.type);
				mv.visitInsn(ISUB);
				Label l = new Label(); 
				Label end = new Label();
				mv.visitJumpInsn(IFEQ, l); 
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end); 
				mv.visitLabel(l); 
				mv.visitLdcInsn(1);
				mv.visitLabel(end); 
			}
			else if(expressionBinary.leftExpression.type == Type.FLOAT){
				mv.visitInsn(FCMPL);
				Label l = new Label(); 
				Label end = new Label();
				mv.visitJumpInsn(IFEQ, l); 
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, end); 
				mv.visitLabel(l); 
				mv.visitLdcInsn(1);
				mv.visitLabel(end); 
			}
			
		}
		break;
		/* != */
		case OP_NEQ:{
			if((expressionBinary.leftExpression.type == Type.INTEGER)||(expressionBinary.leftExpression.type == Type.BOOLEAN)) {
				mv.visitInsn(ISUB);			
			}
			else if(expressionBinary.leftExpression.type == Type.FLOAT){
				mv.visitInsn(FCMPL);
			}
			Label l = new Label(); 
			Label end = new Label();
			mv.visitJumpInsn(IFEQ, l); 
			mv.visitLdcInsn(1);
			mv.visitJumpInsn(GOTO, end); 
			mv.visitLabel(l); 
			mv.visitLdcInsn(0);
			mv.visitLabel(end); 
		}
		break;
		/* >= */
		case OP_GE:{
			if((expressionBinary.leftExpression.type == Type.INTEGER)||(expressionBinary.leftExpression.type == Type.BOOLEAN)) {
				mv.visitInsn(ISUB);			
			}
			else if(expressionBinary.leftExpression.type == Type.FLOAT){
				mv.visitInsn(FCMPL);
			}
			Label l = new Label(); 
			Label end = new Label();
			mv.visitJumpInsn(IFGE, l); 
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, end); 
			mv.visitLabel(l); 
			mv.visitLdcInsn(1);
			mv.visitLabel(end); 
		}
		break;
		/* > */
		case OP_GT:{
			if((expressionBinary.leftExpression.type == Type.INTEGER)||(expressionBinary.leftExpression.type == Type.BOOLEAN)) {
				mv.visitInsn(ISUB);			
			}
			else if(expressionBinary.leftExpression.type == Type.FLOAT){
				mv.visitInsn(FCMPL);
			}
			Label l = new Label(); 
			Label end = new Label();
			mv.visitJumpInsn(IFGT, l); 
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, end); 
			mv.visitLabel(l); 
			mv.visitLdcInsn(1);
			mv.visitLabel(end);
		}
		break;
		/* <= */
		case OP_LE:{
			if((expressionBinary.leftExpression.type == Type.INTEGER)||(expressionBinary.leftExpression.type == Type.BOOLEAN)) {
				mv.visitInsn(ISUB);			
			}
			else if(expressionBinary.leftExpression.type == Type.FLOAT){
				mv.visitInsn(FCMPL);
			}
			Label l = new Label(); 
			Label end = new Label();
			mv.visitJumpInsn(IFLE, l); 
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, end); 
			mv.visitLabel(l); 
			mv.visitLdcInsn(1);
			mv.visitLabel(end);
		}
		break;
		/* < */
		case OP_LT:{
			if((expressionBinary.leftExpression.type == Type.INTEGER)||(expressionBinary.leftExpression.type == Type.BOOLEAN)) {
				mv.visitInsn(ISUB);			
			}
			else if(expressionBinary.leftExpression.type == Type.FLOAT){
				mv.visitInsn(FCMPL);
			}
			Label l = new Label(); 
			Label end = new Label();
			mv.visitJumpInsn(IFLT, l); 
			mv.visitLdcInsn(0);
			mv.visitJumpInsn(GOTO, end); 
			mv.visitLabel(l); 
			mv.visitLdcInsn(1);
			mv.visitLabel(end);
		}
		break;
		default:break;
		}
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		expressionConditional.condition.visit(this, arg);
		Label l = new Label(); 
		Label end = new Label();
		mv.visitJumpInsn(Opcodes.IFGT, l); 
		//System.out.println("false");
		expressionConditional.falseExpression.visit(this, arg);	
		mv.visitJumpInsn(Opcodes.GOTO, end); 
		mv.visitLabel(l); 
		//System.out.println("true");
		expressionConditional.trueExpression.visit(this, arg);
		mv.visitLabel(end); 
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		// TODO Auto-generated method stub
		FunctionWithArg.expression.visit(this, arg);
		switch(FunctionWithArg.functionName) {
		case KW_abs:{	
			if(FunctionWithArg.expression.type == Type.INTEGER) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
			} else if(FunctionWithArg.expression.type == Type.FLOAT) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
			}			
		}
		break;
		case KW_sin:{
			mv.visitInsn(Opcodes.F2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(Opcodes.D2F);
		}
		break;
		case KW_cos:{
			mv.visitInsn(Opcodes.F2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(Opcodes.D2F);
		}
		break;
		case KW_atan:{
			mv.visitInsn(Opcodes.F2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
			mv.visitInsn(Opcodes.D2F);
		}
		break;
		case KW_log:{
			mv.visitInsn(Opcodes.F2D);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
			mv.visitInsn(Opcodes.D2F);
		}
		break;
		case KW_float:{
			if(FunctionWithArg.expression.type == Type.INTEGER) {
				mv.visitInsn(Opcodes.I2F);
			}
		}
		break;
		case KW_int:{
			if(FunctionWithArg.expression.type == Type.FLOAT) {
				mv.visitInsn(Opcodes.F2I);
			}		
		}
		default: break;
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		switch(expressionIdent.type) {
		case INTEGER:{
			System.out.println(expressionIdent.declaration.slotNum);
			mv.visitVarInsn(ILOAD, expressionIdent.declaration.slotNum);
		}
		break;
		case FLOAT:{
			mv.visitVarInsn(FLOAD, expressionIdent.declaration.slotNum);
		}
		break;
		case BOOLEAN:{
			mv.visitVarInsn(ILOAD, expressionIdent.declaration.slotNum);
		}
		break;
		case CHAR:{
			mv.visitVarInsn(ILOAD, expressionIdent.declaration.slotNum);
		}
		break;
		case STRING:{
			mv.visitVarInsn(ALOAD, expressionIdent.declaration.slotNum);
		}
		break;
		default: break;
		}
		
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		//System.out.println("INT");
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionStringLiteral.text);
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(expressionCharLiteral.text);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		statementAssign.expression.visit(this, arg);
		Type type = statementAssign.expression.getType();
		statementAssign.lhs.visit(this, arg);
		if(statementAssign.lhs.declaration.slotNum == 0) {
			statementAssign.lhs.declaration.slotNum = v_slot;
			v_slot++;
		}
		switch(type) {
		case INTEGER:{
			mv.visitVarInsn(ISTORE, statementAssign.lhs.declaration.slotNum);
		}
		break;
		case FLOAT:{
			mv.visitVarInsn(FSTORE, statementAssign.lhs.declaration.slotNum);	
		}
		break;
		case BOOLEAN:{
			mv.visitVarInsn(ISTORE, statementAssign.lhs.declaration.slotNum);	
		}
		break;
		case CHAR:{
			mv.visitVarInsn(ISTORE, statementAssign.lhs.declaration.slotNum);	
		}
		break;
		case STRING:{
			mv.visitVarInsn(ASTORE, statementAssign.lhs.declaration.slotNum);	
		}			
		break;
		default:break;
		}
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.condition.visit(this, arg);
		Label l = new Label(); 
		Label end = new Label();
		mv.visitJumpInsn(Opcodes.IFGT, l);
		mv.visitJumpInsn(Opcodes.GOTO, end); 
		mv.visitLabel(l); 
		ifStatement.block.visit(this, arg);
		mv.visitLabel(end);
		return null;
	}
	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label l = new Label(); 	
		Label end = new Label();
		Label loop = new Label();
		mv.visitJumpInsn(Opcodes.GOTO, loop);
		mv.visitLabel(loop);
		whileStatement.condition.visit(this, arg);
		mv.visitJumpInsn(Opcodes.IFGT, l);
		mv.visitJumpInsn(Opcodes.GOTO, end); 	
		mv.visitLabel(l); 
		whileStatement.b.visit(this, arg);
		mv.visitJumpInsn(Opcodes.GOTO, loop);	
		mv.visitLabel(end);
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		printStatement.expression.visit(this, arg);
		Type type = printStatement.expression.getType();
		switch (type) {
		case INTEGER : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(I)V", false);
		}
		break;
		case BOOLEAN : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Z)V", false);
		}
		break;
		case FLOAT : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(F)V", false);
		}
		break;
		case CHAR : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);			
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(C)V", false);
		}
		break;
		case STRING : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Ljava/lang/String;)V", false);
		}
		break;
		default: break;
		}
		return null;
		
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.time.visit(this, arg);
		mv.visitInsn(Opcodes.I2L);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false); 
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		switch(expressionUnary.op) {
		case OP_EXCLAMATION:{
			if(expressionUnary.expression.type == Type.INTEGER) {
				mv.visitLdcInsn(0xFFFFFFFF);
				mv.visitInsn(Opcodes.IXOR);			
				
			} else if(expressionUnary.expression.type == Type.BOOLEAN) {
				mv.visitLdcInsn(1);
				mv.visitInsn(Opcodes.IXOR);
			}
		}
		break;
		case OP_MINUS:{
			if(expressionUnary.expression.type == Type.INTEGER) {
				mv.visitInsn(Opcodes.INEG);
			} else if(expressionUnary.expression.type == Type.FLOAT) {
				mv.visitInsn(Opcodes.FNEG);
			}
		}
		break;
		default: break;
		}
		return null;
	}

}
