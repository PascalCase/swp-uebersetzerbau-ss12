package main.model;


public class RegisterAddress extends Address {
	
	public int regNumber;
	public boolean free = true;
	
	public RegisterAddress(int register) {
		this.regNumber = register;
	}
	
	public String getName() {
		switch (regNumber) {
		case 0:
			return "%eax";
		case 1:
			return "%ebx";
		case 2:
			return "%ecx";
		case 3:
			return "%edx";
		case 4:
			return "%edi";
		default:
			return "%esi";
			
		}
	}

}
