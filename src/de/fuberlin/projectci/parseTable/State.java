package de.fuberlin.projectci.parseTable;

import de.fuberlin.commons.util.EasyComparableObject;

public class State extends EasyComparableObject{
	private int id;

	public State(int id) {
		this.id = id;
	}

	@Override
	protected Object[] getSignificantFields() {
		return new Object[]{id, getClass()};
	}
	
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}
}
