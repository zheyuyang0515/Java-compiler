package cop5556fa18.PLPAST;

import org.junit.validator.PublicClassValidator;

import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPTypes.Type;

public abstract class Declaration extends PLPASTNode {
	public int slotNum;
	public Declaration(Token firstToken) {
		super(firstToken);
	}
}
