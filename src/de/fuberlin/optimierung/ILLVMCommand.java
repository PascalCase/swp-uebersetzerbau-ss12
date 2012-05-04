package de.fuberlin.optimierung;

interface ILLVMCommand {
	
	public ILLVMCommand getPredecessor();
	public ILLVMCommand getSuccessor();
	public void setPredecessor(ILLVMCommand c);
	public void setSuccessor(ILLVMCommand c);

	public void deleteCommand();
	public String getCmd();
	public int getParamsCount();
	public void changeParam(int position, String param);

}
