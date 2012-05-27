package main;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.model.Address;
import main.model.RegisterAddress;
import main.model.RegisterInformation;
import main.model.StackAddress;
import main.model.Token;
import main.model.Variable;

public class VariableTableContainer {
	static int numRegister = 6;
	Map<String,VarAdministration> variableTableList;
	String actTable = "";
	
	public VariableTableContainer() {
		variableTableList = new HashMap<String,VarAdministration>();
		addVariableTable("global");
		System.out.println("Action: add variabletable global");
		changeVariableTable("global");
		System.out.println("Action: change to variabletable " + actTable);
	}
	
	public void addVariableTable(String name) {
		variableTableList.put(name, new VarAdministration(numRegister));
	}
	
	public VarAdministration getVariableTable(String name) {
		if(name == null)
			return variableTableList.get(this.actTable);
		return variableTableList.get(name);
	}
	
	public int changeVariableTable(String name) {
		this.actTable = name;
		return 0;
	}
	
	public void addVariable(Variable variable) {
		if (!hasVariable(variable.name())) {
			getVariableTable(this.actTable).addVariable(variable);
		}
	}
	
	public List<Variable> getAllVariables() {
		return getVariableTable(this.actTable).getAllVariables();
	}
	
	public Variable getVariable(String name) {
		List<Variable> varList;
		varList = getAllVariables();
		for (Variable v : varList) {
			if(name.equals(v.name()))
				return v;
		}
		return null;
	}
	
	public Address getHomeAddress(Variable var) {
		return getVariableTable(this.actTable).getHomeAddress(var);
	}
	
	public List<Address> getAddresses(Variable var) {
		return getVariableTable(this.actTable).getAddresses(var);
	}
	
	public Address getBestAddress(Variable var) {
		Address addr = getRegister(var);
		if(addr != null)
			return addr;
		addr = getHomeAddress(var);
		return addr;
	}
	
	/*public Address getReg(Variable var) {
		return getVariableTable(this.actTable).get(var);
	}*/
	
	public Address getRegister(Variable var) {
		List<Address> addrList = getAddresses(var);
		Address addr = null;
		for (Address a : addrList) {
			if (a instanceof RegisterAddress) {
				addr = a;
				break;
			}
		}
		return addr;
	}
	
	public Variable getVarByName(String name) {
		return getVariableTable(this.actTable).getVariableByName(name);
	}

	private boolean hasVariable(String varName) {
		
		return getVariableTable(this.actTable).hasVariableWithName(varName);
	}

	public Address getFreeRegister() {
		return getVariableTable(this.actTable).getFreeRegister();
	}
	
	public List<Variable> getVarInRegister(int regNumber) {
		return getVariableTable(this.actTable).getVarInRegister(regNumber);
	}
	
}
