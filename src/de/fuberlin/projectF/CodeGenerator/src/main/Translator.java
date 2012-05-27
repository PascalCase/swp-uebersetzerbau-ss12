package main;

import java.util.List;
import java.util.Vector;

import main.model.Address;
import main.model.RegisterAddress;
import main.model.RegisterInformation;
import main.model.StackAddress;
import main.model.Token;
import main.model.Variable;

public class Translator {
	private StringBuffer sectionData;
	private StringBuffer sectionText;
	private VariableTableContainer vars;
	Vector<Token> code;

	public Translator() {
		/* Variablen/Registerverwaltung erstellen (oder werden die übergeben?) */
		vars = new VariableTableContainer();

		sectionData = new StringBuffer().append("\t.section .data\n");
		sectionText = new StringBuffer().append("\t.section .text\n");
		sectionText.append("\t.global _start\n\n");
		sectionText.append("_start:\t jmp main\n\n");
		
	}

	public void translate(Vector<Token> code) {
		
		this.code = code;
		int tokenNum = 0;
		for(Token t : code) {
			System.out.print("translate token #" + tokenNum++ + " ...");
			Variable target;
			Variable op1;
			Variable op2;
			List<Address> addr;
			List<Address> reg;
			String ziel = "undefined";
			String quelle = "undefined";
	
			switch (t.getType()) {
			case Definition:
				vars.addVariableTable(t.getTarget());
				vars.changeVariableTable(t.getTarget());

				String name = t.getTarget().substring(1);
				sectionText.append("type ").append(name).append(", @function\n")
						.append(name).append(":\n");
				sectionText.append("\tenter $0, $0\n"); // Durch spätere Optimierung
														// oder durch lookahead auf
														// die nächsten Token könnte
														// hier bereits
														// Stackspeicher reserviert
														// werden,
				/* TODO: Variablenkontext wechseln, Sonderbehandlung main */
				break;
	
			case DefinitionEnd:
				vars.changeVariableTable("global");
				sectionText.append("\tleave\n");
				sectionText.append("\tret\n");
				break;
			case CompareLower:
			case CompareGreater:
			case CompareLowerEqual:
			case CompareGreaterEqual:
			case CompareEqual:
			case CompareNotEqual:
				sectionText.append("\tTODO cmp " + t.getOp1() + ", " + t.getOp2() + "\n");
				break;
				
			case Branch:
				if(!t.getOp1().isEmpty()) {
					sectionText.append("\tTODO jxxx _" + t.getOp1().substring(1) + "\n");
				}
				sectionText.append("\tjmp _" + t.getOp2().substring(1) + "\n");
				break;
	
			case String:
				vars.addVariable(new Variable(t.getTarget(),t.getTypeTarget(),t.getOp1()));
				sectionData.append(t.getTarget() + ":\tascii " + t.getOp1() + "\n");
				break;
				
			case Label:
				sectionText.append("_" + t.getTarget() + ":\n");
				break;
	
			case Allocation:
				vars.addVariable(new Variable(t.getTarget(),t.getTypeTarget(),null));
				target = vars.getVariable(t.getTarget());
				sectionText.append("\tsubl $" + target.size() + ", %esp \t\t#"
									+ t.getTarget() + "\t#allocation\n");
				break;
				
			case Assignment:
	
				target = vars.getVariable(t.getTarget());
				addr = vars.getAddresses(target);
				for (Address a : addr) {
					ziel = new String("-" + ((StackAddress) a).getName() + "(%ebp)");
					break;
				}
	
				if (t.getOp1().charAt(0) == '%') {
					op1 = vars.getVariable(t.getOp1());
					Address addr1 = vars.getBestAddress(op1);
					quelle = addr1.getName();
				}
				else
					quelle = new String("$" + t.getOp1());
	
				sectionText.append("\tmovl " + quelle + ", " + ziel + "\t#assignement\n");
	
				break;
	
			case Load:
				System.out.println();
				t.print();
				op1 = vars.getVariable(t.getOp1());
				System.out.println("Get Op1: "+ op1.name());
				target = new Variable(t.getTarget(), t.getTypeOp1(), null);
				System.out.println("Create new Target: "+ target.name());
			
				Address regAddr = vars.getBestAddress(op1);
				System.out.println("Best address of op1: " + regAddr.getName());
				
				Address targetAddress = vars.getFreeRegister();
				System.out.print("Free Register for target: ");
				if(targetAddress == null) {
					System.out.println("\n\tNo free register available");
					System.out.println("\tTry to find a register wich is able to overwrite");
					
					for(int i = 0; i < VariableTableContainer.numRegister; i++) {
						List<Variable> varList = vars.getVarInRegister(i);
						System.out.println("\tregister " + new RegisterAddress(i).getName() + " holds");
						
						int result = 0;
						for(Variable var : varList) {
							System.out.print("\t\t" + var.name());
							result = findNextToken(tokenNum, null, var.name(), var.name());
							if( result == 0) {
								System.out.println("\t-> will not be used in future :)");
								break;
							} else {
								System.out.println("\t-> will be used in token " + result);
							}
						}
						if(result == 0) {
							targetAddress = new RegisterAddress(i);
							break;
						}
						
					}
					
					System.out.print("\tfound: ");
				}
				System.out.println(targetAddress.getName());
				System.out.println("Add new target Variable");
				vars.getVariableTable(null).addVariable(target,targetAddress);
				vars.getVariableTable(null).loadVarInReg(target, (RegisterAddress)targetAddress);
		
				quelle = "-" + vars.getHomeAddress(op1).getName() + "(%ebp)";
				if(targetAddress != null)
					ziel = targetAddress.getName();
				
				sectionText.append("\tmovl " + quelle + ", " + ziel
						+ " \t\t#load\n");
				break;
	
			case Addition:
				op1 = vars.getVariable(t.getOp1());
				if(t.getOp2().charAt(0) != '%') {
					quelle = new String("$" + t.getOp2());
				} else {
					op2 = vars.getVariable(t.getOp2());
					RegisterAddress addr2 = (RegisterAddress)vars.getRegister(op2);
					quelle = addr2.getName();
				}
				
				RegisterAddress addr1 = (RegisterAddress)vars.getRegister(op1);
				ziel = addr1.getName();
				
				if(quelle.equals("$1"))
					sectionText.append("\tincl " + ziel + "\t\t#addition\n");
				else if(quelle.equals("$-1"))
					sectionText.append("\tdecl " + ziel + "\t\t#addition\n");
				else
					sectionText.append("\taddl " + ziel + ", " + quelle + " \t\t#addition\n");
			    //TODO : varAdminstration abdaten
				Variable tmpVar = new Variable(t.getTarget(),t.getTypeTarget(),null);
				vars.addVariable(tmpVar);
				vars.getVariableTable(null).loadVarInReg(tmpVar, addr1);
			default:
				break;
			}
			System.out.println("\t\t[OK]");
		}
	}

	private int findNextToken(int tokenNum, String target, String op1, String op2) {
		for(int i = tokenNum; i < code.size(); i++) {
			if(target != null) {
				if(code.elementAt(i).getTarget().equals(target)) {
					return i;
				}
			}
			if(op1 != null) {
				if(code.elementAt(i).getOp1().equals(op1)) {
					return i;
				}			
			}
			if(op2 != null) {
				if(code.elementAt(i).getOp2().equals(op2)) {
					return i;
				}
			}
		}
		return 0;
	}

	public String getCode() {
		return (sectionData.toString() + sectionText.toString());
	}

	public void print() {
		System.out.println("\nGenerated Code:");
		System.out.print(sectionData);
		System.out.print(sectionText);
		System.out.println();
	}
}
