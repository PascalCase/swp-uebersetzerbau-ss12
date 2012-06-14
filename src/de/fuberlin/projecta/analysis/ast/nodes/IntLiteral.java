package de.fuberlin.projecta.analysis.ast.nodes;

import de.fuberlin.projecta.analysis.SymbolTableStack;

public class IntLiteral extends Statement {
	
	private int value;
	
	public IntLiteral(int value){
		this.value = value;
	}
	
	public void buildSymbolTable(SymbolTableStack tables) {

	}

	@Override
	public boolean checkSemantics() {
		return true;
	}

	@Override
	public String genCode() {
		return "i32 " + this.value;
	}
	
	public int getValue(){
		return this.value;
	}
}