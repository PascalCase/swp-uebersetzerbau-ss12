package main.model;

public class StackAddress extends Address {
	
	private int addr;
	
	public StackAddress(int addr) {
		this.addr = addr;
	}
	
	public String getName() {
		return String.valueOf(addr);
	}

}
